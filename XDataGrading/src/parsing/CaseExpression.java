package parsing;

import java.io.Serializable;
import java.util.ArrayList;

public class CaseExpression implements Cloneable,Serializable{
	
	private static final long serialVersionUID = -7192918525557389737L;
	
	ArrayList<CaseCondition> whenConditionals;
	CaseCondition elseConditional;
	
	public ArrayList<CaseCondition> getWhenConditionals(){
		return whenConditionals;
	}
	
	public void setWhenConditionals(ArrayList<CaseCondition> conditionals){
		whenConditionals=conditionals;
	}
	
	public CaseCondition getElseConditional(){
		return elseConditional;
	}
	
	public void setElseConditional(CaseCondition condition){
		elseConditional=condition;
	}
	
	@Override
	public String toString(){
		String tempString="";
		for(CaseCondition cond:whenConditionals)
			tempString+=cond.toString()+", ";
		if(elseConditional!=null)
			tempString+=" ELSE "+elseConditional.getThenNode().toString();
		return tempString;
	}
	
	@Override
	public CaseExpression clone() throws CloneNotSupportedException{
		Object obj=super.clone();
		((CaseExpression)obj).setElseConditional(elseConditional.clone());
		ArrayList<CaseCondition> whenConds=(ArrayList<CaseCondition>)whenConditionals.clone();
		((CaseExpression)obj).setWhenConditionals(whenConds);
		return ((CaseExpression)obj);
	}
}
