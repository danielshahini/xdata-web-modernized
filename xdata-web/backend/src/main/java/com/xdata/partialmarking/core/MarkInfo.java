package com.xdata.partialmarking.core;

import java.util.ArrayList;
import java.util.List;

public class MarkInfo {
    private double marks;
    private double maxMarks;
    private double percentage;
    private List<QueryInfo> subqueryData = new ArrayList<>();

    public double getMarks() {
        return marks;
    }

    public void setMarks(double marks) {
        this.marks = marks;
    }

    public double getMaxMarks() {
        return maxMarks;
    }

    public void setMaxMarks(double maxMarks) {
        this.maxMarks = maxMarks;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public List<QueryInfo> getSubqueryData() {
        return subqueryData;
    }

    public void setSubqueryData(List<QueryInfo> subqueryData) {
        this.subqueryData = subqueryData;
    }
}
