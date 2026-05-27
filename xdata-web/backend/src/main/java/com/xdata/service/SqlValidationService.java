package com.xdata.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SqlValidationService {

    private static final List<String> FORBIDDEN_KEYWORDS = Arrays.asList(
            "DROP", "TRUNCATE", "DELETE", "UPDATE", "INSERT", "ALTER", "CREATE", "GRANT", "REVOKE", 
            "SHUTDOWN", "EXEC", "EXECUTE", "XP_CMDSHELL", "SYS.", "INFORMATION_SCHEMA"
    );

    /**
     * Sanitizes a student query to prevent SQL injection and unauthorized operations.
     */
    public String sanitizeStudentQuery(String query) {
        if (query == null) return "";
        
        String upperQuery = query.toUpperCase();
        for (String keyword : FORBIDDEN_KEYWORDS) {
            if (upperQuery.contains(keyword)) {
                log.warn("Forbidden SQL keyword detected: {}", keyword);
                throw new SecurityException("Query contains forbidden keyword: " + keyword);
            }
        }
        
        // Remove comments
        query = query.replaceAll("--.*", "");
        query = query.replaceAll("/\\*.*\\*/", "");
        
        return query.trim();
    }

    /**
     * Validates that an instructor query is syntactically correct and safe.
     */
    public boolean validateInstructorQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        try {
            net.sf.jsqlparser.statement.Statement statement = net.sf.jsqlparser.parser.CCJSqlParserUtil.parse(query);
            return statement instanceof net.sf.jsqlparser.statement.select.Select;
        } catch (Exception e) {
            log.warn("SQL Syntax validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Validates a schema script.
     */
    public void validateSchemaScript(String script) {
        if (script == null || script.isEmpty()) {
            throw new IllegalArgumentException("Schema script is empty");
        }
        
        // Basic check: Allow only DDL and INSERT for sample data
        String upperScript = script.toUpperCase();
        if (upperScript.contains("DROP DATABASE") || upperScript.contains("GRANT ALL")) {
            throw new SecurityException("Schema script contains dangerous operations");
        }
    }
}
