package instructor;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

import database.DatabaseConnection;

/**
 * Servlet implementation class SampleDataUpload
 */
//@WebServlet("/SampleDataUpload")
public class SampleDataUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(SampleDataUpload.class.getName());   
    /**
     * @see HttpServlet#HttpServlet() 
     */
    public SampleDataUpload() { 
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
 
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		 HttpSession session =  request.getSession(false);
		if(isMultipart){ 
			int x = 0;
			String schemaId = null ;
			String courseId = (String)session.getAttribute("context_label");
			String dataFileName = "";
			byte[] dataBytes = null; 
			   
			boolean testAndUpload = false;
			 
			try {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload( factory );
				List<FileItem> uploadItems = upload.parseRequest( request );
				String value ="";
				for( FileItem uploadItem : uploadItems )
				{
				  if(uploadItem.isFormField()) {
				    String fieldName = uploadItem.getFieldName();
				    value = uploadItem.getName();  
				    String filename = FilenameUtils.getName(uploadItem.getName());
				    String contentType = getServletContext().getMimeType(filename);
				    if(value != null){
				    	dataFileName = value;
				    }
				    if(fieldName.equals("schemaID")){ 
				    	 schemaId = uploadItem.getString(); 
				    }
				    else if(fieldName.equals("testupload")){
				    	testAndUpload = true;
				    	break;
				    }				  				    
				  }
				  else {
					  String contentType = uploadItem.getContentType();
					  dataBytes = uploadItem.get();
					   dataBytes = uploadItem.get();
					  if(dataFileName.isEmpty()){
						  dataFileName = uploadItem.getName();
					 }				  
				}
				}
				session.setAttribute("DataFileName",dataFileName);
				if(!testAndUpload){
				int sampleDataId = 1;
				String scriptData = new String(dataBytes,"UTF-8");  
				try(Connection dbcon = (new DatabaseConnection()).dbConnection()){
					try(PreparedStatement stmnt = dbcon.prepareStatement("select " +
							"MAX(sampledata_id) as id from xdata_Sampledata")){
						try(ResultSet rset = stmnt.executeQuery()){
							if(rset.next()){
								sampleDataId= rset.getInt("id")+1;
							}
						}
					}
					try(PreparedStatement stmt =  dbcon.prepareStatement("insert into xdata_sampledata(sample_data_name,sample_data,course_id,schema_id,sampledata_id) values (?,?,?,?,?)")){
						stmt.setString(1,dataFileName);
						stmt.setString(2,scriptData); 
						stmt.setString(3, courseId);
						stmt.setInt(4, Integer.parseInt(schemaId)); 
						stmt.setInt(5,sampleDataId);
			
						stmt.executeUpdate(); 
					}
					/*try(PreparedStatement stmt =  dbcon.prepareStatement("update xdata_schemainfo set sample_data_name=?, sample_data = ? where course_id = ? and schema_id = ?")){
						stmt.setString(1,dataFileName);
						stmt.setString(2,scriptData); 
						stmt.setString(3, courseId);
						stmt.setInt(4, Integer.parseInt(schemaId)); 
			
						stmt.executeUpdate(); 
					
					}*///try block for stmt ends
				}//try block for Connection ends
				response.sendRedirect("sampleDataUpload.jsp?upload=success");	
				}	
				else if(testAndUpload){
					session.setAttribute("FileContents",new String(dataBytes,"UTF-8"));
					response.sendRedirect("connectionToTestUpload.jsp?schema_id="+schemaId+"-sampleDataUpload");
					return;
				}
		}
			catch(Exception err){ 
				logger.log(Level.SEVERE,err.getMessage(),err);
				throw new ServletException(err);
			}
		}  
			}

}
