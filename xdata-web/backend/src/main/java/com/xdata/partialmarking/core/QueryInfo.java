package com.xdata.partialmarking.core;

import java.util.ArrayList;
import java.util.List;

public class QueryInfo {
    private List<String> instructorProjections = new ArrayList<>();
    private List<String> studentProjections = new ArrayList<>();
    private List<Double> studentProjectionMarks = new ArrayList<>();

    private List<String> instructorRelations = new ArrayList<>();
    private List<String> studentRelations = new ArrayList<>();
    private List<Double> studentRelationMarks = new ArrayList<>();

    private List<String> instructorPredicates = new ArrayList<>();
    private List<String> studentPredicates = new ArrayList<>();
    private List<Double> studentPredicateMarks = new ArrayList<>();

    private List<String> instructorJoins = new ArrayList<>();
    private List<String> studentJoins = new ArrayList<>();
    private List<Double> studentJoinMarks = new ArrayList<>();

    private List<String> instructorGroupBy = new ArrayList<>();
    private List<String> studentGroupBy = new ArrayList<>();
    private List<Double> studentGroupByMarks = new ArrayList<>();

    private List<String> instructorOrderBy = new ArrayList<>();
    private List<String> studentOrderBy = new ArrayList<>();
    private List<Double> studentOrderByMarks = new ArrayList<>();

    private String instructorHaving;
    private String studentHaving;
    private double studentHavingMark;

    private int level = 1;
    private boolean studentDistinct = false;
    private boolean instructorDistinct = false;
    private double studentDistinctMark;

    public List<String> getInstructorProjections() { return instructorProjections; }
    public void setInstructorProjections(List<String> instructorProjections) { this.instructorProjections = instructorProjections; }

    public List<String> getStudentProjections() { return studentProjections; }
    public void setStudentProjections(List<String> studentProjections) { this.studentProjections = studentProjections; }

    public List<Double> getStudentProjectionMarks() { return studentProjectionMarks; }
    public void setStudentProjectionMarks(List<Double> studentProjectionMarks) { this.studentProjectionMarks = studentProjectionMarks; }

    public List<String> getInstructorRelations() { return instructorRelations; }
    public void setInstructorRelations(List<String> instructorRelations) { this.instructorRelations = instructorRelations; }

    public List<String> getStudentRelations() { return studentRelations; }
    public void setStudentRelations(List<String> studentRelations) { this.studentRelations = studentRelations; }

    public List<Double> getStudentRelationMarks() { return studentRelationMarks; }
    public void setStudentRelationMarks(List<Double> studentRelationMarks) { this.studentRelationMarks = studentRelationMarks; }

    public List<String> getInstructorPredicates() { return instructorPredicates; }
    public void setInstructorPredicates(List<String> instructorPredicates) { this.instructorPredicates = instructorPredicates; }

    public List<String> getStudentPredicates() { return studentPredicates; }
    public void setStudentPredicates(List<String> studentPredicates) { this.studentPredicates = studentPredicates; }

    public List<Double> getStudentPredicateMarks() { return studentPredicateMarks; }
    public void setStudentPredicateMarks(List<Double> studentPredicateMarks) { this.studentPredicateMarks = studentPredicateMarks; }

    public List<String> getInstructorJoins() { return instructorJoins; }
    public void setInstructorJoins(List<String> instructorJoins) { this.instructorJoins = instructorJoins; }

    public List<String> getStudentJoins() { return studentJoins; }
    public void setStudentJoins(List<String> studentJoins) { this.studentJoins = studentJoins; }

    public List<Double> getStudentJoinMarks() { return studentJoinMarks; }
    public void setStudentJoinMarks(List<Double> studentJoinMarks) { this.studentJoinMarks = studentJoinMarks; }

    public List<String> getInstructorGroupBy() { return instructorGroupBy; }
    public void setInstructorGroupBy(List<String> instructorGroupBy) { this.instructorGroupBy = instructorGroupBy; }

    public List<String> getStudentGroupBy() { return studentGroupBy; }
    public void setStudentGroupBy(List<String> studentGroupBy) { this.studentGroupBy = studentGroupBy; }

    public List<Double> getStudentGroupByMarks() { return studentGroupByMarks; }
    public void setStudentGroupByMarks(List<Double> studentGroupByMarks) { this.studentGroupByMarks = studentGroupByMarks; }

    public List<String> getInstructorOrderBy() { return instructorOrderBy; }
    public void setInstructorOrderBy(List<String> instructorOrderBy) { this.instructorOrderBy = instructorOrderBy; }

    public List<String> getStudentOrderBy() { return studentOrderBy; }
    public void setStudentOrderBy(List<String> studentOrderBy) { this.studentOrderBy = studentOrderBy; }

    public List<Double> getStudentOrderByMarks() { return studentOrderByMarks; }
    public void setStudentOrderByMarks(List<Double> studentOrderByMarks) { this.studentOrderByMarks = studentOrderByMarks; }

    public String getInstructorHaving() { return instructorHaving; }
    public void setInstructorHaving(String instructorHaving) { this.instructorHaving = instructorHaving; }

    public String getStudentHaving() { return studentHaving; }
    public void setStudentHaving(String studentHaving) { this.studentHaving = studentHaving; }

    public double getStudentHavingMark() { return studentHavingMark; }
    public void setStudentHavingMark(double studentHavingMark) { this.studentHavingMark = studentHavingMark; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public boolean isStudentDistinct() { return studentDistinct; }
    public void setStudentDistinct(boolean studentDistinct) { this.studentDistinct = studentDistinct; }

    public boolean isInstructorDistinct() { return instructorDistinct; }
    public void setInstructorDistinct(boolean instructorDistinct) { this.instructorDistinct = instructorDistinct; }

    public double getStudentDistinctMark() { return studentDistinctMark; }
    public void setStudentDistinctMark(double studentDistinctMark) { this.studentDistinctMark = studentDistinctMark; }
}
