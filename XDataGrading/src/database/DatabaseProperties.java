package database;

public class DatabaseProperties {

	public String getUsername1() {
		return Configuration.existingDatabaseUser;
	}

	//public String getUsername2() {
	//	return Configuration.testDatabaseUser;
	//}

	public String getPasswd1() {
		return Configuration.existingDatabaseUserPasswd;
	}

	//public String getPasswd2() {
	//	return Configuration.testDatabaseUserPasswd;
	//}

	public String getHostname() {
		return Configuration.databaseIP;
	}

	public String getDbName() {
		return Configuration.databaseName;
	}

	public String getPortNumber() {
		return Configuration.databasePort;
	}
	
}
