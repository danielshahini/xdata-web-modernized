
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;

import database.*;

/**
 * Servlet implementation class LoginChecker
 */
// @WebServlet("/LoginChecker")
public class LoginChecker extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(LoginChecker.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */

	
	public LoginChecker() {
		super();
		// dbcon = null;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void init(ServletConfig c) throws ServletException {
		// Open the connection here

	}

	

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		String uname = ""; 
		String pwd = "";
		Configuration config = new Configuration();
		
		if (request.getParameter("name") != null) {
			session.invalidate();
			session = request.getSession(true);
			uname = request.getParameter("name");
			pwd = request.getParameter("password");
			session.setAttribute("uname", uname);
			session.setAttribute("pwd", pwd);
		} else {
			uname = (String) session.getAttribute("uname");
			pwd = (String) session.getAttribute("pwd");
		}
		// Get login details from DB and compare
		String role = "";
		try(Connection dbCon = new DatabaseConnection().dbConnection()) {
			
			/** RAM:  execute a select * from xdata_users ... inside a try catch.  Exception means relation
			 *  does not exist, in that case create all the relations from Install.sh
			 */
			// my code here:rambabu
				try {
					PreparedStatement pstmt = dbCon
							.prepareStatement("select * from xdata_users where login_user_id =?  and password=?");
						pstmt.setString(1, uname); 
						pstmt.setString(2, DigestUtils.md5Hex(pwd));
						pstmt.executeQuery(); 
						
				}catch (SQLException e) {					
					create_tables(dbCon);
				}
				// my code above
			
					PreparedStatement pstmt = dbCon
							.prepareStatement("select * from xdata_users where login_user_id =?  and password=?");
					try{
						pstmt.setString(1, uname); 
						
						pstmt.setString(2, DigestUtils.md5Hex(pwd));
						
						ResultSet rs = null;
							try{
									rs =pstmt.executeQuery(); 
									logger.log(Level.FINE,pstmt.toString());
									if(rs.next() && !uname.equals("admin")){ 
									  
									PreparedStatement pstmt1 = dbCon
											.prepareStatement("select * from xdata_roles where internal_user_id=?");
									pstmt1.setString(1, rs.getString("internal_user_id"));
									ResultSet rs1 = pstmt1.executeQuery();
										 try{
											if (rs1.next()) {
												role = rs1.getString("role");
												response.setContentType("text/html");
												PrintWriter out2 = response.getWriter();
												session.setAttribute("user_id",rs.getString("internal_user_id"));
												session.setAttribute("login_user_id",rs.getString("login_user_id"));
												session.setAttribute("context_label", rs1.getString("course_id"));
												session.setAttribute("resource_link_id", "");
												session.setAttribute("lis_person_name_full",
														rs.getString("user_name"));
												session.setAttribute("lis_person_contact_email_primary",
														rs.getString("email"));
												//session.setAttribute("roles", role);
												session.setAttribute("ltiIntegration", false);
								  
												if (role.equalsIgnoreCase("instructor")) {
													session.setAttribute("LOGIN_USER", "ADMIN");
													session.setAttribute("role",role);
													response.sendRedirect("Empty.html");
												}else if (role.equalsIgnoreCase("tester")) {
													session.setAttribute("LOGIN_USER", "tester");
													session.setAttribute("role",role);
													response.sendRedirect("Empty.html");
												} else if(uname.equalsIgnoreCase("guest")){
													session.setAttribute("LOGIN_USER", "guest");
													session.setAttribute("role","guest");
													response.sendRedirect("Empty.html");
												}else if (role.equalsIgnoreCase("student")) {
													session.setAttribute("LOGIN_USER", "student");
													session.setAttribute("role",role);
													response.sendRedirect("Empty.html");
												} else if (role.equalsIgnoreCase("admin")) {
													session.setAttribute("LOGIN_USER", "ADMIN");
													session.setAttribute("role",role);
													response.sendRedirect("adminHome.jsp");
												}
												
												
												return;
								
											} 
											else{
												throw new ServletException("User role does not exist. Please contact your administrator for assigning course and role information to user "+uname+".");
											}
						
										 }finally{
											 if(rs1 != null)
													rs1.close();
											if(pstmt1 != null)
												pstmt1.close();
										}	
							}else if(uname.equals("admin")){		
							
								
								String Ad_password=Configuration.getProperty("adminPassword");
								//checking password from config files
								if(Ad_password.equals(pwd))
								{
									PreparedStatement pstmt_adminCheck = dbCon
											.prepareStatement("select * from xdata_users where login_user_id ='admin'");
									
									ResultSet rs1 = null;
						
									rs1 =pstmt_adminCheck.executeQuery(); 
									//if first time login by admin, insert admin credentials
									/*PreparedStatement pstmt1 = dbCon
											.prepareStatement("insert into xdata_users (internal_user_id,user_name,login_user_id,password) values(?,?,?,?)");
									pstmt1.setString(1,"XD1");
									pstmt1.setString(2,"Administrator");
									pstmt1.setString(3, uname);
									pstmt1.setString(4, Configuration.getProperty("adminPassword"));
									
									pstmt1.executeQuery(); */
									if(rs1.next())
									{
										//pulling admin info from database
									role = rs1.getString("role");
									response.setContentType("text/html");
									PrintWriter out2 = response.getWriter();
									session.setAttribute("user_id",rs1.getString("internal_user_id"));
									session.setAttribute("login_user_id",rs1.getString("login_user_id"));
									session.setAttribute("context_label", rs1.getString("course_id"));
									session.setAttribute("resource_link_id", "");
									session.setAttribute("lis_person_name_full",
											rs1.getString("user_name"));
									session.setAttribute("lis_person_contact_email_primary",
											rs1.getString("email"));
									}
									//session.setAttribute("roles", role);
									session.setAttribute("ltiIntegration", false);
									
									
									
									session.setAttribute("LOGIN_USER", "ADMIN");
									session.setAttribute("role",role);
									response.sendRedirect("adminHome.jsp");
								}
								else
								{
									//admin password incorrect-  not first time login !
									session.invalidate();
									response.setContentType("text/html");
									response.sendRedirect("index.jsp?Login=false");
								}
								
							}else{
								session.invalidate();
								response.setContentType("text/html");
								response.sendRedirect("index.jsp?Login=false");
								 

							
							return;
							}
			
						}finally{
						if(rs != null)
							 rs.close();
						
						}
					}finally{
						pstmt.close();
					}
			/* }//For admin user - If login for first time, get password from config file and set in DB - Don't hash the password
			//If it is consecutive admin login's, even then dont hash the password
			 else{
				 dbCon = new DatabaseConnection().dbConnection();
					PreparedStatement pstmt = dbCon
							.prepareStatement("select * from xdata_users where login_user_id =?");
					try{
						pstmt.setString(1, uname);
						ResultSet rs = null;
							try{
									rs =pstmt.executeQuery(); 
									if(rs.next()){
										//This is not the first time login. So authenticate with password stored in DB
									
									}else{
										//First time login, so insert login credentials in DB
										PreparedStatement pstmt1 = dbCon
												.prepareStatement("insert into xdata_users where login_user_id =? and password=?");
										pstmt1.setString(1, uname);
										pstmt1.setString(2, DigestUtils.md5Hex(Get from properties file));
										pstmt1.executeQuery(); 
										logger.log(Level.FINE,"PWD to test = DigestUtils.md5Hex(pwd) =="+DigestUtils.md5Hex(pwd));
									}
							}catch(Exception e){
								throw new ServletException(e);
							}
					}catch(Exception e){
						throw new ServletException(e);
					}
			 }*/
			

		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);		
			}
		
	}
	
	// my code here
	public static void create_tables(Connection dbCon)
	{
		try{  	 
			
			Statement stmt=dbCon.createStatement(); 
			
			String query = "CREATE TABLE xdata_assignment (\n" + 
					"    course_id character varying(20),\n" + 
					"    assignment_id integer,\n" + 
					"    description text,\n" + 
					"    starttime timestamp without time zone,\n" + 
					"    endtime timestamp without time zone,\n" + 
					"    learning_mode boolean DEFAULT false,\n" + 
					"    connection_id integer,\n" + 
					"    defaultschemaid integer,\n" + 
					"    assignmentname character varying(20),\n" + 
					"	 defaultDSetId text, \n" +
					"    evaluationstatus boolean, \n" +
					"    showmarks boolean, \n" +
					"    softtime timestamp without time zone,\n" + 
					"	 penalty character varying(20),\n" + 
					"primary key(course_id,assignment_id)\n" + 
					");" ;
			stmt.executeUpdate(query); 
			
			query = "CREATE TABLE xdata_database_connection (\n" + 
					"    course_id character varying NOT NULL,\n" + 
					"    connection_id integer NOT NULL,\n" + 
					"    connection_name character varying,\n" + 
					"    database_type character varying DEFAULT 'PostgreSql'::character varying,\n" + 
					"    jdbc_url character varying,\n" + 
					"    database_user character varying DEFAULT 'testing1'::character varying,\n" + 
					"    database_password character varying DEFAULT 'testing1'::character varying,\n" + 
					"    test_user character varying,\n" + 
					"    test_password character varying,\n" + 
					"    database_name character varying(50),\n" + 
					"    jdbcData varchar(100),\n" + 
					"    PRIMARY KEY (course_id, connection_id)\n" + 
					");" ;
			stmt.executeUpdate(query); 
			
			query = "CREATE SEQUENCE database_connection_connection_id_seq\n" + 
					"    START WITH 1\n" + 
					"    INCREMENT BY 1\n" + 
					"    NO MINVALUE\n" + 
					"    NO MAXVALUE\n" + 
					"    CACHE 1;" ;
			stmt.executeUpdate(query); 
			
			query = "ALTER SEQUENCE database_connection_connection_id_seq OWNED BY xdata_database_connection.connection_id;" ;
			stmt.executeUpdate(query);
			
			query =	"CREATE TABLE xdata_datasetvalue (\n" + 
					"    queryid character varying,\n" + 
					"    datasetid character varying,\n" + 
					"    value character varying,\n" + 
					"    tag character varying,\n" + 
					"    assignment_id integer,\n" + 
					"    question_id integer,\n" + 
					"    query_id integer,\n" + 
					"    course_id varchar(20),\n" + 
					"    isResultMatch boolean, \n" + 
					"primary key (course_id, assignment_id,question_id,query_id,datasetid)\n" + 
					");" ;
		  stmt.executeUpdate(query);
		  
		  query  = "CREATE TABLE xdata_detectdataset (\n" + 
					"    user_id character varying,\n" + 
					"    queryid character varying(20),\n" + 
					"    datasetid character varying(20),\n" + 
					"    result text,\n" + 
					"    assignment_id integer,\n" + 
					"    question_id integer,\n" + 
					"    course_id varchar(20)\n" + 
					");" ;
		  
		  stmt.executeUpdate(query);
		  
		  query =	"CREATE TABLE xdata_student_queries (\n" + 
					"    dbid text,\n" + 
					"    queryid text NOT NULL,\n" + 
					"    rollnum text NOT NULL,\n" + 
					"    querystring text NOT NULL,\n" + 
					"    tajudgement boolean,\n" + 
					"    verifiedcorrect boolean,\n" +
					"    assignment_id integer,\n" + 
					"    question_id integer,\n" + 
					"    result text,\n" + 
					"    course_id varchar(20),\n" + 
					"	 score numeric, \n" +
					"    max_marks numeric, \n" +
					"	 markinfo text, \n" +
					"    late_submission_flag boolean, \n" +
					"    isEvaluated boolean, \n" + 
					"	 scaled_score numeric, \n" + 
					" 	 submissiontime timestamp without time zone,\n" +
					" 	feedback text, \n" +
					"	manual_score numeric, \n" +
					"	raw_score numeric, \n" +
					"	xdata_score numeric, \n" +
					"    primary key(course_id,assignment_id,question_id,rollnum)\n" + 
					");" ;
		  stmt.executeUpdate(query);
		  
		  query =	"CREATE TABLE xdata_instructor_query (\n" + 
					"    assignment_id integer NOT NULL,\n" + 
					"    question_id integer NOT NULL,\n" + 
					"    sql text,\n" +
					"    course_id character varying(20) NOT NULL,\n" + 
					"    query_id integer NOT NULL,\n" + 
					"    marks integer,\n" + 
					"	 default_sampledataid text, \n" +
					"	 partialmarkinfo text, \n" +
					"	 resultondataset text, \n" + 
					"    evaluationstatus boolean, \n" + 
					"    PRIMARY KEY (course_id, assignment_id, question_id, query_id)\n" + 
					");";
		  stmt.executeUpdate(query);
		  
		  query =	"CREATE TABLE xdata_schemainfo (\n" + 
					"    course_id character varying(20) NOT NULL,\n" + 
					"    schema_id integer NOT NULL,\n" + 
					"    schema_name character varying(20),\n" + 
					"    ddltext text,\n" + 
					"    sample_data text,\n" + 
					"    sample_data_name character varying(50),\n" + 
					"   primary key(course_id,schema_id)\n" + 
					");" ;
		  
		 stmt.executeUpdate(query);
		 
		 query =    "CREATE TABLE xdata_qinfo (\n" + 
					"    course_id character varying(20),\n" + 
					"    assignment_id integer,\n" + 
					"    question_id integer,\n" + 
					"    querytext text,\n" + 
					"    correctquery text,\n" + 
					"    totalmarks integer,\n" + 
					"    learningmode boolean,\n" + 
					"    ignoreduplicates boolean,\n" + 
					"    matchallqueries boolean,\n" + 
					"    query_id integer,\n" + 
					"    optionalschemaid integer,\n" + 
					"    orderindependent boolean,\n" + 
					"    default_sampledataid text, \n" +
					"    equivalence_failed_datasets text, \n" +
					"    equivalenceStatus boolean, \n" + 
					"    latesubmissionmarks int, \n" + 
					"	 scale numeric, \n" +
					" primary key(course_id,assignment_id,question_id,query_id));" ;
		 
		 stmt.executeUpdate(query);
		 
		query =		"CREATE TABLE xdata_users (\n" + 
					"    internal_user_id character varying(20) NOT NULL,\n" + 
					"    user_name character varying(100),\n" + 
					"    email character varying(100),\n" + 
					"    sourceid text,\n" + 
					"    password text,\n" + 
					"    role character varying(30),\n" + 
					"    course_id character varying(20),\n" + 
					"    login_user_id  varchar(50),\n" + 
					"   PRIMARY KEY (internal_user_id)\n" + 
					");" ;
		stmt.executeUpdate(query);
		
		query =		"CREATE TABLE xdata_views (\n" + 
					"    vname text NOT NULL,\n" + 
					"    rollnum text NOT NULL,\n" + 
					"    viewquery text NOT NULL,\n" + 
					"PRIMARY KEY (vname, rollnum)\n" + 
					");" ;
		stmt.executeUpdate(query);
		
		query =		"CREATE TABLE xdata_course (\n" + 
					"    course_id integer,\n" + 
					"    instructor_course_id character varying(30),\n" + 
					"    course_name character varying(100),\n" + 
					"    year numeric,\n" + 
					"    semester character varying(50),\n" + 
					"    description text, \n" + 
					"    primary key(instructor_course_id));" ;
		
		stmt.executeUpdate(query);
		
		query =	"ALTER TABLE ONLY xdata_database_connection ALTER COLUMN connection_id SET DEFAULT nextval('database_connection_connection_id_seq'::regclass);";
		stmt.executeUpdate(query);
		
		query =	"Create table xdata_roles (\n" + 
					"	internal_user_id  varchar(25),\n" + 
					"	login_user_id varchar(50),\n" + 
					"	course_id varchar(20),\n" + 
					"	role varchar(50));" ;
		stmt.executeUpdate(query);
		
		query =	"create table xdata_LTIResponseInfo (course_id varchar(50), \n" + 
					"	internal_user_id varchar(20),\n" + 
					"	rollnum varchar(50),\n" + 
					"	assignment_id numeric, \n" + 
					"	sourceid text,\n" + 
					"	primary key (assignment_id,course_id,internal_user_id) );" ;
		stmt.executeUpdate(query);
		
		query =	"create table xdata_sampledata(\n" + 
					"	sampledata_id integer, \n" + 
					"	schema_id integer, \n" + 
					"	course_id varchar(20), \n" + 
					"	sample_data_name text, \n" + 
					"	sample_data text, \n" + 
					"	primary key(sampledata_id));" ;
		stmt.executeUpdate(query);
					
		query = "create table xdata_lti_credentials(consumer_key varchar(100), \n" + 
					" secret_key varchar(100),\n" + 
					" requesting_url text, \n" +
					" lti_id integer, \n" + 
					" primary key(requesting_url));";
		stmt.executeUpdate(query);
		
		query = " create table xdata_student_log (\n"+
					"	course_id varchar(20),\n" +  
					"   assignment_id integer,\n" +
					"   question_id integer,\n" + 
					"   rollnum text NOT NULL,\n" + 
					"	eventtime timestamp without time zone,\n" +
					"   querytext text);" ;
		stmt.executeUpdate(query);
					
		PreparedStatement pstmt2 = dbCon.prepareStatement("insert into xdata_users values('XD1','admin','admin@infolab', '', ? ,'admin','CS631','admin')") ; 
		pstmt2.setString(1, Configuration.getProperty("adminPassword"));
		pstmt2.executeQuery();
		
		query =	"insert into xdata_roles values('XD1','admin','CS631','admin');" ;
		stmt.executeUpdate(query);
		
		query =	"insert into xdata_course values('1','CS631','DB course','2016','Spring','Default DB course');" ; 
		stmt.executeUpdate(query);
			
			} catch(Exception e){ System.out.println(e);}

	}
	// my code above:rambabu

}