package com.xdata.partialmarking.service;

import com.xdata.partialmarking.dto.ColumnDTO;
import com.xdata.partialmarking.dto.SchemaDTO;
import com.xdata.partialmarking.dto.TableDTO;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.table.ColumnDefinition;

import java.util.ArrayList;

public class SqlSchemaParser {
    public static SchemaDTO parse(String sql) throws Exception {
        SchemaDTO schema = new SchemaDTO();
        schema.setTables(new ArrayList<>());

        try {
            Statements statements = CCJSqlParserUtil.parseStatements(sql);
            for (Statement stmt : statements.getStatements()) {
                if (stmt instanceof CreateTable) {
                    CreateTable createTable = (CreateTable) stmt;
                    TableDTO table = new TableDTO();
                    table.setTableName(createTable.getTable().getName());
                    table.setColumns(new ArrayList<>());

                    if (createTable.getColumnDefinitions() != null) {
                        for (ColumnDefinition colDef : createTable.getColumnDefinitions()) {
                            ColumnDTO column = new ColumnDTO();
                            column.setColumnName(colDef.getColumnName());
                            column.setDataType(colDef.getColDataType().getDataType());
                            table.getColumns().add(column);
                        }
                    }
                    schema.getTables().add(table);
                }
            }
        } catch (Throwable e) {
            String errorMsg = e.getMessage();
            if (e.getClass().getName().contains("TokenMgrException")) {
                errorMsg = "Lexical error: " + e.getMessage();
            }
            throw new Exception("Error parsing Schema SQL: " + errorMsg);
        }

        return schema;
    }
}
