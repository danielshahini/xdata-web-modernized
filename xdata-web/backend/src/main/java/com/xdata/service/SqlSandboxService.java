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
            throw new RuntimeException("SQL query must not be empty.");
        }

        // Basic check for dangerous keywords (in addition to JSqlParser)
        String upperSql = sql.toUpperCase();
        if (upperSql.contains("DROP") || upperSql.contains("DELETE") || upperSql.contains("UPDATE") || upperSql.contains("TRUNCATE") || upperSql.contains("ALTER")) {
            throw new RuntimeException("Only read access (SELECT) is allowed in the sandbox.");
        }

        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            if (!(statement instanceof Select)) {
                throw new RuntimeException("Only SELECT statements are allowed.");
            }

            // Check for cartesian products (simplified)
            if (upperSql.contains(",") && !upperSql.contains("WHERE") && !upperSql.contains("JOIN")) {
                 // Possible cartesian product: SELECT * FROM t1, t2
                 // We may allow it for small tasks, but here we warn or block it.
                 // log.warn("Potential cartesian product detected.");
            }

        } catch (JSQLParserException e) {
            throw new RuntimeException("Invalid SQL syntax: " + e.getMessage());
        }
    }
}
