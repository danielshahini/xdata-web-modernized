
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

}