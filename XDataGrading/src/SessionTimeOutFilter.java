

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

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
 * Servlet Filter implementation class SessionTimeOutFilter
 * 
 * The filter is invoked each time there is a request for a jsp page.
 * This filter checks whether the session is invalid due to timeout condition.
 * 
 */
@WebFilter("/SessionTimeOutFilter")
public class SessionTimeOutFilter implements Filter {
	private static Logger logger = Logger.getLogger(SessionTimeOutFilter.class.getName());
    /**
     * Default constructor. 
     */
    public SessionTimeOutFilter() {
        // TODO Auto-generated constructor stub
    }
  
	/**
	 * @see Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}   

	/**
	 * This method checks whether the session is valid. 
	 * If session is invalid, it checks whether the request is from 
	 * a new session or the context page (in case login page is refreshed)
	 * 
	 * If not, then it invalidates the session on timeout and returns to <code>index.jsp</code>	
	 *    
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		    
		    HttpServletRequest req = (HttpServletRequest) request;
		    HttpServletResponse res = (HttpServletResponse) response;
		    //getSession(false) - does not creates new session-if some session exists it returns true
		    HttpSession session = req.getSession(false); 
		   // check if request is not from same login page
		   if (isSessionControlRequiredForThisResource(req)) {
			   //If session is not valid
			   if (isSessionInvalid(req)) { 
				 
				   String timeoutUrl =  "index.jsp?TimeOut=true";  
				
				System.out.println("Request ContextPath for redirection : "+req.getContextPath());
				System.out.println("Local address for redirection : "+request.getLocalAddr());
				System.out.println("Servlet context path - for redirection on timeout :  "+ request.getServletContext().getContextPath());
				
				//Invalidate the session and forward to login page
					   if(session!= null){   
						   	session.invalidate();
					   } 
				   	   res.setHeader("Cache-Control","no-cache"); 
					   res.setHeader("Cache-Control","no-store"); 
					   res.setDateHeader("Expires", 0);
				       response.setContentType("text/html"); 
						PrintWriter out = response.getWriter();
						out.write("<script>window.parent.location.href='"+timeoutUrl+"'</script>"); 
					    out.close();  
				
				   }  
			   else{  	
				   chain.doFilter(request, response);
			   }
			   } 
		   else{ 
			   	
					chain.doFilter(request, response);			   
		   } 
	}    
   
	 /*
	  * This method checks whether the jsp page needs session check. 
	  * Session shouldn't be checked for some pages. For example: for login page.. 
	  * Since we're redirecting to login page from this filter, 
	  * if we don't disable session control for it, filter will again redirect to it 
	  * and this will be result with an infinite loop... */
	 
	    private boolean isSessionControlRequiredForThisResource(HttpServletRequest httpServletRequest) { 
		
	    boolean controlRequired = false;
		// If it is a  new session or no session exists, check whether the servlet path contains login page
		 if(httpServletRequest.getSession(false) == null || httpServletRequest.getSession(false).isNew()){
			 
			 controlRequired  = !httpServletRequest.getServletPath().contains("index.jsp") 
					 && !httpServletRequest.getServletPath().contains("tool.jsp") 
					 && !httpServletRequest.getServletPath().contains("LtiLogout.jsp")
					 &&  !httpServletRequest.getServletPath().contains("LoginChecker")
			 		 && !httpServletRequest.getServletPath().contains(".html") 
			 		 && !httpServletRequest.getServletPath().contains(".jsp") 
			 		 && !httpServletRequest.getServletPath().contains(".swf")
			 		 && !httpServletRequest.getServletPath().contains(".js")
			 		 && !httpServletRequest.getServletPath().contains(".css") 
			 		 && !httpServletRequest.getServletPath().contains(".jpeg") 
			 		 && !httpServletRequest.getServletPath().contains(".gif")
			 		 && !httpServletRequest.getServletPath().contains(".png")
			 		 && !httpServletRequest.getServletPath().contains(".jpg") 
			 		 && !httpServletRequest.getServletPath().contains(".pdf");
			 		
		 } 
		 //check if the request is from the login page. case on refreshing login page
		 else{ 
			 controlRequired = !httpServletRequest.getRequestURI().contains("index.jsp");   
		 }  
		 return controlRequired;  
		 } 
	    
	    /*
	     * This method checks whether the session is valid. 
	     */
		 private boolean isSessionInvalid(HttpServletRequest httpServletRequest) { 
		 boolean sessionInValid = false;
			sessionInValid=  (httpServletRequest.isRequestedSessionIdValid() == false); 
			return sessionInValid;     
		 }  
		      
		 
		
	/** 
	 * @see Filter#init(FilterConfig)
	 */ 
	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}
}
