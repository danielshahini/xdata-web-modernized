package database;
import java.io.*;
//import javax.servlet.*;
import javax.servlet.http.*;

public class FileHandler {
  private String saveFile = "";
 public  String fileHandling(HttpServletRequest request,HttpServletResponse response) throws IOException{
	  //to get the content type information from JSP Request Header
     String contentType = request.getContentType();
   //here we are checking the content type is not equal to Null and as well as the passed data from mulitpart/form-data is greater than or equal to 0
     if ((contentType != null) && (contentType.indexOf("multipart/form-data") >= 0)) {
    	// try{
           DataInputStream in = new DataInputStream(request.getInputStream());
         //we are taking the length of Content type data
           int formDataLength = request.getContentLength();
           byte dataBytes[] = new byte[formDataLength];
           int byteRead = 0;
           int totalBytesRead = 0;
         //this loop converting the uploaded file into byte code
           while (totalBytesRead < formDataLength) {
                 byteRead = in.read(dataBytes, totalBytesRead, formDataLength);
                 totalBytesRead += byteRead;
           }
         //for saving the file name
           String file = new String(dataBytes);
           saveFile = file.substring(file.indexOf("filename=\"") + 10);
           saveFile = saveFile.substring(0, saveFile.indexOf("\n"));
           saveFile = saveFile.substring(saveFile.lastIndexOf("\\") + 1, saveFile.indexOf("\""));
           int lastIndex = contentType.lastIndexOf("=");
           String boundary = contentType.substring(lastIndex + 1, contentType.length());
           int pos;
         //extracting the index of file 
           pos = file.indexOf("filename=\"");
           pos = file.indexOf("\n", pos) + 1;
           pos = file.indexOf("\n", pos) + 1;
           pos = file.indexOf("\n", pos) + 1;
           
           int boundaryLocation = file.indexOf(boundary, pos) - 4;
           int startPos = ((file.substring(0, pos)).getBytes()).length;
           int endPos = ((file.substring(0, boundaryLocation)).getBytes()).length;
           //LINUX style of specifying the folder name
           saveFile = "/tmp/" + saveFile;
           File ff = new File(saveFile);
           
        // creating a new file with the same name and writing the content in new file
           FileOutputStream fileOut = new FileOutputStream(ff);
           fileOut.write(dataBytes, startPos, (endPos - startPos));
           fileOut.flush();
           fileOut.close();
    	
     }
     return saveFile;
     }
}
