<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%> 
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@ page import="java.text.*"%>
<%@page import="java.sql.*"%>
<%@page import="database.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head> 
<script type="text/javascript" src = "scripts/jquery.js"></script>
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>View Courses</title>
<style>
table, tr, td {
    border: 0px;
}
</style> 
<script>

function getParameterByName(name) { 
	 name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
   var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
       results = regex.exec(location.search);
   return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 
function LoadPage(){ 
	if(getParameterByName("showAll") != ""){	
		
		document.getElementById('showAllCourses').style.display= "block";
	} 
	else{
		document.getElementById('showCurrentYearCourses').style.display= "block";
	}
}

function onSubmit(id){
	
	var a = confirm('Are you sure you want to delete the course?');
	if(a==1) 
		window.location.href="deleteCourse.jsp?courseId="+id;
	else
		return false; 
}
function sendToStudent(courseId,path){ 
	window.top.location.href = path+"/selectMode.jsp?contextLabel="+courseId;
} 
 
function setSessionParam(id){	
	document.getElementById("hdnContextLabel").value = id;
	//alert("--"+id+"--"); 
	document.forms[0].submit();
}

</script>
</head>
<body onload="LoadPage()">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}
%>
<div>
		<div class="fieldset">
			<fieldset>
				<legend>Course List</legend>
				<form name="form1" method="post" action="selectMode.jsp" target="_top">
				<%
				Connection dbcon = null;
				boolean showAll = false;
				if(request.getParameter("showAll") != null
						&& request.getParameter("showAll").equals("true")){
					showAll = true;
				}
				int index=0; 
				String output = "";
				String userId = (String)session.getAttribute("user_id");
				String role = (String)session.getAttribute("role");
				Calendar c = Calendar.getInstance();
				int year = c.get(Calendar.YEAR);
				try{
				dbcon = (new DatabaseConnection()).dbConnection();
				PreparedStatement stmt;
				//Select courses to display for the logged in user
				if( role!= null && role.equalsIgnoreCase("admin")){
					stmt = dbcon.prepareStatement("select * from xdata_course;");
				} 
				else{
					stmt = dbcon
							.prepareStatement("select * from  xdata_course xc inner join xdata_roles xr on xc.instructor_course_id=xr.course_id  where internal_user_id=?");
						stmt.setString(1,userId); 
				}
				
				if(! showAll){
					ResultSet rs = stmt.executeQuery();
					output +="<div id=\"showCurrentYearCourses\" style='display:none'>";
					while(rs.next()){
						if(year == rs.getInt("year")){
						session.removeAttribute("contextLabel");
					//	 request.getSession().setAttribute("context_label",rs.getString("course_id"));
						index++;
						output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
						//Row 1 - column 1- index value, column 2- Course Id, column 3-course name,
						//column 4 - buttons
						output +=	"<tr><td width =\"2%\">"+index +".</td>";
						output += "<input type = \"hidden\" id=\"hdnContextLabel\" name=\"contextLabel\">";
						output += "<td class=\"wrapword\" width=\"40%\"><b>Course Id: </b>"+rs.getString("instructor_course_id")+"</td>";
						output +=   "<td></td><td class=\"wrapword\" width=\"35%\"> <b>Name: </b>"+rs.getString("course_name")+"</td>";
						output += "<td></td><td width=\"8%\"><input name=\"View\" type=\"button\" id=\""+rs.getString("instructor_course_id")+"\" value=\"View\" onclick=\"setSessionParam(this.id)\"></td>";
						output+="</tr>"; 
						//Row 2
						output +=   "<tr><td></td><td class=\"wrapword\"><b>Year: </b>"+rs.getString("year") +"&nbsp;&nbsp;<b>Semester: </b>"+rs.getString("semester") +"</td>";
						output +=   "<td></td><td class=\"wrapword\"><b>Description: </b>"+rs.getString("description") +"</td></tr>";
						   
						
					} else{
						output += "No courses available for current year.";
					}
					}
					output += "</div>";
				}
				else{
						//output = "";			
						ResultSet rs = stmt.executeQuery();
						output +="<div id=\"showAllCourses\" style='display:none'>";
						while(rs.next()){
							
							session.removeAttribute("contextLabel");
						//	 request.getSession().setAttribute("context_label",rs.getString("course_id"));
							index++;
							output +="<table border=\"0\" style=\"table-layout:fixed;\" width=\"100%\">";
							//Row 1 - column 1- index value, column 2- Course Id, column 3-course name,
							//column 4 - buttons
							output +=	"<tr><td width =\"2%\">"+index +".</td>";
							output += "<input type = \"hidden\" id=\"hdnContextLabel\" name=\"contextLabel\">";
							output += "<td class=\"wrapword\" width=\"40%\"><b>Course Id: </b>"+rs.getString("instructor_course_id")+"</td>";
							output +=   "<td></td><td class=\"wrapword\" width=\"35%\"> <b>Name: </b>"+rs.getString("course_name")+"</td>";
							output += "<td></td><td width=\"8%\"><input name=\"View\" type=\"button\" id=\""+rs.getString("instructor_course_id")+"\" value=\"View\" onclick=\"setSessionParam(this.id)\"></td>";
							output+="</tr>"; 
							//Row 2
							output +=   "<tr><td></td><td class=\"wrapword\"><b>Year: </b>"+rs.getString("year") +"&nbsp;&nbsp;<b>Semester: </b>"+rs.getString("semester") +"</td>";
							output +=   "<td></td><td class=\"wrapword\"><b>Description: </b>"+rs.getString("description") +"</td></tr>";
							   
							
						
						}
						output += "</div>";
				}		
							
				out.println(output);
				  
				//}
				
				
			} catch (Exception err) {	

				err.printStackTrace();
				//out.println("Error in getting list of assignments");
				throw new ServletException(err);
				
			}
			finally{
				dbcon.close();
			}%>

				
	</fieldset></div></div>									

</body>
</html>