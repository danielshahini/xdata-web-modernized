
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.digest.DigestUtils;

import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttribute;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPAttributeSet;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPConnection;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPEntry;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPException;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPReferralException;
import com.unboundid.ldap.sdk.migrate.ldapjdk.LDAPSearchResults;
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
	private Connection dbcon;

	public LoginChecker() {
		super();
		dbcon = null;
		// TODO Auto-generated constructor stub
	}

	public void init(ServletConfig c) throws ServletException {
		// Open the connection here

	}

	/*public String LdapAuthentication(String uname, String pwd)
			throws ServletException {
		String qry = "", mesg = " ", dn = " ";
		boolean checkFlag = false;
		String empcode = null;
		try {
			LDAPConnection ldapHandle = null;
			LDAPEntry findEntry = null;
			ldapHandle = new LDAPConnection();
			String My_Host = "ldap.iitb.ac.in";
			int My_Port = 389;
			String ENTRYDN = "dc=iitb,dc=ac,dc=in";
			ldapHandle.connect(My_Host, My_Port, "", "");
			LDAPSearchResults ldapResult = ldapHandle.search(ENTRYDN,
					LDAPConnection.SCOPE_SUB, "(uid=" + uname + ")", null,
					false);
			while (ldapResult.hasMoreElements()) {
				findEntry = null;
				findEntry = ldapResult.next();
				LDAPAttributeSet entries = findEntry.getAttributeSet();
				LDAPAttribute mms = entries.getAttribute("mailMessageStore");

				String[] myValue = mms.getStringValueArray();
				int myValueArraySize = myValue.length;
				StringTokenizer strtok = new StringTokenizer(myValue[0], "/");
				if (strtok.hasMoreTokens()) {
					String position = strtok.nextToken();
					String dept = strtok.nextToken();
					empcode = strtok.nextToken();
					position = position.substring(0, position.indexOf("."));
					position = position.toLowerCase();
				}
				dn = findEntry.getDN();
				ldapHandle.disconnect();
				ldapHandle.connect(My_Host, My_Port, dn, pwd);

			}
			if ((ldapHandle != null) && ldapHandle.isConnected()
					&& empcode != null && !empcode.equals(" ")) {
				mesg = "OK";
			} else {
				mesg = "NotOK";
			}
		} catch (LDAPReferralException e) {
			logger.log(Level.SEVERE,"Error :" + e.getMessage(),e);
			throw new ServletException(e);
		} catch (LDAPException e) {
			logger.log(Level.SEVERE,"Error :" + e.getMessage(),e);
			throw new ServletException(e);
		} catch (Exception gexp) {
			logger.log(Level.SEVERE,gexp.getMessage(),gexp);
			throw new ServletException(gexp);
		}
		return mesg;
	}*/

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub

	}

	public void destroy() {
		// Close the connection here
		try {
			dbcon.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		String uname = ""; 
		String pwd = "";
		Configuration config = new Configuration();
		try {
			dbcon = (new DatabaseConnection()).dbConnection();
			
		} catch (Exception ex) {
			logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
			throw new ServletException(ex);
		}
		
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
		Connection dbCon = null;
		String role = "";
		try { 
			
					dbCon = new DatabaseConnection().dbConnection();
					PreparedStatement pstmt = dbCon
							.prepareStatement("select * from xdata_users where login_user_id =?  and password=?");
					try{
						pstmt.setString(1, uname); 
						//pstmt.setString(2,pwd); 
						if(uname.equalsIgnoreCase("admin")){
							pstmt.setString(2, pwd);
						}else{
							pstmt.setString(2, DigestUtils.md5Hex(pwd));
						}
						//logger.log(Level.FINE,"PWD to test = DigestUtils.md5Hex(pwd) =="+DigestUtils.md5Hex(pwd));
						ResultSet rs = null;
							try{
									rs =pstmt.executeQuery(); 
									logger.log(Level.FINE,pstmt.toString());
									if(rs.next()){ 
									  
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
													session.setAttribute("LOGIN_USER", "Tester");
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
												
												try {
													dbcon.close();
												} catch (SQLException e) {
													logger.log(Level.SEVERE,e.getMessage(),e);
													throw new ServletException(e); 
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
							}else if(uname.equalsIgnoreCase("admin")){		
								//First login, so insert login credentials in DB
								PreparedStatement pstmt1 = dbCon
										.prepareStatement("insert into xdata_users (internal_user_id,user_name,login_user_id,password) values(?,?,?,?)");
								pstmt1.setString(1,"XD1");
								pstmt1.setString(2,"Administrator");
								pstmt1.setString(3, uname);
								pstmt1.setString(4, config.getProperty("adminPassword"));
								
								pstmt1.executeQuery(); 
								session.setAttribute("LOGIN_USER", "ADMIN");
								session.setAttribute("role",role);
								response.sendRedirect("adminHome.jsp");
								
							}else{
								session.invalidate();
								response.setContentType("text/html");
								response.sendRedirect("index.jsp?Login=false");
								 
							try {
								dbcon.close();
							} catch (SQLException e) {
								logger.log(Level.SEVERE,e.getMessage(),e);
								throw new ServletException(e);
							}
							
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
			//If it is consecutive admin login's, even then dont has the password
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
			
			/*
			 * Commented by shree strt
			 * if((uname.equalsIgnoreCase("instructor")&&
			 * pwd.equalsIgnoreCase("xdata!@#")) ||
			 * (uname.equalsIgnoreCase("student")
			 * &&pwd.equalsIgnoreCase("icde")))
			 * 
			 * { response.setContentType("text/html"); PrintWriter out2 =
			 * response.getWriter();
			 * 
			 * if(uname.equalsIgnoreCase("instructor")){
			 * session.setAttribute("LOGIN_USER", "ADMIN"); } else{
			 * session.setAttribute("LOGIN_USER", "student"); }
			 * 
			 * response.sendRedirect("Empty.html");
			 * 
			 * return; commented by Shree end
			 */
			/*
			 * BufferedReader reader = new BufferedReader(new
			 * FileReader(request.
			 * getSession().getServletContext().getRealPath("/"
			 * )+"/instructorOptions.html")); String line = null; while ((line =
			 * reader.readLine()) != null) { out2.println(line); } out2.close();
			 * return;
			 */
			/*
			 * commented by shree} else{ session.invalidate();
			 * response.setContentType("text/html"); Commented by shree ends
			 */
			/*
			 * PrintWriter out2 = response.getWriter(); out2.println("<html>"+
			 * "<header><title>Error</title></header>"+ "<body>"+
			 * "Invalid username/password"+ "</body>"+ "</html>"); out2.close();
			 */

			/*
			 * commented by shree strt
			 * response.sendRedirect("index.jsp?Login=false"); return;
			 * }commented by shree end
			 */

		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}finally{
			try {
				if(dbcon != null){
					dbcon.close();
				}
			} catch (SQLException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				throw new ServletException(e);
			}
		}
		/*
		 * if(!LdapAuthentication(uname,pwd).equalsIgnoreCase("ok")) {
		 * session.invalidate(); response.setContentType("text/html");
		 * PrintWriter out2 = response.getWriter(); out2.println("<html>"+
		 * "<header><title>Error</title></header>"+ "<body>"+
		 * "Invalid username/password"+ "</body>"+ "</html>"); out2.close();
		 * return; }
		 */

		/*
		 * response.setContentType("text/html"); PrintWriter out_assignment =
		 * response.getWriter(); out_assignment.println(
		 * "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
		 * "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+
		 * 
		 * "<html xmlns=\"http://www.w3.org/1999/xhtml\">"+ "<head>"+
		 * 
		 * "<title>"+ "XData &middot; Assignment"+ "</title>"+
		 * "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
		 * +
		 * 
		 * 
		 * "<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"+
		 * 
		 * "<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
		 * +
		 * "<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"
		 * +
		 * "<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"
		 * +
		 * 
		 * "<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"
		 * +
		 * 
		 * "</head>"+
		 * 
		 * "<body id=\"public\">"+
		 * 
		 * "<div id=\"container\">"+
		 * 
		 * 
		 * "<form class=\"wufoo\" action=\"ViewAssignment\" method=\"get\">"+
		 * 
		 * "<div class=\"info\">"+ "<h2>Assignments</h2>"+ "</div>");
		 * 
		 * String assignments="select * from assignment"; try {
		 * PreparedStatement pstmt=dbcon.prepareStatement(assignments);
		 * ResultSet rst=pstmt.executeQuery();
		 * out_assignment.println("<table border=\"1\">");
		 * out_assignment.println("<tr>"+ "<td>Assignment Number</td>"+
		 * "<td> Start time</td>"+ "<td> End time</td>"+ "<td> </td>"+
		 * "<td> </td>"+ "</tr>"); while(rst.next()) { String edit="";
		 * java.util.Date date= new java.util.Date(); Timestamp ts=new
		 * Timestamp(date.getTime()); if(ts.after(rst.getTimestamp("end_time")))
		 * { edit="";
		 * 
		 * } else {
		 * edit="<input type=\"submit\" name="+rst.getInt("assignment_id"
		 * )+"E value=\"Solve\" /> "; } out_assignment.println("<tr>"+
		 * "<td>Assignment "+rst.getInt("assignment_id") +"</td>"+
		 * "<td>"+rst.getTimestamp("start_time")+"</td>"+
		 * "<td>"+rst.getTimestamp("end_time")+"</td>"+ "<td> "+edit+"</td>"+
		 * "<td> <input type=\"submit\" name="
		 * +rst.getInt("assignment_id")+"V Value=\"Result\" /> </td>"+ "</tr>");
		 * } rst.close(); out_assignment.println("</table>"); } catch
		 * (SQLException e3) { // TODO Auto-generated catch block
		 * e3.printStackTrace(); } if(session.getAttribute("login")==null) {
		 * session.setAttribute("login", uname);
		 * System.out.println("login added"); }
		 * 
		 * out_assignment.println(
		 * "<p><p><p><p><p><p><a href=\"index.jsp\">logout</a>");
		 * out_assignment.println("</form>"+
		 * 
		 * "</div>"+
		 * 
		 * 
		 * "<!-- End Page Content -->"+
		 * 
		 * "</body>"+
		 * 
		 * "</html>"); out_assignment.close(); try{ dbcon.close(); }
		 * catch(SQLException e){} catch(NullPointerException e){}
		 */

	}

}