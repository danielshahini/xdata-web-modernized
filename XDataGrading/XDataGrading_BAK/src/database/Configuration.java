package database;

import java.io.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.ConfigurationInterface;
import util.InitLogger;

public class Configuration implements ConfigurationInterface{
	
	public static Logger logger = Logger.getLogger(Configuration.class.getName());
	public static String databaseName = getProperty("databaseName");
	public static String existingDatabaseUser = getProperty("existingDatabaseUser");
	public static String existingDatabaseUserPasswd = getProperty("existingDatabaseUserPasswd");
	
	public static String testDatabaseUser = getProperty("testDatabaseUser");
	public static String testDatabaseUserPasswd = getProperty("testDatabaseUserPasswd");
	public static String databaseIP = getProperty("databaseIP");
	public static String databasePort = getProperty("databasePort");
	public static String homeDir= getProperty("homeDir");
	
	//Properties required for LTI Integration
	public static String consumerAuthKey = getProperty("consumerAuthKey");
	public static String sectetKey = getProperty("secretKey");
	public static String callBackUrl = getProperty("callBackURL");
	public static String moodleXdataUrl = getProperty("XDataUrlFromLMS");
	public static String logFile=getProperty("logFile");
	public static String logLevel=getProperty("logLevel");
	public static String adminPassword = getProperty("adminPassword");
	
	//public static String  assignmentFolder=getProperty("assignmentFolder");
	public static ConfigurationInterface object = new Configuration();
	 
	public static String getProperty(String property)
	{
		if(object==null){
			object = new Configuration();
			InitLogger.initLogger();
		}
		return object.getPropetyValue(property);
	}

	@Override
	public String getPropetyValue(String property) {
		Properties properties=new Properties();		
		try{
            properties.load(Configuration.class.getResourceAsStream("../../../XData.properties"));
			//  properties.load(Configuration.class.getResourceAsStream("XData.properties"));
		}catch(IOException e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			e.printStackTrace();
			System.exit(1);
		}  
		String prop = properties.getProperty(property);
		if (prop== null)
		{
			System.out.println("Property "+property+" not found");
			throw new NullPointerException();
		}
		return prop;
	}
}
