package com.xdata.db;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResultRowsTest {

    private static ResultRows rows(List<Object>... rs) {
        return new ResultRows(new java.util.ArrayList<>(List.of(rs)));
    }

    @Test
    void identical_rows_match() {
        assertThat(rows(List.of(1, "a"), List.of(2, "b"))
                .matches(rows(List.of(1, "a"), List.of(2, "b")))).isTrue();
    }

    @Test
    void same_rows_in_different_order_match() {
        assertThat(rows(List.of(1, "a"), List.of(2, "b"))
                .matches(rows(List.of(2, "b"), List.of(1, "a")))).isTrue();
    }

    @Test
    void different_row_counts_do_not_match() {
        assertThat(rows(List.of(1, "a"))
                .matches(rows(List.of(1, "a"), List.of(1, "a")))).isFalse();
    }

    @Test
    void duplicate_multiplicity_matters() {
        // multiset, not set: [a,a,b] != [a,b,b]
        assertThat(rows(List.of("a"), List.of("a"), List.of("b"))
                .matches(rows(List.of("a"), List.of("b"), List.of("b")))).isFalse();
    }

    @Test
    void different_values_do_not_match() {
        assertThat(rows(List.of(1, "a")).matches(rows(List.of(1, "z")))).isFalse();
    }

    @Test
    void empty_matches_empty() {
        assertThat(new ResultRows(List.of()).matches(new ResultRows(List.of()))).isTrue();
    }
}
