
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import database.DatabaseConnection;

/**
 * Servlet implementation class InitAssignment
 */
// @WebServlet("/InitAssignment")
public class InitAssignment extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(InitAssignment.class.getName());
	public InitAssignment() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		// ELT - External Learning Tool
		boolean ELTReq  = false;
		if(session.getAttribute("ltiIntegration") != null){
		 ELTReq = (Boolean) session.getAttribute("ltiIntegration");
		}
		
		String role = (String)session.getAttribute("role");
		
		try (Connection dbCon = (new DatabaseConnection()).dbConnection()){
			
			//String role = (String) request.getSession().getAttribute("roles");
			//if (role.equalsIgnoreCase("instructor") || role.equalsIgnoreCase("admin")) {
			String getAssignment = "Select * from xdata_assignment where course_id = ?";
			String roleDetail = (String)request.getSession().getAttribute("roles");
			
			try(PreparedStatement pstmt = dbCon.prepareStatement(getAssignment)){
				pstmt.setString(1,(String) request.getSession().getAttribute(
								"context_label"));
				logger.log(Level.FINE,"context: "+ (String) request.getSession().getAttribute(
								"context_label"));
				try(ResultSet rs = pstmt.executeQuery()){
				//If ELTReq and session.setAttribute("allowedAssignment") is there and roledetail chk
				
					if (ELTReq && roleDetail.toUpperCase().contains("INSTRUCTOR")) {
						
						try(PreparedStatement statment = dbCon.prepareStatement("Select * from xdata_course where instructor_course_id = ?")){
						statment.setString(1,(String) request.getSession().getAttribute(
										"context_label"));
						try(ResultSet rset = statment.executeQuery()){ 
						//If course does not exist, create a new course in XData DB
						if(rset== null || (rset != null && !rset.next())){
												
							try(PreparedStatement statemnt = dbCon
											.prepareStatement("select MAX(course_id) as courseId from xdata_course")){
									ResultSet result = statemnt.executeQuery();
									result.next();
									int internalCouresId = result.getInt("courseId");
									try(PreparedStatement statemnt1 = dbCon
												.prepareStatement("insert into xdata_course values (?,?,?,?,?,?)")){
										statemnt1.setInt(1, internalCouresId + 1);
										statemnt1.setString(2, (String) request.getSession()
												.getAttribute("context_label"));
										statemnt1.setString(3, "");// name
										// Persist current year when course gets created
										Calendar c = Calendar.getInstance();
										int year = c.get(Calendar.YEAR);
										statemnt1.setInt(4, year);
										// Persist current year and month for semester as this info may not
										// come from ELT
										String month1 = c.getDisplayName(Calendar.MONTH,
												Calendar.LONG, Locale.getDefault());
										statemnt1.setString(5, month1 + " " + year);
										statemnt1.setString(6,
												"Course added by external learning tool"); 
										statemnt1.execute();
										}//try block for statemnt1 ends
							
									}//try block for statemnt ends
						}
					}//try block for resultset ends
				}//try block for statement ends
				// TODO : Set the  course id to session once all
				// other table references are changed
				if(session.getAttribute("allowedAssignment") == null ){
					RequestDispatcher rd = request
							.getRequestDispatcher("/InstructorHome.jsp");
					rd.include(request, response);
				}
				else if(session.getAttribute("allowedAssignment") != null ){
					RequestDispatcher rd = request
							.getRequestDispatcher("/InstructorHome.jsp?FrmLtiToAssign=true");
					rd.include(request, response);
				}
			}else if (ELTReq && roleDetail.toUpperCase().contains("LEARNER")) {
				if(session.getAttribute("allowedAssignment") == null ){
					RequestDispatcher rd = request
							.getRequestDispatcher("/StudentHome.jsp");
					rd.include(request, response);
				}
				else if(session.getAttribute("allowedAssignment") != null ){
					RequestDispatcher rd = request
							.getRequestDispatcher("/StudentHome.jsp?FrmLtiToAssign=true");
					rd.include(request, response);
				}	
			}
			else if(role != null && role.equalsIgnoreCase("Tester")){
				
				RequestDispatcher rd = request
						.getRequestDispatcher("/TesterHome.jsp");
				rd.include(request, response);
				
			}else if(role != null && role.equalsIgnoreCase("guest")){
				
				RequestDispatcher rd = request
						.getRequestDispatcher("/CourseHome.jsp");
				rd.include(request, response);
			}
			else{
				RequestDispatcher rd = request
						.getRequestDispatcher("/CourseHome.jsp");
				rd.include(request, response);
			}
			/*else if(role != null && role.equalsIgnoreCase("student")){
				RequestDispatcher rd = request
						.getRequestDispatcher("Student/StudentHome.jsp");
				rd.include(request, response);
			}*/
				}//try block to close resultset ends
			}//try block to close statement ends
				
			} catch (Exception e) {
				logger.log(Level.SEVERE,e.getMessage(),e);
				//e.printStackTrace();
				throw new ServletException(e);
			}
			
	}

}
