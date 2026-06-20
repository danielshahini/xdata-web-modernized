package com.xdata.legacy.database;

import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

	public static Logger logger = Logger.getLogger(Configuration.class.getName());

	/**
	 * Bundled datagen defaults (tempDatabaseType, cntFlag, isEnumInt, smtsolver, ...),
	 * lazily loaded from the classpath resource {@code util/XData.properties}. These are
	 * the canonical XData-Datagen values; without them the legacy TableMap/GenerateCVC1
	 * path NPEs on the first missing key. Runtime-set static fields below take priority.
	 */
	private static volatile Properties datagenDefaults;

	private static Properties datagenDefaults() {
		Properties p = datagenDefaults;
		if (p == null) {
			synchronized (Configuration.class) {
				p = datagenDefaults;
				if (p == null) {
					p = new Properties();
					try (InputStream in = Configuration.class.getResourceAsStream("/util/XData.properties")) {
						if (in != null) {
							p.load(in);
						} else {
							logger.warning("util/XData.properties not found on classpath; datagen keys will be null");
						}
					} catch (Exception e) {
						logger.log(Level.WARNING, "Could not load util/XData.properties: " + e.getMessage(), e);
					}
					datagenDefaults = p;
				}
			}
		}
		return p;
	}

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
        // Runtime-overridden fields take priority (set from env by LegacyConfigBridge).
        if ("databaseName".equals(property) && databaseName != null) return databaseName;
        if ("existingDatabaseUser".equals(property) && existingDatabaseUser != null) return existingDatabaseUser;
        if ("existingDatabaseUserPasswd".equals(property) && existingDatabaseUserPasswd != null) return existingDatabaseUserPasswd;
        if ("testDatabaseUser".equals(property) && testDatabaseUser != null) return testDatabaseUser;
        if ("testDatabaseUserPasswd".equals(property) && testDatabaseUserPasswd != null) return testDatabaseUserPasswd;
        if ("databaseIP".equals(property) && databaseIP != null) return databaseIP;
        if ("databasePort".equals(property) && databasePort != null) return databasePort;
        if ("homeDir".equals(property) && homeDir != null) return homeDir;
        // Fall back to bundled XData.properties (datagen keys live here).
        return datagenDefaults().getProperty(property);
    }
}
