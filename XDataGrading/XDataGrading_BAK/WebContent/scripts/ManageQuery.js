function addRow0(tableID) { 
	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;
	var row = table.insertRow(rowCount);
	row.id = rowCount;
	var cell1 = row.insertCell(0);
	var qid = rowCount;

	var element1 = document.createElement("p");
	element1.name = "qID" ;
	element1.innerHTML = "Question: ".concat(rowCount); 
	element1.id = qid;
	element1.setAttribute('readOnly','readonly');
	cell1.appendChild(element1);


	var cell2 = row.insertCell(1); 
	var element2 = document.createElement('textarea'); 
	element2.type="textarea" ;
	element2.name= "quesTxt".concat(qid) ;
	element2.id= "quesTxt".concat(qid) ;
	element2.rows= "6 " ;
	element2.cols= "30 " ;
	cell2.appendChild(element2);


	var cell3 = row.insertCell(2);
	var element3 = document.createElement('textarea'); 
	element3.type="textarea" ;
	element3.name="query" ;
	var quer="query".concat(rowCount);
	element3.id=quer; 
	element3.rows="6" ;
	element3.cols="30" ;
	cell3.appendChild(element3);

	var cell4 = row.insertCell(3); 
	var buttonnode= document.createElement('input');
	buttonnode.setAttribute('type','button');
	var name = "button ".concat(qid).concat(" 8");/**FIXME: add assignment id*/
	buttonnode.setAttribute('name',name);
	buttonnode.setAttribute('value','Update Query');
	buttonnode.setAttribute('id',"button ".concat(rowCount));
	buttonnode.setAttribute('onclick','report(this,1)' );
	cell4.appendChild(buttonnode);
}

function report(btn,selected) {
	

	//alert(btn.name.split(" "));
	var qid = btn.name.split(" ")[1];
	var asID = btn.name.split(" ")[2];
	var quer = "query ".concat(qid).concat(" ").concat('1');
	
	if(window.editor != undefined){
		document.getElementById(quer).innerHTML = window.editor.getValue();
	}
	
	var que = document.getElementById(quer).value;
	var desc = document.getElementById("quesTxt".concat(qid)).value; 
	var schemaId = document.getElementById("optionalschemaid").value;
	
	if (selected == "1") {
		var out = "UpdateSingleQuery.jsp?assignment_id=" + encodeURIComponent(asID)
		+ "&question_id=" + encodeURIComponent(qid) + "&query=" + encodeURIComponent(que) + "&desc=" + encodeURIComponent(desc)+"&optschemaid="+encodeURIComponent(schemaId);
		window.location.href = out;
	} else if (selected == "2") {
		var out = "AssignmentChecker?assignment_id=" + encodeURIComponent(asID)
		+ "&question_id=" + encodeURIComponent(qid) + "&query=" + encodeURIComponent(que);
		window.open(out, "Generating Dataset", "height=400,width=400 ");
	} else if (selected == 3) {
		var out = "ListOfQuestions.jsp?AssignmentID=" + encodeURIComponent(asID);
		window.location.href = out;
	} 

}

function submitter(btn,tableID){
	var table = document.getElementById(tableID);
	var num=btn.id.split(" ")[1]
	var quer="query".concat(num);
	var Qid="qID".concat(num);
	var quer="query".concat(num);
	var num1=btn.id.split(" ")[2]
	var asID=num1;
	var que= document.getElementById(quer).value; 
	var qid=document.getElementById(Qid).value;
	var out = "AssignmentChecker?assignment_id="+encodeURIComponent(asID) 
	+ "&question_id="+ encodeURIComponent(qid)+"&query="+ encodeURIComponent(que); 
	window.open( out,"Generating Dataset","height=400,width=400 ");
}




function getColumnMax(tableId, columnIndex) {
	var table = document.getElementById(tableId);
	var max = 1;
	for(var i = 2; i <= Number.MAX_VALUE; i++){
		var list = document.getElementsByClassName(i);
		if(list.length == 0) break;
		max = i;
	}
	return [max];	
}




//add a new row to table
function addRow(assignId, tableID) { 

	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;
	var row = table.insertRow(rowCount);
	var ids = getColumnMax(tableID, 0) ;
	var id = ids[0] + parseInt(1); 
	row.id = id;
	row.className = id; 
	
	
	
	var cell1 = row.insertCell(0);
	var element1 = document.createElement("p");	
	element1.name="qID" ;	
	element1.innerHTML = "Question: ".concat(id); 
	var qid="qID".concat(id);
	element1.id=qid; 
	element1.setAttribute('readOnly','readonly');
	cell1.appendChild(element1); 

	var cell2 = row.insertCell(1); 
	var element2 = document.createElement('textarea');
	element2.type="textarea" ;
	element2.name="quesTxt".concat(id) ;
	element2.id="quesTxt".concat(id) ;
	element2.rows="6" ;
	element2.cols="35" ;
	cell2.appendChild(element2); 
	
//	add third cell
	var cell3 = row.insertCell(2); 
	var element3 = document.createElement('textarea');
	element3.type="textarea" ;	
	var quer="query ".concat(id).concat(" 1");
	element3.id = quer; 
	element3.className = quer; 
	element3.name= "query".concat(id) ;
	element3.rows="6" ;
	element3.cols="35" ;
	cell3.appendChild(element3); 
//	cell2.innerHTML =<textarea name="quesTxt" rows="6" cols="57" ></textarea>; 


	

	var cell5 = row.insertCell(3); 
	var buttonnode= document.createElement('input');
	buttonnode.setAttribute('type','button');
	var name = "button ".concat(id).concat(" ").concat(assignId);/**FIXME: add assignment id*/
	buttonnode.setAttribute('name',name);
	buttonnode.setAttribute('value','Update Query');
	buttonnode.setAttribute('id',"button ".concat(rowCount));
	buttonnode.setAttribute('onclick','report(this,1)' );
	cell5.appendChild(buttonnode);
	var br = document.createElement('br');
	cell5.appendChild(br);
	
	var cell4 = row.insertCell(4); 
	var button= document.createElement('input');
	button.setAttribute('type','button');
	var name = "button ".concat(id).concat(" ").concat(assignId);/**FIXME: add assignment id*/
	
	button.setAttribute('name',name);
	button.setAttribute('value','Add More Correct Queries');
	button.setAttribute('id',name);
	//buttonnode.onClick = submitter(this,'queryTable');
	//buttonnode.setAttribue('onclick','submitter(this,"'+tableID+'")' 
	button.setAttribute('onclick','addCorrectQuery(this,"'+tableID+'")' );
	
	
	cell5.appendChild(button);

	/**create a hidden field to store query ids*/
	var field = document.createElement('input');
	field.setAttribute('type','hidden');
	field.setAttribute('name','qID');
	field.setAttribute('value',id);
	cell5.appendChild(field);;
	



}






///Add a new correct query
function addCorrectQuery(btn,tableID){

	var table = document.getElementById(tableID);
	var rowCount = table.rows.length;


	//get maximum row of this query id
//	var ids =  getColumnMax(tableID, 0) ;
//	var rowId = parseInt(ids[1])
//	remove below line and uncomment above two lines after fixing getcolumnmax
	var rowId = parseInt( btn.id.split(" ")[1] );

//	get the number of rows with each id <= rowId
	var len = 0
	for (var i = 1; i <= rowId; i++) {
		var list = document.getElementsByClassName(i);
		len = parseInt(len) + parseInt(list.length);
	}
	var row = table.insertRow( parseInt(len) +1 );
	row.id = rowId; 
	row.className = rowId;
	var cell0 = row.insertCell(0);
	var cell1 = row.insertCell(1);

	var cell2 = row.insertCell(2);    
	var element2 = document.createElement('textarea');
	element2.type="textarea" ;
	element2.id = "query ".concat(rowId).concat(" ").concat( parseInt(document.getElementsByClassName(rowId).length)  ) ;
	var quer="query".concat(rowId);
	element2.className = quer; 
	element2.name = quer ;
	element2.rows="6" ;
	element2.cols="35" ;
	cell2.appendChild(element2); 


	
	var cell3 = row.insertCell(3);   
/*	var buttonnode= document.createElement('input');
	buttonnode.setAttribute('type','button');
	buttonnode.setAttribute('name','generte');
	buttonnode.setAttribute('value','Generate Dataset');
	buttonnode.setAttribute('id',"button ".concat(rowId).concat(" ").concat( parseInt(document.getElementsByClassName(rowId).length) + 1 ) );
	buttonnode.setAttribute('onclick','submitter(this,"'+tableID+'")' );
	cell3.appendChild(buttonnode);*/

	var cell4 = row.insertCell(4);
}




