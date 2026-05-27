package com.xdata.partialmarking.core;

import net.sf.jsqlparser.expression.Expression;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;

public class CanonicalizeQuery {
    public static void canonicalize(QueryStructure qStruct) {
        if (qStruct == null) return;

        // Sort simple components to allow different orders
        if (qStruct.getProjections() != null) Collections.sort(qStruct.getProjections());
        if (qStruct.getRelations() != null) Collections.sort(qStruct.getRelations());
        if (qStruct.getPredicates() != null) Collections.sort(qStruct.getPredicates());
        if (qStruct.getJoins() != null) Collections.sort(qStruct.getJoins());
        if (qStruct.getGroupBy() != null) Collections.sort(qStruct.getGroupBy());
        if (qStruct.getOrderBy() != null) Collections.sort(qStruct.getOrderBy());

        // Sort expressions too if possible
        sortExpressions(qStruct.getProjectionExpressions());
        sortExpressions(qStruct.getPredicateExpressions());
        sortExpressions(qStruct.getJoinExpressions());
        sortExpressions(qStruct.getGroupByExpressions());

        if (qStruct.getSubqueries() != null) {
            for (QueryStructure sub : qStruct.getSubqueries()) {
                canonicalize(sub);
            }
        }
    }

    private static void sortExpressions(List<Expression> exprs) {
        if (exprs != null && exprs.size() > 1) {
            exprs.sort((e1, e2) -> {
                if (e1 == null && e2 == null) return 0;
                if (e1 == null) return 1;
                if (e2 == null) return -1;
                return e1.toString().compareTo(e2.toString());
            });
        }
    }
}
