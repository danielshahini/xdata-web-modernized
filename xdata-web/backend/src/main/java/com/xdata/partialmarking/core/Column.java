package com.xdata.partialmarking.core;

import java.io.Serializable;

public class Column implements Serializable {
    private String columnName;
    private String dataType;

    public Column(String columnName, String dataType) {
        this.columnName = columnName;
        this.dataType = dataType;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getDataType() {
        return dataType;
    }
}
