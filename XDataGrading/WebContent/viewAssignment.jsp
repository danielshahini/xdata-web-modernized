<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.DatabaseConnection"%>
<%@page import="database.DatabaseProperties"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<html>
<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Assignment List</title>
<script type="text/javascript" src="scripts/newrow.js"></script>
<!--  <script type="text/javascript" src="scripts/wufoo.js"></script> -->

<!-- CSS -->
<link rel="stylesheet" href="css/structure.css" type="text/css" />
<link rel="stylesheet" href="css/form.css" type="text/css" />
<link rel="stylesheet" href="css/theme.css" type="text/css" />

<link rel="canonical"
	href="https://www.wufoo.com/gallery/designs/template.html">
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null &&(!session.getAttribute("role").equals("instructor") || !session.getAttribute("role").equals("tester"))){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

	//int assignID=Integer.parseInt(request.getParameter("AssignmentID").trim());
	String assignID = (String) request.getSession().getAttribute(
	"resource_link_id");
	String courseID = (String) request.getSession().getAttribute(
	"context_label");
	
	//get connection
	//Connection dbcon = (new DatabaseConnection()).dbConnection();
	
	Timestamp start = null;
	Timestamp end = null;
	try(Connection dbcon = (new DatabaseConnection()).dbConnection()) {
		PreparedStatement stmt1;
		ResultSet rs1;
		stmt1 = dbcon
		.prepareStatement("SELECT * FROM xdata_assignment where assignment_id=? and course_id=?");
		stmt1.setString(1, assignID);
		stmt1.setString(2, courseID);
		rs1 = stmt1.executeQuery();
		while (rs1.next()) {
	start = rs1.getTimestamp("starttime");
	end = rs1.getTimestamp("endtime");
	//start=rs.getString("end_date");
		}

	} catch (Exception err) {
		err.printStackTrace();
		throw new ServletException(err);
	}

	//TODO: Remove this date comparision as it is not needed any more
	//now check whether current time is less than start time.Then only assignment can be edited
	boolean yes = false;
	SimpleDateFormat formatter = new SimpleDateFormat(
	"yyyy-MM-dd HH:mm:ss");
	formatter.setLenient(false);
	String ending = formatter.format(end);
	String starting = formatter.format(start);
	//String oldTime = "2012-07-11 10:55:21";
	java.util.Date oldDate = formatter.parse(ending);
	//get current date
	Calendar c = Calendar.getInstance();

	String currentDate = formatter.format(c.getTime());
	java.util.Date current = formatter.parse(currentDate);
	//out.println("current "+current);
	//out.println("end "+oldDate);
	//out.println("Compare c->o: "+current.compareTo(oldDate));
	//out.println("Compare o->c: "+oldDate.compareTo(current));

	//compare times
	if (current.compareTo(oldDate) < 0) {
		yes = true;
	}

	String output = "";
	output += "<table  cellspacing=\"10\"  class=\"authors-list\" id=\"queryTable\" align=\"center\"> <tr> <th style=\"font-family:arial;color:red;font-size:20px;\">Question ID</th>       <th style=\"font-family:arial;color:red;font-size:20px;\">Question Text</th>  <th style=\"font-family:arial;color:red;font-size:20px;\">Correct Query</th> <th> </th></tr>";
	//get query details
	try (Connection dbcon = (new DatabaseConnection()).dbConnection()){
		PreparedStatement stmt;
		//stmt = dbcon.prepareStatement("SELECT * FROM xdata_qinfo ,assignment where qinfo.assignment_id=? AND qinfo.assignment_id=assignment.assignment_id");
		stmt = dbcon
		.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id=?");
		stmt.setString(1, assignID);
		stmt.setString(2, courseID);
		ResultSet rs;
		int qID;
		String query;
		String description;
		rs = stmt.executeQuery();
		while (rs.next()) {
	//get query details
	qID = rs.getInt("question_id");
	query = rs.getString("correctquery");
	description = rs.getString("querytext");
	if (true) {
		output += "<tr> <td> <label><input type=\"number\" name=\"qID\" size=\"4\" id=\"qID"
				+ qID
				+ "\" value=\""
				+ qID
				+ "\" readonly></label> </td>";
		output += "<td> <textarea name=\"quesTxt\"  rows=\"6\" cols=\"57\" >"
				+ description + " </textarea> </td>";
		output += " <td><textarea name=\"query\" id=\"query"
				+ qID + "\"rows=\"6\" cols=\"57\">" + query
				+ " </textarea></td> ";
		//output +="<td><input type=\"button\" value=\"Generate\" name=\"Generate\""; 
		//output +=" onclick=\"document.forms[0].action = 'CreateCourse.jsp'; return true;\" /></td>";
		//output+="<td> <a href=\"AssignmentChecker.jsp?assignment_id="+assignID+"&question_id="+qID+"&question_id="+qID+"&query="+query+"\" method=\" POST\"><span  style=\"font-family:arial;font-size:20px;background-color:white;\" >Generate Dataset</span> </a></td> </tr>";
		output = output
				.concat("<td> <input type=\"button\" id=\"button "
						+ qID
						+ "\" value=\"Generate Dataset\" onclick=\"submitter(this,'queryTable')\"> </td></tr>"
						+ "\n");
	}
	//this else is dead code--not needed
	else {
		output += "<tr> <td> <label><input type=\"number\" name=\"qID\" size=\"4\" id=\"qID\" value=\""
				+ qID + "\" readonly></label> </td>";
		output += "<td> <textarea name=\"quesTxt\" rows=\"6\" cols=\"57\" readonly>"
				+ description + " </textarea> </td>";
		output += " <td><textarea name=\"query\" rows=\"6\" cols=\"57\" readonly>"
				+ query + " </textarea></td> </tr>";
	}
		}
		
		rs.close();
		output += "</table>";
	} catch (Exception err) {
		err.printStackTrace();
		throw new ServletException(err);
	}

	//dbcon.close();
	//out.println(output);
	//print data
	/*
	  out.println("  <!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" ");
	 	out.println( "\"https://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> ");

	 	out.println(" <html xmlns=\"https://www.w3.org/1999/xhtml\"> ");
	out.println("<head> 
 <link rel="stylesheet" href="css/structure.css" type="text/css"/>");
	out.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />");
	
	
	out.println("function getColumnMax(tableId, columnIndex) {");
	out.println("var table = document.getElementById(tableId);");
	out.println("var max = 1;");
	out.println("for(var i = 2; i <= Number.MAX_VALUE; i++){");
	out.println(" var list = document.getElementsByClassName(i);");
	out.println(" if(list.length == 0) break;");
	out.println(" max = i;");
	out.println("}");
    out.println("  return [max];");	
	out.println("}");

	
	out.println("<link rel=\"canonical\" href=\"https://www.wufoo.com/gallery/designs/template.html\">");
	out.println("<title>View Assignment</title>");
	 *///out.println("<script>");
	out.println(" <script language=\"javascript\">");


	
	out.println("function submitter(btn,tableID){");
	out.println("var table = document.getElementById(tableID);");
	out.println("var rowCount = table.rows.length;");
	out.println("var row = table.insertRow(rowCount);");
	out.println("row.id=rowCount");
	out.println("var cell1 = row.insertCell(0);");
	out.println("var element1 = document.createElement(\"input\"); ");
	out.println("element1.type=\"number\" ;");
	out.println("element1.name=\"qID\" ;");
	out.println("element1.size=\"4\" ;");
	out.println("element1.value=rowCount ; ");
	out.println(" var qid=\"qID\".concat(rowCount);");
	out.println("element1.id=qid; ");
	//out.println("alert(\"element1: \"+element1.id);");
	out.println("element1.setAttribute('readOnly','readonly');");
	out.println("cell1.appendChild(element1); ");

	out.println("var table = document.getElementById(tableID);");
	out.println("var rowCount = table.rows.length;");
   
    
  	//get maximum row of this query id
  //	out.println("var ids =  getColumnMax(tableID, 0) ;");
  //	out.println("var rowId = parseInt(ids[1])");
  //remove below line and uncomment above two lines after fixing getcolumnmax
  out.println("var rowId = parseInt( btn.id.split(\" \")[1] );");
  
  //get the number of rows with each id <= rowId
	out.println("var len = 0");
	out.println("for (var i = 1; i <= rowId; i++) {");
	out.println("  var list = document.getElementsByClassName(i);");
	out.println("   len = parseInt(len) + parseInt(list.length);");
	out.println("}");
  	out.println("var row = table.insertRow( parseInt(len) + 1);");
    out.println("row.id = rowId;"); 
    out.println("row.className = rowId;");
    out.println("var cell0 = row.insertCell(0);");
    out.println("var cell1 = row.insertCell(1);");
    out.println("var cell2 = row.insertCell(2);");
    
    out.println("var element2 = document.createElement(\"textarea\"); ");
	out.println("element2.type=\"textarea\" ;");
	out.println("element2.id = \"query \".concat(rowId).concat(\" \").concat( parseInt(document.getElementsByClassName(rowId).length)  ) ;");
	out.println(" var quer = \"query\".concat(rowId);");
	out.println("element2.className = quer; ");
	out.println("element2.name = quer ;");
	out.println("element2.rows=\"6\" ;");
	out.println("element2.cols=\"57\" ;");
	out.println("cell2.appendChild(element2); ");
	
	
    out.println("var cell3 = row.insertCell(3);");   
	out.println("var buttonnode= document.createElement('input');");
	out.println("buttonnode.setAttribute('type','button');");
	out.println("buttonnode.setAttribute('name','generte');");
	out.println("buttonnode.setAttribute('value','Generate Dataset');");
	out.println("buttonnode.setAttribute('id',\"button \".concat(rowId).concat(\" \").concat( parseInt(document.getElementsByClassName(rowId).length)  ) );");
	out.println(" buttonnode.setAttribute('onclick','submitter(this,\"'+tableID+'\")' );");
	out.println("cell3.appendChild(buttonnode);");
	
	out.println("var cell4 = row.insertCell(4);");
	out.println("}");

	out.println("function submitter(btn,tableID){");
	out.println("var table = document.getElementById(tableID);");
	out.println(" var num=btn.id.split(\" \")[1]");
	out.println(" var quer=\"query\".concat(num);");
	out.println(" var Qid=\"qID\".concat(num);");
	//out.println("alert(Qid);");
	out.println(" var quer=\"query\".concat(num);");
	out.println("var asID=document.getElementById(\"NOQ\").value;");
	out.println("var que= document.getElementById(quer).value; ");
	out.println("var qid=document.getElementById(Qid).value;");
	String in = "AssignmentChecker?assignment_id=\"+asID+\"&question_id=\"+qid+\"&query=\"+que; ";
	out.println("var out=\"" + in + ";");
	out.println("window.open( out,\"Generating Dataset\",\"height=400,width=400\"); ");
	out.println("}");

	out.println("</script>");

	out.println("</head>");

	out.println("<body id=\"public\">");

	out.println("<form class=\"wufoo\" name=\"queryForm\" action=\"UpdateExistingAssignment.jsp\" method=\"post\" onsubmit=\"return(validate());\" >");
	out.println("<label style=\"font-family:arial;color:red;font-size:20px;\"><strong>Asssignment ID</strong></label>");
	out.println("<label><input type=\"number\" name=\"AssignmentID\" size=\"30\" id=\"NOQ\" value=\""
	+ assignID + "\" readonly ></label><br/>");
	out.println("<label style=\"font-family:arial;color:red;font-size:20px;\"><strong>Start Date</strong></label>");

	//printing start date and time
	String[] Starting = starting.split(" ");
	String[] startYear = Starting[0].split("-");
	String[] startTime = Starting[1].split(":");
	//start year
	String[] month = { "", "January", "February", "March", "April",
	"May", "June", "July", "August", "September", "October",
	"November", "December" };

	String startmonth = "<select name=\"startmonth\"  > " + "\n";
	for (int i = 1; i <= 12; i++) {
		//out.println(i+" "+month[i]+" "+startYear[1]+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i)))+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i))).equals(startYear[1]) +"<br/>");
		if (i < 10
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startYear[1])))
	startmonth += "<option value=\"" + '0' + (i) + "\">"
			+ month[i] + "</option>" + "\n";
		else if (i != (Integer.parseInt(startYear[1])))
	startmonth += "<option value=\"" + (i) + "\">" + month[i]
			+ "</option>" + "\n";
		else if (i < 10
		&& String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startYear[1]))
	startmonth += "<option value=\"" + '0' + (i)
			+ "\" selected>" + month[i] + "</option>" + "\n";
		else if (i == (Integer.parseInt(startYear[1])))
	startmonth += "<option value=\"" + (i) + "\" selected>"
			+ month[i] + "</option>" + "\n";
	}
	startmonth += "</select>";

	String startyear = "  <select name=\"startyear\"  > " + "\n";
	String[] year = { "2012", "2013", "2014", "2015", "2016", "2017",
	"2018", "2019", "2020" };

	for (int i = 0; i < 9; i++) {
		if (year[i].equals(startYear[0]))
	startyear += " <option value=\"" + startYear[0]
			+ "\" selected>" + startYear[0] + "</option> "
			+ "\n";
		else
	startyear += " <option value=\"" + year[i] + "\">"
			+ year[i] + "</option>" + "\n";
	}
	startyear += "</select>";

	String startday = "<select name=\"startday\"  >" + "\n";
	//<option value="01">1</option>
	for (int i = 1; i < 32; i++) {

		//out.println(i+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i)))+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i))).equals(startYear[1]) +"<br/>");

		if (i < 10
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startYear[2])))
	startday += " <option value=\"" + '0' + i + "\">" + i
			+ "</option>" + "\n";
		else if (i < 10
		&& String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startYear[2]))
	startday += " <option value=\"" + '0' + i + "\" selected>"
			+ i + "</option>" + "\n";
		else if (i != (Integer.parseInt(startYear[2])))
	startday += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
		else if (i == (Integer.parseInt(startYear[2])))
	startday += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
	}
	startday += "</select>" + "\n";

	String starthour = " <select name=\"starthour\"  > " + "\n";
	String hour = startTime[0];
	boolean ampm = false;
	boolean done = false;
	if (Integer.parseInt(startTime[0]) == 0
	|| Integer.parseInt(startTime[0]) > 12) {
		ampm = true;
		if (Integer.parseInt(startTime[0]) != 0)
	hour = Integer.toString(Integer.parseInt(hour) - 12);
		else {
	//starthour += " <option value=\"12\">12</option>"+"\n";
	done = true;
		}
	}
	for (int i = 1; i < 13; i++) {
		if (i < 10
		&& !(String.format("%01d",
				Integer.parseInt(Integer.toString(i)))
				.equals(hour)))
	starthour += " <option value=\"" + '0' + i + "\">" + '0'
			+ i + "</option>" + "\n";
		else if (i < 10
		&& (String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(hour)))
	starthour += " <option value=\"" + '0' + i + "\" selected>"
			+ '0' + i + "</option>" + "\n";
		else if (i != (Integer.parseInt(hour))
		&& (i != 12 || done != true)) {
	starthour += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
	//System.out.println("hello");
		} else if (i == (Integer.parseInt(hour))
		&& (i != 12 || done != true))
	starthour += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
		else if (done = true && i == 12)
	starthour += " <option value=\"12\" selected>12</option>"
			+ "\n";
	}
	starthour += "</select>" + "\n";

	String startmin = " <select name=\"startmin\"  > " + "\n";
	for (int i = 0; i < 60; i += 10) {
		if (i == 0
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startTime[1])))
	startmin += " <option value=\"" + '0' + i + "\" >" + '0'
			+ i + "</option>" + "\n";
		else if (i == 0
		&& (String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(startTime[1])))
	startmin += " <option value=\"" + '0' + i + "\" selected>"
			+ '0' + i + "</option>" + "\n";
		else if (i != (Integer.parseInt(startTime[1])))
	startmin += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
		else if (i == (Integer.parseInt(startTime[1])))
	startmin += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
	}
	startmin += "</select>" + "\n";

	String startampm = " <select name=\"startampm\"  >" + "\n";
	if (ampm) {
		startampm += "<option value=\"00\">AM</option>" + "\n";
		startampm += "<option value=\"12\" selected>PM</option>" + "\n";
	} else {
		startampm += "<option value=\"00\" selected>AM</option>" + "\n";
		startampm += "<option value=\"12\" >PM</option>" + "\n";
	}
	startampm += "</select>" + "\n";
	/*
	for(int i=1;i<13;i++){
		if(i<10 && !(String.format("%02d",Integer.parseInt("0"+Integer.toString(i)) ).equals(startTime[0])) )
	starthour += " <option value=\""+'0'+i+"\">"+'0'+i+"</option>"+"\n";
		else if(i<10 && (String.format("%02d",Integer.parseInt("0"+Integer.toString(i)) ).equals(startTime[0])) )
	starthour += " <option value=\""+'0'+i+"\" selected>"+'0'+i+"</option>"+"\n";
		else if ( i!=(Integer.parseInt(startTime[0])) )
	starthour +=" <option value=\""+i+"\" >"+i+"</option>"+"\n";
		else if ( i ==(Integer.parseInt(startTime[0])) )
	starthour +=" <option value=\""+i+"\" selected>"+i+"</option>"+"\n";    	
	}
	starthour +="</select>";
	 */

	if (true) { //if(yes)
		//out.println("<label><input type=\"datetime\" name=\"startTime\" size=\"30\" id=\"start\" value=\""+start+"\" ></label><br/>");

		out.println(startday + "&nbsp" + startmonth + "&nbsp"
		+ startyear + "<br/>");
		out.println("<label style=\"font-family:arial;color:red;font-size:20px;\"><strong>Start Time</strong></label>");
		out.println(starthour + "&nbsp" + startmin + "&nbsp"
		+ startampm + "<br/>");
	} else
		//dead code
		out.println("<label><input type=\"datetime\" name=\"startTime\" size=\"30\" id=\"start\" value=\""
		+ start + "\" readonly></label><br/>");

	//printing end date and time
	String[] Ending = ending.split(" ");
	String[] endYear = Ending[0].split("-");
	String[] endTime = Ending[1].split(":");

	String endmonth = "<select name=\"endmonth\"  > " + "\n";
	for (int i = 1; i <= 12; i++) {
		//out.println(i+" "+month[i]+" "+startYear[1]+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i)))+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i))).equals(startYear[1]) +"<br/>");
		if (i < 10
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endYear[1])))
	endmonth += "<option value=\"" + '0' + (i) + "\">"
			+ month[i] + "</option>" + "\n";
		else if (i != (Integer.parseInt(endYear[1])))
	endmonth += "<option value=\"" + (i) + "\">" + month[i]
			+ "</option>" + "\n";
		else if (i < 10
		&& String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endYear[1]))
	endmonth += "<option value=\"" + '0' + (i) + "\" selected>"
			+ month[i] + "</option>" + "\n";
		else if (i == (Integer.parseInt(endYear[1])))
	endmonth += "<option value=\"" + (i) + "\" selected>"
			+ month[i] + "</option>" + "\n";
	}
	endmonth += "</select>" + "\n";

	String endyear = "  <select name=\"endyear\"  > " + "\n";

	for (int i = 0; i < 9; i++) {
		if (year[i].equals(endYear[0]))
	endyear += " <option value=\"" + endYear[0]
			+ "\" selected>" + endYear[0] + "</option> " + "\n";
		else
	endyear += " <option value=\"" + year[i] + "\">" + year[i]
			+ "</option>" + "\n";
	}
	endyear += "</select>" + "\n";

	String endday = "<select name=\"endday\"  >" + "\n";
	//<option value="01">1</option>
	for (int i = 1; i < 32; i++) {

		//out.println(i+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i)))+" "+String.format("%02d",Integer.parseInt("0"+Integer.toString(i))).equals(startYear[1]) +"<br/>");

		if (i < 10
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endYear[2])))
	endday += " <option value=\"" + '0' + i + "\">" + i
			+ "</option>" + "\n";
		else if (i < 10
		&& String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endYear[2]))
	endday += " <option value=\"" + '0' + i + "\" selected>"
			+ i + "</option>" + "\n";
		else if (i != (Integer.parseInt(endYear[2])))
	endday += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
		else if (i == (Integer.parseInt(endYear[2])))
	endday += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
	}
	endday += "</select>" + "\n";

	String endhour = " <select name=\"endhour\"  > " + "\n";
	hour = endTime[0];
	ampm = false;
	done = false;
	if (Integer.parseInt(endTime[0]) == 0
	|| Integer.parseInt(endTime[0]) > 12) {
		ampm = true;
		if (Integer.parseInt(endTime[0]) != 0)
	hour = Integer.toString(Integer.parseInt(hour) - 12);
		else
	done = true;
	}
	for (int i = 1; i < 13; i++) {
		if (i < 10
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(hour)))
	endhour += " <option value=\"" + '0' + i + "\">" + '0' + i
			+ "</option>" + "\n";
		else if (i < 10
		&& (String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(hour)))
	endhour += " <option value=\"" + '0' + i + "\" selected>"
			+ '0' + i + "</option>" + "\n";
		else if (i != (Integer.parseInt(hour))
		&& (i != 12 || done != true))
	endhour += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
		else if (i == (Integer.parseInt(hour)))
	endhour += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
		else if (i == 12 && done == true)
	endhour += "<option value=\"12\" selected>12</option>";
	}
	endhour += "</select>" + "\n";

	String endmin = " <select name=\"endmin\"  > " + "\n";
	for (int i = 0; i < 60; i += 10) {
		if (i == 0
		&& !(String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endTime[1])))
	endmin += " <option value=\"" + '0' + i + "\" >" + '0' + i
			+ "</option>" + "\n";
		if (i == 0
		&& (String.format("%02d",
				Integer.parseInt("0" + Integer.toString(i)))
				.equals(endTime[1])))
	endmin += " <option value=\"" + '0' + i + "\" selected>"
			+ '0' + i + "</option>" + "\n";
		else if (i != (Integer.parseInt(endTime[1])))
	endmin += " <option value=\"" + i + "\" >" + i
			+ "</option>" + "\n";
		else if (i == (Integer.parseInt(endTime[1])))
	endmin += " <option value=\"" + i + "\" selected>" + i
			+ "</option>" + "\n";
	}
	endmin += "</select>" + "\n";

	String endampm = " <select name=\"endampm\"  >" + "\n";
	if (ampm) {
		endampm += "<option value=\"00\">AM</option>" + "\n";
		endampm += "<option value=\"12\" selected>PM</option>" + "\n";
	} else {
		endampm += "<option value=\"00\" selected>AM</option>" + "\n";
		endampm += "<option value=\"12\" >PM</option>" + "\n";
	}
	endampm += "</select>" + "\n";

	out.println("<label style=\"font-family:arial;color:red;font-size:20px;\"><strong>End Date</strong></label>");
	if (true) {//if(yes) 
		//out.println("<label><input type=\"datetime\" name=\"endTime\" size=\"30\" id=\"end\" value=\""+end+"\" ></label><br/>");
		out.println(endday + "&nbsp" + endmonth + "&nbsp" + endyear
		+ "<br/>");
		out.println("<label style=\"font-family:arial;color:red;font-size:20px;\"><strong>End Time</strong></label>");
		out.println(endhour + "&nbsp" + endmin + "&nbsp" + endampm
		+ "<br/>");
	} else
		//dead code
		out.println("<label><input type=\"datetime\" name=\"endTime\" size=\"30\" id=\"end\" value=\""
		+ end + "\" readonly></label><br/>");

	out.println("<h3  style=\"font-family:arial;color:red;font-size:20px;background-color:white;\" >Following is the list of queries in the assignment</h3>");
	//String ht="alert ("+str+")";

	out.println(output);
	//if( yes)
	out.println("<input type=\"button\" id=\"quer\" onClick=\"javascript:addRow('queryTable')\" value=\"New Query\" >");
	// out.println("<a href=\"#\" title=\"\" class=\"add-author\">Add Author</a> ");
	//if(yes) 
	out.println("<input type=\"submit\" id=\"sub\" value=\"Update\"><br>");
	//if(!yes)   	out.println("<h3  style=\"font-family:arial;color:red;font-size:20px;background-color:white;\" >Assignment cannot be edited</h3>");
	out.println("</form>");
	out.println("<input type=\"button\" id=\"home\" value=\"Home\" onClick=\"document.location.href='instructorOptions.html'\"><br>");
	out.println("</body>");
%>

</html>