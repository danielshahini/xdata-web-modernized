package com.xdata.legacy.stringSolver;
import java.util.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dk.brics.automaton.Automaton;
import dk.brics.automaton.State;
import dk.brics.automaton.Transition;

public class StringGraph {

	StringConstraintNode[] node;
	
	public StringGraph(int n){
		node=new StringConstraintNode[n];
		for(int i=0;i<n;i++){
			node[i]=new StringConstraintNode();
		}
	}
	
	boolean add(StringConstraint s){
		int l=s.getLeft();
		int r=s.getRight();
		
		if(s.getOperator().equalsIgnoreCase("=")){
			node[l].equals.add(r);
			node[r].equals.add(l);
		}
		else if(s.getOperator().equalsIgnoreCase("<")){
			node[l].less.add(r);
			node[r].greater.add(l);
		}
		else if(s.getOperator().equalsIgnoreCase("<=")){
			node[l].lessEqual.add(r);
			node[r].greaterEqual.add(l);
		}
		else if(s.getOperator().equalsIgnoreCase(">")){
			node[r].less.add(l);
			node[l].greater.add(r);
		}
		else if(s.getOperator().equalsIgnoreCase(">=")){
			node[r].lessEqual.add(l);
			node[l].greaterEqual.add(r);
		}
		return true;
	}
	
	boolean add(Vector<StringConstraint> v){
		for(StringConstraint  c :v){
			if(add(c)==false)
				return false;
		}
		return true;
	}
	
	String getSmallestStringFromAutomata(int varNo){
		int max=node[varNo].maxL;
		int min=node[varNo].minL;
		
		Automaton a=StringConstraint.giveAutomatonForConstraints(node[varNo].constraints);		
		
		String singleton = (String) getFieldValue(a, "singleton");
		State initial = (State) getFieldValue(a, "initial");

		if (singleton != null && singleton.length() >=min && singleton.length()<=max)
			return singleton;
		else if (max >= 0 && initial != null)
			return getStrings(initial, new StringBuilder(),min,max,node[varNo].notEqualLength, 0);
		return null;	
	}
	
	public static String getStrings(Automaton a, int min,int max) {
		String str=null;
		String singleton = (String) getFieldValue(a, "singleton");
		State initial = (State) getFieldValue(a, "initial");

		if (singleton != null && singleton.length() >=min && singleton.length()<=max)
			return singleton;
		else if (max >= 0 && initial != null)
			str=getStrings(initial, new StringBuilder(),min,max,null, 0);
		return str;
	}
	
	private static String getStrings(State s, StringBuilder path, int min,int max,Set<Integer> notEqualLengths,int length) {
		boolean isAccept = (Boolean) getFieldValue(s, "accept");
		if(length>=min && length<=max && isAccept && (notEqualLengths == null || !notEqualLengths.contains(length))){
			return path.toString();
		}
		
		if(length>max)
			return null;
		else{
			Collection<Transition> transitions = s.getTransitions();
			for (Transition t : transitions) {
				int tMin = (Integer) getFieldValue(t, "min");
				int tMax = (Integer) getFieldValue(t, "max");
				State tTo = (State) getFieldValue(t, "to");
				
				for (int n = tMin; n <= tMax; n+=1) {
					path.append((char)n);
					String str=getStrings(tTo, path, min,max,notEqualLengths,length +1);
					if (str!=null)
						return str;
					path.deleteCharAt(path.length() - 1);
				}
			}
		}
		return null;
	}
	
	private static Object getFieldValue(Object obj, String fieldName) {
		try {
			Field field = obj.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			return field.get(obj);
		} catch (Exception e) {
			return null;
		}
	}
	
	HashMap<Integer,String> solve(){
		HashMap<Integer,String> map= new HashMap<Integer,String>();
		while(true){
			boolean flag=false;
			Vector<Integer> toSolve=new Vector<Integer>();
			for(int i=0;i<node.length;i++){
				if(node[i].value >=0 &&node[i].less.size()==0 && node[i].lessEqual.size()==0){
					toSolve.add(i);
				}
			}
			if(toSolve.size()==0){
				for(int i=0;i<node.length;i++){
					if(node[i].value >=0 &&node[i].less.size()==0){
						toSolve.add(i);
					}
					else flag=true;
				}
			
				if(toSolve.size()==0)
					return null;
				else if(flag==false){
					toSolve.removeAllElements();
					for(int i=0;i<node.length;i++){
						if(node[i].value >=0 &&node[i].lessEqual.size()==0){
							toSolve.add(i);
						}
					}
				}
			}
			Map<Integer,String> temp=findValuesFor(toSolve);
			
			if(temp==null) //could not solve
				return null;
			
			for(int i:toSolve){      //mark it solved
				node[i].value=-1;			
			}
			
			for(int i=0;i<node.length;i++){
				if(node[i].value >=0){
					for(int j:toSolve){
						node[i].less.remove(Integer.valueOf(j));
						node[i].lessEqual.remove(Integer.valueOf(j));
					}
				}
			}
			map.putAll(temp);
			
			boolean solved=true;
			for(int i=0;i<node.length;i++){
				if(node[i].value>=0){
					solved=false;
					break;
				}
			}
			if(solved) return map;
		}
	}
	
	Map<Integer,String> findValuesFor(Vector<Integer> toSolve){
		HashMap<Integer,String> value=new HashMap<Integer,String>();
		for(int i:toSolve){
			String str=getSmallestStringFromAutomata(i);
			if(str==null) return null;
			value.put(i,str);
		}
		
		for(int j=0;j<node.length;j++){
			if(node[j].value <0) continue;
			for(int i:toSolve){
				String str=value.get(i);
				StringConstraint s= new StringConstraint();
				s.setLeft(j);
				s.setOperator("i~");
				s.setConstant(str+"");
				node[j].constraints.add(s);
			}
		}
		return value;
	}
}

class StringConstraintNode {
	int value = 0;
	int minL = 0;
	int maxL = 10;
	Vector<Integer> equals = new Vector<>();
	Vector<Integer> less = new Vector<>();
	Vector<Integer> lessEqual = new Vector<>();
	Vector<Integer> greater = new Vector<>();
	Vector<Integer> greaterEqual = new Vector<>();
	Vector<StringConstraint> constraints = new Vector<>();
	Set<Integer> notEqualLength = new HashSet<>();
}
