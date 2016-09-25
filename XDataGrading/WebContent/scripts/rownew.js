function addRow(tableID) { 
var table = document.getElementById(tableID);
var rowCount = table.rows.length;
var row = table.insertRow(rowCount);
var cell1 = row.insertCell(0);
var element1 = document.createElement("input"); 
element1.type="number" ;
element1.name="qID" ;
element1.size="4" ;
element1.value=rowCount ;
element1.setAttribute('readOnly','readonly'); 
cell1.appendChild(element1); 
var cell2 = row.insertCell(1); 
var element2 = document.createElement("textarea"); 
element2.type="textarea" ;
element2.name="quesTxt" ;
element2.rows="6" ;
element2.cols="57" ;
cell2.appendChild(element2); 
var cell3 = row.insertCell(2); 
var element3 = document.createElement("textarea"); 
element3.type="textarea" ;
element3.name="query" ;
element3.rows="6" ;
element3.cols="57" ;
cell3.appendChild(element3); 
var cell4 = row.insertCell(3); 
var element4 = document.createElement('a'); 
var span = document.createElement('span');
span.style.fontSize = '20px';
span.style.fontFamily = 'arial';
span.style.backgroundColor = 'white';
var linkText = document.createTextNode("Generate Dataset");
span.appendChild(linkText)
element4.appendChild(span);
element4.href = "AssignmentChecker.jsp?assignment_id=6&ques_id="+rowCount;
cell4.appendChild(element4); 
}
