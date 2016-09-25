<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Welcome To XData</title>
<script type="text/javascript">
function getParameterByName(name) { 
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 

function LoadPage(){ 
	var assignId;
	//alert("Comes here abd get session info: param val =  "+getParameterByName("FrmLtiToAssign"));
	
	if(getParameterByName("FrmLtiToAssign") != null && <%= session.getAttribute("allowedAssignment")%> != null){
		assignId = <%= session.getAttribute("allowedAssignment")%>;
		//alert("Comes here abd get session info: "+assignId);
		document.getElementById('right2').src = "Student/ListOfQuestions.jsp?assignmentid="+assignId;
	}else{
		document.getElementById('right2').src = "Student/ListAllAssignments.jsp" ;
	}
}
</script>

<frameset rows=50px,* border=0 frameborder=0 onload="LoadPage();" framespacing=0  id="leftframeset">
	<frame src="Header.jsp"  name="rightPage1" id="right1" tabindex="60" scrolling="no" style="border-bottom: 1px solid black">
	 
	<frameset cols=23%,* border=0 frameborder=0 framespacing=0  >
		<frame  src = "Student/StudentMenu.jsp" name="leftPage" id="leftname" tabindex="1">
		<frame src="Welcome.jsp"  name="rightPage" scrolling="yes" id="right2" tabindex="60" style="padding:10px 0 0 5px;">
	</frameset>	
</frameset>
</head> 
<body>
<%
String courseId = request.getParameter("contextLabel");
System.out.println("Instructor Home - context label value : "+ courseId);
if(! ((Boolean)session.getAttribute("ltiIntegration"))){
	session.setAttribute("context_label",courseId);
}%>
</body>
</html> 