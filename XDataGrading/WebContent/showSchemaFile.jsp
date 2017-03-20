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
 <link rel="stylesheet" href="css/structure.css" type="text/css"/> 
		<link rel="stylesheet" href="highlight/styles/xcode_white.css"/>
		<link rel="stylesheet" href="highlight/styles/default_white.css"/>
		<script src="highlight/highlight.pack.js"></script> 
		<script type="text/javascript">hljs.initHighlightingOnLoad();</script>
		<title>Schema File</title>
</head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Schema File</title>
<style>
#breadcrumbs
{
  position: absolute;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 10px;
  font: 13px/13px Arial, Helvetica, sans-serif;
  background-color: #f0f0f0;
  font-weight: bold;
}
</style>
</head>
<body> 
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
  <a style='color:#353275;text-decoration: none;font-weight: normal;' href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>"  target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
  <!-- //Find page path and update here-->
  <a style='color:#353275;text-decoration: none;font-weight: normal;' href="javascript:history.go(-1);"  target="_top">View Schema</a>&nbsp; >> &nbsp;
    
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Schema File</a>
 </div>
 
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
		out.print("<h2>Schema Name: "+result.getString("schema_name")+"</h2></div>");
		out.println("<div class=\"filedata\"><pre class=\"sql\"><code>");
		String filedata =result.getString("ddltext").replace("\n", "<br>\n"); 
		fileContent= filedata.replace("\\\\","'");  
		
		//fileContent =result.getString("ddltext").replace("\t", "        ");
		out.println(fileContent); 
		out.println("</code></pre></div>"); 

		  
	}  
%>			 
<input type="button" onclick="javascript:history.go(-1)" value="Back"> </div></fieldset></div>
</div> 
</body>
</html>