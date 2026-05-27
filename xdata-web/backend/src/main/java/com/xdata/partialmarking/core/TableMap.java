package com.xdata.partialmarking.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TableMap implements Serializable {
    private Map<String, Table> tables = new HashMap<>();

    public Map<String, Table> getTables() {
        return tables;
    }

    public void addTable(Table table) {
        tables.put(table.getTableName().toUpperCase(), table);
    }

    public Table getTable(String tableName) {
        return tables.get(tableName.toUpperCase());
    }
}
