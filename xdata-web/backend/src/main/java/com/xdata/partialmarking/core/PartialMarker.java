package com.xdata.partialmarking.core;

import net.sf.jsqlparser.expression.Expression;
import java.util.ArrayList;
import java.util.List;

/**
 * Structural partial marking: scores a student query against the instructor's
 * pattern across the SQL dimensions. {@link #getMarks} is the stable seam (called
 * via {@code PartialMarkingPort}); behind it the comparison is decomposed into
 * named steps — per-query dimension comparison, the HAVING and DISTINCT marks, and
 * the score aggregation — each small enough to reason about and test in isolation.
 */
public class PartialMarker {

    public static MarkInfo getMarks(QueryStructure instructor, QueryStructure student, PartialMarkParameters params) {
        MarkInfo markInfo = new MarkInfo();

        QueryInfo queryInfo = compareDimensions(instructor, student, params);

        List<QueryInfo> subqueryData = new ArrayList<>();
        subqueryData.add(queryInfo);
        if (instructor.getSubqueries() != null && student.getSubqueries() != null) {
            int minSize = Math.min(instructor.getSubqueries().size(), student.getSubqueries().size());
            for (int i = 0; i < minSize; i++) {
                MarkInfo subMark = getMarks(instructor.getSubqueries().get(i), student.getSubqueries().get(i), params);
                subMark.getSubqueryData().get(0).setLevel(instructor.getSubqueries().get(i).getLevel());
                subqueryData.addAll(subMark.getSubqueryData());
            }
        }
        markInfo.setSubqueryData(subqueryData);

        aggregateScore(markInfo, subqueryData, instructor, params);
        return markInfo;
    }

    /** Compare one query level across every dimension into a {@link QueryInfo}. */
    private static QueryInfo compareDimensions(QueryStructure instructor, QueryStructure student, PartialMarkParameters params) {
        QueryInfo queryInfo = new QueryInfo();

        queryInfo.setInstructorProjections(instructor.getProjections());
        queryInfo.setStudentProjections(student.getProjections());
        queryInfo.setStudentProjectionMarks(compareLists(instructor.getProjections(), student.getProjections(), params.getProjection(), instructor.getProjectionExpressions(), student.getProjectionExpressions(), instructor.getAliasMap(), student.getAliasMap()));

        queryInfo.setInstructorPredicates(instructor.getPredicates());
        queryInfo.setStudentPredicates(student.getPredicates());
        queryInfo.setStudentPredicateMarks(compareLists(instructor.getPredicates(), student.getPredicates(), params.getPredicate(), instructor.getPredicateExpressions(), student.getPredicateExpressions(), instructor.getAliasMap(), student.getAliasMap()));

        queryInfo.setInstructorRelations(instructor.getRelations());
        queryInfo.setStudentRelations(student.getRelations());
        queryInfo.setStudentRelationMarks(compareLists(instructor.getRelations(), student.getRelations(), params.getRelation()));

        queryInfo.setInstructorJoins(instructor.getJoins());
        queryInfo.setStudentJoins(student.getJoins());
        queryInfo.setStudentJoinMarks(compareLists(instructor.getJoins(), student.getJoins(), params.getJoins(), instructor.getJoinExpressions(), student.getJoinExpressions(), instructor.getAliasMap(), student.getAliasMap()));

        queryInfo.setInstructorGroupBy(instructor.getGroupBy());
        queryInfo.setStudentGroupBy(student.getGroupBy());
        queryInfo.setStudentGroupByMarks(compareLists(instructor.getGroupBy(), student.getGroupBy(), params.getGroupBy(), instructor.getGroupByExpressions(), student.getGroupByExpressions(), instructor.getAliasMap(), student.getAliasMap()));

        queryInfo.setInstructorOrderBy(instructor.getOrderBy());
        queryInfo.setStudentOrderBy(student.getOrderBy());
        queryInfo.setStudentOrderByMarks(compareLists(instructor.getOrderBy(), student.getOrderBy(), params.getOrderBy()));

        queryInfo.setInstructorHaving(instructor.getHaving());
        queryInfo.setStudentHaving(student.getHaving());
        queryInfo.setStudentHavingMark(havingMark(instructor, student, params));

        queryInfo.setInstructorDistinct(instructor.isDistinct());
        queryInfo.setStudentDistinct(student.isDistinct());
        queryInfo.setStudentDistinctMark(distinctMark(instructor.isDistinct(), student.isDistinct(), params));

        return queryInfo;
    }

    /** Mark for the HAVING dimension: +weight on match, -weight on mismatch/spurious, 0 if neither has one. */
    private static double havingMark(QueryStructure instructor, QueryStructure student, PartialMarkParameters params) {
        if (instructor.getHavingExpression() != null && student.getHavingExpression() != null) {
            boolean match = ExpressionComparator.areEqual(instructor.getHavingExpression(), student.getHavingExpression(), instructor.getAliasMap(), student.getAliasMap());
            return match ? params.getHavingClause() : -params.getHavingClause();
        } else if (instructor.getHaving() != null && student.getHaving() != null) {
            boolean match = instructor.getHaving().equalsIgnoreCase(student.getHaving());
            return match ? params.getHavingClause() : -params.getHavingClause();
        } else if (instructor.getHaving() == null && student.getHaving() != null) {
            return -params.getHavingClause();
        } else {
            return 0;
        }
    }

    /** Mark for the DISTINCT dimension: +weight when both agree on DISTINCT, -weight on disagreement, else 0. */
    private static double distinctMark(boolean instructorDistinct, boolean studentDistinct, PartialMarkParameters params) {
        if (instructorDistinct && studentDistinct) {
            return params.getDistinct();
        } else if (!instructorDistinct && studentDistinct) {
            return -params.getDistinct();
        } else if (instructorDistinct && !studentDistinct) {
            return -params.getDistinct();
        } else {
            return 0;
        }
    }

    /** Sum positive marks and the achievable maximum across this query and its subqueries. */
    private static void aggregateScore(MarkInfo markInfo, List<QueryInfo> subqueryData, QueryStructure instructor, PartialMarkParameters params) {
        double totalMarks = 0;
        double totalMaxMarks = 0;
        for (QueryInfo qi : subqueryData) {
            for (Double d : qi.getStudentProjectionMarks()) if (d > 0) totalMarks += d;
            for (Double d : qi.getStudentPredicateMarks()) if (d > 0) totalMarks += d;
            for (Double d : qi.getStudentRelationMarks()) if (d > 0) totalMarks += d;
            for (Double d : qi.getStudentJoinMarks()) if (d > 0) totalMarks += d;
            for (Double d : qi.getStudentGroupByMarks()) if (d > 0) totalMarks += d;
            for (Double d : qi.getStudentOrderByMarks()) if (d > 0) totalMarks += d;
            if (qi.getStudentHavingMark() > 0) totalMarks += qi.getStudentHavingMark();

            if (qi.getInstructorProjections() != null) totalMaxMarks += qi.getInstructorProjections().size() * params.getProjection();
            if (qi.getInstructorPredicates() != null) totalMaxMarks += qi.getInstructorPredicates().size() * params.getPredicate();
            if (qi.getInstructorRelations() != null) totalMaxMarks += qi.getInstructorRelations().size() * params.getRelation();
            if (qi.getInstructorJoins() != null) totalMaxMarks += qi.getInstructorJoins().size() * params.getJoins();
            if (qi.getInstructorGroupBy() != null) totalMaxMarks += qi.getInstructorGroupBy().size() * params.getGroupBy();
            if (qi.getInstructorOrderBy() != null) totalMaxMarks += qi.getInstructorOrderBy().size() * params.getOrderBy();
            if (qi.getInstructorHaving() != null || instructor.getHavingExpression() != null) totalMaxMarks += params.getHavingClause();

            totalMarks += qi.getStudentDistinctMark();
            if (qi.isInstructorDistinct()) {
                totalMaxMarks += params.getDistinct();
            }
        }

        markInfo.setMarks(Math.max(0, totalMarks));
        markInfo.setMaxMarks(totalMaxMarks);
        markInfo.setPercentage(totalMaxMarks > 0 ? (markInfo.getMarks() / totalMaxMarks) * 100.0 : 0);
    }

    private static List<Double> compareLists(List<String> instList, List<String> studList, double weight) {
        return compareLists(instList, studList, weight, null, null, null, null);
    }

    private static List<Double> compareLists(List<String> instList, List<String> studList, double weight, List<Expression> instExprs, List<Expression> studExprs, java.util.Map<String, String> aliasMap1, java.util.Map<String, String> aliasMap2) {
        List<Double> marks = new ArrayList<>();
        if (studList == null) return marks;

        List<String> remainingInstStrs = instList != null ? new ArrayList<>(instList) : new ArrayList<>();
        List<Expression> remainingInstExprs = instExprs != null ? new ArrayList<>(instExprs) : new ArrayList<>();

        for (int sIdx = 0; sIdx < studList.size(); sIdx++) {
            String s = studList.get(sIdx);
            Expression sExpr = (studExprs != null && sIdx < studExprs.size()) ? studExprs.get(sIdx) : null;
            boolean found = false;
            int foundIdx = -1;

            if (sExpr != null && !remainingInstExprs.isEmpty()) {
                for (int i = 0; i < remainingInstExprs.size(); i++) {
                    Expression iExpr = remainingInstExprs.get(i);
                    if (ExpressionComparator.areEqual(iExpr, sExpr, aliasMap1, aliasMap2)) {
                        found = true;
                        foundIdx = i;
                        break;
                    }
                }
            }

            if (!found && !remainingInstStrs.isEmpty()) {
                for (int i = 0; i < remainingInstStrs.size(); i++) {
                    if (remainingInstStrs.get(i).trim().equalsIgnoreCase(s.trim())) {
                        found = true;
                        foundIdx = i;
                        break;
                    }
                }
            }

            if (found && foundIdx != -1) {
                if (foundIdx < remainingInstExprs.size()) remainingInstExprs.remove(foundIdx);
                if (foundIdx < remainingInstStrs.size()) remainingInstStrs.remove(foundIdx);
                marks.add(weight);
            } else {
                marks.add(-weight);
            }
        }
        return marks;
    }
}
