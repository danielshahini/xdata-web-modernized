import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import java.io.IOException;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SendEmail extends HttpServlet {

	Properties emailProperties;
	Session mailSession;
	MimeMessage emailMessage;
	private static Logger logger = Logger.getLogger(SendEmail.class.getName());
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		
		// Recipient's email ID needs to be mentioned.
		/*String to = request.getParameter("mail");

		String userName = request.getParameter("userName");
		String newPwd = request.getParameter("newPasswrd");

		System.out.println("B4 mailing details : user name = " + userName
				+ "---new password === " + newPwd + "---mail Id==" + to);

		// Sender's email ID needs to be mentioned
		String from = "admin@xdata";

		// Assuming you are sending email from localhost
		String host = "localhost";

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.setProperty("mail.smtp.host", host);

		// Get the default Session object.
		Session session = Session.getDefaultInstance(properties);

		// Set response content type
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		try {
			// Create a default MimeMessage object.
			MimeMessage message = new MimeMessage(session);
			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));
			// Set To: header field of the header.
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			// Set Subject: header field
			message.setSubject("New XData Password");
			// Now set the actual message
			message.setText("Dear "
					+ userName
					+ ","
					+ "\n\t"
					+ "Your request for password reset is accepted and new login information as follows: "
					+ "\n User Name :"
					+ userName
					+ "\n New Password: "
					+ newPwd
					+ "\n  Please contact system administrator for changing password with these details.\n Thanks, \n XData Team");

			// Send message
			Transport.send(message);
			String status = "success";
			response.sendRedirect("forgotPwd.jsp?pwdStatus=");

		} catch (MessagingException mex) {
			mex.printStackTrace();
			response.sendRedirect("forgotPwd.jsp?pwdStatus=" + "error");
		}*/
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String toMailId = request.getParameter("mail");

		String userName = request.getParameter("userName");
		String newPwd = request.getParameter("newPasswrd");
		try{
			String emailHost = "smtp.gmail.com";
			String fromUser = "xdata.sysad";//just the id alone without @gmail.com
			String fromUserEmailPassword = "xdata@123";
				 
				String emailPort = "465";//gmail's smtp port
		
				emailProperties = System.getProperties();
				
				Properties props = new Properties();
				props.put("mail.smtp.host", "smtp.gmail.com");
				props.put("mail.smtp.socketFactory.port", "465");
				props.put("mail.smtp.socketFactory.class",
						"javax.net.ssl.SSLSocketFactory");
				props.put("mail.smtp.auth", "true");
				props.put("mail.smtp.port", "465");

				Session session = Session.getDefaultInstance(props,
					new javax.mail.Authenticator() {
						@Override
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication("fromUser","fromUserEmailPassword");
						}
					});
				
				emailProperties.put("mail.smtp.port", emailPort);
				emailProperties.put("mail.smtp.auth", "true");
				emailProperties.put("mail.smtp.starttls.enable", "true");
				
				String[] toEmails = { toMailId };
				String emailSubject = "New XData password";
				String emailBody = "";//This is an email sent by JavaMail api.";
		
				mailSession = Session.getDefaultInstance(emailProperties, null);
				emailMessage = new MimeMessage(mailSession);
		
				for (int i = 0; i < toEmails.length; i++) {
					emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
				}
		
				emailMessage.setSubject(emailSubject);
				//emailMessage.setContent(emailBody, "text/html");//for a html email
		
				emailMessage.setText("Dear "
						+ userName
						+ ","
						+ "\n\t"
						+ "Your request for password reset is accepted and new login information as follows: "
						+ "\n User Name :"
						+ userName
						+ "\n New Password: "
						+ newPwd
						+ "\n  Please contact system administrator for changing password with these details.\n Thanks, \n XData Team");
				


				Transport transport = mailSession.getTransport("smtp");

				transport.connect(emailHost, fromUser, fromUserEmailPassword);
				transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
				transport.close();
				System.out.println("Email sent successfully.");
				response.sendRedirect("forgotPwd.jsp?pwdStatus=");
				PrintWriter out = response.getWriter();
		
			/*	try {
					// Create a default MimeMessage object.
					MimeMessage message = new MimeMessage(session);
					// Set From: header field of the header.
					message.setFrom(new InternetAddress(from));
					// Set To: header field of the header.
					message.addRecipient(Message.RecipientType.TO, new InternetAddress(
							to));
					// Set Subject: header field
					message.setSubject("New XData Password");
					// Now set the actual message
					message.setText("Dear "
							+ userName
							+ ","
							+ "\n\t"
							+ "Your request for password reset is accepted and new login information as follows: "
							+ "\n User Name :"
							+ userName
							+ "\n New Password: "
							+ newPwd
							+ "\n  Please contact system administrator for changing password with these details.\n Thanks, \n XData Team");
		
					// Send message
					 Transport transport = session.getTransport("smtp");
				      transport.connect(host, from, pwd);
				      transport.sendMessage(message, message.getAllRecipients());
				      transport.close(); 
				      String status = "success";
					response.sendRedirect("forgotPwd.jsp?status=" + status);
		*/
		} catch (MessagingException mex) {
			logger.log(Level.SEVERE,mex.getMessage(),mex);
			response.sendRedirect("forgotPwd.jsp?status=" + "error");
		}
	}
	
	public void sendMail() {
		//JavaEmail javaEmail = new JavaEmail();
	
		//javaEmail.setMailServerProperties();
		//javaEmail.createEmailMessage();
		//javaEmail.sendEmail();
	}
	public void setMailServerProperties() {

		String emailPort = "587";//gmail's smtp port

		emailProperties = System.getProperties();
		emailProperties.put("mail.smtp.port", emailPort);
		emailProperties.put("mail.smtp.auth", "true");
		emailProperties.put("mail.smtp.starttls.enable", "true");
	}
	
	public void createEmailMessage() throws AddressException,
	MessagingException {
String[] toEmails = { "joe@javapapers.com" };
String emailSubject = "Java Email";
String emailBody = "This is an email sent by JavaMail api.";

mailSession = Session.getDefaultInstance(emailProperties, null);
emailMessage = new MimeMessage(mailSession);

for (int i = 0; i < toEmails.length; i++) {
	emailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmails[i]));
}

emailMessage.setSubject(emailSubject);
//emailMessage.setContent(emailBody, "text/html");//for a html email

emailMessage.setText(emailBody);// for a text email

}

public void sendEmail() throws AddressException, MessagingException {

String emailHost = "smtp.gmail.com";
String fromUser = "your emailid here";//just the id alone without @gmail.com
String fromUserEmailPassword = "your email password here";

Transport transport = mailSession.getTransport("smtp");

transport.connect(emailHost, fromUser, fromUserEmailPassword);
transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
transport.close();
logger.log(Level.FINE,"Email sent successfully.");
}


}
