import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Deprecated
public class SessionFilter implements Filter {

  private String contextPath;

  @Override
public void init(FilterConfig fc) throws ServletException {
    contextPath = fc.getServletContext().getContextPath();
  }

  @Override
public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;  
    String path = ((HttpServletRequest) request).getRequestURI();
    HttpSession session = req.getSession(false);
    
    if(path.contains("asgnmentCreation.html") || path.contains("asgnmentList.jsp") || path.contains("assignment_eval.html") || path.contains("Assignment.html") || 
    		path.contains("assignmentCreation.jsp") || path.contains("dataUpload") || path.contains("evaluateAssignment.jsp") || path.contains("gradeAssignment.jsp") ||
    		path.contains("initialDataUpload.jsp") || path.contains("instructorOptions.html") || path.contains("newAssignmentCreation.jsp") || 
    		path.contains("schemaUpload") || path.contains("updateAssignment.jsp") || path.contains("UpdateExistingAssignment.jsp") || path.contains("updateQuery.jsp")){
    	
    
	    if(path.contains("LoginChecker") || path.equals(contextPath) || path.equals(contextPath + "/") || path.contains(".css")){
	    	fc.doFilter(request, response);
	    	return;
	    } 
	
	    if(session == null){
	    	response.setContentType("text/html"); 
			PrintWriter out = response.getWriter();
			//System.out.println(" session is: -------- ++ "+req.getSession(false));
			//out.write("<script>window.parent.location.href='"+req.getContextPath() + "/index.jsp"+"'</script>");
			out.write("<script>window.parent.location.href='/index.jsp'></script>");
		    out.close();   
	    }
	    if (session != null && session.getAttribute("LOGIN_USER") == null) { //checks if there's a LOGIN_USER set in session...
	    	response.setContentType("text/html"); 
			PrintWriter out = response.getWriter();
			//System.out.println(" session is:******* "+req.getSession(false));
			//out.write("<script>window.parent.location.href='"+req.getContextPath() + "/index.jsp?TimeOut=true"+"'</script>"); 
			out.write("<script>window.parent.location.href='/index.jsp?TimeOut=true'</script>");
		    out.close();  //or page where you want to redirect
	    } else { 
	      String userType = (String) req.getSession().getAttribute("LOGIN_USER"); 
	      if (!userType.equals("ADMIN") && !userType.equals("student")){ //check if user type is not admin 
	        res.sendRedirect(contextPath); //or page where you want to  
	      }
	      fc.doFilter(request, response);
	    }
    }
    else
    	fc.doFilter(request, response);
    }

  @Override
public void destroy() {
  }
}
