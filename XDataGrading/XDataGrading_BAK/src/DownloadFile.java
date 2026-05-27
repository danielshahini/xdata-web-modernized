

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// import org.apache.tomcat.dbcp.dbcp.DbcpException;

import database.DatabaseConnection;

/**
 * Servlet implementation class DownloadFile
 */
@WebServlet("/DownloadFile")
public class DownloadFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(DownloadFile.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DownloadFile() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String download = request.getParameter("download");
		logger.log(Level.FINE,"Download  = "+download);
		String courseId = (String) request.getSession().getAttribute(
				"context_label"); 
		String sampledataText = "";
		String schemaText = "";
		String downloadString = "";
		String downloadFileName = "";
		String zipFileName="";
		int schemaId = Integer.parseInt(request.getParameter("schemaId").trim());
		int sampleDataId  =0 ;
		if(request.getParameter("sampleDataID") != null){
		 sampleDataId = Integer.parseInt(request.getParameter("sampleDataID").trim());
		}
		int BUFSIZE = 4096; 
		InputStream inputStream = null;
		try(Connection dbcon = (new DatabaseConnection()).dbConnection()){		
			try(PreparedStatement stmt1= dbcon.prepareStatement("SELECT schema_name,ddltext from xdata_schemainfo where course_id=? and schema_id =?")){
				stmt1.setString(1,courseId);
				stmt1.setInt(2,schemaId); 
				try(ResultSet rs1 = stmt1.executeQuery()){
				List <File> files = new  ArrayList();
				
				if(rs1.next()){ 
					if(download.equalsIgnoreCase("schemaFile")){
						downloadString=rs1.getString("ddltext");
						downloadFileName = rs1.getString("schema_name");
						inputStream = rs1.getBinaryStream("ddltext");					
					}
					else if(download.equalsIgnoreCase("dataFile")){
						try(PreparedStatement stmt2= dbcon.prepareStatement("SELECT sample_data,sample_data_name from xdata_sampledata where course_id=? and schema_id =? and sampledata_id=?")){
							stmt2.setString(1,courseId);
							stmt2.setInt(2,schemaId);
							stmt2.setInt(3,sampleDataId);
							try(ResultSet rs2 = stmt2.executeQuery()){
							while(rs2.next()){
								//Get data file String, create a file and put the contents.Add to list
								downloadString=rs2.getString("sample_data");
								downloadFileName = rs2.getString("sample_data_name"); 
								inputStream = rs2.getBinaryStream("sample_data");
							}
							}
						}
					}
					 
					else{
						throw new ServletException("Internal error. May be the session expired, Please re-login");
						}
					}
				}//close PreparedStatement try block
			}//close RESULTSET try block
			
		try(ServletOutputStream outStream = response.getOutputStream()){
			response.setContentType("text/html");
			response.setContentLength(downloadString.length());
			int length = downloadString.length();
			String fileName = downloadFileName; 
			response.setHeader("Content-Disposition", "attachment; filename=\""+downloadFileName+"\"");

			byte[] byteBuffer = new byte[BUFSIZE];
			try(DataInputStream in = new DataInputStream(inputStream)){
			 
				while ((in != null) && ((length = in.read(byteBuffer)) != -1))
				{
					outStream.write(byteBuffer,0,length);
				} 
			}//try block for DataInputStream ends
		}//try block for outStream ends
	}catch(Exception e){			 
		logger.log(Level.SEVERE,e.getMessage(),e);
		throw new ServletException(e);
	} 

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
