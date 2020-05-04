package database;

public class DBConnectionInfo {

	private String dbName = "";
	private String dbType="";
	private String user = "";
	private String password = "";
	//private String testUser="";
	//private String testPassword = "";
	private String jdbc_Url="";
	private String connName="";
	private String connId = "";
	private String jdbcToSave = "";
	
	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}
	/**
	 * @param dbName the dbName to set
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	/**
	 * @return the dbType
	 */
	public String getDbType() {
		return dbType;
	}
	
	
	/**
	 * @return the dbType
	 */
	public String getDbType(String dbSelected) {
		String db = "";
		if(dbSelected.equals("01")){
			db = "PostgreSQL";
		}
		else if(dbSelected.equals("02")){
			db ="MSSQL";
		}
		else if(dbSelected.equals("03")){
			db = "Oracle";
		}
		else if(dbSelected.equals("04")){
			db = "SQLite";
		}
		else if(dbSelected.equals("05")){
			db = "MySql";
		}
		return db;
	}
	
	/** 
	 * @param dbType the dbType to set
	 */
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	/**
	 * @return the dbUser
	 */
	public String getDbUser() {
		return user;
	}
	/**
	 * @param dbUser the dbUser to set
	 */
	public void setDbUser(String dbUser) {
		this.user = dbUser;
	}
	/**
	 * @return the dbPwd
	 */
	public String getDbPwd() {
		return password;
	}
	/**
	 * @param dbPwd the dbPwd to set
	 */
	public void setDbPwd(String dbPwd) {
		this.password = dbPwd;
	}
	/**
	 * @return the testUser
	 */
	/*public String getTestUser() {
		return testUser;
	}*/
	/**
	 * @param testUser the testUser to set
	 */
	/*public void setTestUser(String testUser) {
		this.testUser = testUser;
	}*/
	/**
	 * @return the testPassword
	 */
	/*public String getTestPassword() {
		return testPassword;
	}*/
	/**
	 * @param testPassword the testPassword to set
	 */
	/*public void setTestPassword(String testPassword) {
		this.testPassword = testPassword;
	}*/
	/**
	 * @return the jdbc_Url
	 */
	public String getJdbc_Url() {
		return jdbc_Url;
	}
	/**
	 * @param jdbc_Url the jdbc_Url to set
	 */
	public void setJdbc_Url(String jdbc_Url) {
		this.jdbc_Url = jdbc_Url;
	}
	/**
	 * @return the connName
	 */
	public String getConnName() {
		return connName;
	}
	/**
	 * @param connName the connName to set
	 */
	public void setConnName(String connName) {
		this.connName = connName;
	}
	/**
	 * @return the connId
	 */
	public String getConnId() {
		return connId;
	}
	/**
	 * @param connId the connId to set
	 */
	public void setConnId(String connId) {
		this.connId = connId;
	}
	/**
	 * @return the jdbcToSave
	 */
	public String getJdbcToSave() {
		return jdbcToSave;
	}
	/**
	 * @param jdbcToSave the jdbcToSave to set
	 */
	public void setJdbcToSave(String jdbcToSave) {
		this.jdbcToSave = jdbcToSave;
	}
	
}
