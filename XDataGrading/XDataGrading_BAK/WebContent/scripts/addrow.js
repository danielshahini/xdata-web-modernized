out.println(" <script language=\"javascript\">");
	out.println("function addRow(tableID) { ");
	
	out.println("var table = document.getElementById(tableID);");
    out.println("var rowCount = table.rows.length;");
    out.println("var row = table.insertRow(rowCount);");
    out.println("var cell1 = row.insertCell(0);");
	out.println("var element1 = document.createElement(\"input\"); ");
	out.println("element1.type=\"number\" ;");
	out.println("element1.name=\"qID\" ;");
	out.println("element1.size=\"4\" ;");
	out.println("element1.value=rowCount ; ");
	out.println("cell1.appendChild(element1); ");
	out.println("var cell2 = row.insertCell(1); ");
	out.println("var element2 = document.createElement(\"input\"); ");
	out.println("element2.type=\"textarea\" ;");
	out.println("element2.name=\"quesTxt\" ;");
	out.println("element2.rows=\"6\" ;");
	out.println("element2.cols=\"57\" ;");
	out.println("cell2.appendChild(element2); ");
	//add third cell
	out.println("var cell3 = row.insertCell(2); ");
	out.println("var element3 = document.createElement(\"input\"); ");
	out.println("element3.type=\"textarea\" ;");
	out.println("element3.name=\"query\" ;");
	out.println("element3.rows=\"6\" ;");
	out.println("element3.cols=\"57\" ;");
	out.println("cell3.appendChild(element3); ");
	//out.println("cell2.innerHTML =<textarea name=\"quesTxt\" rows=\"6\" cols=\"57\" ></textarea>; ");
	
	out.println("}");
	out.println("</script>");
