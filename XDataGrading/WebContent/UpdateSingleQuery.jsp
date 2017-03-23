<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Update a single query</title>

<style>

</style>

</head>
<body>
<%
		if (session.getAttribute("LOGIN_USER") == null) {
			response.sendRedirect("index.jsp");
			return;
		}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
		 		&& session.getAttribute("role") != null && (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
			response.sendRedirect("index.jsp?NotAuthorised=true");
			session.invalidate();
			return;
		}

	//--_________----------------------------------
	String btn = request.getParameter("btn");
	System.out.println(btn);
 	String qid = btn.split(" ")[1]; 
	 String assignID = btn.split(" ")[2];
	// String quer = "query ".concat(qid).concat(" ").concat('1');
	//alert(document.getElementsByName("query").length);
	// alert("between");
	 String [] queries = request.getParameterValues("query");
	
	String desc = request.getParameter("quesTxt");//.concat(qid)).value; 
	String optionalSchemaId = request.getParameter("optionalschemaid"); 

	 
	//---------------------------------------------
		
			int queryID = Integer.parseInt((String) request.getParameter("question_id"));
			int asID = Integer.parseInt(request.getParameter("assignment_id"));
			int maxMarks = Integer.parseInt(request.getParameter("maxMarks"));
			//String optionalSchemaId = (String)request.getParameter("optschemaid");
			String [] queryIds = request.getParameterValues("queries");
			for(String s:queryIds){ 
			
			 System.out.println("QUERY ID ========  " + s.toString()); 
			}
			   
			System.out.println("optional schema id"+ optionalSchemaId);  
 
			int optId = Integer.parseInt(request.getParameter("optschemaid")); 
			String courseID = (String) request.getSession().getAttribute(
			"context_label");
  
			String correctquery = (String) request.getParameter("quer").trim().replaceAll("\r\n+", " ").trim().replaceAll("\n+", " ").trim().replaceAll(" +", " ");
			String queryDesc = (String) request.getParameter("desc").trim().replaceAll("\r\n+", " ").trim().replaceAll("\n+", " ").trim().replaceAll(" +", " ");

			System.out.println("Desc: "+queryDesc);
			System.out.println("Correct: "+correctquery);
			System.out.println("optional schema id"+ optId);  

			Connection dbcon = null;

			dbcon = (new DatabaseConnection()).dbConnection();

			try {

		PreparedStatement stmt;
		stmt = dbcon
				.prepareStatement("SELECT * from xdata_qinfo where course_id=? and assignment_id=? and question_id=?");
		stmt.setString(1, courseID);
		stmt.setInt(2, asID);
		stmt.setInt(3, queryID);
		ResultSet rs = stmt.executeQuery();

		if (rs.next()) {
			/**query arleady present*/

			stmt = dbcon
					.prepareStatement("UPDATE xdata_qinfo SET querytext=?, correctquery=?,optionalschemaid=?, totalmarks = ? WHERE course_id=? and assignment_id=? and question_id=?");

			stmt.setString(5, courseID);
			stmt.setInt(6, asID);
			stmt.setInt(7, queryID);

			stmt.setString(1, queryDesc);
			stmt.setString(2, correctquery);
			stmt.setInt(3,optId);
			stmt.setInt(4, maxMarks);

		} else {
			stmt = dbcon
					.prepareStatement("INSERT INTO xdata_qinfo VALUES (?,?,?,?,?,?,?,?,?,?)");
			stmt.setString(1, courseID);
			stmt.setInt(2, asID);
			stmt.setInt(3, queryID);
			
			stmt.setString(4, queryDesc);
			stmt.setString(5, correctquery);
			stmt.setInt(6, maxMarks);
			stmt.setBoolean(7, false);
			stmt.setBoolean(8, false);
			stmt.setBoolean(9, false);
			stmt.setInt(10, optId);
			 

		}
		
		rs.close();
		stmt.executeUpdate();
		String remoteLink = "ListOfQuestions.jsp?AssignmentID=" + asID;
		response.sendRedirect(remoteLink);

			} catch (SQLException sep) {	
					sep.printStackTrace();
					throw new ServletException(sep); 
		
			}
			finally{
		dbcon.close();
			}
	%>
</body>
</html>