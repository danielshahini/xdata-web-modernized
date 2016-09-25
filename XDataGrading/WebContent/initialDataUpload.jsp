<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html> 
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="scripts/wufoo.js"></script>
<link rel="canonical"
	href="http://www.wufoo.com/gallery/designs/template.html">
<title>Upload Sample Data</title>
<style>
 
</style>
<script>
	function checkValue1() {

		if (document.getElementById("upload").value != "") {
			return true;
		} else {
			alert("Please upload file");
			return false;
		}

	}

	function trim(str, chars) {
		return ltrim(rtrim(str, chars), chars);
	}

	function ltrim(str, chars) {
		chars = chars || "\\s";
		return str.replace(new RegExp("^[" + chars + "]+", "g"), "");
	}

	function rtrim(str, chars) {
		chars = chars || "\\s";
		return str.replace(new RegExp("[" + chars + "]+$", "g"), "");
	}
</script>

</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
	<div>
		<form name="dataForm" enctype="multipart/form-data"
			action="dataUpload.jsp" method="post">

			<div class="fieldset">
				<fieldset>
					<legend> Upload Data File</legend>

					<p>
						<label class="field">Upload the SQL script containing the
							initial data </label>
					</p>

					<%
						if (session.getAttribute("LOGIN_USER") == null || !session.getAttribute("LOGIN_USER").equals("ADMIN")) {
										response.sendRedirect("index.jsp");
										return;
									}  

									//get the connection for testing1
									Connection dbcon = (new DatabaseConnection()).dbConnection();

									try {

										PreparedStatement stmt;
										stmt = dbcon
												.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
										stmt.setString(1, (String) (String) request.getSession()
												.getAttribute("context_label"));
										

										String output = "";
										ResultSet rs = stmt.executeQuery();
										while (rs.next()) {
											output += " <option value = \"" + rs.getInt("schema_id")
													+ "\"> " + rs.getInt("schema_id") + "-"
													+ rs.getString("schema_name") + " </option> ";
										} 
										
										rs.close();
										out.println("<label style=\"text-align:left;float:left;\"><strong>Schema Name</strong></label> <br/> ");
										out.println(" <select name=\"schemaid\" required> " + output
												+ "</select> &nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp");
										out.println("<label class=\"field\"><span>*</span>Choose File: </label> <input	type=\"file\" required name=\"dataFile\" size=\"20\"> <input name=\"submit\" onclick=\"return checkValue1();\" type=\"submit\"	id=\"upload\" value=\"Upload\">");
									} catch (Exception err) {

										err.printStackTrace();
										out.println("<p style=\"color:red;font-size: 17px;\">Error in retrieving schema file details<p>");
										//throw new ServletException(err); 
									}
									finally{
										dbcon.close();
									}
					%> 
					
				</fieldset>

			</div>
		</form>
	</div>

</body>
</html>