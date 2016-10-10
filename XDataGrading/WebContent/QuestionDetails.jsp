<%@page import="partialMarking.PartialMarkParameters"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" errorPage="errorPage.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page import="java.io.*"%>
<%@ page import="java.util.*"%>
<%@page import="java.sql.*"%>
<%@ page import="java.text.*"%>
<%@page import="database.*"%>
<%@page import="com.google.gson.Gson"%>
<%@page import="java.lang.reflect.Type"%>
<%@page import="com.google.gson.reflect.TypeToken"%>
<%
	String qID = (String) request.getParameter("questionId");
    int index = 1;
    int newQueryID = 1;
%>
 
<html> 
<head> 
<link rel="stylesheet" type="text/css" href="css/structure.css"/>
<link rel="stylesheet" href="scripts/codemirror/lib/codemirror.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
 <script type="text/javascript"  src = "scripts/jquery.js"></script> 
<!-- <script type="text/javascript" src = "scripts/jquery-ui.min.js"></script>-->
<script src="scripts/bootstrap/dist/js/bootstrap.js"></script>
<link href="scripts/bootstrap/dist/css/bootstrap.css" rel="stylesheet"/>
<script type="text/javascript" src="scripts/codemirror/lib/codemirror.js"></script>
<script type="text/javascript" src="scripts/codemirror/mode/sql/sql.js"></script>
<script type="text/javascript" src="scripts/codemirror/addon/hint/show-hint.js"></script>
<script type="text/javascript" src="scripts/codemirror/addon/hint/sql-hint.js"></script>
<script src="scripts/rangeslider/rangeslider.min.js"></script>
<link rel="stylesheet" href="scripts/rangeslider/rangeslider.css">
 
 
<!-- <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js'></script> -->
<title>Details of the Question</title>
  
<script>
//Holds count of text box created
var counter =1;
//holds query box count including existing queries 
var boxname=0;
//Holds previous query_id box added
var prevId = parseInt("0");

window.closeModal = function(){
    $('#PartialParamModal').modal('hide');
};

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
};

$(function() {

    var $document   = $(document),
        selector    = '[data-rangeslider-main]',
        $element    = $(selector);

    // Example functionality to demonstrate a value feedback
    function valueOutput(element) {
    	console.log(element);
        var value = element.value,
            output = element.parentNode.parentNode.getElementsByTagName('output')[0];
        output.innerHTML = value;
    }
    for (var i = $element.length - 1; i >= 0; i--) {
        valueOutput($element[i]);
    };
    $document.on('input', 'input[type="range"]', function(e) {
        valueOutput(e.target);
    });    

    // Basic rangeslider initialization
    $element.rangeslider({

        // Deactivate the feature detection
        polyfill: false,

        // Callback function
        onInit: function() {},

        // Callback function
        onSlide: function(position, value) {
            
        },

        // Callback function
        onSlideEnd: function(position, value) {
        	console.log('Slide ended: ' + value);
        	var $inputRange = $('input[data-rangeslider-sub1]', document),
            attributes = {      
                max: value,
            };

	        $inputRange.attr(attributes);
	        $inputRange.rangeslider('update', true);
        }
    });
    
    attributes = {      
        max: '100',
    };

    $element.attr(attributes);
    $element.rangeslider('update', true);

});

$(function() {

    var $document   = $(document),
        selector    = '[data-rangeslider-sub1]',
        $element    = $(selector);

    // Example functionality to demonstrate a value feedback
    function valueOutput(element) {
        var value = element.value,
            output = element.parentNode.parentNode.getElementsByTagName('output')[0];
        output.innerHTML = value;
    }
    for (var i = $element.length - 1; i > 0; i--) {
        valueOutput($element[i]);
    };
    $document.on('input', 'input[type="range"]', function(e) {
        valueOutput(e.target);
    });    

    // Basic rangeslider initialization
    $element.rangeslider({

        // Deactivate the feature detection
        polyfill: false,

        // Callback function
        onInit: function() {},

        // Callback function
        onSlide: function(position, value) {
          
        },

        // Callback function
        onSlideEnd: function(position, value) {
           
        }
    });
    
    attributes = {      
        max: $('[data-rangeslider-main]')[0].value,
    };

    $element.attr(attributes);
    $element.rangeslider('update', true);

});
	
function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 

if(getParameterByName("PSQLError") != null){
	  //Show error div with error message above the Incorrect Query Text Area
	$('.showError'+getParameterByName("queryId")).show();
}

$(document).ready(function(){
	$('#loadDefaultDataSets').show();
	//alert("Loads on ready");
	var querySize = $('textarea[name=query]').size();
	// alert(querySize);
	  if(querySize > 1){
		  $('#matchAll').show();
	  } 
	  else{
		  $('#matchAll').hide(); 
	  }
	  
	  	//Added for Modal Start
	  	  $("#PartialParamModal").on("show.bs.modal", function(e) {
        	    var link = $(e.relatedTarget);
        	    //alert("link = "+link.attr("href"));
        	    $(this).find(".modal-body").load(link.attr("href"));
        });
	  	
	 
		  
	  	//Added for Modal End
		$('#optionalschemaid').on('change',function(e){
			//alert("OnChange---");
			var schemaSel = $(this).val(); 
			//window.location = "NewAssignmentCreation.jsp?selectedOption=" + $(this).val();
			var course_id=$(this).attr('class');
			if(schemaSel != 'select'){
				//$('#test').attr('class');   
				var dataString = "schemaId="+schemaSel+"&&course_id="+course_id;
				//alert("--DataString--"+dataString);
				$.ajax({ 
			        type: "POST",  
			        url: 'GetDefaultDataSets', 
			        data: dataString,
			        context:$(this),        
			        success: function(data) {
			        	try{
			        		//alert("Success Function");
			        		 $('#loadDefaultDataSets').html(data);
			        		// alert(data);
			        		 $('#loadDefaultDataSets').show();
			        	}catch(err){ 
				        	 alert("Error in loading default datasets.");
			        	}
			        }	        
			      });
			}else{
				$('#loadDefaultDataSets').hide();
			}
		});
		
		//Added for displaying default sample data end
});  

//Function to add a new query text area for adding new queries
$(document).on('click', '.queryBox' ,function (event) { 
 	//alert("Onclick Query Box");
 	 var $this = $(this);
 	 
 	 idname = parseInt(this.name); 
 	 
 	 if(idname == prevId) { 
 		 //alert("in if");
 		 boxname = boxname+1;
 	 }
 	 else{
 		 boxname=idname;
 	 }
 	 //alert(name);
 	  counter = counter+1;
 	 var correctId=parseInt(this.id);
 	 var txtBoxId = "query " +correctId +" "+boxname;
 	 var txt = "'query " + correctId+" "+boxname+"'";
 	  $('#matchAll').show();		
 	  var newTextAreaDiv = $(document.createElement('div'))
 	     .attr("id", 'TextAreaDiv' + counter);
 	  	newTextAreaDiv.attr("class","answer");                                                                    
 	    
 	  	var htmlString = 
 	  	"<div style='height: 25px; width:100%'><label style='float: left'>SQL answer:</label>                     "
		+ "<div style='float: left; width: 400px'><label style='float:left; margin-left: 10px;'>Marks:</label>      "
		+ "	 <output style='float: left; width: 20px;'></output>                                                  "
		+ "	 <div style='float: left; width: 300px; margin-left: 10px;  padding-top: 3px;'>                       "
		+ "	 	<input name='newMarks' type='range' min='0' max='100' value='100' data-rangeslider-sub1>  "
		+ "	 </div>                                                                                               "
		+ "</div>" 
		+"<b><a data-toggle=\"modal\" data-target=\"#PartialParamModal"+boxname+"\" "
			+	"href=\"PartialMarkingParamsPerInstrQuery?assignment_id=2&question_id="+correctId+"&query_id="+boxname+"\">View/Edit partial marking parameters</a></b></td></div>"
		
	  
	    + '<textarea  style="padding:5px;width:98%; height:200px;" class="textForSQL" name="newQuery" id="'+txtBoxId+'"></textarea>'
		+ '<br/><input type="button" class="remove" id="remove" name="" value="Delete">'
		+'<div class="modal fade" id="PartialParamModal'+boxname+'" tabindex="-1" role="dialog" aria-labelledby="PartialParamModal" aria-hidden="true">'
		+'	 <div class="modal-dialog">'
		+'	  <div class="modal-content">'
		+'	     <div class="modal-header"></div>'
		+'	            <div class="modal-body"></div>'         
		+'   </div>'
		+' </div></div>'
 	  	
 		newTextAreaDiv.after().html(htmlString);
   		newTextAreaDiv.appendTo("#dynamicAdd");
 		correctId++;
 		prevId = idname;
 		
 		$element = $('[data-rangeslider-sub1]');
 	// Basic rangeslider initialization
 	    $element.rangeslider({

 	        // Deactivate the feature detection
 	        polyfill: false,

 	        // Callback function
 	        onInit: function() {},

 	        // Callback function
 	        onSlide: function(position, value) {
 	            
 	        },

 	        // Callback function
 	        onSlideEnd: function(position, value) {
 	     
 	        }
 	    });
 	
 	   attributes = {      
	        max: $('[data-rangeslider-main]')[0].value,
	    };
	
	    $element.attr(attributes);
	    $element.rangeslider('update', true);
 		
 		/****CODE mirror for dynamic obj*****/
 		  var mime = 'text/x-mariadb';
 		  if (window.location.href.indexOf('mime=') > -1) {
	    		mime = window.location.href.substr(window.location.href.indexOf('mime=') + 5);
	 	 }
 		 window.editor = CodeMirror.fromTextArea( document.getElementById(txtBoxId) , {
        	mode: mime,
      	    indentWithTabs: true,
      	    smartIndent: true,
      	    matchBrackets : true, 
      	    lineWrapping: true,
      	    autofocus: true,
      	    extraKeys: {"Ctrl-Space": "autocomplete"},
        hintOptions: {tables: {
    	      users: {name: null, score: null, birthDate: null},
    	      countries: {name: null, population: null, size: null}
    	    }}
          }); 
        CodeMirror.commands.autocomplete = function(cm) {};
        editor.on("blur", function() {editor.save();});         
 	
 		
 }); 
	  //Func to remove newly added text areas
$(document).on('click', '.remove',function(event) {	 
	
		if(counter <= 1 && ($('textarea[name=newQuery]').size())>1){
			$('#matchAll').hide();
			counter = $('textarea[name=newQuery]').size();
		}
		else if(counter==1){ 
              alert("No more query boxes to remove. Atleast one correct query is necessary.");
              $('#matchAll').hide();
              return false;
           } 		
            $("#TextAreaDiv" + counter).remove(); 
        	counter--;
        	if(prevId>0){
        		prevId--;
        	}
        	if(counter <= 1){
        		 $('#matchAll').hide();
 
        	}
}); 
	  //Func to remove the query - the text box that was already added
$(document).on('click', '.deleteQuery',function(event) {
	 var $this = $(this); 
	 var query_id = this.id; 
	 var questId= $('#qsId').val();
	 var assignId = $('#assgnID').val();
	 alert("Warning - you are deleting an existing query. The page will be refreshed and any unsaved data will be lost.\n Are you sure you want to continue? ");
	 location.href = "deleteQuery.jsp?queryIdToDelete="+this.id+"&assignment_id="+assignId+"&question_id="+questId; 
});
  
function onSubmit(btn){
	var s=false;
	 $('.textForSQL').each(function(){ 
		 var a = editor.getValue();

		 if ((a == null || a == "" || a == " " || a.length == 0) &&
			 ($(this).val() == null || $(this).val() == "" || $(this).val() == " " ||  $(this).val().length == 0)) {
	    	 alert("Please enter the SQL answer.");
	          s=true;	 		
	       } 
		 
	    });
	 if(s == true){ 
		 e.preventDefault();
		 return false;
	 } 
	 else{
		 
		 document.forms["assignmentEditForm"].submit();
		 } 
	 } 
</script>
<style>
.questionelement .answer label{
font-family: Lucida Grande,Tahoma,Arial,Verdana,sans-serif;
font-size: 14px;
font-style: normal;
}
.questionelement .description label{
font-family: Lucida Grande,Tahoma,Arial,Verdana,sans-serif;
font-size: 14px;
font-style: normal;
}
.fieldset #loadDefaultDataSets{
	height:80px;
	width:120%;	
}
.fieldset  #loadDefaultDataSets input{
	width: 3%;
	height: 20px;
	margin-left: 0%;	
}
.fieldset #loadDefaultDataSets label{
	float:left;
	height: 20px;
	width: 89%;
}
.fieldset #loadDefaultDataSets label#dsname{
	width:20%;
	margin-left: 4%;
	position: absolute;
	padding-top:5px;
}
.fieldset #loadDefaultDataSets div#dSet{
	height:20px;
	width:52%;
	float:left;
}
#breadcrumbs
{
  position: absolute;
  padding-left:10px;
  padding-right:10px;
  left: 5px;
  top: 5px;
  font: 13px/13px Arial, Helvetica, sans-serif;
  background-color: #f0f0f0;
  font-weight: bold;
}

</style> 
</head>
<body>
<%
if (session.getAttribute("LOGIN_USER") == null) {
	response.sendRedirect("index.jsp?TimeOut=true");
	return;
}

if(! Boolean.parseBoolean(session.getAttribute("ltiIntegration").toString())){
		%> 
<div id="breadcrumbs"> 
  <a style='color:#353275;text-decoration: none;' href="CourseHome.jsp" target="_top">Home</a> &nbsp; >> &nbsp;
   <a href="InstructorHome.jsp?contextLabel=<%=(String) request.getSession().getAttribute("context_label")%>" style='color:#353275;text-decoration: none;' target="_top"><%=(String) request.getSession().getAttribute("context_label")%></a>&nbsp; >> &nbsp;
   <a href="ListAllAssignments.jsp" style='color:#353275;text-decoration: none;' target="_self">Assignment List</a>&nbsp; >> &nbsp;
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>&showQuestions=true" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
   <!-- <a href="ListOfQuestions.jsp?AssignmentID=<%//=request.getParameter("AssignmentID") %>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp;-->
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Edit Question</a>
 
  </div> 
<%}else{ %>
<div id="breadcrumbs"> 
   <a href="asgnmentList.jsp?assignmentId=<%=request.getParameter("AssignmentID") %>" target="_self" style='color:#353275;text-decoration: none;'>Assignment Details</a>&nbsp; >> &nbsp;    
 <!--   <a href="ListOfQuestions.jsp?AssignmentID=<%//=request.getParameter("AssignmentID") %>" style='color:#353275;text-decoration: none;'>Question List</a>&nbsp; >> &nbsp; -->
   <a href="#" style='color:#0E0E0E;text-decoration: none;font-weight: normal;'>Edit question</a>
 
 </div> 
<%} %>
<br/>
	<div>
		<form id="assignmentEditForm" name="assignmentEditForm"
			action="UpdateSingleQuery" method="post">
	
		<div class="fieldset">
			<fieldset>
				<legend> Assignment Details and Instructions</legend>
				<%
					int assignID =  Integer.parseInt(request.getParameter("AssignmentID"));
					String assignmentName = request.getParameter("assignmentName");
					String courseID = (String) request.getSession().getAttribute("context_label");
					int query_id =0;
					String instructions = "";//(new CommonFunctions()).getAssignmentInstructions(courseID, assignID);
					instructions += "<p></p><label><b>Assignment id: </b></label> <b><label style='color:#657B9E'>" + assignID+"</b></label>";
					instructions += "<p></p><label><b>Assignment Name: </b></label> <b><label style='color:#657B9E'>" +assignmentName +"</b></label>";
					out.println(instructions);					
					String defaultDataIdsForAssignment = "";
				%>
					</fieldset>			
			<br/>			
			<fieldset>
				<legend> Question Details</legend>
				
				<%/*Display optional schema id drop down */
				Connection dbcon = (new DatabaseConnection()).dbConnection();
				PreparedStatement stmt,stmt1;
				String output = "", instr = ""; 
				int defaultID = 0;
				try{    instr = "<div style='height: 25px'><div style='float:left'>"; 
						instr += "<label>Schema for this question:</label>";
						stmt = dbcon 
								.prepareStatement("SELECT schema_id,schema_name FROM xdata_schemainfo WHERE course_id = ?");
						stmt.setString(1, courseID);
						ResultSet rs = stmt.executeQuery();
						stmt1 = dbcon.prepareStatement("Select defaultschemaid,defaultdsetid,assignmentname from xdata_assignment where assignment_id = ? and course_id=?");
						stmt1.setInt(1,assignID);
						stmt1.setString(2,courseID);
						ResultSet rs1 = stmt1.executeQuery();
						rs1.next();
						defaultID = rs1.getInt("defaultschemaid");						
						defaultDataIdsForAssignment = rs1.getString("defaultdsetid");
						output+="<option value=\"select\"> Select schema</option>";
						while (rs.next()) { 
							if(defaultID == rs.getInt("schema_id")){
									output += "<option value =\""+defaultID+"\" selected>"+"Default  ("+rs.getString("schema_name") +")"+"</option>";
							}
							else if (defaultID != rs.getInt("schema_id")){
							
								output += " <option value = \"" + rs.getInt("schema_id")
										+ "\"  > " + rs.getInt("schema_id") + "-"
										+ rs.getString("schema_name") + " </option> ";
							}
							
						}
						//instr += ("  <select class=\"optionalschemaid\" id=\""+courseID+"\" name=\"optionalschemaid\" style=\"clear:both;\"> "
							//	+ output + "</select><p></p>" );
						instr += ("  <select id=\"optionalschemaid\" class=\""+courseID+"\" name=\"optionalschemaid\" style=\"clear:both;\"> "
								+ output + "</select><p></p></div>" );
						
						
						//instr +="<p></p>";
						
						out.println(instr); 
						
						
// 						instr += "</div><div style='float: left; width: 400px'><label style='float:left; margin-left: 10px;'>Marks:</label><output style='float: left; width: 20px;'>"
// 								 +"</output><div style='float: left; width: 300px; margin-left: 10px;  padding-top: 3px;'><input name='maxMarks' type='range' min='0' max='100' value='100' data-rangeslider-main></div></div></div>";
 						
						
					} catch (Exception err) { 
		
						err.printStackTrace();
						throw new ServletException(err);						
					}				
					/* Display existing question and SQL */
					String questionID = (String) request.getParameter("questionId");
					int q_id=Integer.parseInt(questionID);
					//get connection		
					
					Hashtable existingQueries=new Hashtable();					
					//get query details
					try {
						 
						
						 
						stmt = dbcon
								.prepareStatement("SELECT * FROM xdata_qinfo  where assignment_id=? and course_id=? and question_id=?");
						stmt.setInt(1, assignID);
						stmt.setString(2, courseID.trim());
						stmt.setInt(3, q_id);
						System.out.println("QId :" + questionID + "##");
						System.out.println("Course Id: " + courseID);
						System.out.println("AssId :" + assignID);
						System.out.println(stmt);
						instr = "";
						ResultSet rs = stmt.executeQuery();
							if (rs.next()) {
								String query = rs.getString("correctquery");
								String description = rs.getString("querytext");
								int maxMarks = rs.getInt("totalmarks");
								instr += "<div style='float: left; width: 400px'><label style='float:left; margin-left: 10px;'>Marks:</label><output style='float: left; width: 20px;'>"
										 +"</output><div style='float: left; width: 300px; margin-left: 10px;  '><input name='maxMarks' type='range' min='0' max='100' value='" + maxMarks + "' data-rangeslider-main>"
											+"</div></div></div>";
		
								out.println(instr); 
								instr = "";
								String value =rs.getString("default_sampledataid"); 
								instr +="<div><div id=\"loadDefaultDataSets\" style='display:none;'><p></p>";
								PreparedStatement stmt2 = dbcon
										.prepareStatement("SELECT * from xdata_sampledata where course_id=? and schema_id=?");
										stmt2.setString(1, courseID);
										stmt2.setInt(2,defaultID);
										
										ResultSet rs2= stmt2.executeQuery();
										String sampleDataName = "";
										int sampleDataId = 0;
										if(value == null || value.isEmpty() || value != null && value.equalsIgnoreCase("null")){
											value = defaultDataIdsForAssignment;
										}
										if(value != null){
											Gson gson = new Gson();
											Type listType = new TypeToken<String[]>() {}.getType();
						                    String[] dsList = new Gson().fromJson(value, listType);
						                    
						                    if(dsList != null && dsList.length != 0 ){
						                    	 instr += "<p></p><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p><br/>"; 
								                for(int i=0;i<dsList.length;i++){
						                    		while(rs2.next()){
						                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
						                    				sampleDataName = rs2.getString("sample_data_name");
						                    				instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" checked value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
						                    				instr+="</div><br/>";
						                    				break;
						                    				//continue;
						                    			} 
						                    			else{
						                    				instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
						                    				instr+="</div><br/>";
						                    			}
						                    		}
						                    	}
						                    }
						                    else{
												instr += "<br/><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p>"; 
												
												while(rs2.next()){
													instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
													instr+="</div>";
						        					}}
						                	
						                    instr += "<p></p></div></div>";
									}  else{
										instr += "<br/><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p>"; 
										
										while(rs2.next()){
											instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
											instr+="</div>";
				        					}
										instr += "<p></p></div></div>";	
									}	 //instr += "<div><div id=\"loadDefaultDataSets\" style='display:none;'></div></div></div>";
										out.println(instr); 
										rs2.close();
										instr = "";
								
						%> 
						
							
						 <script>document.getElementById("optionalschemaid").value = <%=rs.getInt("optionalschemaid")%>;
						</script>
						
								<div class="questionelement">
								<input type ="hidden" name="question_id" id="qsId" value=<%=qID %> >
								<input type ="hidden" name="assignment_id" id="assgnID" value=<%=assignID %> >
								<input type="hidden" name="desc" value=<%=description %>>
								<input type="hidden" name="assignmentName" value=<%=assignmentName %>>
								
									<div class="question">
									<span>Qustion Id: <%= qID %>. </span>
									<%= description %>
									</div>
								 
									<div style='height: 80px'>
										<div class="description" style='padding:5px 5px 5px 10px;float:left;width:100%;'>
										<label>Question text:</label> 
						 					<textarea style="padding:5px;width:99%; height:80px;" name ='quesTxt' id = 'quesTxt<%= qID%>'><%= description %></textarea>
										</div>
									</div>
									<p></p>
								<% 
								//out.println(assignID +" -- "+ qID +"---"+courseID )							 
									stmt = dbcon
										.prepareStatement("SELECT * FROM xdata_instructor_query  where assignment_id=? and course_id=? and question_id=? order by query_id");
									stmt.setInt(1, assignID); 
									stmt.setString(2, courseID.trim());
									stmt.setInt(3, q_id);
									
									rs2 = stmt.executeQuery();
									int serialNumber = 1;
							        while(rs2.next()){
							        	
							        	newQueryID++;
							        	query = rs2.getString("sql");
							        	query_id = rs2.getInt("query_id");
							        	//Set the partialMarkInfo details into session - if no changes are made,
							        	//the session values will be stored
							        	Gson gson = new Gson();
										Type listType = new TypeToken<PartialMarkParameters>() {}.getType();
				                    	PartialMarkParameters partialMarks = new Gson().fromJson(rs2.getString("partialmarkinfo"), listType);
				                   
							        	session.setAttribute(assignID+"_"+q_id+"_"+query_id,partialMarks);
							        	int marks = rs2.getInt("marks");
							        	//For processing edited query
							        	existingQueries.put(query_id,query);
						 			    //Display all SQL answers for that question
								%> <br>
								
							<div class="answer">
							<!-- style='padding:10px 5px 5px 5px;border-top: 1px solid #3B5998;float:left; width:50%;' -->
							<div style="height: 25px; width:100%">
								<label style="float: left">SQL answer <%=serialNumber %>:</label>
								<div style='float: left; width: 400px'><label style='float:left; margin-left: 10px;'>Marks:</label>
									 <output style='float: left; width: 20px;'></output>
									 <div style='float: left; width: 300px; margin-left: 10px;'>
									 	<input name = 'existingMarks' type='range' min='0' max='<%=maxMarks%>' value='<%=marks%>' data-rangeslider-sub1>	 	
									 </div>
								</div>

								<b><a data-toggle="modal" data-target="#PartialParamModal<%=query_id %>"
		 							href="PartialMarkingParamsPerInstrQuery?assignment_id=<%=assignID%>&question_id=<%=q_id %>&query_id=<%=query_id%>">View/Edit partial marking parameters</a></b></td>
									</div>		
							 
							<textarea name='query' class="textForSQL"
								id='query <%=qID +" " + query_id %>'><%=query %>
							</textarea>
								<input type='hidden' name='queryIdsEdited' value=<%=query_id %>>
							   
							
	  						<%if(query_id>1){ %>
	  							 <input type='button' class='deleteQuery' name='delete' id='<%=query_id %>' value="Delete">
							<%} 
								serialNumber++;%>
								
								<!-- Modal to show Partial Marking Parameters Start -->
					<div class="modal fade" id="PartialParamModal<%=query_id%>" tabindex="-1" role="dialog" aria-labelledby="PartialParamModal" aria-hidden="true">
			   			 <div class="modal-dialog">
				   			  <div class="modal-content">
							     <div class="modal-header"></div>
							            <div class="modal-body"></div>         
						    </div>
						<!-- <div class="modal-footer"><br><button type="button" class="btn btn-default" data-dismiss="modal">Set</button></div></br> -->
					  </div>
					  </div>
		  <!-- Modal to show Partial Marking Parameters End -->
		  
							<%}
				       		 session.setAttribute("existingSQL",existingQueries);
							%>									 
								<%			  	
					}						 		
					else{
						stmt = dbcon.prepareStatement("SELECT MAX(query_id) from xdata_instructor_query where course_id=? and assignment_id=? and question_id=?");
						stmt.setString(1,courseID);
						stmt.setInt(2,assignID);
						stmt.setInt(3,q_id);
						ResultSet rs1 = stmt.executeQuery();
						if(rs1.next()){
							newQueryID = (rs1.getInt(1));
						}
						
						String query = "";
						String description = ""; 
						newQueryID=1;
						instr += "</div><div style='float: left; width: 400px'>"
						+"<label style='float:left; margin-left: 10px;'>Marks:</label>"
						+"<output style='float: left; width: 20px;'>"
						 +"</output><div style='float: left;width:300px;margin-left:10px;padding-top: 3px;'>"
						+"<input name='maxMarks' type='range' min='0' max='100' value='100' data-rangeslider-main></div></div></div>";
						out.println(instr); 
						PreparedStatement stmt2 = dbcon
								.prepareStatement("SELECT * from xdata_sampledata where course_id=? and schema_id=?");
								stmt2.setString(1, courseID);
								stmt2.setInt(2,defaultID);
								boolean flag = false;
								ResultSet rs2= stmt2.executeQuery();
								instr ="<div><div id=\"loadDefaultDataSets\" style='display:none;'>";
								
								if(defaultDataIdsForAssignment != null && !defaultDataIdsForAssignment.isEmpty()){
									Gson gson = new Gson();
									Type listType = new TypeToken<String[]>() {}.getType();
				                    String[] dsList = new Gson().fromJson(defaultDataIdsForAssignment, listType);
				                   
				                    if(dsList != null && dsList.length != 0 ){
				                    	 instr += "<p></p><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p>"; 
						                for(int i=0;i<dsList.length;i++){
				                    		while(rs2.next()){
				                    			if(rs2.getString("sampledata_id").equalsIgnoreCase(dsList[i])){
				                    				String sampleDataName = rs2.getString("sample_data_name");
				                    				instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" checked value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
				                    				instr+="</div><br/>";
				                    				//break;
				                    				//continue;
				                    			}
				                    			else{
				                    				instr += "<div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
				                    				instr+="</div><br/>";
				                    			}
				                    		}
				                    	}
				                    }
				                    else{
										instr += "<br/><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p>"; 
										
										while(rs2.next()){
											instr += "<br/><div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
											instr+="</div><br/>";
				        					}
				                    }
				                    instr += "<p></p></div></div>";
							}  else{
								instr += "<br/><label style='float:left;'>Please select the default datasets for evaluation.</label><p></p>"; 
								
								while(rs2.next()){
									instr += "<br/><div id='dSet'><input type=\"checkbox\" name=\"defaultDSId\" value=\""+rs2.getString("sampledata_id")+"\"/><label id=\"dsname\">"+rs2.getString("sample_data_name")+"</label>";
									instr+="</div><br/>";
		        					}
								instr += "<p></p></div></div>";
							}
                		instr += "</div>";
						out.println(instr); 
	%>  					<!-- <div>
								<div id="loadDefaultDataSets" style='display:none;'><p></p>
								</div> 
							</div>-->
						<div class="questionelement">
								<input type ="hidden" name="question_id" value=<%=qID %> >
								<input type ="hidden" name="assignment_id" value=<%=assignID %> >
							 	
							 	
							<div class="question"><span>Question Id:<%= qID %>. </span><%= description %></div>
							<div style='height: 80px'>
								<div class="description" style='padding:5px 5px 5px 10px;float:left;width:98%;'>
								<label>Question text:</label> 
										<textarea style="padding:5px;width:99%; height:80px;" name ='quesTxt' id = 'quesTxt<%= qID%>'><%= description %></textarea>
							</div> 		 					
							</div>	
							<p></p><br>						  
								<div class="answer"> 
								<div style="height: 25px; width:100%">
								<label style="float: left">SQL answer:</label>
								<div style='float: left; width: 400px'><label style='float:left; margin-left: 10px;'>Marks:</label>
									 <output style='float: left; width: 20px;'></output>
									 <div style='float: left; width: 300px; margin-left: 10px; '>
									 	<input name = 'newMarks' type='range' min='0' max='100' value='100' data-rangeslider-sub1>
									 	
									 </div>
								</div>
								<b><a  data-toggle="modal" data-target="#PartialParamModal<%=newQueryID%>"
		 							href="PartialMarkingParamsPerInstrQuery?assignment_id=<%=assignID%>&question_id=<%=q_id %>&query_id=<%=query_id%>">View/Edit partial marking parameters</a></b></td>
								</div>
								
								
								 
									<textarea name='newQuery' class="textForSQL" id='query <%=qID +" " + newQueryID %>'><%=query %></textarea>
									<input type='hidden' name='newQueries' value='javascript:editor.getValue();'>
								
								<!-- Modal to show Partial Marking Parameters Start -->
					<div class="modal fade" id="PartialParamModal<%=newQueryID%>" tabindex="-1" role="dialog" aria-labelledby="PartialParamModal" aria-hidden="true">
			   			 <div class="modal-dialog">
				   			  <div class="modal-content">
							     <div class="modal-header"></div>
							            <div class="modal-body"></div>         
						    </div>
					  </div>
					  </div>
		  <!-- Modal to show Partial Marking Parameters End -->
		  								
						<%
			 		}
						rs.close(); 
					%><br/>
						<div id="dynamicAdd" style='float:center;height:auto;'> 
						</div>
						
						<div  style='float:center' id="matchAll" class="matchAll" style='display:none;'> 
							<input type="radio" name="matchAll" value="matchOne" checked>Match results of any one
							<input type="radio" name="matchAll" value="matchAll">Match results of all 
							</div><br/>
							
						<input type="button" class="queryBox" id="<%=q_id%>" name="<%=newQueryID%>" value="Add Another Query" >	
					  </div>  
					</div>
					
		</div>
		
					</div>
					<%					
					} 
					catch (Exception err) {
						err.printStackTrace(); 
						out.println("Error in retrieving question details");
						throw new ServletException(err);
					}
					finally{
						dbcon.close();
					}
				%>		
				<div class="editbutton"> 
						<input type="button" onclick="onSubmit(this)"  value="Update Query" name="update" id="button <%= qID + " "+ assignID%>"/>
						<input type="button" onclick="window.location.href = 'asgnmentList.jsp?assignmentId=<%=assignID %>&showQuestions=true'" value="Cancel" name="cancel" id="cancelButton"/>
				</div>
				 </div>
				</div> 
			</fieldset>
		</div>
		</form>
	</div>

</body>
</html>