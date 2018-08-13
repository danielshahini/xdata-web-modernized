<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="javax.servlet.http.HttpServletRequest"%>
<%@ page import="java.util.Enumeration"%>
<%@ page import="net.oauth.OAuth"%> 
<%@ page import="net.oauth.OAuthMessage"%>
<%@ page import="net.oauth.OAuthConsumer"%>
<%@ page import="net.oauth.OAuthAccessor"%>
<%@ page import="net.oauth.OAuthValidator"%>
<%@ page import="net.oauth.SimpleOAuthValidator"%>
<%@ page import="net.oauth.signature.OAuthSignatureMethod"%>
<%@ page import="net.oauth.server.HttpRequestMessage"%>
<%@ page import="net.oauth.server.OAuthServlet"%> 
<%@ page import="net.oauth.signature.OAuthSignatureMethod"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@page import="database.LtiProperties"%>
 
<%
 	Enumeration<String> en = request.getParameterNames();
 	while (en.hasMoreElements()) {
 		String paramName = (String) en.nextElement();
 		out.println(paramName + " = " + request.getParameter(paramName));
 	} 
 	
 	// Consumer key will be set if the app is running within LMS system (Moodle, Canvas, Blackboard)
 	//System.out.println("Request param value for oauth_consumer_key = "+request.getParameter("oauth_consumer_key"));
 	Boolean ltiMode = request.getParameter("oauth_consumer_key") != null;
 	//System.out.println("ltimode = " + ltiMode);
 	//System.out.println("pageContext.getServletContext().getContextPath() = " + pageContext.getServletContext().getContextPath());
 	
 	//System.out.println("server nam.e : " + pageContext.getRequest().getServerName());
 	//System.out.println("local name : " + pageContext.getRequest().getLocalName());
 	
 	String userId = "", name = "", email = "", role = "";
	int assignment_id = 0;
	String course_id = "";
 	OAuthValidator oav = new SimpleOAuthValidator();
 	//Get LTI connection details (like consumer key, secret key and callbackURL) from properties for validating the request
 	LtiProperties ltiProp = new LtiProperties();
 	OAuthMessage oam = null;
 	OAuthConsumer cons = null;
 	OAuthAccessor acc = null;
 	String outcomeURL = request.getParameter("lis_outcome_service_url");
 	
 	if (ltiMode) {
 		oam = OAuthServlet.getMessage(request, null);

 		String oauth_consumer_key = request.getParameter("oauth_consumer_key");
 		if (oauth_consumer_key == null) {
		 	out.println("<b>Missing oauth_consumer_key</b>\n");
		 	return;
 		}
		
 		cons = null;
 		Connection dbcon = (new DatabaseConnection()).dbConnection();

 		PreparedStatement stmt = dbcon
 									.prepareStatement("SELECT * FROM xdata_lti_credentials where requesting_url=? and consumer_key=?");	
 		stmt.setString(1, outcomeURL);
 		stmt.setString(2, oauth_consumer_key);
 		ResultSet rs = stmt.executeQuery(); 
 		if(rs.next()){ 			
	 		if (rs.getString("consumer_key").equals(oauth_consumer_key)) { 			
	 			cons = new OAuthConsumer(ltiProp.getCallBackURL(), rs.getString("consumer_key"),rs.getString("secret_key"), null);
	 		} else {
	 			out.println("<b>oauth_consumer_key=" + oauth_consumer_key + " not found.</b>\n");
	 			
	 			return;
	 		}
 		}else {
 			out.println("<b>oauth_consumer_key=" + oauth_consumer_key + " not found.</b>\n");			
 			return;
 		}
 		userId = cons.consumerKey+request.getParameter("user_id");
 		//userId = cons.consumerKey+"_"+request.getParameter("user_id");
 		//userId = request.getParameter("user_id");
 		name = request.getParameter("lis_person_name_full");
 		email = request.getParameter("lis_person_contact_email_primary");
 		//course_id = request.getParameter("context_label");
 		course_id = oauth_consumer_key+"_"+request.getParameter("context_id");
 		acc = new OAuthAccessor(cons);
 		/* System.out.println("Accessot Obj =access token =  " + acc.accessToken);
 		System.out.println("Accessot Obj = requestToken =  " + acc.requestToken);
 		System.out.println("Accessot Obj = tokenSecret = " + acc.tokenSecret);
 		System.out.println("Accessot Obj = Consumer callback url = " + acc.consumer.callbackURL);
 		System.out.println("Accessot Obj = Consumer key = " +acc.consumer.consumerKey);
 		System.out.println("Accessot Obj = Consumer secret key = " +acc.consumer.consumerSecret);
 		System.out.println("Accessot Obj = Consumer service provider = " +acc.consumer.serviceProvider);
 		System.out.println("Req :" + request.getParameter("lis_person_name_full"));		 */
 		
 		System.out.println("Assignment Id : " + request.getParameter("assignmentId"));
 		if(request.getParameter("assignmentId") != null){
 			session.setAttribute("allowedAssignment", Integer.parseInt(request.getParameter("assignmentId")));
 			assignment_id = Integer.parseInt(request.getParameter("assignmentId"));
 		}		 
 		
 		session.setAttribute("ltiIntegration", true);
 		session.setAttribute("oauth_consumer_key", cons.consumerKey);
 		session.setAttribute("oauth_consumer_secret", cons.consumerSecret);
 		session.setAttribute("user_id", userId);
 		session.setAttribute("course_id", request.getParameter("context_label"));
 		session.setAttribute("context_label",oauth_consumer_key+"_"+request.getParameter("context_id"));
 		session.setAttribute("resource_link_id", request.getParameter("resource_link_id"));
 		session.setAttribute("lis_result_sourcedid", request.getParameter("lis_result_sourcedid"));
 		session.setAttribute("lis_outcome_service_url", request.getParameter("lis_outcome_service_url"));
 		session.setAttribute("lis_person_name_full", name);
 		session.setAttribute("lis_person_contact_email_primary", email);
 		session.setAttribute("roles", request.getParameter("roles"));
 		String roleDetail = (String)request.getSession().getAttribute("roles");
 		
 		roleDetail=roleDetail.toLowerCase();
 		
 		if(roleDetail.contains("instructor")){
 			session.setAttribute("LOGIN_USER", "ADMIN");
 			session.setAttribute("role","instructor");
 		} else{
 			session.setAttribute("LOGIN_USER", "student");
 			session.setAttribute("role","student");
 		}
 	} 

 	try{
 		DatabaseProperties properties = new DatabaseProperties();
 		Connection dbcon = (new DatabaseConnection()).dbConnection();
 		PreparedStatement stmt; 
 		ResultSet rs = null;
 		//To insert new users from learning tool login
 		stmt = dbcon
 		.prepareStatement("SELECT * FROM xdata_users where internal_user_id=?");
 		stmt.setString(1, (String)session.getAttribute("user_id"));
 		rs = stmt.executeQuery(); 
 		System.out.println("session.getAttribute ltiIntegration = " + session.getAttribute("ltiIntegration"));
	if (!rs.next() && session.getAttribute("ltiIntegration") != null 
			&& Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())) {			
	 	stmt = dbcon.prepareStatement("INSERT INTO xdata_users VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
	 	stmt.setString(1, userId);
	 	stmt.setString(2, name);
	 	stmt.setString(3, email);
	 	
	 	String lis_result_sourcedid = "";
	 	
	 	if(session.getAttribute("lis_result_sourcedid") != null){
	 		lis_result_sourcedid = session.getAttribute("lis_result_sourcedid").toString();
	 	}
	 	 
	 	stmt.setString(4, lis_result_sourcedid);
	 	//For moodle users, the user name itself is set as password
	 	//TODO : check if it needs to be hashed 
	 	stmt.setString(5, name);
	 	stmt.setString(6, session.getAttribute("LOGIN_USER").toString());
	 	stmt.setString(7, session.getAttribute("context_label").toString());
	 	//If user comes thru External tool, create user with name as login id and name as password
	 	stmt.setString(8,name);
	 	stmt.executeUpdate();
	 	
	 	stmt = dbcon.prepareStatement("INSERT INTO xdata_LTIResponseInfo VALUES(?, ?, ?, ?, ?)");
	 	stmt.setString(1, course_id);
	 	stmt.setString(2, userId);
	 	stmt.setString(3,userId);
	 	stmt.setInt(4,assignment_id);
	 	stmt.setString(5, lis_result_sourcedid);
	 	stmt.executeUpdate();
	 	
	 	stmt = dbcon.prepareStatement("INSERT INTO xdata_roles VALUES(?, ?, ?, ?)");
	 	stmt.setString(1, userId);
		stmt.setString(2,name);
		stmt.setString(3, course_id);
		stmt.setString(4, session.getAttribute("LOGIN_USER").toString());
		stmt.executeUpdate();

 		} else {//If user already exists
 			//Even if it is instructor login, update sourceid for reporting grades back
 	//if(session.getAttribute("LOGIN_USER") != null && session.getAttribute("LOGIN_USER").equals("student")){		
 		if(session.getAttribute("ltiIntegration")!= null && (Boolean)session.getAttribute("ltiIntegration")){
 			
 			stmt = dbcon.prepareStatement("select * from xdata_LTIResponseInfo where internal_user_id = ? and course_id = ? and assignment_id = ?");
 			stmt.setString(1, (String)session.getAttribute("user_id"));
 			stmt.setString(2,course_id);
 			stmt.setInt(3,assignment_id);
 			 //Check whether for given assignment and course, student had logged in previously
 			ResultSet rs1 = stmt.executeQuery(); 			
 			if(rs1.next()){
 				//If student entry for the given course and assignement exists, then update source id for marks update
			 			stmt = dbcon.prepareStatement("update xdata_LTIResponseInfo set sourceid = ? where internal_user_id = ? and course_id = ? and assignment_id = ?");
			 			String lis_result_sourcedid = "";
			 			if(session.getAttribute("lis_result_sourcedid") != null){
			 				lis_result_sourcedid = session.getAttribute("lis_result_sourcedid").toString();
			 			} 			
			 			stmt.setString(1, lis_result_sourcedid);
			 			stmt.setString(2, (String)session.getAttribute("user_id"));
						stmt.setString(3,course_id);
			 			stmt.setInt(4,assignment_id);
			 			System.out.println("statement : "+ stmt.toString());
			 			stmt.executeUpdate();
			 			System.out.println("statement : "+ stmt.toString());
			 			stmt = dbcon.prepareStatement("update xdata_users set sourceid = ? where internal_user_id = ?");			 						
			 			stmt.setString(1, lis_result_sourcedid);
			 			stmt.setString(2, (String)session.getAttribute("user_id"));
			 			stmt.executeUpdate();
 				}
 			else{
 				//If no result set exists for given assignment_id, then this user is logging in for the first
 				//time for this assignment. So insert into xdata_LTIResponseInfo table.
 				stmt = dbcon.prepareStatement("insert into xdata_LTIResponseInfo VALUES(?, ?, ?, ?, ?)");
 				String lis_result_sourcedid = "";
	 			if(session.getAttribute("lis_result_sourcedid") != null){
	 				lis_result_sourcedid = session.getAttribute("lis_result_sourcedid").toString();
	 			} 
 				stmt.setString(1, course_id);
 			 	stmt.setString(2, userId);
 			 	stmt.setString(3,userId);
 			 	stmt.setInt(4,assignment_id);
 			 	stmt.setString(5, lis_result_sourcedid);
 				stmt.executeUpdate();
 			}
 		}
 	//}
 		}
 		
 		stmt.close();
 		rs.close();
 		dbcon.close();
 		System.out.println("Closing all connections");
 	} catch (Exception e) {
 		out.println("<b>Error inserting/retrieving user information</b>\n");
 		out.println(e);
 		throw new ServletException(e);
 	}

 	if (ltiMode) {
 		//System.out.println("ltimode @ end : "+ ltiMode);

 		try {
 			
 	//System.out.println("OAM String : Signature =  " + oam.getSignature() + " --------  " +oam.URL);
 	//System.out.println("OAM String : consumer key and tokem =  " + oam.getConsumerKey() + " --------  " +oam.getToken());
 	
 	out.println("\n<b>Base Message</b>\n</pre><p>\n");
 	oam.URL =ltiProp.getMoodleXdataUrl();
 	//System.out.println("url from properties file : " + oam.URL);
 	out.println("<pre>\n");
 	oav.validateMessage(oam, acc);
 	//System.out.println("Mesage Validated ---");
 	out.println("Message validated" );
 	 
 		} catch (Exception e) {

 //	System.out.println("OAM String : Error ");
 		 		
 	out.println("<b>Error while valdating message:</b>\n");
 	System.out.println(e);
 	request.setAttribute("ErrorLocation", e.getClass());
 	request.setAttribute("Error", e.getMessage());
 	e.printStackTrace();
 	RequestDispatcher rd = request.getRequestDispatcher("errorPage.jsp");
 	rd.forward(request, response); 
 		}
 	}

 	//System.out.println("updated2");
 %>
<jsp:forward page="/InitAssignment" />
<html>
<head> 
<link rel="stylesheet" href="css/structure.css" type="text/css"/>
<title>IMS Basic Learning Tools Interoperability</title>
</head>
<body style="font-family: sans-serif">
	<img src="http://www.sun.com/images/l2/l2_duke_java.gif" align="right">
	<p>
		<b>IMS BasicLTI Java Provider</b>
	</p>
	<p>This is a very simple reference implementaton of the tool side
		(i.e. provider) for IMS BasicLTI.</p>
	<p>This tool is configured with an LMS-wide guid of
		"lmsng.school.edu" protected by a secret of "secret". For this tool,
		all resource level secrets are also "secret".</p>
</body>
</html>
