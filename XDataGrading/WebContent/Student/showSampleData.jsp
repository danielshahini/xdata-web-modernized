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

<title>Sample Data File</title>
</head> 
<body>  
<%

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){%>
<div id="breadcrumbs"> 
  <a id="bcrumb_link" href="../CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a  id="bcrumb_link" href="ListAllAssignments.jsp" target="_self">Assignment List</a>&nbsp; >> &nbsp;
   	<a  id="bcrumb_link" href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>
   <a id="bcrumb_no_link" href="#">Answer</a>
  </div>
<%}else{ %>
   <a  id="bcrumb_link" href="ListOfQuestions.jsp?assignmentid=<%=request.getParameter("AssignmentID") %>" target="_self">Question List</a>
   <a id="bcrumb_no_link" href="#">Answer</a>
<%} %> 

 <div> 
 <div class="fieldset">
				<fieldset>
 		<br /><div>			 
<%          
	String schemaID = (String)request.getParameter("schema_id");
String courseId = (String) request.getSession().getAttribute("context_label");
String sampleDataID = (String)request.getParameter("sampledata_id");
//	System.out.println("SCHEMA ID = = "+ schemaID); 
	PrintWriter output = response.getWriter();
	int i=Integer.parseInt(schemaID);
	Connection conn= (new DatabaseConnection()).dbConnection();
	try{
	PreparedStatement stmt = conn.prepareStatement("select sample_data_name, sample_data from xdata_sampledata where course_id=? and schema_id = ?");
	stmt.setString(1, courseId);
	stmt.setInt(2, i); 
	ResultSet result = stmt.executeQuery();
	String fileContent = "";
	String fc="";
	//out.close(); 
	if(result.next()){ 
		out.print("<h2>Data File Name: "+result.getString("sample_data_name")+"</h2>");
		out.println("<h3><a  href=\"../DownloadFile?schemaId="+schemaID+"&&sampleDataID="+sampleDataID+"&&download=dataFile\">Click here to download</a></h3></div>");
		out.println("<div  class=\"filedata\"><pre class=\"sql\"><code>");
		fileContent= result.getString("sample_data").replace("\n", "<br>\n");
		out.println(fileContent);
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
	
	}
	catch (Exception err) {

		err.printStackTrace();
		throw new ServletException(err);
	}
finally{
if(conn != null)
	conn.close();
}
	
%>
<input type="button" onclick="javascript:history.go(-1)" value="Back"> </div></fieldset></div>
</div> 

</body>
</html>