package com.xdata.partialmarking.service;

import com.xdata.partialmarking.core.*;
import com.xdata.partialmarking.dto.*;

public class TableMapBuilder {
    public static TableMap build(SchemaDTO schemaDto) {
        TableMap tm = new TableMap();
        if (schemaDto != null && schemaDto.getTables() != null) {
            for (TableDTO tDto : schemaDto.getTables()) {
                Table table = new Table(tDto.getTableName());
                if (tDto.getColumns() != null) {
                    for (ColumnDTO cDto : tDto.getColumns()) {
                        Column col = new Column(cDto.getColumnName(), cDto.getDataType() != null ? cDto.getDataType() : "VARCHAR");
                        table.addColumn(col);
                    }
                }
                tm.addTable(table);
            }
        }
        return tm;
    }
}
