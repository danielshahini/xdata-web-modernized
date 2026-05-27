package com.xdata.service;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Service;

@Service
public class SqlSandboxService {

    public void validateQuery(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new RuntimeException("SQL Query darf nicht leer sein.");
        }

        // Grundlegende Prüfung auf gefährliche Keywords (zusätzlich zu JSqlParser)
        String upperSql = sql.toUpperCase();
        if (upperSql.contains("DROP") || upperSql.contains("DELETE") || upperSql.contains("UPDATE") || upperSql.contains("TRUNCATE") || upperSql.contains("ALTER")) {
            throw new RuntimeException("Nur Lesezugriffe (SELECT) sind in der Sandbox erlaubt.");
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw new RuntimeException("Nur SELECT-Statements sind erlaubt.");
            }

            // Prüfung auf kartesische Produkte (vereinfacht)
            if (upperSql.contains(",") && !upperSql.contains("WHERE") && !upperSql.contains("JOIN")) {
                 // Mögliches kartesisches Produkt: SELECT * FROM t1, t2
                 // Wir lassen es für kleine Aufgaben evtl. zu, aber hier warnen wir oder blocken es.
                 // log.warn("Potentielles kartesisches Produkt erkannt.");
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException("Ungültige SQL-Syntax: " + e.getMessage());
        }
    }
}
