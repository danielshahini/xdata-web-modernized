import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import com.opensymphony.xwork2.ActionSupport;
import partialMarking.*;

public class ComputePartialMarks extends ActionSupport implements ServletRequestAware{
  
		private String instructorQuery; 
		private String studentQuery;
		private float marks;		
		private float naiveMarks;
		private String errorMessage;
		private String rootDir;
		private String hiddenString;
		private String textareaCount;
		boolean shouldCanonicalize;
		
		public HttpServletRequest request;

		 @Override
		  public void setServletRequest(HttpServletRequest request) {		 
		    this.request = request;  
		    }
		 
		 public void setRootDir(String s){
			 rootDir=s;
		 }
		 public String getRootDir(){
			 return rootDir;
		 }
		
		 public boolean getShouldCanonicalize(){
			 return shouldCanonicalize;
		 }
		 
		 public void setShouldCanonicalize(boolean val){
			 shouldCanonicalize=val;
		 }

		 
		 public String getHiddenString(){
			 return hiddenString;
		 }
		 public void setHiddenString(String str){
			 hiddenString=str;
		 }
		 
		 public String getTextareaCount(){
			 return textareaCount;
		 }
		 public void setTextareaCount(String s){
			 textareaCount=s;
		 }
		 
		public String getInstructorQuery() {  
			return instructorQuery;  
		}  
		public void setInstructorQuery(String query){
			this.instructorQuery=query;
		}
		public void setStudentQuery(String query) {  
			this.studentQuery = query;  
		}  
		public String getStudentQuery() {  
			return studentQuery;  
		}  
		
		public float getMarks(){
			return marks;
		}
		
		public float getNaiveMarks(){
			return naiveMarks;
		}
				
		public String getErrorMessage(){
			return errorMessage;
		}

		
		public static void main(String[] args){
			TestPartialMarking canonicalizedObj=new TestPartialMarking();
			TestPartialMarking naiveObj=new TestPartialMarking();
			
			String studentQuery="SELECT ID FROM TEACHES WHERE EXISTS (SELECT ID FROM INSTRUCTOR WHERE INSTRUCTOR.ID=TEACHES.ID)";
	       String instructorQuery="With T1(ID,semester,year,time_slot_id) as (select ID,semester,year,time_slot_id from (select ID,semester,year from takes) as T4 natural join section),  "
	       		+ "T2(ID,semester,year,time_slot_id) as (select ID,semester,year,time_slot_id from (select ID,semester,year,course_id from takes) as T5  natural join section), "
	       		+ "T3(ID,semester,year,time_slot_id) as (select ID,semester,year,time_slot_id from (select ID,semester,year,sec_id from takes) as T6  natural join section),  "
	       		+ "T7(ID,semester,year,time_slot_id) as ((select * from T2) union (select * from T3)) "
	       		+ "select distinct ID from ((select * from T1) except (select * from T7)) as T";
	       instructorQuery="SELECT DISTINCT INSTRUCTOR.ID,  D.budget FROM  INSTRUCTOR, DEPARTMENT D WHERE INSTRUCTOR.dept_name=D.dept_name";
			try{
				//marks=this.studentQuery  + " : "+ this.instructorQuery;
				canonicalizedObj.StudentQuery=canonicalizedObj.processCanonicalize(canonicalizedObj.StudentQuery, studentQuery);
				naiveObj.StudentQuery=naiveObj.process(naiveObj.StudentQuery, studentQuery);
				canonicalizedObj.InstructorQuery=canonicalizedObj.processCanonicalize(canonicalizedObj.InstructorQuery, instructorQuery);		
				naiveObj.InstructorQuery=naiveObj.process(naiveObj.InstructorQuery, instructorQuery);
		
				Float studMarks=partialMarking.PartialMarker.calculateScore( canonicalizedObj.InstructorQuery.getQueryStructure(), canonicalizedObj.StudentQuery.getQueryStructure(), 0).Marks;
				Float instMarks=partialMarking.PartialMarker.calculateScore(canonicalizedObj.InstructorQuery.getQueryStructure(), canonicalizedObj.InstructorQuery.getQueryStructure(), 0).Marks;
				
				Float studMarksNaive=partialMarking.PartialMarker.calculateScore( naiveObj.InstructorQuery.getQueryStructure(), naiveObj.StudentQuery.getQueryStructure(), 0).Marks;
				Float instMarksNaive=partialMarking.PartialMarker.calculateScore(naiveObj.InstructorQuery.getQueryStructure(), naiveObj.InstructorQuery.getQueryStructure(), 0).Marks;
				
				Float newMarks=studMarks*100/instMarks;
				Float newNaiveMarks=studMarksNaive*100/instMarksNaive;
				
				SerializeXML.serializeXML("instructorCan.xml", canonicalizedObj.InstructorQuery.getQueryStructure());		
				SerializeXML.serializeXML("studentCan.xml", canonicalizedObj.StudentQuery.getQueryStructure());
				SerializeXML.serializeXML("instructorNaive.xml", naiveObj.InstructorQuery.getQueryStructure());		
				SerializeXML.serializeXML("studentNaive.xml", naiveObj.StudentQuery.getQueryStructure());


				System.out.println("student marks:"+newMarks+ " instructor marks"+instMarks+ " normalizedMarks"+newNaiveMarks); 
			}
			catch(Exception e){				
				e.printStackTrace();
			}

		}

		@Override
		public String execute(){
		TestPartialMarking canonicalizedObj=new TestPartialMarking();
		TestPartialMarking naiveObj=new TestPartialMarking();
		
		setRootDir(request.getSession().getServletContext().getRealPath("/"));
		String instructorQueries[]=instructorQuery.split(",,&,,");
		QueryStructure bestInstructorQueryData=null;
		QueryStructure bestInstructorQueryDataNaive=null;
		String bestInstructorQueryString="";
		try{
			//marks=this.studentQuery  + " : "+ this.instructorQuery;
			Exception caughtException=null;
			canonicalizedObj.StudentQuery=canonicalizedObj.processCanonicalize(canonicalizedObj.StudentQuery, studentQuery);
				naiveObj.StudentQuery=naiveObj.process(naiveObj.StudentQuery, studentQuery);
			try{
				for(String instQuery:instructorQueries){
					canonicalizedObj.InstructorQuery=canonicalizedObj.processCanonicalize(canonicalizedObj.InstructorQuery, instQuery);		
						naiveObj.InstructorQuery=naiveObj.process(naiveObj.InstructorQuery, instQuery);
					
					Float studMarks=partialMarking.PartialMarker.calculateScore(canonicalizedObj.InstructorQuery.getQueryStructure(), canonicalizedObj.StudentQuery.getQueryStructure(), 0).Marks;
					Float instMarks=partialMarking.PartialMarker.calculateScore(canonicalizedObj.InstructorQuery.getQueryStructure(), canonicalizedObj.InstructorQuery.getQueryStructure(), 0).Marks;
					
					Float studMarksNaive=partialMarking.PartialMarker.calculateScore(naiveObj.InstructorQuery.getQueryStructure(), naiveObj.StudentQuery.getQueryStructure(), 0).Marks;
					Float instMarksNaive=partialMarking.PartialMarker.calculateScore(naiveObj.InstructorQuery.getQueryStructure(), naiveObj.InstructorQuery.getQueryStructure(), 0).Marks;
					
					Float newMarks=studMarks*100.0f/instMarks;
					Float newNaiveMarks=studMarksNaive*100.0f/instMarksNaive;
					
					if(newMarks>=marks){
						marks=newMarks;
						naiveMarks=newNaiveMarks;
						
						bestInstructorQueryData=canonicalizedObj.InstructorQuery.getQueryStructure();
						bestInstructorQueryDataNaive=naiveObj.InstructorQuery.getQueryStructure();
						bestInstructorQueryString=instQuery;
					}
				}
			}
			catch(Exception e){
				errorMessage="Error!\n"+ e.getMessage();
				caughtException=e;
			}
			if(bestInstructorQueryData==null && caughtException!=null){
				throw caughtException;
			}
			partialMarking.SerializeXML.serializeXML(getRootDir()+"/instructorCan.xml", bestInstructorQueryData);		
			partialMarking.SerializeXML.serializeXML(getRootDir()+"/studentCan.xml", canonicalizedObj.StudentQuery.getQueryStructure());
			partialMarking.SerializeXML.serializeXML(getRootDir()+"/instructorNaive.xml", bestInstructorQueryDataNaive);		
			partialMarking.SerializeXML.serializeXML(getRootDir()+"/studentNaive.xml", naiveObj.StudentQuery.getQueryStructure());

			setInstructorQuery(bestInstructorQueryString);

		}
		catch(Exception e){
			errorMessage="Error!\n"+ e.getStackTrace().toString();
			//e.printStackTrace();
			return "error";
		}

			return "success";  
		}  
	
}