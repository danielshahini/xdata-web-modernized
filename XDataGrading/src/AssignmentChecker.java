
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.CommonFunctions;
import database.DatabaseConnection;
import testDataGen.GenerateDataset_new;
import testDataGen.preProcessForDataGeneration;

//import testDataGen.TestAssignment;

/**
 * Servlet implementation class AssignmentChecker
 */
// @WebServlet("/AssignmentChecker")
public class AssignmentChecker extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(AssignmentChecker.class.getName());
	/**
	 * Default Constructor.
	 * 
	 * @see HttpServlet#HttpServlet()
	 */
	public AssignmentChecker() {
		super();
	}

	/**
	 * This gets the request for generating data sets and invokes the java class
	 * for the same. This method updates the session with objects for which data
	 * generation has started and for which data generation is completed so that
	 * UI is updated accordingly.
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		HttpSession session = request.getSession(false);
		if (session.getAttribute("LOGIN_USER") == null) {
			response.sendRedirect("index.jsp?TimeOut=true");
			return;
		}

		String course_id = (String) session.getAttribute("context_label");
		boolean isStop = Boolean.getBoolean(request.getParameter("stop"));
		if (!isStop) {
			// holds the objects for which the data generation is completed
			List<String> dataGenerationCompleted = null;

			// holds the objects for which data generation is in progress
			Map<String, Long> dataGenStartedObj = null;

			String loginUsr = null;
			int assignment_id = Integer.parseInt(request
					.getParameter("assignment_id"));
			int question_id = Integer.parseInt(request
					.getParameter("question_id"));
			int query_id = Integer.parseInt(request.getParameter("query_id"));

			String query = CommonFunctions.decodeURIComponent(request
					.getParameter("query"));
			loginUsr = (String) session.getAttribute("LOGIN_USER");
			

			Long id = Thread.currentThread().getId();

			String uniqueID = loginUsr + "&" + assignment_id + "&"
					+ question_id + "&" + query_id;
			if (session.getAttribute("DataGeneratingInProgressMap") != null) {

				dataGenStartedObj = (Hashtable) session
						.getAttribute("DataGeneratingInProgressMap");
				dataGenStartedObj.put(uniqueID, id);
			} else {
				dataGenStartedObj = new Hashtable();
				dataGenStartedObj.put(uniqueID, id);
			}
			session.setAttribute("DataGeneratingInProgressMap",
					dataGenStartedObj);

			logger.log(Level.FINE,"------AssignmentChecker----------");
			logger.log(Level.FINE,"Assignment_Id :" + assignment_id);
			logger.log(Level.FINE,"Question_Id " + question_id);
			logger.log(Level.FINE,"Query :" + query);
			String deldataset = "delete from xdata_datasetvalue where assignment_id=? and question_id = ? and query_id=? and course_id=?";
			try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
				
				try(PreparedStatement delstmt = dbcon.prepareStatement(deldataset)){
					delstmt.setInt(1, assignment_id);
					delstmt.setInt(2, question_id);
					delstmt.setInt(3, query_id);
					delstmt.setString(4,course_id);
					delstmt.executeUpdate();
			}//try block for delstmt ends
			}//try block for dbcon ends
			catch (SQLException e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				//e.printStackTrace();
			}
			String s = null;

			response.setContentType("text/html");
			PrintWriter out_assignment = response.getWriter();
			String args[] = { String.valueOf(assignment_id),
					String.valueOf(question_id), String.valueOf(query_id) ,course_id};
			ResultSet rsDataSet = null;
			if (!Thread.currentThread().interrupted()) {
				try {
					//GenerateDataset_new.entry(args);
					preProcessForDataGeneration preProcess = new preProcessForDataGeneration();
					preProcess.generateDatasetForQuery(assignment_id,question_id,query_id,course_id);
					String dataGeneratedID = loginUsr + "&" + assignment_id
							+ "&" + question_id + "&" + query_id;
					
					dataGenerationCompleted = (ArrayList) session
							.getAttribute("DataGenerationCompleted");
					// Once data is generated, remove it from the HashMap in the
					// session - to stop showing 'Data Generation in Progress....' in jsp
					if (dataGenStartedObj.containsKey(dataGeneratedID)
							&& id == dataGenStartedObj.get(dataGeneratedID)) {

						dataGenStartedObj.remove(dataGeneratedID);

						// Data is generated, add it to the list to show 'Show
						// Generated Data' in jsp
						if (dataGenerationCompleted != null
								&& dataGenerationCompleted.size() > 0
								&& !dataGenerationCompleted
										.contains(dataGeneratedID)) {

							dataGenerationCompleted.add(dataGeneratedID);
						} else {
							dataGenerationCompleted = new ArrayList<String>();
							dataGenerationCompleted.add(dataGeneratedID);
						}
					}
					session.setAttribute("DataGeneratingInProgressMap",
							dataGenStartedObj);
					session.setAttribute("DataGenerationCompleted",
							dataGenerationCompleted);
					response.sendRedirect("asgnmentList.jsp?assignmentId="
										+ assignment_id+"&&showQuestions=true");
					return;
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE,"Interrupted Exception : "+e.getMessage(),e);
					response.sendRedirect("asgnmentList.jsp?assignmentId="
										+ assignment_id+"&&showQuestions=true");
					return;
				} catch (Exception e2) {
					logger.log(Level.SEVERE,e2.getMessage(),e2);
					// Data generation failed and exception is thrown. So delete
					// it from list and update session.
					String dataGeneratedID = loginUsr + "&" + assignment_id
							+ "&" + question_id + "&" + query_id;
					dataGenStartedObj = (Hashtable) session
							.getAttribute("DataGeneratingInProgressMap");
					if (dataGenStartedObj.containsKey(dataGeneratedID)
							&& id == dataGenStartedObj.get(dataGeneratedID)) {

						dataGenStartedObj.remove(dataGeneratedID);

						session.setAttribute("DataGeneratingInProgressMap",
								dataGenStartedObj);
					}
					dataGenerationCompleted = (ArrayList) session
							.getAttribute("DataGenerationCompleted");
					if (dataGenerationCompleted != null
							&& dataGenerationCompleted.size() > 0
							&& dataGenerationCompleted
									.contains(dataGeneratedID)) {

						dataGenerationCompleted.remove(dataGeneratedID);
						session.setAttribute("DataGenerationCompleted",dataGenerationCompleted);
					}
					if (Thread.currentThread().interrupted()) {
						RequestDispatcher rd = request
								.getRequestDispatcher("asgnmentList.jsp?assignmentId="
										+ assignment_id+"&&showQuestions=true");
						rd.forward(request, response);
					} else {

						throw new ServletException(e2);
					} 
				}
			}  
			
		} else {
			this.stopProcess(request, response);
		}
		
	}

	/**
	 * This method interrupts a thread from executing. It gets the id of the
	 * thread that is to be stopped. This method finds all currently running
	 * threads and interrupts the one that is to be stopped.
	 * 
	 * @param request
	 *            - HttpServletRequest
	 * @param response
	 *            - HttpServletResponse
	 * @throws IOException
	 */
	public void stopProcess(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		
		if (!Thread.interrupted()) {
			String loginUsr = null;
			HttpSession session = request.getSession(false);
			/**Once thread is interruptes, remove the Question ID from dataGenerationInProgress**/
			Hashtable<String, Long> requestAttribute = (Hashtable) session
					.getAttribute("DataGeneratingInProgressMap");
			int assignment_id = Integer.parseInt(request
					.getParameter("assignment_id"));
			int question_id = Integer.parseInt(request
					.getParameter("question_id"));
			int query_id = Integer.parseInt(request.getParameter("query_id"));

			long threadIDtoInterrupt = 0;

			if (session.getAttribute("LOGIN_USER") == null
					|| !session.getAttribute("LOGIN_USER").equals("ADMIN")) {
				response.sendRedirect("index.jsp");
				return;
			} else {
				loginUsr = (String) session.getAttribute("LOGIN_USER");
			}

			String requestProcessId = loginUsr + "&" + assignment_id + "&"
					+ question_id + "&" + query_id;
			if (requestAttribute != null && requestAttribute.size() > 0) {
				// Remove the object for which processing is to be stopped
				threadIDtoInterrupt = (Long) requestAttribute
						.get(requestProcessId);
				requestAttribute.remove(requestProcessId);
				session.setAttribute("DataGeneratingInProgressMap",
						requestAttribute);
			}

			Thread currentThread = Thread.currentThread();
			ThreadGroup threadGroup = getRootThreadGroup(currentThread);
			int allActiveThreads = threadGroup.activeCount();
			Thread[] allThreads = new Thread[allActiveThreads];
			threadGroup.enumerate(allThreads);

			for (int i = 0; i < allThreads.length; i++) {
				Thread thread = allThreads[i];

				if (threadIDtoInterrupt == thread.getId()) {

					thread.interrupt();
					break;
				}

			}
			response.sendRedirect("asgnmentList.jsp?assignmentId="
					+ assignment_id+"&&showQuestions=true");
			return;
		}
	}

	/**
	 * This method gets the currently running threads from thread group
	 * 
	 * @param thread
	 * @return
	 */
	private static ThreadGroup getRootThreadGroup(Thread thread) {
		ThreadGroup rootGroup = thread.getThreadGroup();
		while (true) {
			ThreadGroup parentGroup = rootGroup.getParent();
			if (parentGroup == null) {
				break;
			}
			rootGroup = parentGroup;
		}
		return rootGroup;
	}

	/**
	 * This method stops the thread when "Stop" is requested while Data
	 * generation process is in progress by interrupting the thread.
	 * 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		if (!Thread.interrupted()) {
			this.stopProcess(request, response);
		}
	}

}