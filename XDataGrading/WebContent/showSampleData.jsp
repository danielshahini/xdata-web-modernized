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
<title>Schema File</title>
</head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Sample Data File</title>
</head> 
<body>  

<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;font-weight: normal;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
    <!-- //Find page path and update here-->
  <a style='color:#353275;text-decoration: none;font-weight: normal;' href="javascript:history.go(-1);"  target="_top">View Data</a>&nbsp; >> &nbsp;
   
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Data File</a>
 </div>
 <div> 
 <div class="fieldset">
				<fieldset>
 		<br /><div>			 
<%          
	String schemaID = (String)request.getParameter("schema_id");
	String courseId = (String) request.getSession().getAttribute("context_label");
	String sampledata_id = request.getParameter("sampledata_id");
	
	//	System.out.println("SCHEMA ID = = "+ schemaID); 
	PrintWriter output = response.getWriter();
	int i=Integer.parseInt(schemaID);
	Connection conn= (new DatabaseConnection()).dbConnection();
	PreparedStatement stmt = conn.prepareStatement("select sample_data_name, sample_data from xdata_sampledata where course_id=? and schema_id = ? and sampledata_id=?");
	stmt.setString(1, courseId);
	stmt.setInt(2, i); 
	stmt.setInt(3,Integer.parseInt(sampledata_id));
	ResultSet result = stmt.executeQuery();
	String fileContent = "";
	String fc="";
	//out.close(); 
	if(result.next()){
		out.print("<h2>Data File Name: "+result.getString("sample_data_name")+"</h2></div>");
		out.println("<div><pre class=\"sql\"><code>");
		fileContent= result.getString("sample_data").replace("\n", "<br>\n");
		fc=fileContent.replace("\\\\","'");
		//fileContent =result.getString("ddltext").replace("\t", "        ");
		out.println(fc);
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
%>
<input type="button" onclick="javascript:history.go(-1)" value="Back"> </div></fieldset></div>
</div> 

</body>
</html>