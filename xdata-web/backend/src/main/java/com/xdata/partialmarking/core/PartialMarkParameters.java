package com.xdata.partialmarking.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PartialMarkParameters implements Serializable {

    @JsonProperty("relation")
    private double relation = 1.0;
    @JsonProperty("predicate")
    private double predicate = 1.0;
    @JsonProperty("projection")
    private double projection = 1.0;
    @JsonProperty("joins")
    private double joins = 1.0;
    @JsonProperty("whereSubQueries")
    private double whereSubQueries = 1.0;
    @JsonProperty("fromSubQueries")
    private double fromSubQueries = 1.0;
    @JsonProperty("outerQuery")
    private double outerQuery = 1.0;
    @JsonProperty("groupBy")
    private double groupBy = 1.0;
    @JsonProperty("havingClause")
    private double havingClause = 1.0;
    @JsonProperty("subQConnective")
    private double subQConnective = 1.0;
    @JsonProperty("aggregates")
    private double aggregates = 1.0;
    @JsonProperty("setOperators")
    private double setOperators = 1.0;
    @JsonProperty("distinct")
    private double distinct = 1.0;
    @JsonProperty("orderBy")
    private double orderBy = 1.0;
    @JsonProperty("maxPartialMarks")
    private int maxPartialMarks = 90;

    public double getPredicate() {
        return predicate;
    }

    public void setPredicate(double predicate) {
        this.predicate = predicate;
    }

    public double getProjection() {
        return projection;
    }

    public void setProjection(double projection) {
        this.projection = projection;
    }

    public double getJoins() {
        return joins;
    }

    public void setJoins(double joins) {
        this.joins = joins;
    }

    public double getWhereSubQueries() {
        return whereSubQueries;
    }

    public void setWhereSubQueries(double whereSubQueries) {
        this.whereSubQueries = whereSubQueries;
    }

    public double getFromSubQueries() {
        return fromSubQueries;
    }

    public void setFromSubQueries(double fromSubQueries) {
        this.fromSubQueries = fromSubQueries;
    }

    public double getOuterQuery() {
        return outerQuery;
    }

    public void setOuterQuery(double outerQuery) {
        this.outerQuery = outerQuery;
    }

    public double getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(double groupBy) {
        this.groupBy = groupBy;
    }

    public double getRelation() {
        return relation;
    }

    public void setRelation(double relation) {
        this.relation = relation;
    }

    public double getHavingClause() {
        return havingClause;
    }

    public void setHavingClause(double havingClause) {
        this.havingClause = havingClause;
    }

    public double getSubQConnective() {
        return subQConnective;
    }

    public void setSubQConnective(double subQConnective) {
        this.subQConnective = subQConnective;
    }

    public double getAggregates() {
        return aggregates;
    }

    public void setAggregates(double aggregates) {
        this.aggregates = aggregates;
    }

    public double getSetOperators() {
        return setOperators;
    }

    public void setSetOperators(double setOperators) {
        this.setOperators = setOperators;
    }

    public double getDistinct() {
        return distinct;
    }

    public void setDistinct(double distinct) {
        this.distinct = distinct;
    }

    public double getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(double orderBy) {
        this.orderBy = orderBy;
    }

    @JsonIgnore
    public Double getValue() {
        return (this.relation + this.projection + this.predicate + this.joins + this.whereSubQueries + this.fromSubQueries + this.outerQuery + this.groupBy + this.havingClause
                + this.subQConnective + this.aggregates + this.setOperators + this.distinct + this.orderBy);
    }

    public int getMaxPartialMarks() {
        return maxPartialMarks;
    }

    public void setMaxPartialMarks(int maxPartialMarks) {
        this.maxPartialMarks = maxPartialMarks;
    }
}
