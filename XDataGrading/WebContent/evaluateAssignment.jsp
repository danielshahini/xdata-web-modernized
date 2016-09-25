<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>

<%if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

		//get the input data
		String[] queryText=(String[])request.getParameterValues("quesTxt");
		String[] correctQuery=(String[])request.getParameterValues("query");
		String[] queryID=(String[])request.getParameterValues("qID");
		String asgnmentID =(String)request.getParameter("AssignmentID");
	
		if(queryText.length != correctQuery.length){
		out.println("Error in the query information");
		System.exit(1);
		}
	
		//out.println(asgnmentID);
		for(int i=0;i<queryText.length ; i++){
			//out.println("id "+i+": "+queryID[i]);
			//out.println("text "+i+": "+queryText[i]);
			//out.println("query "+i+": "+correctQuery[i]);
		}
%>


</body>
</html>