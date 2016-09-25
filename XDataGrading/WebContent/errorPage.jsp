<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" isErrorPage="true"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>  
<head>
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Internal Error</title>
<style> 
label {
	font-size: 15px;
	font-weight: bold; 
	color: #666;
} 

fieldset.action {
	background: #9da2a6;
	border-color: #e5e5e5 #797c80 #797c80 #e5e5e5;
	margin-top: -20px;
}
legend {
	background: -webkit-linear-gradient(top, #657B9E, #4E5663);
	background: -moz-linear-gradient(top, #657B9E, #4E5663);
	color: #fff;
	font: 17px/21px Calibri, Arial, Helvetica, sans-serif;
	padding: 0 10px;
	margin: 0 0 0 -11px;
	/* font-weight: bold; */
	border: 1px solid #fff;
	border-color: #3D5783 #3D5783 #3D5783 #3D5783;
	text-align: left;
}
	
label
span,.required {
	color: red;
	font-weight: bold;
	font-size: 17px;
}

font{
font: 15px/16px Arial, Helvetica, sans-serif;
padding: 0;
}
</style> 
</head>  
<body> 

	<div>
		<br />
		<br />
		<div class="fieldset">
		<fieldset>
		<legend> Web page creation Internal Error</legend> 
		
		<p></p>  <p></p>
		<div align="left"><font class="label">
		<span>The requested page can not be displayed due to an internal error.<br> </span></font>
  		<p></p><p></p> 
  		<div align = "left">  
   		 <% //if ( pageContext.getErrorData()!= null && pageContext.getErrorData().getStatusCode() != 0) { %>
        	<!-- <font><b>Error Code:</b> </font> -->
<!--       <span style='font-weight:normal;font: 15px/16px Arial, Helvetica, sans-serif;'> <%//= pageContext.getErrorData().getStatusCode() %>
                </span> -->
         <%//} */%>
 
    	
        <p></p>  
        <p></p>          
        <% if((exception != null && exception.getClass() != null)){  %>           	
                <font><b>Error Details: </b></font>
			    <span style='font-weight:normal; font: 15px/16px Arial, Helvetica, sans-serif;'> <%=exception.getClass()%>  
			    </span>  
        <%} %> 
     
        <p></p>  
        <p></p>                   
        <% if (exception != null && exception.getMessage() != null) { %>
        	     <font><b>Error Message:</b></font>
                 <span style='font-weight:normal; font: 15px/16px Arial, Helvetica, sans-serif;'> 
                 <%= exception.getMessage() %>  </span>
        <%} %>            
 	   </div> 
       <p></p> 
       <p></p>
        <a class="header" href ="javascript:history.go(-1)"> Back</a>
	</fieldset>
		</div>
	</div>

</body>
</html>