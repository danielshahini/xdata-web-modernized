package com.xdata.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import jakarta.annotation.PostConstruct;

@org.springframework.context.annotation.Configuration
public class LegacyConfigBridge {

    @Autowired
    private XDataProperties xDataProperties;

    @Autowired
    private ApplicationContext applicationContext;

    private static ApplicationContext context;

    @PostConstruct
    public void init() {
        context = applicationContext;
        // Überbrücke Spring-Properties zu statischen Legacy-Feldern
        com.xdata.legacy.database.Configuration.homeDir = xDataProperties.getHomeDir();
        com.xdata.legacy.database.Configuration.databaseIP = xDataProperties.getDatabaseIP();
        com.xdata.legacy.database.Configuration.databasePort = xDataProperties.getDatabasePort();
        com.xdata.legacy.database.Configuration.databaseName = xDataProperties.getDatabaseName();
        com.xdata.legacy.database.Configuration.existingDatabaseUser = xDataProperties.getDatabaseUsername();
        com.xdata.legacy.database.Configuration.existingDatabaseUserPasswd = xDataProperties.getDatabasePassword();
        com.xdata.legacy.database.Configuration.testDatabaseUser = xDataProperties.getTest().getUser();
        com.xdata.legacy.database.Configuration.testDatabaseUserPasswd = xDataProperties.getTest().getPassword();

        // Auch für util.Configuration setzen, falls diese direkt genutzt wird
        util.Configuration.homeDir = xDataProperties.getHomeDir();
        util.Configuration.databaseName = xDataProperties.getDatabaseName();
        util.Configuration.existingDatabaseUser = xDataProperties.getDatabaseUsername();
        util.Configuration.existingDatabaseUserPasswd = xDataProperties.getDatabasePassword();
        util.Configuration.databaseIP = xDataProperties.getDatabaseIP();
        util.Configuration.databasePort = xDataProperties.getDatabasePort();
        util.Configuration.smtsolver = "z3";
    }

    public static ApplicationContext getContext() {
        return context;
    }
}
