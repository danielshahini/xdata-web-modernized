
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import database.*;
import testDataGen.PopulateTestDataGrading;
import util.DataSetValue;
import util.DatabaseConnectionDetails;

/**
 * Servlet implementation class TestCaseDataset
 */
// @WebServlet("/TestCaseDataset")
public class TestCaseDataset extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(TestCaseDataset.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public TestCaseDataset() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
		HttpServletResponse response) throws ServletException, IOException {
		String query = request.getParameter("query");
		HttpSession session = request.getSession();
		int assignment_id = Integer.parseInt(request
				.getParameter("assignment_id"));
		String course_Id = (String)session.getAttribute("context_label");
		int question_id = Integer.parseInt(request.getParameter("question_id"));
		// String user_id=request.getParameter("user_id");
		String user_id = (String) request.getSession().getAttribute("user_id");
		String dataset_sel = "select datasetid from xdata_detectdataset where user_id=? and queryid=? and course_id = ?";

		Connection testcon = null;
		try {
			DatabaseConnectionDetails dbConnDetails = (new util.DatabaseConnection()).getTesterConnection(assignment_id);
			testcon = dbConnDetails.getTesterConn();
			
			if (testcon != null) {
				logger.log(Level.FINE,"Connected successfully");
			}
		} catch (Exception ex) {
			logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
			throw new ServletException(ex);
		}
		Connection dbCon = (Connection) session.getAttribute("dbConnection");
		if (dbCon == null) {
			try {
				dbCon = (new DatabaseConnection()).dbConnection();
				if (dbCon != null) {
					logger.log(Level.FINE,"Connected successfullly");
				}
			} catch (Exception ex) {
				logger.log(Level.SEVERE,"SQLException: " + ex.getMessage(),ex);
				throw new ServletException(ex);
			}
		}
		response.setContentType("text/html");
		PrintWriter out_assignment = response.getWriter();
		out_assignment.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\""
						+"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
						+"<html xmlns=\"http://www.w3.org/1999/xhtml\">"
						+"<head>"
						+"<title>"
						+"XData &middot; Assignment"
						+"</title>"
						+"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />"
						+"<script type=\"text/javascript\" src=\"scripts/wufoo.js\"></script>"
						+"<link rel=\"stylesheet\" href=\"css/structure.css\" type=\"text/css\" />"
						+"<link rel=\"stylesheet\" href=\"css/form.css\" type=\"text/css\" />"
						+"<link rel=\"stylesheet\" href=\"css/theme.css\" type=\"text/css\" />"
						+"<link rel=\"canonical\" href=\"http://www.wufoo.com/gallery/designs/template.html\">"
						+"</head>"
						+"<body id=\"public\">"
						+"<div id=\"container\">"
						+"<form class=\"wufoo\" action=\"LoginChecker\" method=\"post\">"
						+"<div class=\"info\">"
						+ "<h2>Question: "
						+ question_id
						+ "</h2>" + "</div>");
		try(PreparedStatement stmt1 = dbCon.prepareStatement(dataset_sel)){
			stmt1.setString(1, user_id);
			stmt1.setString(2, "A" + assignment_id + "Q" + question_id + "S"
					+ 1);
			stmt1.setString(3,course_Id);
			try(ResultSet rs = stmt1.executeQuery()){
				while (rs.next()) {
					String s = null;
					String args[] = { rs.getString("datasetid"),
							"A" + assignment_id + "Q" + question_id + "S" + 1+"CS631" };
					try {
						PopulateTestDataGrading.entry(args);
					} catch (Exception e) {
						logger.log(Level.SEVERE,e.getMessage(),e);
						throw new ServletException(e);
					}
	
					out_assignment.println("<h4>Dataset ID: "
							+ rs.getString("datasetid") + "</h4>");
					out_assignment.println("<p></p>");
					try(PreparedStatement pstmt = dbCon
							.prepareStatement("select value from xdata_datasetvalue where assignmane_id =? and question_id=? and datasetid=? and course_id = ?")){
						pstmt.setInt(1, assignment_id);
						pstmt.setInt(2, question_id);
						pstmt.setString(3, rs.getString("datasetid"));
						pstmt.setString(4,course_Id);
						try(ResultSet rsi = pstmt.executeQuery()){
								rsi.next();
								String value = rsi.getString("value");
								//It holds JSON obj tat has list of Datasetvalue class
								Gson gson = new Gson();
								//ArrayList dsList = gson.fromJson(value,ArrayList.class);
								Type listType = new TypeToken<ArrayList<DataSetValue>>() {
			                    }.getType();
				
								List<DataSetValue> dsList = new Gson().fromJson(value, listType);
								for(int i = 0 ; i < dsList.size();i++ ){
									DataSetValue dsValue = dsList.get(i);
									String tname,values;
									tname = dsValue.getFilename().substring(0,dsValue.getFilename().indexOf(".copy"));
									try(PreparedStatement detailStmt = testcon.prepareStatement("select * from " + tname + " where 1 = 0")){
										ResultSetMetaData columnDetail = detailStmt.executeQuery().getMetaData();
										out_assignment.println("<table border=\"1\">");
										out_assignment.println("<caption>"+tname+"</caption>");
										out_assignment.println("<tr>");
										//Column names get from metadata
										 for(int cl=1; cl <= columnDetail.getColumnCount(); cl++)
										    {
											 out_assignment.println("<th>" + columnDetail.getColumnLabel(cl)+"</th>");
										  }
										 out_assignment.println("</tr>");
										//Get Column values
										for(String dsv: dsValue.getDataForColumn()){
											String columns[]=dsv.split("\\|");
											out_assignment.println("<tr>");
											for(String column: columns)
											{
												out_assignment.println("<td>"+column+"</td>");
											}
											out_assignment.println("</tr>");
										}
										out_assignment.println("</table>");
									}//detailStmt close
								}
							}//rsi close
							}//stmt close
					// out_assignment.println("<h5>Result of executing query</h5>");
					try(PreparedStatement stmt = testcon.prepareStatement(query)){
						try(ResultSet r = stmt.executeQuery()){
							ResultSetMetaData metadata = r.getMetaData();
							int no_of_columns = metadata.getColumnCount();
							out_assignment.println("<p></p>");
							out_assignment.println("<table border=\"1\">");
							out_assignment.println("<caption> Result</caption>");
							out_assignment.println("<tr>");
							for (int cl = 1; cl <= no_of_columns; cl++) {
								out_assignment.println("<th>" + metadata.getColumnLabel(cl)
										+ "</th>");
							}
							out_assignment.println("</tr>");
							while (r.next()) {
								out_assignment.println("<tr>");
								for (int j = 1; j <= no_of_columns; j++) {
									int type = metadata.getColumnType(j);
									if (type == Types.VARCHAR || type == Types.CHAR) {
										out_assignment.println("<td>" + r.getString(j)
												+ "</td>");
									} else {
										out_assignment.println("<td>" + r.getLong(j)
												+ "</td>");
									}
			
								}
								out_assignment.println("</tr>");
							}
						}
					}//stmt ends
					out_assignment.println("</table>");
					out_assignment.println("<hr>");
				}//close resutlset
			}
			out_assignment
					.println("<input type=\"submit\"  name=\"\" value=\"Home page\" />");
			out_assignment.println("</form></div><!-- End Page Content --></body></html>");
			out_assignment.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
		try {
			testcon.close();
			dbCon.close();
		} catch (SQLException e) {
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
