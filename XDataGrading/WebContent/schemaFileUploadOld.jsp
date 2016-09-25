<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.FileToSql"%>
<%@page import="database.FileHandler"%>
<%@page import="database.DatabaseProperties"%>
<%@page import = "org.apache.commons.fileupload.*" %>
<%@page import = "org.apache.commons.fileupload.util.*" %>
<%@page import = "org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import = "org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
<%@ page errorPage="errorPage.jsp" %> 
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

<title>Update Query</title>
<script type="text/javascript" src="scripts/wufoo.js"></script>

<!-- CSS -->
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="stylesheet" href="css/form.css" type="text/css" />
<link rel="stylesheet" href="css/theme.css" type="text/css" />

<link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">
<style>


textarea,select {
	font: 12px/12px Arial, Helvetica, sans-serif;
	padding: 0;
}

input {
	font: 15px/15px Arial, Helvetica, sans-serif;
	padding: 0;
}

fieldset.action {
	background: #9da2a6;
	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;
	margin-top: -20px;
}


label {
	font-size: 15px;
	font-weight: bold;
	color: #666;
}

label.opt {
	font-weight: normal;
}

dl {
	clear: both;
}

dt {
	float: left;
	text-align: right;
	width: 90px;
	line-height: 25px;
	margin: 0 10px 10px 0;
}

dd {
	float: left;
	width: 475px;
	line-height: 25px;
	margin: 0 0 10px 0;
}

#footer {
	font-size: 11px;
}

#container {
	width: 100%;
	margin: 0 auto;
}

label span,.required {
	color: red;
	font-weight: bold;
	text-align: left;
	font-size: 17px;
}

.stop-scrolling {
	height: 100%;
	/*overflow: hidden;*/
}
</style>
</head>
<body>
	<%
	
		if (session.getAttribute("LOGIN_USER") == null || !session.getAttribute("LOGIN_USER").equals("ADMIN")) {
			response.sendRedirect("index.jsp");
			return;
		}
	
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		
		if(isMultipart){
			int x = 0;
			String courseId = (String) request.getSession().getAttribute("context_label");
			String fileName = "";
			byte[] dataBytes = null;
			try {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload( factory );
				List<FileItem> uploadItems = upload.parseRequest( request );
		
				for( FileItem uploadItem : uploadItems )
				{
				  if(uploadItem.isFormField()) {
				    String fieldName = uploadItem.getFieldName();
				    String value = uploadItem.getString();
				    
				    if(fieldName.equals("schemaname")){
				    	fileName = value;
				    }
				  }
				  else {
					  dataBytes = uploadItem.get();
					  if(fileName.isEmpty()){
						  fileName = uploadItem.getName();
					  }				  
				  }
				}
				
				Connection dbcon = (new DatabaseConnection()).dbConnection();

				PreparedStatement stmt;
				
				int newSchemaId = 1;
				
				//Get schema id
				stmt =  dbcon.prepareStatement("SELECT MAX(schema_id) as schemaId from xdata_schemainfo");
				ResultSet rs = stmt.executeQuery();
				
				if(rs.next()){
					newSchemaId = rs.getInt("schemaId") + 1;
				}
				
				stmt = dbcon.prepareStatement("Insert into xdata_schemainfo values(?, ?, ?, ?)");
				stmt.setString(1, courseId);
				stmt.setInt(2, newSchemaId);
				stmt.setString(3, fileName);
				stmt.setBytes(4, dataBytes);
				
				stmt.executeUpdate();
				dbcon.close();
			
		}
		catch(Exception err){
				err.printStackTrace();
				x = 1; 
				 
			}
			 
			if (x == 0) {
				out.println("<p > You have successfully uploaded the file </p> ");
			} 
		} else {
			out.println("<p > Error in file uploading. please verify it! </p> ");
	}
	%>
</body>
</html>