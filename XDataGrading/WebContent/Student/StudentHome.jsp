<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Welcome to XData</title> 
 
<script type="text/javascript">
function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

function LoadPage(){ 
	var assignId;
	if(getParameterByName("FrmLtiToAssign")!= null){
		assignId = <%= session.getAttribute("variableName")%>;
		document.getElementById('right2').src = "Student/ListOfQuestion.jsp?assignmentid="+assignId;
	}else{
		document.getElementById('right2').src = "Student/ListAllAssignments.jsp" ;
	}
}
</script>

</head>

<frameset rows=50px,* border=0 frameborder=0 framespacing=0   id="leftframeset">
	<frame src="Header.jsp"  name="rightPage1" id="right1" tabindex="60" scrolling="no" style="border-bottom: 1px solid black">
	  
	<frameset cols=23%,* border=0 onload="LoadPage();" frameborder=0 framespacing=0  >
		<frame  src = "StudentMenu.jsp" name="leftPage" id="leftname" tabindex="1">
		<frame src="Welcome.jsp"  name="rightPage" id="right2" scrolling="yes" tabindex="60" style="padding:10px 0 0 5px;">
	</frameset>	
</frameset>
 
<body> 
<% 


String courseId = (String)session.getAttribute("contextLabel"); 
System.out.println("Student  Home - context label value : "+ courseId);
if(! ((Boolean)session.getAttribute("ltiIntegration"))){
	session.setAttribute("context_label",courseId);
} %>
</body>
</html> 