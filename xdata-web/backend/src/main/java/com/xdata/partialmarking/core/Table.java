package com.xdata.partialmarking.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Table implements Serializable {
    private String tableName;
    private Map<String, Column> columns = new HashMap<>();

    public Table(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public void addColumn(Column column) {
        columns.put(column.getColumnName().toUpperCase(), column);
    }

    public Map<String, Column> getColumns() {
        return columns;
    }
}
