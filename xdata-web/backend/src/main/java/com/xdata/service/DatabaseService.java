package com.xdata.service;

import com.xdata.model.DbConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
@Slf4j
public class DatabaseService {

    public void loadDriver(String url) {
        try {
            if (url == null) return;
            if (url.startsWith("jdbc:postgresql:")) {
                Class.forName("org.postgresql.Driver");
            } else if (url.startsWith("jdbc:mysql:") || url.startsWith("jdbc:mariadb:")) {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } else if (url.startsWith("jdbc:sqlite:")) {
                Class.forName("org.sqlite.JDBC");
            } else if (url.startsWith("jdbc:derby:")) {
                Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            }
        } catch (ClassNotFoundException e) {
            log.error("JDBC Driver not found for URL {}: {}", url, e.getMessage());
        }
    }

    public Connection getConnection(DbConnection dbConn) throws SQLException {
        if (dbConn == null || dbConn.getUrl() == null) {
            throw new SQLException("Datenbankverbindung oder URL ist null");
        }
        loadDriver(dbConn.getUrl());
        return DriverManager.getConnection(dbConn.getUrl(), dbConn.getUser(), dbConn.getPassword());
    }

    public boolean testConnection(DbConnection dbConn) {
        try (Connection conn = getConnection(dbConn)) {
            return conn.isValid(5);
        } catch (Exception e) {
            log.error("Verbindungstest fehlgeschlagen für {}: {}", dbConn.getUrl(), e.getMessage());
            return false;
        }
    }
}
