<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "https://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="java.lang.reflect.Type"%>
<%@page import="java.math.BigDecimal"%>
<%!
	public String listToString(List<String> list1, List<String> list2){
		String ret = "<ul>";
		for(String s:list1){
			if(list2.contains(s)){
				ret += "<li>" + s + "</li>";
			}else{
				ret += "<li style='color:red;'>" + s + "</li>";
			}		 
		}
		ret += "</ul>";
		return ret;
	}

    public float roundToDecimal(float marks){
	return BigDecimal.valueOf(marks).setScale(1, BigDecimal.ROUND_HALF_UP).floatValue();
}
    int index = 1;
    int newQueryID = 1;
    String instrQuery="";
    String studQuery = "";
%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Partial Marking Demo</title>
<head> 

<link rel="stylesheet" type="text/css" href="css/structure.css"/>
<link rel="stylesheet" type="text/css" href="css/jquery-ui.css"/>

<script type="text/javascript" src="scripts/jquery-2.1.4.js"></script>
<script type="text/javascript" src="scripts/jquery-1.11.2.js"></script>

<link rel="stylesheet" href="scripts/codemirror/lib/codemirror.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

 <script src="scripts/bootstrap/dist/js/bootstrap.js"></script> 
<link href="scripts/bootstrap/dist/css/bootstrap.css" rel="stylesheet"/>
<script type="text/javascript" src="scripts/codemirror/lib/codemirror.js"></script>
<script type="text/javascript" src="scripts/codemirror/mode/sql/sql.js"></script>
<script type="text/javascript" src="scripts/codemirror/addon/hint/show-hint.js"></script>
<script type="text/javascript" src="scripts/codemirror/addon/hint/sql-hint.js"></script>
<script>

//Holds count of text box created
var counter =1;
//holds query box count including existing queries 
var boxname=0;
//Holds previous query_id box added
var prevId = parseInt("0");

/*Following method loads the page with default code mirror text area*/
window.onload = function() { 
	   
	  var mime = 'text/x-mariadb';
	  // get mime type
	  if (window.location.href.indexOf('mime=') > -1) {
	    mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
	  }
	  //Initialize code mirro text area
	 $('.textForSQL').each(function(index) {		
       $(this).attr('id', 'textarea-' + index);  
  
      window.editor =  CodeMirror.fromTextArea(document.getElementById('textarea-' + index), {
     	mode: mime,
   	    indentWithTabs: true,
   	    smartIndent: true,
   	    matchBrackets : false, 
   	    lineWrapping: true,
   	    autofocus: true,
   	  extraKeys: {"Ctrl-Space": "autocomplete"},
	  	  hintOptions: {tables: {
	      users: {name: null, score: null, birthDate: null},
	      countries: {name: null, population: null, size: null}
	    }}
        } ); 
      editor.on("blur", function() {editor.save();});
   });  
	 CodeMirror.commands.autocomplete = function(cm) {};
	 
	 $('.CodeMirror').resizable({
		  resize: function() {
		    editor.setSize($(this).width(), $(this).height());
		  }
		});
};


function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 

$( document ).ready(function() {

	
	
//Function to add a new query text area - view/delete partial marking params link - for more than one instructor queries
$(document).on('click', '.queryBox' ,function (event) { 
	//alert("Onclick Query Box");
	 var $this = $(this);
	 
	 idname = parseInt(this.name);
	 boxname = boxname+1;
	
	  counter = counter+1;
	 var correctId=parseInt(this.id);
	 var txtBoxId = "query " +correctId +" "+boxname;
	 var txt = "'query " + correctId+" "+boxname+"'";
			
	  var newTextAreaDiv = $(document.createElement('div'))
	     .attr("id", 'TextAreaDiv' + counter);
	  	newTextAreaDiv.attr("class","answer");                                                                    
	    
	  	var htmlString = "<label style='float: left;height: 30px; width:100%;'>Instructor Query:"+counter+"</label><br/>"
		
	  	+'<textarea  style="padding:5px;width:98%; height:200px;" class="textForSQL" name="newQuery" id="'+txtBoxId+'"></textarea>'
	  	
	  	+"<div style='height: 25px; width:100%'>"
	  	+"<b><a data-toggle=\"modal\" data-target=\"#PartialParamModal"+counter+"\" "
		+	"href=\"PartialMarkingParamsPerInstrQuery?reqFrom=demo&&assignment_id=1&question_id="+counter+"&query_id=1\">View/Edit partial marking parameters</a></b></td></div>"
	
				
		+ '<br/><input type="button" class="remove" id="remove" name="" value="Delete">'
		+'<div class="modal fade" id="PartialParamModal'+counter+'" tabindex="-1" role="dialog" aria-labelledby="PartialParamModal" aria-hidden="true">'
		+'	 <div class="modal-dialog">'
		+'	  <div class="modal-content">'
		+'	     <div class="modal-header"></div>'
		+'	            <div class="modal-body"></div>'         
		+'   </div>'
		+' </div></div>';
	   // + '<textarea" style="padding:5px;width:98%; height:200px;" id="'+txtBoxId+'" class="textForSQL" name="newQuery" ></textarea> ';
		//+ '<br/><div style="position:relative;"><input type="button" class="remove" id="remove" name="remove" value="Delete"></div>';
		 //+'<input type="hidden" name="newQueries" value="javascript:editor.getValue();">';
	  	 
		newTextAreaDiv.add().html(htmlString);
 		newTextAreaDiv.appendTo("#dynamicAdd");
		correctId++; 
		prevId = idname;
		
		/****CODE mirror for dynamic obj*****/
		  var mime = 'text/x-mariadb';
		  if (window.location.href.indexOf('mime=') > -1) {
	    		mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
	 	 }
		  //alert("txtBoxId = "+ txtBoxId);
		 window.editor = CodeMirror.fromTextArea(document.getElementById(txtBoxId) , {
      		 mode: mime,
    	    indentWithTabs: true,
    	    smartIndent: true,
    	    matchBrackets : false, 
    	    lineWrapping: true,
    	    autofocus: true,
    	    extraKeys: {"Ctrl-Space": "autocomplete"},
      		hintOptions: {tables: {
  	      		users: {name: null, score: null, birthDate: null},
  	      		countries: {name: null, population: null, size: null}
  	    }}
        }); 
      //CodeMirror.commands.autocomplete = function(cm) {};
      editor.on("blur", function() {editor.save();});         
      CodeMirror.commands.autocomplete = function(cm) {};    
});



//Func to remove newly added text areas
$(document).on('click', '.remove',function(event) {	 
	
		if(counter <= 1 && ($('textarea[name=newQuery]').size())>1){
			
			counter = $('textarea[name=newQuery]').size();
		}
		else if(counter==1){ 
            alert("No more query boxes to remove. Atleast one correct query is necessary.");
          
            return false;
         } 		
          $("#TextAreaDiv" + counter).remove(); 
      	counter--;
      	if(prevId>0){
      		prevId--;
      	}
      	
}); 
	  
/*Following method displays the contenets of the clicked  tab*/
$('.nav-tabs > li > a').click(function(event){
	//alert("Comes to onclick");
	event.preventDefault();//stop browser to take action for clicked anchor

	//get displaying tab content jQuery selector
	var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');

	//find actived navigation and remove 'active' css
	var actived_nav = $('.nav-tabs > li.active');
	actived_nav.removeClass('active');

	//add 'active' css into clicked navigation
	$(this).parents('li').addClass('active');

	//hide displaying tab content
	$(active_tab_selector).removeClass('active');
	$(active_tab_selector).addClass('hide');

	//show target tab content
	var target_tab_selector = $(this).attr('href');
	$(target_tab_selector).removeClass('hide');
	$(target_tab_selector).addClass('active');
     });
  
  /*Method to compute the partial marks using ajax call*/
$(document).on('click','#getPartialMarks',function (event) {
	
	var sch = document.getElementById("id_schema");
	var id_schema = sch.options[sch.selectedIndex].value;
	var db = document.getElementById("id_dbconnection");
	var id_dbconnection = db.options[db.selectedIndex].value;
	event.preventDefault();
	 var isCanonicalized;
    if ($('#canonicalize').is(":checked")){
    	isCanonicalized = "canonicalize"
    }
    var ed = $('.CodeMirror')[0].CodeMirror;
    var query;
	//Get all instructor queries separated by the following delimiter
    query = ed.getValue() + '#@###@#';
    
    var codeMirrorValues = document.getElementsByName("newQuery");
    //alert("codeMirrorValues.length = "+codeMirrorValues.length);
    var edt;
    for(var i=0;i<codeMirrorValues.length;i++){
    	edt= codeMirrorValues[i].value;
    	query += (edt+'#@###@#');
    	//alert("i = "+edt);
    }
  	 var dataString = "instructorQuery="+ query +'&&studentQuery='+document.getElementById("textarea-1").value+
    '&&canonicalize='+isCanonicalized+'&&dbConnectionId='+id_dbconnection+'&&schemaId='+id_schema; 
	var index = this.name;
	var self = this; 
	$.ajax({ 
        type: "POST",  
        url: "PartialMarkingDemo",
        data: dataString,
        context:this,           
	        success: function(data) { 
	        	try{	
	        		//Get the html content and display it in a div
	        		$('#showCanonicalizationResult').html( data );
	        		$('html,body').animate({ scrollTop: $("#showCanonicalizationResult").offset().top-10});	
	   
				} 
        		catch(err)
        		{	
        			alert(err);         			       			
        		} 	        	
	        }, 
	        error : function(xhr, thrownError){
	        	
	        	if(xhr.status == 500){
	        		 alert(xhr.responseText );
	        	}else{
	        		alert("Internal error. Please check the log file for details.");
	        	}
            }
	      }); 
		
	      return false; 
});

  /*The following code shows the tabbed contents based on which tab is clicked*/
$("#getPartialMarks").ajaxComplete(function( event,request, settings ){
	   //alert("ajaxCompleted");

		    // put the code you need to run when the load completes in here
		   $('.nav-tabs > li > a').click(function(event){
				//alert("Comes to line 329 - doc ready - nav tabs click functn");
				event.preventDefault();//stop browser to take action for clicked anchor
				
				//get displaying tab content jQuery selector
				var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');					
				
				//find actived navigation and remove 'active' css
				var actived_nav = $('.nav-tabs > li.active');
				actived_nav.removeClass('active');
				//alert("active _ tab_ name = "+actived_nav );
				//add 'active' css into clicked navigation
				$(this).parents('li').addClass('active');
				
				//hide displaying tab content
				$(active_tab_selector).removeClass('active');
				$(active_tab_selector).addClass('hide');
				
				//show target tab content
				var target_tab_selector = $(this).attr('href');
				$(target_tab_selector).removeClass('hide');
				$(target_tab_selector).addClass('active');
				//alert("active _ tab_ name = "+target_tab_selector );
			}); 	    
	});
  
});

/*This following method is used to show the tab 1 contents on load.*/
$(document).ready(function() {
	$('.nav nav-tabs > li > a').click(function(event){
		//alert("Comes to line 329 - doc ready - nav tabs click functn");
		event.preventDefault();//stop browser to take action for clicked anchor
		
		//get displaying tab content jQuery selector
		var active_tab_selector = $('.nav-tabs > li.active > a').attr('href');					
		
		//find actived navigation and remove 'active' css
		var actived_nav = $('.nav-tabs > li.active');
		actived_nav.removeClass('active');
		
		//add 'active' css into clicked navigation
		$(this).parents('li').addClass('active');
		
		//hide displaying tab content
		$(active_tab_selector).removeClass('active');
		$(active_tab_selector).addClass('hide');
		
		//show target tab content
		var target_tab_selector = $(this).attr('href');
		$(target_tab_selector).removeClass('hide');
		$(target_tab_selector).addClass('active');
	});
});

 
</script>
<style>
.CodeMirror {
  /* Set height, width, borders, and global font properties here */
  font-family: monospace;
  height: 100px;
  width: 900px;
  border: 1px solid black;
  font-size: large;
}


/** Start: to style navigation tab **/
			.nav {
			  margin-bottom: 18px;
			  margin-left: 0;
			  list-style: none;
			}

			.nav > li > a {
			  display: block;
			}

			.nav-tabs{
			  *zoom: 1;
			}

			.nav-tabs:before,
			.nav-tabs:after {
			  display: table;
			  content: "";
			}

			.nav-tabs:after {
			  clear: both;
			}

			.nav-tabs > li {
			  float: left;
			  color: #3D5783;
			}

			.nav-tabs > li > a {
			  padding-right: 12px;
			  padding-left: 12px;
			  margin-right: 2px;
			  line-height: 14px;
			}

			.nav-tabs {
			  border-bottom: 1px solid #ddd;
			}

			.nav-tabs > li {
			  margin-bottom: -1px;
			  color: #3D5783;
			}

			.nav-tabs > li > a {
			  padding-top: 8px;
			  padding-bottom: 8px;
			  line-height: 18px;
			  border: 1px solid transparent;
			  -webkit-border-radius: 4px 4px 0 0;
				 -moz-border-radius: 4px 4px 0 0;
					  border-radius: 4px 4px 0 0;
			}

			.nav-tabs > li > a:hover {
			  border-color: #eeeeee #eeeeee #dddddd;
			}

			.nav-tabs > .active > a,
			.nav-tabs > .active > a:hover {
			  color: #555555;
			  cursor: default;
			  background-color: #ffffff;
			  border: 1px solid #ddd;
			  border-bottom-color: transparent;
			}

			li {
			  line-height: 18px;
			}

			.tab-content.active{
				display: block;
			}

			.tab-content.hide{
				display: none;
			}


			/** End: to style navigation tab **/
			
</style>
</head>
<body>
<% //As it is a part of instructor login and web session, check for session expiry
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}else if(session.getAttribute("LOGIN_USER") != null && !session.getAttribute("LOGIN_USER").equals("ADMIN")
 		&& session.getAttribute("role") != null && !session.getAttribute("role").equals("instructor")){
	response.sendRedirect("index.jsp?NotAuthorised=true");
	session.invalidate();
	return;
}

%>
<%
	String courseId = (String) request.getSession().getAttribute(
			"context_label");
	//get the connection for testing1
	Connection dbcon = (new DatabaseConnection()).dbConnection();
	try {
		//PreparedStatement stmnt = dbcon.prepareStatement("select sampledata_id,sample_data_name from xdata_sampledata where course_id=? and schema_id=?");
		//stmnt.setString(1, courseId);
		//stmnt.setString(2,schema_id);
		//ResultSet rSet = stmnt.executeQuery();
		
		PreparedStatement stmt = dbcon
				.prepareStatement("SELECT connection_id,connection_name FROM xdata_database_connection WHERE course_id = ?");
		stmt.setString(1, courseId);
		String output = "";
		ResultSet rs = stmt.executeQuery();
		//TODO -  Get DB user and Test user and show it in the drop down
		// CLARIFY : How it will b shown for various Schema's
		// On select schema, show the user name options??????
		while (rs.next()) {
			output += " <option value = \""
					+ rs.getInt("connection_id") + "\"> "
					+ rs.getInt("connection_id") + "-"
					+ rs.getString("connection_name") + " </option> ";
		}							 
		rs.close();%>
		<div><label>Database Connection</label>  
		<select id="id_dbconnection" name="dbConnection" style='clear:both;'> 
				<%=output%> </select>
				 					
		<%
		output = "";			
		//output +="<option value=\"select\" selected> Select schema</option>";
		stmt = dbcon
				.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
		stmt.setString(1, courseId);
		rs = stmt.executeQuery();							
		while (rs.next()) {
			output += " <option value = \"" + rs.getInt("schema_id")
					+ "\"> " + rs.getInt("schema_id") + "-"
					+ rs.getString("schema_name") + " </option> ";
		}							
		%>
		
		<label>Default Database Schema</label>
		<select class="schemaId" id="id_schema" name="schemaid" style="clear:both;">
				<%=output%> </select>
		</div>
		<%
	} catch (Exception err) {

		err.printStackTrace();
		throw new ServletException(err);
		
	}
	finally{
		dbcon.close();
	}
%>

<br/>
<div>		<div class="fieldset">	
			<fieldset>
			<legend>Partial Marking Analysis</legend>
			<div id='resizable'>
			<label><b>Instructor Query:</b></label>
			<textarea name='query' class="textForSQL" id='query1'></textarea>
			</div>
			<br/>
			<%int questionId = 1; %>
			<div style="height: 25px; width:100%">
			
				<b><a data-toggle="modal" data-target="#PartialParamModal<%=questionId %>"
					href="PartialMarkingParamsPerInstrQuery?reqFrom=demo&&assignment_id=0&question_id=1&query_id=0">View/Edit partial marking parameters</a></b></td>
			</div>		
			<input type="button" class="queryBox" id="1" name="1" value="Add Instructor Query"/>  
			<input type='hidden' name='instructorQuery' id='instructorQuery'  value='javascript:editor.getValue();'/>
			<br/><br/>
			<div id="dynamicAdd"  class='dynamicAddDiv'></div>
			<br/>
			<div id='studentquery' style='position:relative;'>	
				<label><b>Student Query:</b></label>
				<textarea name='studentquery' class="textForSQL" id='query'></textarea>
				<input type='hidden' name='studentquery' id='studentquery' value='javascript:editor.getValue();'>
				<br/>		
			</div>
			<div id='dSet'>
			<!-- <input type="checkbox" name="canonicalize"  id="canonicalize" value="canonicalize" checked> 
			<label>Canonicalize</label> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-->
			
			<input type="button" align="left" id="getPartialMarks" value="Compute Partial Marks"></div>
			<br/>

		<!-- Modal to show Partial Marking Parameters Start -->
					<div class="modal fade" id="PartialParamModal<%=questionId%>" tabindex="-1" role="dialog" aria-labelledby="PartialParamModal" aria-hidden="false">
			   			 <div class="modal-dialog">
				   			  <div class="modal-content">
							     <div class="modal-header"></div>
							            <div class="modal-body"></div>         
						    </div>
						<!-- <div class="modal-footer"><br><button type="button" class="btn btn-default" data-dismiss="modal">Set</button></div></br> -->
					  </div>
					 </div>
		  <!-- Modal to show Partial Marking Parameters End -->



<div id="showCanonicalizationResult"></div>	
</div>	
			
</fieldset>			
</div>

</body>
</html>