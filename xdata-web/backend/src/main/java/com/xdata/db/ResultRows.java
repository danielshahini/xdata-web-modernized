package com.xdata.db;

import java.util.ArrayList;
import java.util.List;

/**
 * The rows returned by a query, as an ordered list of column-value lists.
 *
 * <p>{@link #matches} is the single, canonical result comparison for grading:
 * order-insensitive multiset equality. It replaces the two divergent comparisons
 * that previously lived in {@code TestExecutionService} (true multiset) and
 * {@code AssignedDbStage} (sort-by-toString) — see CONTEXT.md → "QueryRunner".
 */
public final class ResultRows {

    private final List<List<Object>> rows;

    public ResultRows(List<List<Object>> rows) {
        this.rows = rows;
    }

    public List<List<Object>> rows() {
        return rows;
    }

    public int size() {
        return rows.size();
    }

    /** Order-insensitive multiset equality: same rows, same multiplicities. */
    public boolean matches(ResultRows other) {
        if (other == null || rows.size() != other.rows.size()) {
            return false;
        }
        List<List<Object>> remaining = new ArrayList<>(other.rows);
        for (List<Object> row : rows) {
            if (!remaining.remove(row)) {
                return false;
            }
        }
        return remaining.isEmpty();
    }
}
