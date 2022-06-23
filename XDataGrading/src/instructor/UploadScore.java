package instructor;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
//import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//import org.apache.jasper.compiler.ServletWriter;

import util.MyConnection;

/**
 * Servlet implementation class UploadScore
 */
//@WebServlet("/UploadScore")
public class UploadScore extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(UploadScore.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UploadScore() {
		super();
		// TODO Auto-generated constructor stub
	}

	/** 
	 * 
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		double score = 0;
		double max = 100;
		String userId = "";
		PrintWriter out = response.getWriter();
		String course_id = (String)request.getSession().getAttribute("context_label") ;
		int assignment_id = Integer.parseInt(request.getParameter("AssignmentID"));
		
		String singleUpload = request.getParameter("singleUpload");
		if(singleUpload.equalsIgnoreCase("true")){
		try (Connection conn = MyConnection.getDatabaseConnection()){
			//Gets the user id for which score is to be uploaded
			userId = request.getParameter("userId");
			score = Double.parseDouble(request.getParameter("score"));
			max = Double.parseDouble(request.getParameter("max"));
			String lis_result_sourcedid = "";
			// Score has to be in the range of 0.0 - 1.0
			score = score / max;

			//Shree modified this for marks updation in moodle for specific assignment
			//PreparedStatement stmt = conn
			//		.prepareStatement("select * from users where internal_user_id = ?");
			
			//stmt.setString(1, userId);
			//ResultSet rs1 = stmt.executeQuery();
			
			try(PreparedStatement stmt1 = conn
					.prepareStatement("select * from xdata_LTIResponseInfo where internal_user_id = ? and course_id = ? and assignment_id = ?")){
			
					stmt1.setString(1, userId);
					stmt1.setString(2,course_id);
					stmt1.setInt(3,assignment_id);
					try(ResultSet rs = stmt1.executeQuery()){
					
						if (rs.next()) {
							lis_result_sourcedid = rs.getString("sourceid");
						}else{
							lis_result_sourcedid = "";
						}
					}
			}//stmt1 ends
			String oauth_consumer_key = request.getSession()
					.getAttribute("oauth_consumer_key").toString();

			String lis_outcome_service_url = request.getSession()
					.getAttribute("lis_outcome_service_url").toString();

			String secret = request.getSession()
					.getAttribute("oauth_consumer_secret").toString();
			;

			// Refer : http://www.imsglobal.org/LTI/v1p1/ltiIMGv1p1.html
			String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<imsx_POXEnvelopeRequest xmlns = \"https://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">"
					+ "<imsx_POXHeader>" + "<imsx_POXRequestHeaderInfo>"
					+ "<imsx_version>V1.0</imsx_version>"
					+ "<imsx_messageIdentifier>1</imsx_messageIdentifier>"
					+ "</imsx_POXRequestHeaderInfo>" + "</imsx_POXHeader>"
					+ "<imsx_POXBody>" + "<replaceResultRequest>"
					+ "<resultRecord>" + "<sourcedGUID>" + "<sourcedId>"
					+ lis_result_sourcedid + "</sourcedId>" + "</sourcedGUID>"
					+ "<result>" + "<resultScore>" + "<language>en</language>"
					+ "<textString>" + Double.toString(score) + "</textString>"
					+ "</resultScore>" + "</result>" + "</resultRecord>"
					+ "</replaceResultRequest>" + "</imsx_POXBody>"
					+ "</imsx_POXEnvelopeRequest>";

			String replyBody = new LTIMessage("", body,
					lis_outcome_service_url, oauth_consumer_key, secret).send();

			if (replyBody.toLowerCase().contains("success")) {
				//response.sendRedirect("assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID"));
				String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=success";
				out.write("<script>window.location.href='"+forwardUrl+"'</script>");
				
			} else {
				logger.log(Level.SEVERE,"UploadScores Failed - Internal error");
				//response.sendRedirect("assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&ResultUploadStatus=fail");
				String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=fail";
				out.write("<script>window.location.href='"+forwardUrl+"'</script>");
				
				}
		} catch (Exception e) {
			
			logger.log(Level.SEVERE,e.getMessage(),e);
			String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=fail";
			out.write("<script>window.location.href='"+forwardUrl+"'</script>");
			
		}
		}
		//Upload marks for assignment as a whole  (singleupload=false)
		else{
			//Get student one by one and calculate total marks and send to moodle.
			
			String total = "select sum(totalmarks) total from xdata_qinfo where assignment_id = ?";
			String result = "select sum(score) result, user_name, email, rollnum from xdata_users u " +
					"left join xdata_student_queries s on u.internal_user_id = s.rollnum " +
					"where assignment_id = ? group by user_name, email, rollnum order by rollnum";
			Float totalMarks = 100F;
				try (Connection conn = MyConnection.getDatabaseConnection()){
				try(PreparedStatement pstmt = conn.prepareStatement(total)){
					pstmt.setInt(1, assignment_id);
					ResultSet rs = pstmt.executeQuery();
					if (rs.next()) {
						totalMarks = rs.getFloat("total");
					}
					boolean uploadSuccess = false;
					boolean present = false;
					try(PreparedStatement pstmt1 = conn.prepareStatement(result)){
			
						pstmt1.setInt(1,assignment_id);
						try(ResultSet rs1 = pstmt1.executeQuery()){
							while (rs1.next()) {
								present = true;
								userId = rs1.getString("rollnum");
								score = rs1.getFloat("result");
								max = totalMarks;
								String lis_result_sourcedid = "";
								// Score has to be in the range of 0.0 - 1.0
								score = score / max;
								//try(PreparedStatement stmt1 = conn.prepareStatement("select * from xdata_LTIResponseInfo where internal_user_id = ? and course_id = ? and assignment_id = ?")){
								try(PreparedStatement stmt1 = conn.prepareStatement("select * from xdata_LTIResponseInfo where internal_user_id = ? and course_id = ? ")){

									stmt1.setString(1, userId);
									stmt1.setString(2,course_id);
									//stmt1.setInt(3,assignment_id);
									try(ResultSet rs2 = stmt1.executeQuery()){
										if (rs2.next()) {
											lis_result_sourcedid = rs2.getString("sourceid");
										}else{
											lis_result_sourcedid = "";
										}
									}//rs2.ends
								}//stmt2 ends
								String oauth_consumer_key = request.getSession()
										.getAttribute("oauth_consumer_key").toString();
	
								String lis_outcome_service_url = request.getSession()
										.getAttribute("lis_outcome_service_url").toString();
	
								String secret = request.getSession()
										.getAttribute("oauth_consumer_secret").toString();
								;
	
								// Refer : http://www.imsglobal.org/LTI/v1p1/ltiIMGv1p1.html
								String body = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
										+ "<imsx_POXEnvelopeRequest xmlns = \"https://www.imsglobal.org/services/ltiv1p1/xsd/imsoms_v1p0\">"
										+ "<imsx_POXHeader>" + "<imsx_POXRequestHeaderInfo>"
										+ "<imsx_version>V1.0</imsx_version>"
										+ "<imsx_messageIdentifier>1</imsx_messageIdentifier>"
										+ "</imsx_POXRequestHeaderInfo>" + "</imsx_POXHeader>"
										+ "<imsx_POXBody>" + "<replaceResultRequest>"
										+ "<resultRecord>" + "<sourcedGUID>" + "<sourcedId>"
										+ lis_result_sourcedid + "</sourcedId>" + "</sourcedGUID>"
										+ "<result>" + "<resultScore>" + "<language>en</language>"
										+ "<textString>" + Double.toString(score) + "</textString>"
										+ "</resultScore>" + "</result>" + "</resultRecord>"
										+ "</replaceResultRequest>" + "</imsx_POXBody>"
										+ "</imsx_POXEnvelopeRequest>";
	
								String replyBody = new LTIMessage("", body,
										lis_outcome_service_url, oauth_consumer_key, secret).send();
	
								if (replyBody.toLowerCase().contains("success")) {
									//response.sendRedirect("AssignmentScores?AssignmentID="+request.getParameter("AssignmentID"));
									logger.log(Level.FINE,"Upload success");
									uploadSuccess = true;
								} else {
									uploadSuccess = false;
									
									//response.sendRedirect("AssignmentScores?AssignmentID="+request.getParameter("AssignmentID")+"&ResultUploadStatus=fail");
								}
							}
						
							
							if(uploadSuccess){
								String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=success";
								out.write("<script>window.location.href='"+forwardUrl+"'</script>"); 
							    
								//response.sendRedirect("../../assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=success");
							}
							else{
								String forwardUrl = "assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=fail";
								out.write("<script>window.location.href='"+forwardUrl+"'</script>");
								//response.sendRedirect("../../assignmentScores.jsp?AssignmentID="+request.getParameter("AssignmentID")+"&&ResultUploadStatus=fail");
							}
							out.close();
						}catch(Exception ex){
							logger.log(Level.SEVERE,ex.getMessage(),ex);
						}
				    }
				}
				catch(Exception e){
					logger.log(Level.SEVERE,e.getMessage(),e);
					//log statement exception
				}
				}catch(Exception e){
				//log connection exception
				logger.log(Level.SEVERE,e.getMessage(),e);
			}
			
		} //else statement
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
	}
}

