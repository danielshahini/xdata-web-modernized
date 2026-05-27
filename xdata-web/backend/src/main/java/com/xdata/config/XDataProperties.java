package com.xdata.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "xdata")
@Data
public class XDataProperties {
    
    public String getSmtsolver() { return smtsolver; }
    public void setSmtsolver(String smtsolver) { this.smtsolver = smtsolver; }
    
    public String getSmtargs() { return smtargs; }
    public void setSmtargs(String smtargs) { this.smtargs = smtargs; }
    
    public String getHomeDir() { return homeDir; }
    public void setHomeDir(String homeDir) { this.homeDir = homeDir; }
    
    public String getDataDir() { return dataDir; }
    public void setDataDir(String dataDir) { this.dataDir = dataDir; }
    
    public String getDatabaseIP() { return databaseIP; }
    public void setDatabaseIP(String databaseIP) { this.databaseIP = databaseIP; }
    
    public String getDatabasePort() { return databasePort; }
    public void setDatabasePort(String databasePort) { this.databasePort = databasePort; }
    
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    
    public String getDatabaseUsername() { return databaseUsername; }
    public void setDatabaseUsername(String databaseUsername) { this.databaseUsername = databaseUsername; }
    
    public String getDatabasePassword() { return databasePassword; }
    public void setDatabasePassword(String databasePassword) { this.databasePassword = databasePassword; }

    private String smtsolver;
    private String smtargs;
    private String homeDir;
    private String dataDir;
    private String databaseIP;
    private String databasePort;
    private String databaseName;
    private String databaseUsername;
    private String databasePassword;
    
    public TestDb getTest() { return test; }
    public void setTest(TestDb test) { this.test = test; }

    private TestDb test = new TestDb();

    @Data
    public static class TestDb {
        
        public String getUser() { return user; }
        public void setUser(String user) { this.user = user; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        private String user;
        private String password;
    }
}
