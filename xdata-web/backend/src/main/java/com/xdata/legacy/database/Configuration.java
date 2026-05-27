package com.xdata.legacy.database;

import java.util.logging.Logger;

public class Configuration {
	
	public static Logger logger = Logger.getLogger(Configuration.class.getName());
	public static String databaseName;
	public static String existingDatabaseUser;
	public static String existingDatabaseUserPasswd;
	
	public static String testDatabaseUser;
	public static String testDatabaseUserPasswd;
	public static String databaseIP;
	public static String databasePort;
	public static String homeDir;
	
	public static String consumerAuthKey;
	public static String sectetKey;
	public static String callBackUrl;
	public static String moodleXdataUrl;
	public static String logFile;
	public static String logLevel;
	public static String adminPassword;
    
    public static String getProperty(String property) {
        if ("databaseName".equals(property)) return databaseName;
        if ("existingDatabaseUser".equals(property)) return existingDatabaseUser;
        if ("existingDatabaseUserPasswd".equals(property)) return existingDatabaseUserPasswd;
        if ("testDatabaseUser".equals(property)) return testDatabaseUser;
        if ("testDatabaseUserPasswd".equals(property)) return testDatabaseUserPasswd;
        if ("databaseIP".equals(property)) return databaseIP;
        if ("databasePort".equals(property)) return databasePort;
        if ("homeDir".equals(property)) return homeDir;
        return null;
    }
}
