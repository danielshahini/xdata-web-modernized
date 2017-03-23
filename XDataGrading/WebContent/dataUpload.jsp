<%@ page import="java.io.*" errorPage="errorPage.jsp"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.FileToSql"%>
<%@page import="database.FileHandler"%> 
<%@page import="database.DatabaseProperties" %>
<%@page import = "org.apache.commons.fileupload.*" %>
<%@page import = "org.apache.commons.fileupload.util.*" %>
<%@page import = "org.apache.commons.fileupload.servlet.ServletFileUpload" %>
<%@page import = "org.apache.commons.fileupload.disk.DiskFileItemFactory" %>
 <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<script type="text/javascript" src="scripts/wufoo.js"></script>

<!-- CSS -->
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="stylesheet" href="css/form.css" type="text/css" />
<link rel="stylesheet" href="css/theme.css" type="text/css" />

<link rel="canonical" href="http://www.wufoo.com/gallery/designs/template.html">

<%
	if (session.getAttribute("LOGIN_USER") == null || !session.getAttribute("LOGIN_USER").equals("ADMIN")) {
		response.sendRedirect("index.jsp");
		return;
	}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
	 		&& session.getAttribute("role") != null &&  (!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
		response.sendRedirect("index.jsp?NotAuthorised=true");
		session.invalidate();
		return;
	}


	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	
	if(isMultipart){
		int x = 0;
		String courseId = (String) request.getSession().getAttribute("context_label");
		int schemaId = 0;
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
			    
			    if(fieldName.equals("schemaid")){
			    	schemaId = Integer.parseInt(value);
			    }
			  }
			  else {
				  dataBytes = uploadItem.get();				  
			  }
			}
			
			Connection dbcon = (new DatabaseConnection()).dbConnection();
	
			PreparedStatement stmt;
			
			//Get schema id
			/*
			stmt =  dbcon.prepareStatement("insert into xdata_sampledata(sample_data) values (?) where course_id = ? and schema_id = ?");
			
			stmt.setString(2, courseId);
			stmt.setInt(3, schemaId);
			stmt.setBytes(1, dataBytes);
			
			stmt.executeUpdate();*/
			dbcon.close();		
	}
		catch(Exception err){
			//err.printStackTrace();
			x = 1;
		}		
		if (x == 0) {
			out.println("<p > You have successfully uploaded the file </p> ");
		}
	} else {
		out.println("<p > Error in file uploading. please verify it! </p> ");
		}
	
			/*  //for saving the file name
            String file = new String(dataBytes);
            saveFile = file.substring(file.indexOf("filename=\"") + 10);
            saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
            saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1, saveFile.indexOf("\""));
            int lastIndex = contentType.lastIndexOf("=");
            String boundary = contentType.substring(lastIndex + 1, contentType.length());
            int pos;
          //extracting the index of file 
            pos = file.indexOf("filename=\"");
            pos = file.indexOf("\n", pos) + 1;
            pos = file.indexOf("\n", pos) + 1;
            pos = file.indexOf("\n", pos) + 1;
            
            int boundaryLocation = file.indexOf(boundary, pos) - 4;
            int startPos = ((file.substring(0, pos)).getBytes()).length;
            int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;
            //LINUX style of specifying the folder name
            saveFile = "/tmp/" + saveFile;
            File ff = new File(saveFile);
            
         // creating a new file with the same name and writing the content in new file
            FileOutputStream fileOut = new FileOutputStream(ff);
            fileOut.write(dataBytes, startPos, (endPos - startPos));
            fileOut.flush();
            fileOut.close();
          //Now execute the script in the database
          
	     
			//now execute this script for testing1
			ArrayList<String> listOfQueries=(new FileToSql()).createQueries(saveFile);
			String[] inst = listOfQueries.toArray(new String[listOfQueries.size()]);
	        //get the connection for testing1
	        Connection dbcon=(new DatabaseConnection()).graderConnection();			
	        
	        try{
	        	
	        	String insertData = "";
	        	PreparedStatement stmt;  
	            for(int i = 0; i<inst.length; i++){  
	               // we ensure that there is no spaces before or after the request string  
	                // in order to not execute empty statements  
	                if(!inst[i].trim().equals("")){
	                	String str = inst[i].replaceAll("'","''");
	                	stmt = dbcon.prepareStatement(str+";");	                	
	                	stmt.executeUpdate();
	                	insertData += inst[i].replaceAll("'", "''").trim().replaceAll("\r\n+", " ").trim().replaceAll("\n+", " ").trim().replaceAll(" +", " ") + ";";
	                }  
	            }
	            
	            /**update this information in schema info table*/
	        	/*PreparedStatement stmt1;
	        	stmt1 = dbcon.prepareStatement("UPDATE xdata_schemainfo SET sample_data = ? where course_id = ? and schema_id = ?");
	        	stmt1.setString(2, "CS-101");
	    		stmt1.setInt(3, schemaId);
	    		stmt1.setString(1, insertData);
	    		//stmt1.setString(4, "schemaId");
	        	
	    		stmt1.executeUpdate();
	        	
	         }  
    		catch (Exception err) {
    			out.println("<p style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">"+err+" </p>");
    			out.println("<p style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">Not updated properly </p>");
      		  	 err.printStackTrace();
      		  	 x=1;
      		  	 //System.exit(1);
    		}
	        dbcon.close();
          
          
          
          //print the output
          if(x==0){

    		out.println("</head>");
    		
			out.println("<h3 class=\"wufoo\"  style=\"font-family:arial;color:red;font-size:20px;background-color:white;\"> You have successfully uploaded the file </h3> ");
           // out.println(saveFile);
            
          }
                        
      
      else{
    	  out.println("<h3 class=\"wufoo\" style=\"font-family:arial;color:red;font-size:20px;background-color:white;\">Error in file uploading. Please verify it ! <h3>");

      }
    out.println("Wait some time....Automatically redirected");*/
%>
</body>
</html>