

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import database.DatabaseConnection;

/**
 * Servlet implementation class showSchemaFile
 */
@WebServlet("/showSchemaFile")
public class ShowSchemaFile extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static Logger logger = Logger.getLogger(ShowSchemaFile.class.getName());   
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ShowSchemaFile() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();		  
		String schemaID = (String)request.getParameter("schema_id");
		int i=Integer.parseInt(schemaID); 
		String schemaName = ""; 
		String fileContent= "";
		
		try(Connection conn= (new DatabaseConnection()).dbConnection()){
			try(PreparedStatement stmt = conn.prepareStatement("select schema_name,ddltext from xdata_schemainfo where schema_id = ?")){
				stmt.setInt(1, i);
				try(ResultSet result = stmt.executeQuery()){  
					if(result.next()){
						schemaName=result.getString(1); 
						fileContent = result.getString("ddltext");		 
					} 
				}//try block for ResultSet ends
			}//try block for stmt ends
		}catch(Exception e){
			logger.log(Level.SEVERE,e.getMessage(),e);
			throw new ServletException(e);
		}		
	}

	/** 
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
