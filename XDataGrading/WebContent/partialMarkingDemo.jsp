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
<script type="text/javascript" src="scripts/jquery-1.7.2.js"></script>
<link rel="stylesheet" href="scripts/codemirror/lib/codemirror.css" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
 <!--  <script type="text/javascript"  src = "scripts/jquery.js"></script>--> 
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


function getParameterByName(name) { 		
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
} 

$( document ).ready(function() {
	
//Function to add a new query text area for adding new queries
$(document).on('click', '.queryBox' ,function (event) { 
	//alert("Onclick Query Box");
	 var $this = $(this);
	 
	 idname = parseInt(this.name);
	 boxname = boxname+1;
	/* if(idname == prevId) { 
		 //alert("in if");
		 boxname = boxname+1;
	 }
	 else{
		 boxname=idname;
	 }*/
	 //alert(name);
	  counter = counter+1;
	 var correctId=parseInt(this.id);
	 var txtBoxId = "query " +correctId +" "+boxname;
	 var txt = "'query " + correctId+" "+boxname+"'";
			
	  var newTextAreaDiv = $(document.createElement('div'))
	     .attr("id", 'TextAreaDiv' + counter);
	  	newTextAreaDiv.attr("class","answer");                                                                    
	    
	  	var htmlString = "<label style='float: left;height: 30px; width:100%;'>Instructor Query:"+counter+"</label><br/>"
	  //	"<div style='height: 30px; width:100%;position:relative;'><label style='float: left'>Instructor Query:"+boxname+"</label></div> "
	  	+'<textarea  style="padding:5px;width:98%; height:200px;" class="textForSQL" name="newQuery" id="'+txtBoxId+'"></textarea>'
		+ '<br/><input type="button" class="remove" id="remove" name="" value="Delete">';
	   // + '<textarea" style="padding:5px;width:98%; height:200px;" id="'+txtBoxId+'" class="textForSQL" name="newQuery" ></textarea> ';
		//+ '<br/><div style="position:relative;"><input type="button" class="remove" id="remove" name="remove" value="Delete"></div>';
		 //+'<input type="hidden" name="newQueries" value="javascript:editor.getValue();">';
	  	 
		newTextAreaDiv.after().html(htmlString);
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
  
$(document).on('click','#getPartialMarks',function (event) {
	
	event.preventDefault();
	//var dataString=this.id;
  /*  $('#label_0').hide();
    $('#label_1').hide();
    $('#label_2').hide();
    $('#label_3').hide();
    $('#label_4').hide();
    $('#label_5').hide();
    $('#label_6').hide();
    $('#label_7').hide();*/
    //$('#showCanonicalizationResult').hide();
    //$('#canonicalizeSteps').hide();
    
   // $('#withCanonicalize').hide();
	//$('#withoutCanonicalize').hide();
    var isCanonicalized;
    if ($('#canonicalize').is(":checked")){
    	isCanonicalized = "canonicalize"
    }
    var ed = $('.CodeMirror')[0].CodeMirror;
    var query;
    //Get all codemirror values, separate with #&# and pass it to servlet for processing. 
    //This works but gets only student query
    /*$('.CodeMirror').each(function (i,el){
    	 var ed = $('.CodeMirror')[1].CodeMirror;
    	query = el.CodeMirror.getValue() +'#&#';
    });*/
    query = ed.getValue() + '#@###@#';
    
    var codeMirrorValues = document.getElementsByName("newQuery");
    //alert("codeMirrorValues.length = "+codeMirrorValues.length);
    var edt;
    for(var i=0;i<codeMirrorValues.length;i++){
    	edt= codeMirrorValues[i].value;
    	query += (edt+'#@###@#');
    	//alert("i = "+edt);
    }
  	//alert("query ="+ query);
    var dataString = "instructorQuery="+ query +'&&studentQuery='+document.getElementById("textarea-1").value+
    '&&canonicalize='+isCanonicalized;
   //alert("dataString ="+ dataString);
	var index = this.name;
	var self = this; 
	$.ajax({ 
        type: "POST",  
        url: "PartialMarkingDemo",
        data: dataString,
        context:this,  
        /* beforeSend : function() {
        	//$('#canonicalizeSteps').show();
        	/*$(function () {
						var counter = 8,
        		        divs = $('#label_0, #label_1, #label_2,#label_3,#label_4,#label_5,#label_6,#label_7');
        		    
        		    function showDiv () {
        		    	
        		        divs. // hide all divs
        		            filter(function (index) { return index == 8-counter; }) // figure out correct div to show
        		           .show('fast'); // and show it

        		        counter--;
        		    }; // function to loop through divs and show correct div

        		    showDiv(); // show first div    

        		    setInterval(function () {
        		        showDiv(); // show next div
        		    },100);
        		});
        	}, */ 
         
	        success: function(data) { 
	        	try{	
	        		//Get the html content and display it in a div
	        		$('#showCanonicalizationResult').html( data );
	        		$('html,body').animate({ scrollTop: $("#showCanonicalizationResult").offset().top-10});	
				} 
        		catch(err)
        		{	    	
        			if(xhr.status ===88){
    	        		alert("Error: Syntax Error with student Query.");
    	        	}
    	        	else if(xhr.status ===89){
    	        		alert("Error: Syntax Error with instructor Query.");
    	        	}
    	        	else{
    	        	alert("Internal Server Error while computing partial mark.");
    	        	}       			
        		} 	        	
	        }, 
	        error : function(xhr, ajaxOptions, thrownError){
	        	if(xhr.status ===88){
	        		alert("Error: Syntax Error with student Query.");
	        	}
	        	else if(xhr.status ===89){
	        		alert("Error: Syntax Error with instructor Query.");
	        	}
	        	else{
	        	alert("Internal Server Error while computing partial mark.");
	        	} 	 
            }
	      }); 
	      return false; 
});

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

$(document).ready(function() {
	$('.nav-tabs > li > a').click(function(event){
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
<br/>
<div>		<div class="fieldset">	
			<fieldset>
			<legend>Partial Marking Analysis</legend>
			
			<label><b>Instructor Query:</b></label>
			<textarea name='query' class="textForSQL"
								id='query1'>
			</textarea>
							<br/>
		<input type="button" class="queryBox" id="1" name="1" value="Add Instructor Query"/>  
			<input type='hidden' name='instructorQuery' id='instructorQuery'  value='javascript:editor.getValue();'/>
							<br/>
							<br/>
			<div id="dynamicAdd"  class='dynamicAddDiv'></div>
						<br/>
				<div id='studentquery' style='position:relative;'>	
			<label><b>Student Query:</b></label>
			<textarea name='studentquery' class="textForSQL"
								id='query'>
			</textarea>
			<input type='hidden' name='studentquery' id='studentquery' value='javascript:editor.getValue();'>
							<br/>		
				</div>
			<div id='dSet'>
			<!-- <input type="checkbox" name="canonicalize"  id="canonicalize" value="canonicalize" checked> 
			<label>Canonicalize</label> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-->
			
			<input type="button" align="left" id="getPartialMarks" value="Compute Partial Marks"></div>
			<br/>

<!--<div class="fieldset" id="canonicalizeSteps" style='display:none;'> -->
<!--<fieldset>-->
<!--<label id="label_0" style="display: none;">Canonicalizing queries...</label><br/> -->
<!--<label id="label_1"  style="display: none">Normalizing selection conditions (eg. (3>A.B)=>(A.B &lt 2)) ..</label><br/>-->
<!--<label id="label_2" style="display: none">Normalizing join conditions (eg. (C.D>A.B)=>(A.B &lt C.D)) ..  </label><br/>-->
<!--<label id="label_3" style="display: none">Normalizing selection having conditions (eg. (3>A.B)=>(A.B <=2)) ..</label><br/>-->
<!--<label id="label_4" style="display: none">Normalizing join having conditions (eg. C.D>A.B)=>(A.B &lt C.D))..</label><br/>-->
<!--<label id="label_5"  style="display: none">Outer join minimization, Canonicalizing order/group by </label><br/>-->
<!--<label id="label_6"  style="display: none">Removing redundant tables.., Removing redundant distincts..</label><br/>-->
<!--<label id="label_7"  style="display: none">Canonicalization Completed.</label><br/>-->

<!--</fieldset></div>	-->


<div id="showCanonicalizationResult"></div>	
</div>	
			
</fieldset>			
</div>

</body>
</html>