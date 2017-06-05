

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter implementation class AuthorizationFilter
 */
@WebFilter("/AuthorizationFilter")
public class AuthorizationFilter implements Filter {

    /**
     * Default constructor. 
     */
    public AuthorizationFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
	  	HttpServletRequest req = (HttpServletRequest) request;
	    HttpServletResponse res = (HttpServletResponse) response;
	    //getSession(false) - does not creates new session-if some session exists it returns true
	    HttpSession session = req.getSession(false); 
		String unAurthorisedUrl = "index.jsp?NotAuthorised=true";
		
		 //access allowed filter check for all servlets - the user's role filter check - student's should not be allowed to see instructor / admin pages
	    //First - jsp/servlet check for instructor and admin
	    //Then course_id check for the instructor - whether he is authenticated to view the specific course(compare session course id with user_allowed course id's)
	    
		if(   !req.getRequestURI().contains("index.jsp") 
				&& !req.getServletPath().contains("index.jsp") 
				 && !req.getServletPath().contains("tool.jsp") 
				 && !req.getServletPath().contains("LtiLogout.jsp")
				 && !req.getServletPath().contains("LoginChecker")
				  && !req.getServletPath().contains("Logout")
				 && !req.getRequestURI().contains("index.jsp")
				  && !req.getServletPath().contains(".js") 
						  && !req.getServletPath().contains(".jsp") 
						  && !req.getServletPath().contains(".css")
				){
				if( !isUserAuthorised(req)){
				  if(session != null){   
					   	session.invalidate();
				   } 
			   	   res.setHeader("Cache-Control","no-cache"); 
				   res.setHeader("Cache-Control","no-store"); 
				   res.setDateHeader("Expires", 0);
			       response.setContentType("text/html"); 
					PrintWriter out = response.getWriter();
					out.write("<script>window.parent.location.href='"+unAurthorisedUrl+"'</script>"); 
				    out.close();  
			}else{
				chain.doFilter(request, response);
			}
		}else{
			chain.doFilter(request, response);
		}
	}

	 private boolean isUserAuthorised(HttpServletRequest req){ 
		 boolean isAuthorised = false;
		 HttpSession session = req.getSession(false); 
		 String path = ((HttpServletRequest) req).getServletPath();
		 if(session.getAttribute("LOGIN_USER") != null && session.getAttribute("role").equals("admin")){
			 if(path.contains("LoginChecker") || path.contains("AssignmentOptions") || path.contains("InitAssignment")|| path.contains("FailedTestCases") || path.contains("DeleteLtiSetup") 
					 || path.contains("AssignRole") ||  path.contains("CreateNewCourse")  ||  path.contains("EditCourse") || path.contains("NewLmsCredential") || path.contains("EditLmsCredential")
					 || path.contains("CreateUser") || path.contains("EditUser") 
					 ){
				 return true;
				 
			 }
			 
		 }
		 
		 if(session.getAttribute("LOGIN_USER") != null &&session.getAttribute("role") != null &&  session.getAttribute("role").equals("instructor")){
			 
			 if( path.contains(".html") || path.contains(".jsp") ||  path.contains(".swf") ||  path.contains(".js")||  path.contains(".css") ||  path.contains(".jpeg") ||  path.contains(".gif")||  path.contains(".png")||  path.contains(".jpg") ||  path.contains(".pdf")    
					 || path.contains("AssignmentChecker")|| path.contains("AssignmentOptions")
					 || path.contains("InitAssignment")|| path.contains("FailedTestCases")
					 || path.contains("GetDefaultDataSets") || path.contains("GetQuestionsForLateSumbission")|| path.contains("LateSubmission")|| path.contains("PartialMarkingParamsPerInstrQuery") 
					 || path.contains("ShowDatasets") || path.contains("StudentTestCase")|| path.contains("DownloadFile") || path.contains("TestCaseDataset")|| path.contains("ViewAssignment")
					 || path.contains("GuestStudentTestCase") || path.contains("UpdateServlet")|| path.contains("EvaluateQuestion") || path.contains("QueryStatus")
					 || path.contains("EvaluateAssignment")|| path.contains("DeleteAssignments")|| path.contains("SchemaUpload") || path.contains("DeleteSchema") 
					 || path.contains("SampleDataUpload")|| path.contains("DeleteSampleData") || path.contains("Logout") || path.contains("DeleteDatabaseConnection")
					 || path.contains("EditDatabaseConnection")|| path.contains("DeleteQuestionWithQueries")|| path.contains("ShowSchemaFile") 
					 || path.contains("UpdateSingleQuery") || path.contains("TestUploadedFile")|| path.contains("UpdateDatabaseConnection") || path.contains("UploadScore")
					 || path.contains("UpdateMarks") || path.contains("PartialMarkerServlet")|| path.contains("AssignmentScores")
					 || path.contains("PartialMarkingDemo")					
					 ){
				 return true;
			 }
		 }
		 if(session.getAttribute("LOGIN_USER") != null && session.getAttribute("role") !=null && (session.getAttribute("role").equals("student") ||  session.getAttribute("role").equals("guest"))){
			 
			 if( path.contains(".html") || path.contains(".jsp") ||  path.contains(".swf")  ||  path.contains(".js")||  path.contains(".css") ||  path.contains(".jpeg") ||  path.contains(".gif")||  path.contains(".png")||  path.contains(".jpg") ||  path.contains(".pdf")
					 ||  path.contains("AssignmentOptions") || path.contains("InitAssignment")|| path.contains("FailedTestCases") 
					 || path.contains("StudentAssignment")|| path.contains("StudentTestCase") || path.contains("DownloadFile")|| path.contains("TestCaseDataset") || path.contains("ViewAssignment")
				 || path.contains("GuestStudentTestCase")|| path.contains("UpdateServlet")
				 ){
				 return true;
			 }
		 }
		 if(session.getAttribute("LOGIN_USER") != null && session.getAttribute("role") != null && session.getAttribute("role").equals("tester")){
			 
			 if( path.contains(".html") || path.contains(".jsp") ||   path.contains(".swf")  ||  path.contains(".js")||  path.contains(".css") ||  path.contains(".jpeg") ||  path.contains(".gif")||  path.contains(".png")||  path.contains(".jpg") ||  path.contains(".pdf")
					 || path.contains("AssignmentOptions")|| path.contains("InitAssignment") 
					 || path.contains("FailedTestCases")|| path.contains("MatchResult")|| path.contains("GetDefaultDataSets") || path.contains("ShowDatasets")
					 || path.contains("CheckQueryEquivalence") || path.contains("DownloadFile")|| path.contains("TestCaseDataset") || path.contains("ViewAssignment")
					 || path.contains("UpdateServlet")|| path.contains("QueryStatus") || path.contains("DeleteAssignments")
					 || path.contains("SchemaUpload") || path.contains("DeleteSchema")|| path.contains("SampleDataUpload") || path.contains("DeleteSampleData")
					 || path.contains("Logout")|| path.contains("DeleteDatabaseConnection") || path.contains("EditDatabaseConnection")|| path.contains("DeleteQuestionWithQueries") 
					 || path.contains("ShowSchemaFile")|| path.contains("UpdateSingleQuery") || path.contains("TestUploadedFile")
					 || path.contains("UpdateDatabaseConnection")
					 //|| path.contains("")|| path.contains("") || path.contains("")|| path.contains("") || path.contains("")
					 ){
				    return true;
			 }
		 }
		 //path.contains("")|| path.contains("") || path.contains("")|| path.contains("") || path.contains("")|| path.contains("") || path.contains("")
		 return isAuthorised;
	 }
	 
	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
