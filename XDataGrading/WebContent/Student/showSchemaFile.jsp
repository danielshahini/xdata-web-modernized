<%@ page language="java" contentType="text/html; charset=UTF-8"
  pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="../css/structure.css" type="text/css"/> 
 
		<link rel="stylesheet" href="../highlight/styles/xcode_white.css"/>
		<link rel="stylesheet" href="../highlight/styles/default_white.css"/>
		<script src="../highlight/highlight.pack.js"></script> 
		<script type="text/javascript">hljs.initHighlightingOnLoad();</script>
		<title>Schema File</title>
		<script src="../crumb.js"></script>
<script language="JavaScript">
breadcrumbs();
</script>
</head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Schema File</title>
</head>
<body> 
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
 <div>
 <div class="fieldset">
				<fieldset>						
 	<div class="info">

<%          
	String schemaID = (String)request.getParameter("schema_id");
	System.out.println("SCHEMA ID = = "+ schemaID);
	PrintWriter output = response.getWriter();
	int i=Integer.parseInt(schemaID);
	Connection conn= (new DatabaseConnection()).dbConnection();
	PreparedStatement stmt = conn.prepareStatement("select schema_name,ddltext from xdata_schemainfo where schema_id = ?");
	stmt.setInt(1, i);
	ResultSet result = stmt.executeQuery();
	String fileContent = "";
	//out.close();
	if(result.next()){
		out.print("<h2>Schema Name: "+result.getString("schema_name")+"</h2>");
	
		out.println("<h3><a  href=\"../DownloadFile?schemaId="+schemaID+"&&download=schemaFile\">Click here to download</a></h3></div>");
	
		
		out.println("<div class=\"filedata\"><pre class=\"sql\"><code>");
		String filedata =result.getString("ddltext").replace("\n", "<br>\n"); 
		out.println(filedata);  
		out.println("</code></pre></div>"); 
		//style=\"word-wrap:normal\"
				
		//byte[] dataBytes = result.getBytes("ddltext");
		//String fileContents = result.getString("ddltext"); 
		//ServletOutputStream output = response.getOutputStream();
		//output.println(dataBytes);
		//output.write(fileContents);
		// tempFile = "/tmp/dummy"; 
		//String c = new String(dataBytes,"UTF-8");
		//out.write(c.toCharArray()); 
		//out.close();
		  
	}  
	result.close();
	stmt.close();
	conn.close();
%>			 
<input type="button" onclick="javascript:history.go(-1)" value="Back"> </div></fieldset></div>
</div> 
</body>
</html>