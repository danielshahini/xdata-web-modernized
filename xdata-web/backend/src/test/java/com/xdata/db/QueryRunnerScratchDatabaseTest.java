package com.xdata.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Exercises QueryRunner + ScratchDatabaseFactory against a real Derby in-memory DB. */
class QueryRunnerScratchDatabaseTest {

    private final ScratchDatabaseFactory factory = new ScratchDatabaseFactory();
    private final QueryRunner runner = new QueryRunner();

    private static final String DDL = "CREATE TABLE t (id INT, name VARCHAR(20))";
    private static final List<String> DATA = List.of(
            "INSERT INTO t VALUES (1, 'a')",
            "INSERT INTO t VALUES (2, 'b')",
            "INSERT INTO t VALUES (3, 'c')");

    @Test
    void scratch_database_serves_loaded_data_to_queries() throws Exception {
        try (ScratchDatabase db = factory.create(DDL, DATA)) {
            ResultRows rows = runner.run(db.connection(), "SELECT id, name FROM t WHERE id > 0");
            assertThat(rows.size()).isEqualTo(3);
        }
    }

    @Test
    void equivalent_queries_match_on_the_same_scratch_data() throws Exception {
        try (ScratchDatabase db = factory.create(DDL, DATA)) {
            ResultRows a = runner.run(db.connection(), "SELECT id FROM t WHERE id <= 2");
            ResultRows b = runner.run(db.connection(), "SELECT id FROM t WHERE id < 3");
            assertThat(a.matches(b)).isTrue();
        }
    }

    @Test
    void differing_queries_do_not_match() throws Exception {
        try (ScratchDatabase db = factory.create(DDL, DATA)) {
            ResultRows a = runner.run(db.connection(), "SELECT id FROM t WHERE id = 1");
            ResultRows b = runner.run(db.connection(), "SELECT id FROM t WHERE id = 2");
            assertThat(a.matches(b)).isFalse();
        }
    }

    @Test
    void maxRows_caps_the_extracted_rows() throws Exception {
        try (ScratchDatabase db = factory.create(DDL, DATA)) {
            ResultRows rows = runner.run(db.connection(), "SELECT id FROM t WHERE id > 0", 0, 2);
            assertThat(rows.size()).isEqualTo(2);
        }
    }

    @Test
    void scratch_database_can_be_reused_after_a_previous_one_closed() throws Exception {
        // proves close() drops cleanly and unique naming avoids collisions
        try (ScratchDatabase first = factory.create(DDL, DATA)) {
            assertThat(runner.run(first.connection(), "SELECT id FROM t WHERE id > 0").size()).isEqualTo(3);
        }
        try (ScratchDatabase second = factory.create(DDL, List.of("INSERT INTO t VALUES (9, 'z')"))) {
            assertThat(runner.run(second.connection(), "SELECT id FROM t WHERE id > 0").size()).isEqualTo(1);
        }
    }
}
