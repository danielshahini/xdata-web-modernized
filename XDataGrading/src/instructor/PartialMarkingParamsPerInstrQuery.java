package instructor;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import partialMarking.PartialMarkParameters;

import database.DatabaseConnection;

/**
 * Servlet implementation class PartialMarkingParamsPerInstrQuery
 */
//@WebServlet("/PartialMarkingParamsPerInstrQuery")
public class PartialMarkingParamsPerInstrQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PartialMarkingParamsPerInstrQuery() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		response.setContentType("text/html");
		try{
		PrintWriter out = response.getWriter();
		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""+
		"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"+

		"<html xmlns=\"http://www.w3.org/1999/xhtml\">"  	
		+"<head>"
		+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
		+"<script type=\"text/javascript\" src=\"scripts/jquery.js\"></script>"
		+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\"/>"
		//+"<script type=\"text/javascript\" src = \"scripts/jquery-ui.min.js\"></script>"
		+"<script src=\"scripts/rangeslider/rangeslider.min.js\"></script>"
		+"<link rel=\"stylesheet\" href=\"scripts/rangeslider/rangeslider.css\"/>"
		+"<link rel=\"stylesheet\" href=\"highlight/styles/xcode.css\"/>" 	
		+"<link rel=\"stylesheet\" href=\"highlight/styles/default.css\"/>"
		+"<script src=\"scripts/bootstrap/dist/js/bootstrap.js\"></script>"
		+"<link href=\"scripts/bootstrap/dist/css/bootstrap.css\" rel=\"stylesheet\"/>"
		+"<script src=\"highlight/highlight.pack.js\"></script>"
		+"<script type=\"text/javascript\">hljs.initHighlightingOnLoad();"
		+"$(document).ready(function(){	var $document   = $(document),selector= '[data-rangeslider-sub]'," +
		"$element= $(selector);"
		+"function valueOutput(element) {var value = element.value," +
		"output = element.parentNode.parentNode.getElementsByTagName('output')[0];" +
		"output.innerHTML = value;"
		+"}"
		+"for (var i = $element.length - 1; i >= 0; i--) {valueOutput($element[i]);	};"
		+"$document.on('input', 'input[type=\"range\"]', function(e) {valueOutput(e.target);}); "    
		+"$element.rangeslider({"		
				// Deactivate the feature detection
					+"		polyfill: false,"		
				// Callback function
					+"		onInit: function() {},"		
				// Callback function
					+"onSlide: function(position, value) {"      
					+"},"		
				// Callback function
				+"onSlideEnd: function(position, value) {"  	     
				+"}"
			+"});"
				/*+"$(\".btn btn-default\").click(function (e) {"
				//+"HideDialog();"
				+"alert(\"Onclick evnt fired\");"
				+"e.preventDefault();"
				+"$(\"#btnSubmit\").click();"
				+"});"*/
				+"$(\"#btnSubmit\").click(function (e)  {"
				 // HideDialog(); 
				//+"alert(\"Comest to onclick\");"
				 +" e.preventDefault();"
				 +"  var queryId = $('#queryId').text();"
				 +" var assignId = $('#assignId').text();"
				 +" var quesId = $('#questionId').text();"
				 +" var reqFromPage = $('#reqFrom').text();"
				 +"var predicate =$('#predicates').val();"
				 +"var projection=$('#projections').val();"
				 +"var relation=$('#relations').val();"
				 +"var groupBy=$('#groupBy').val();"
				 +"var joins=$('#joins').val();"
				 +"var having=$('#having').val();"
				 +"var subQConnective=$('#subQconnective').val();"
				 +"var aggregates=$('#aggregates').val();"
				 +"var setoperators=$('#setoperators').val();"
				 +"var outer=$('#outer').val();"
				 +"var distinct=$('#distinct').val();"
				 +"var from=$('#fromSub').val();"
				 +"var where=$('#whereSub').val();"
				 +" var dataString =\"assignmentId=\"+assignId+\"&&questionId=\"+quesId+\"&&queryId=\"+queryId+\"&&reqFromPage=\"+reqFromPage" 
				 + "+\"&&predicate=\"+$('#predicates').val()+\"&&projection=\"+$('#projections').val()+" 
				 + "\"&&relation=\"+$('#relations').val()+\"&&groupBy=\"+$('#groupBy').val()+\"&&outer=\"+" 
				 + "$('#outer').val()+\"&&joins=\"+$('#joins').val()+\"&&distinct=\"+$('#distinct').val()+\"&&having=\"+$('#having').val()+\"&&setoperators=\"+$('#setoperators').val()"
				 +"+\"&&subQConnective=\"+$('#subQconnective').val()+\"&&aggregates=\"+$('#aggregates').val()+\"&&from=\"+$('#fromSub').val()+\"&&where=\"+$('#whereSub').val();"
				 //+"alert(dataString);"
				  +"$.ajax({" 	
				  +"    type: \"POST\","  
				  +"    url: 'PartialMarkerServlet'," 
				  +" 	data: dataString,"
				  +"	context:$(this),"        
				  +"      success: function(data) {"
				  +"		try{"
				        		+"alert(\"Partial marking Parameters are set.\");"
				        		+"$('#modalClose').show();"
				        		//+"alert(\"Show div\");"
				        		+"$('#close').click();"
				        	+"}catch(err){" 
					        +"	 alert(\"Error in setting partial marking parameter values.\");"
				        	+"}"
						+"}"			        
				      +"});"
				
				+"});"
				/*+"function ShowDialog(modal)"
				+"{"
				 +"$(\"#overlay\").show();"
				 +"$(\"#dialog\").fadeIn(300);"     
				 +"$(\"#overlay\").unbind(\"click\");"
				+"}"
				
				+"function HideDialog()"
				+"{$(\"#overlay\").hide();"
				+"$(\"#dialog\").fadeOut(300);}"*/
				+"});"
				+"</script>" 
 
				+"<title>Partial Mark Details</title>"
				+"<style>" 
				+".web_dialog_overlay"
				+"{ position: fixed;"
				+"top: 0; right: 0; bottom: 0;left: 0;height: 100%;width: 100%;margin: 0;padding: 0;background: #000000;"
				+"opacity: .15;filter: alpha(opacity=15);-moz-opacity: .15;z-index: 101;display: none;}"
				+".web_dialog{  display: none;position: fixed;width: 500px;height: 500px;top: 50%;left: 50%;margin-left: -190px;"
				+"margin-top: -100px; background-color: #ffffff;border: 2px solid #ffffff;padding: 0px; z-index: 102;"
				+"font-family: Verdana;font-size: 10pt;}"
				+".web_dialog_title{  border-bottom: solid 2px #3B5998;background-color: #3B5998;padding: 4px;color: White;"
				+"font-weight:bold;} " 
				+".web_dialog_title a { color: White;text-decoration: none;}"
				+".align_right{   text-align: right; }" 
				+".web_dialog table { border: 0;}"
				+".web_dialog th {  border: 0;}"
				+".web_dialog td{  border: 0; }"
				+"label {font-size: 15px; font-weight: bold;color: #666; } " 
				+"fieldset.action { background: #9da2a6;border-color: #e5e5e5 #797c80 #797c80 #e5e5e5; margin-top: -20px;}"
				+"legend {background: -webkit-linear-gradient(top, #657B9E, #4E5663);"
				+"background: -moz-linear-gradient(top, #657B9E, #4E5663);"
				+"color: #fff;font: 17px/21px Calibri, Arial, Helvetica, sans-serif; "
				+"padding: 0 10px; margin: 0 0 0 -11px;/* font-weight: bold; */	border: 1px solid #fff;"
				+"border-color: #3D5783 #3D5783 #3D5783 #3D5783;text-align: left;}"
				+"fieldset{	width:auto;	}"	
				+"label	span,.required {color: red;font-weight: bold;font-size: 17px;}"
				+"font{font: 15px/16px Arial, Helvetica, sans-serif;padding: 0;	}"
				+".topDiv{float: left;width: 400px;padding-top: 15px;}"
				+".querypartialmark{  margin: 0; padding: 0; width:auto;margin-bottom: 20px;}"
				+".querypartialmark ul{ margin: 0; padding: 0;padding-left: 20px;}"
				+".querypartialmark ul li{padding-bottom: 5px;list-style: circle;}"
				+".querypartialmark .number{text-align: right;}"
				+".querypartialmark h4{margin: 5px;}"
				+".emph{font-weight: bold;}"
				+"</style></head><body><div><div class=\"fieldset\"><fieldset>"
				+"<legend>Mark Details</legend>	<div align = \"left\">" );
				
		 int assignID = Integer.parseInt(request.getParameter("assignment_id"));
		       int questionID = Integer.parseInt(request.getParameter("question_id"));
		       String requestingPage = request.getParameter("reqFrom");
		       int queryId = Integer.parseInt(request.getParameter("query_id"));
		       Connection conn = (new DatabaseConnection()).dbConnection();
		       
		      if(!requestingPage.equalsIgnoreCase("demo")){ 
			       out.println("<p><h4>Assignment: <label id='assignId'>"+assignID+"</label></h4></p>"
			    		   +"<p><h4>Question: <label id='questionId'>"+questionID+"</label></h4></p>"
			    		   +"<p><h4>Query Id: <label id='queryId'>"+ queryId +"</label></h4></p>"
			    		   + "<p><label hidden' id='reqFrom' "+ "Partial Mark" +"</label></p>");
		      }else{
		    	  out.println("<p><label hidden id='assignId' >"+assignID+"</label></p>"
		    			  		+"<p><label hidden  id='questionId' >"+questionID+"</label></p>"
		    			  		+"<p><label hidden  id='queryId' >"+ queryId +"</label></p>" 
		    			  		+"<p><label hidden  id='reqFrom'> "+ requestingPage +"</label></p>" );
		      }
			       PreparedStatement stmt = conn.prepareStatement("select * from xdata_instructor_query where assignment_id = ? and question_id = ? and query_id=?");
	  		       stmt.setInt(1, assignID);
			       stmt.setInt(2, questionID);
			       stmt.setInt(3,queryId);
	  		       ResultSet rs = stmt.executeQuery();
	  		     out.println("<div id=\"output\"></div>");
		      
	  		   //out.println("<div class=\"modal fade\" id=\"PartialParamModal\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"PartialParamModal\" aria-hidden=\"true\">"
	  				 //  	+"<div class=\"modal-dialog\">"
	  				   //	+"<div class=\"modal-content\">"
	  				 // +"<div class=\"modal-header\"></div>"
	  				//+" <div class=\"modal-body\"></div>    "     
				 //   +"</div></div>  ");
	  		     if(rs.next()){
	  		       out.println("<p><h4>Instructor Query: <pre><code class=\"sql\">"+ rs.getString("sql")+"</code></pre></h4></p>");
	  		   
	  		       PartialMarkParameters marksParam = new PartialMarkParameters();
			     	Gson gson = new Gson();
			     	PartialMarkParameters marks = gson.fromJson( rs.getString("partialmarkinfo"), PartialMarkParameters.class);
			     	
			     	if(marks != null){
			     	out.println("<table style=\"width: 70%; border: 0px;\" cellpadding=\"2\" cellspacing=\"0\">"	
			     			+"<tr>"
			     			+"<td colspan=\"2\" class=\"web_dialog_title\" style=\"text-align: center;\">Partial Marking Parameters</td>"
			     				//+"<td class=\"web_dialog_title\">&nbsp;"
			     			//+"</td>"
			     			+"</tr>"
			     			//+"<tr>"
			     			//	+"<td>&nbsp;</td> <td>&nbsp;</td>"
			     			//+"</tr>" 
			     			+"<tr>"
			     				+"<td colspan=\"2\" style=\"text-align: center;\">"
			     				+"<b>Set weightage for partial marking parameters</b>"
			     				+"</td>" 
			     			+"</tr>" 	);	     				
			     				
				         //   +"<tr><td>&nbsp;</td><td>&nbsp;</td> </tr>");
			     	
			     	out.println("  <tr>"
			     				+"<td colspan=\"2\" style=\"padding-left: 15px;\">"
			     					+"<div id=\"paremeters\">"
				                    	+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px; width:100px;'>Predicates:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id='predicates' type='range' min='0' max=10 value='"+marks.getPredicate()+"' data-rangeslider-sub>"
											 +"</div>"
											  +"<output style='float: left; width: 30px;'></output>"										
										+"</div>"
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Projections:</label>"
											 +" <div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'projections' type='range' min='0' max=10 value='"+marks.getProjection()+"' data-rangeslider-sub>"
											 +"</div>"
											 +"<output style='float: left; width: 30px;'></output>"
											 +"</div>");
			     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Relations:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'relations' type='range' min='0' max=10 value='"+marks.getRelation()+"' data-rangeslider-sub>"
											 +"</div>"
											 +"<output style='float: left; width: 30px;'></output>"
										+"</div>"
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Group By:</label>"
										+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'groupBy' type='range' min='0' max=10 value='"+marks.getGroupBy() +"' data-rangeslider-sub>"
											 +"</div>"
											  +"<output style='float: left; width: 30px;'></output>"										
										+"</div>");		
			     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Joins:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'joins' type='range' min='0' max=10 value='"+marks.getJoins()+"' data-rangeslider-sub>"
											 +"</div>"
											 +"<output style='float: left; width: 30px;'></output>"	
											 
										+"</div>"
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Having Clause:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'having' type='range' min='0' max=10 value='"+marks.getHavingClause() +"' data-rangeslider-sub>"
										 +"</div>" +"<output style='float: left; width: 30px;'></output>"	 
										 	
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Subquery Connective:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'subQconnective' type='range' min='0' max=10 value='"+marks.getSubQConnective() +"' data-rangeslider-sub>"
										 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
		 	
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Aggregates:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'aggregates' type='range' min='0' max=10 value='"+marks.getAggregates() +"' data-rangeslider-sub>"
										 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
										 	
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Set Operators:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'setoperators' type='range' min='0' max=10 value='"+marks.getSetOperators() +"' data-rangeslider-sub>"
										 +"</div>" +"<output style='float: left; width: 30px;'></output>"
										 	
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Distinct:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'distinct' type='range' min='0' max=10 value='"+marks.getDistinct() +"' data-rangeslider-sub>"
										 +"</div>" +"<output style='float: left; width: 30px;'></output>"
										 	
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Outer Query:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'outer' type='range' min='0' max=10 value='"+marks.getOuterQuery() +"' data-rangeslider-sub>"
											 +"</div>"
											 +"<output style='float: left; width: 30px;'></output>"	 
										+"</div>"
										
										 +"<output style='float: left; width: 30px;'></output>"	 
									+"</div>");	
			     	
			     			
	  		     out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>From Subquery:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'fromSub' type='range' min='0' max=10 value='"+marks.getFromSubQueries()+"' data-rangeslider-sub>"
											 +"</div>"
											 +" <output style='float: left; width: 30px;'></output>"									
										+"</div>"
										+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Where Subquery:</label>"
											+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												+"<input id = 'whereSub' type='range' min='0' max=10 value='"+marks.getWhereSubQueries()+"' data-rangeslider-sub>"										 
											+" </div>"
											+"<output style='float: left; width: 30px;'></output>");/*
	  		     out.println("</div></div></td></tr><tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td colspan=\"2\" style=\"text-align: center;\">"
	  		     				+"<input id=\"btnSubmit\" type=\"button\" value=\"Evaluate\" />"
	  		     				+"</td> </tr></table>");*/
			     	}else{
		 		     	out.println("<table style=\"width: 70%; border: 0px;\" cellpadding=\"2\" cellspacing=\"0\">"	
				     			+"<tr>"
				     			+"<td colspan=\"2\" class=\"web_dialog_title\" style=\"text-align: center;\">Partial Marking</td>"
				     				//+"<td class=\"web_dialog_title\">&nbsp;"
				     			//+"</td>"
				     			+"</tr>"
				     			
				     			+"<tr>"
				     				+"<td colspan=\"2\"  style=\"text-align: center;\">"
				     				+"<b>Set the partial marking parameters</b>"
				     				+"</td>" 
				     			+"</tr>" 		     				
					            +"<tr><td>&nbsp;</td><td>&nbsp;</td> </tr>");
				     	out.println("  <tr>"
				     				+"<td colspan=\"2\" style=\"padding-left: 15px;\">"
				     					+"<div id=\"paremeters\">"
					                    	+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px; width:100px;'>Predicates:</label>"
												 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id='predicates' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												  +"<output style='float: left; width: 30px;'></output>"										
											+"</div>"
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Projections:</label>"
												 +" <div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'projections' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												 +"<output style='float: left; width: 30px;'></output>"
												 +"</div>");
				     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Relations:</label>"
												 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'relations' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												 +"<output style='float: left; width: 30px;'></output>"
											+"</div>"
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Group By:</label>"
											+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'groupBy' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												  +"<output style='float: left; width: 30px;'></output>"										
											+"</div>");		
				     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Joins:</label>"
												 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'joins' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												 +"<output style='float: left; width: 30px;'></output>"									
											+"</div>"
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Having Clause:</label>"
											+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												+"<input id = 'having' type='range' min='0' max=10 value='1' data-rangeslider-sub>"													
											+" </div>"
											 +"<output style='float: left; width: 30px;'></output>"
											 +"</div>"
												
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Subquery Connective:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'subQconnective' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
											 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
																				 	
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Aggregates:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'aggregates' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
											 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
		 	
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Set Operators:</label>"
											 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											 	+"<input id = 'setoperators' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
											 +"</div>" +"<output style='float: left; width: 30px;'></output>"
											 	
			+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Distinct:</label>"
			 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
			 	+"<input id = 'distinct' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
			 +"</div>" +"<output style='float: left; width: 30px;'></output>"
			 	
											 +"<output style='float: left; width: 30px;'></output>"	 
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Outer Query:</label>"
												 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'outer' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												 +"<output style='float: left; width: 30px;'></output>"	 
											+"</div>"
											);	
				     	
				     			
		  		     out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>From Subquery:</label>"
												 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
												 	+"<input id = 'fromSub' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
												 +"</div>"
												 +" <output style='float: left; width: 30px;'></output>"									
											+"</div>"
											+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Where Subquery:</label>"
												+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
													+"<input id = 'whereSub' type='range' min='0' max=10 value='1' data-rangeslider-sub>"										 
												+" </div>"
											
												+"<output style='float: left; width: 30px;'></output>");/*
		  		     out.println("</div></div></td></tr><tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td colspan=\"2\" style=\"text-align: center;\">"
		  		     				+"<input id=\"btnSubmit\" type=\"button\" value=\"Evaluate\" />"
		  		     				+"</td> </tr></table>");*/
		  		     }
  		     }else{
 		     	out.println("<table style=\"width: 70%; border: 0px;\" cellpadding=\"2\" cellspacing=\"0\">"	
		     			+"<tr>"
		     			+"<td colspan=\"2\" class=\"web_dialog_title\" style=\"text-align: center;\">Partial Marking</td>"
		     				//+"<td class=\"web_dialog_title\">"
		     			//+"</td>"
		     			+"</tr>"
		     		
		     			+"<tr>"
		     				+"<td colspan=\"2\" style=\"text-align: center;\">"
		     				+"<b>Set the partial marking parameters</b>"
		     				+"</td>" 
		     			+"</tr>" );
		     	out.println("  <tr>"
		     				+"<td colspan=\"2\" style=\"padding-left: 15px;\">"
		     					+"<div id=\"paremeters\">"
			                    	+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px; width:100px;'>Predicates:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id='predicates' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										  +"<output style='float: left; width: 30px;'></output>"										
									+"</div>"
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Projections:</label>"
										 +" <div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'projections' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										 +"<output style='float: left; width: 30px;'></output>"
										 +"</div>");
		     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Relations:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'relations' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										 +"<output style='float: left; width: 30px;'></output>"
									+"</div>"
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Group By:</label>"
									+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'groupBy' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										  +"<output style='float: left; width: 30px;'></output>"										
									+"</div>");		
		     	out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Joins:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'joins' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										 +"<output style='float: left; width: 30px;'></output>"									
									+"</div>"
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Having Clause:</label>"
									 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
									 	+"<input id = 'having' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
									 +"</div>"
									 +"<output style='float: left; width: 30px;'></output>"									
										+"</div>"
									 	
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Subquery Connective:</label>"
									 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
									 	+"<input id = 'subQconnective' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
									 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
									 	
	+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Aggregates:</label>"
	 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
	 	+"<input id = 'aggregates' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
	 +"</div>" +"<output style='float: left; width: 30px;'></output>"	
	 	
	+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Set Operators:</label>"
	 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
	 	+"<input id = 'setoperators' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
	 +"</div>" +"<output style='float: left; width: 30px;'></output>"
	 	
		+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Distinct:</label>"
		 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
		 	+"<input id = 'distinct' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
		 +"</div>" +"<output style='float: left; width: 30px;'></output>"
									 	
									 +"<output style='float: left; width: 30px;'></output>"	 
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Outer Query:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'outer' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										 +"<output style='float: left; width: 30px;'></output>"	 
									+"</div>");	
		     	
		     			
  		     out.println("<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>From Subquery:</label>"
										 +"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
										 	+"<input id = 'fromSub' type='range' min='0' max=10 value='1' data-rangeslider-sub>"
										 +"</div>"
										 +" <output style='float: left; width: 30px;'></output>"									
									+"</div>"
									+"<div class=\"topDiv\"><label style='float:left; margin-left: 10px;width:100px;'>Where Subquery:</label>"
										+"<div style='float: left; width: 250px; margin-left: 10px;  padding-top: 3px;'>"
											+"<input id = 'whereSub' type='range' min='0' max=10 value='1' data-rangeslider-sub>"										 
										+" </div>"
										+"<output style='float: left; width: 30px;'></output>");/*
  		     out.println("</div></div></td></tr><tr><td>&nbsp;</td><td>&nbsp;</td></tr><tr><td colspan=\"2\" style=\"text-align: center;\">"
  		     				+"<input id=\"btnSubmit\" type=\"button\" value=\"Evaluate\" />"
  		     				+"</td> </tr></table>");*/
  		     }
  		     out.println("</div></div></td></tr>" +
  		     		//"<tr><td>&nbsp;</td><td>&nbsp;</td></tr>" +
  		     		"<tr><td colspan=\"2\" style=\"text-align: center;\">"
	     				+"<input id=\"btnSubmit\" type=\"button\" value=\"Set\" />"
  		     		//+"<button type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Set</button>"
	     				+"</td> </tr></table>");
  		     out.println("<div id=\"modalClose\" style='display:none;'><button id=\"close\" type=\"button\" class=\"btn btn-default\" data-dismiss=\"modal\">Close</button></div>");
		}catch(Exception e){
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
