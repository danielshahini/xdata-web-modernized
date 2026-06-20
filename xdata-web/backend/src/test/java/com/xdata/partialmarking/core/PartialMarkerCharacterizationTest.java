package com.xdata.partialmarking.core;

import com.xdata.partialmarking.service.SqlSchemaParser;
import com.xdata.partialmarking.service.TableMapBuilder;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Characterization tests: pin the OBSERVABLE scoring of PartialMarker.getMarks
 * before decomposing the god-method, so the refactor cannot change behaviour.
 */
class PartialMarkerCharacterizationTest {

    private static final String DDL = "CREATE TABLE users (id INT, name VARCHAR(20))";

    private static MarkInfo marks(String instructorSql, String studentSql) throws Exception {
        TableMap tableMap = TableMapBuilder.build(SqlSchemaParser.parse(DDL));
        QueryStructure instructor = new QueryStructure(instructorSql, tableMap);
        QueryStructure student = new QueryStructure(studentSql, tableMap);
        CanonicalizeQuery.canonicalize(instructor);
        CanonicalizeQuery.canonicalize(student);
        return PartialMarker.getMarks(instructor, student, new PartialMarkParameters());
    }

    @Test
    void identical_queries_score_full_marks() throws Exception {
        MarkInfo mi = marks("SELECT id, name FROM users WHERE id = 1",
                            "SELECT id, name FROM users WHERE id = 1");
        assertThat(mi.getPercentage()).isEqualTo(100.0);
        assertThat(mi.getMarks()).isEqualTo(mi.getMaxMarks());
    }

    @Test
    void student_missing_the_predicate_loses_those_marks() throws Exception {
        MarkInfo mi = marks("SELECT id, name FROM users WHERE id = 1",
                            "SELECT id, name FROM users");
        assertThat(mi.getPercentage()).isEqualTo(75.0);
    }

    @Test
    void student_with_a_wrong_predicate_loses_those_marks() throws Exception {
        MarkInfo mi = marks("SELECT id, name FROM users WHERE id = 1",
                            "SELECT id, name FROM users WHERE id = 2");
        assertThat(mi.getPercentage()).isEqualTo(75.0);
    }

    @Test
    void student_missing_distinct_loses_the_distinct_mark() throws Exception {
        // instructor: 1 projection + 1 relation + DISTINCT  -> maxMarks 3
        // student matches projection+relation (+2) but lacks DISTINCT (-1) -> marks 1 -> 33.33%
        MarkInfo mi = marks("SELECT DISTINCT id FROM users WHERE id = 1",
                            "SELECT id FROM users WHERE id = 1");
        assertThat(mi.getMaxMarks()).isEqualTo(4.0);
        assertThat(mi.getMarks()).isEqualTo(2.0);
        assertThat(mi.getPercentage()).isEqualTo(50.0);
    }
}
