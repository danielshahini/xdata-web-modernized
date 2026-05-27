package com.xdata;

import java.sql.*;

public class TestMetadata {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:postgresql://127.0.0.1:5432/xdatadb";
        String user = "postgres";
        String password = "password"; // Wir wissen, dass es "1709" oder "password" ist.
        
        // Versuche "1709" zuerst, da es im Backend-Startbefehl verwendet wurde
        password = "1709";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            DatabaseMetaData meta = conn.getMetaData();
            System.out.println("Database: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
            
            System.out.println("--- All Tables in 'public' ---");
            try (ResultSet rs = meta.getTables(conn.getCatalog(), "public", "%", null)) {
                while (rs.next()) {
                    System.out.println("Table: " + rs.getString("TABLE_NAME") + " (Type: " + rs.getString("TABLE_TYPE") + ")");
                }
            }
            
            System.out.println("--- Tables with filter ['TABLE', 'VIEW'] in 'public' ---");
            try (ResultSet rs = meta.getTables(conn.getCatalog(), "public", "%", new String[]{"TABLE", "VIEW"})) {
                while (rs.next()) {
                    System.out.println("Filtered Table: " + rs.getString("TABLE_NAME"));
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
