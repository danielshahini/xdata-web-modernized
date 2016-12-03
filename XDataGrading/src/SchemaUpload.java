
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import database.DatabaseConnection;

/**
 * Servlet implementation class SchemaUpload
 */
@WebServlet("/SchemaUpload")
public class SchemaUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(SchemaUpload.class.getName());
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SchemaUpload() {
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
		String courseId = (String) request.getSession().getAttribute(
				"context_label");
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);

		if (isMultipart) {
			int x = 0;
			String fileName = "";
			byte[] dataBytes = null;
			boolean testAndUpload = false;
			try(Connection dbcon = (new DatabaseConnection())
						.dbConnection()){
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				upload.setHeaderEncoding("UTF-8");
				List<FileItem> uploadItems = upload.parseRequest(request);

				for (FileItem uploadItem : uploadItems) {
					if (uploadItem.isFormField()) {
						String fieldName = uploadItem.getFieldName();
						String value = uploadItem.getString();

						// Check if the file item has the schema Name and value
						if (fieldName.equals("schemaname")) {
							fileName = value;
						} else if (fieldName.equals("testupload")) {
							testAndUpload = true;
							// break;
						}
					} else {
						String contentType = uploadItem.getContentType();
						dataBytes = uploadItem.get();
						dataBytes = uploadItem.get();
						if (fileName.isEmpty()) {
							fileName = uploadItem.getName();
						}
					}
				}
				session.setAttribute("schemaFileName", fileName);
				if (!testAndUpload) {
					String file = new String(dataBytes, "UTF-8");
					
					int newSchemaId = 1;
					// Get schema id
					try(PreparedStatement stmt  = dbcon
							.prepareStatement("SELECT MAX(schema_id) as schemaId from xdata_schemainfo")){
					try(ResultSet rs = stmt.executeQuery()){
						if (rs.next()) {
							newSchemaId = rs.getInt("schemaId") + 1;
						}
					}//close try block for ResultSet 
					 }//close try block for stmt 
					try(PreparedStatement stmt = dbcon
							.prepareStatement("Insert into xdata_schemainfo values(?, ?, ?, ? )")){
						stmt.setString(1, courseId);
						stmt.setInt(2, newSchemaId);
						stmt.setString(3, fileName);
						stmt.setString(4, file);
						stmt.executeUpdate();
					}
					response.sendRedirect("schemaUpload.jsp?upload=success");
				} else if (testAndUpload) {
					session.setAttribute("FileContents", new String(dataBytes,
							"UTF-8"));
					response.sendRedirect("connectionToTestUpload.jsp?schema_id=-schemaUpload");
					return;
				}
			} catch (Exception err) {
				logger.log(Level.SEVERE,err.getMessage(),err);
				x = 1;
				throw new ServletException(err);
			}

		}

	}

	protected String getFileContent(String f) {
		return "";
	}
}
