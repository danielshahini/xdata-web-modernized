package partialMarking.queryEdit;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import parsing.ConjunctQueryStructure;
import parsing.Node;
import parsing.QueryStructure;
import util.Utilities;

public class JoinCondition implements QueryComponent {

	@Override
	public List<QueryStructure> edit(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		QueryStructure stu_not_matched = (QueryStructure)Utilities.copy(student);
		QueryStructure stu_matched = (QueryStructure)Utilities.copy(student);	
		QueryStructure ins_not_matched = (QueryStructure)Utilities.copy(instructor);
		for(Node t:instructor.getLstJoinConditions())
		{
			if(student.getLstJoinConditions().contains(t))
			{
				ins_not_matched.getLstJoinConditions().remove(t);
			}
		}
		for(Node t:student.getLstJoinConditions())
		{
			if(instructor.getLstJoinConditions().contains(t))
			{
				stu_not_matched.getLstJoinConditions().remove(t);
			}
			else
			{
				stu_matched.getLstJoinConditions().remove(t);
			}
		}
		for(Node st:stu_not_matched.getLstJoinConditions())
		{
			for(Node t: ins_not_matched.getLstJoinConditions())
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstJoinConditions().remove(st);
				temp.getLstJoinConditions().add(t);
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					conjunctElements.getJoinCondsForEquivalenceClasses().remove(st);
					conjunctElements.getJoinCondsForEquivalenceClasses().add(t);
					conjunctElements.getEquivalenceClasses().removeAll(conjunctElements.getEquivalenceClasses());
					conjunctElements.createEqClass();	
				}
				Vector<Vector<Node>> NewEqClass=new Vector<Vector<Node>>();
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					for(Vector<Node> EqClasses: conjunctElements.getEquivalenceClasses())
						NewEqClass.add(EqClasses);
				}
				//Changing the Equivalence Class (lstEqClasses) Each time
				temp.getLstEqClasses().removeAll(temp.getLstEqClasses());
				for(Vector<Node> EqClassElements : NewEqClass)
				{
					ArrayList<Node> EqClassArrayList = new ArrayList<Node>(EqClassElements);
					temp.getLstEqClasses().add(EqClassArrayList);
				}
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> add(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:instructor.getLstJoinConditions())
		{
			if(!student.getLstJoinConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstJoinConditions().add(t);
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					if(!conjunctElements.getJoinCondsForEquivalenceClasses().contains(t))
					{
						conjunctElements.getJoinCondsForEquivalenceClasses().add(t);
						conjunctElements.getEquivalenceClasses().removeAll(conjunctElements.getEquivalenceClasses());
						conjunctElements.createEqClass();	
					}
				}
				Vector<Vector<Node>> NewEqClass=new Vector<Vector<Node>>();
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					for(Vector<Node> EqClasses: conjunctElements.getEquivalenceClasses())
						NewEqClass.add(EqClasses);
				}
				//Changing the Equivalence Class (lstEqClasses) Each time
				temp.getLstEqClasses().removeAll(temp.getLstEqClasses());
				for(Vector<Node> EqClassElements : NewEqClass)
				{
					ArrayList<Node> EqClassArrayList = new ArrayList<Node>(EqClassElements);
					temp.getLstEqClasses().add(EqClassArrayList);
				}
				a.add(temp);
			}
		}
		return a;
	}

	@Override
	public List<QueryStructure> remove(QueryStructure student, QueryStructure instructor) throws Exception {
		List<QueryStructure> a = new ArrayList <QueryStructure>();
		for(Node t:student.getLstJoinConditions())
		{
			if(!instructor.getLstJoinConditions().contains(t))
			{
				QueryStructure temp = (QueryStructure)Utilities.copy(student);
				temp.getLstJoinConditions().remove(t);
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					if(conjunctElements.getJoinCondsForEquivalenceClasses().contains(t))
					{
						conjunctElements.getJoinCondsForEquivalenceClasses().remove(t);
						conjunctElements.getEquivalenceClasses().removeAll(conjunctElements.getEquivalenceClasses());
						conjunctElements.createEqClass();
						
					}
				}
				Vector<Vector<Node>> NewEqClass=new Vector<Vector<Node>>();
				for(ConjunctQueryStructure conjunctElements : temp.getConjuncts())
				{
					for(Vector<Node> EqClasses: conjunctElements.getEquivalenceClasses())
						NewEqClass.add(EqClasses);
				}
				
				//Changing the Equivalence Class (lstEqClasses) Each time
				temp.getLstEqClasses().removeAll(temp.getLstEqClasses());
				for(Vector<Node> EqClassElements : NewEqClass)
				{
					ArrayList<Node> EqClassArrayList = new ArrayList<Node>(EqClassElements);
					temp.getLstEqClasses().add(EqClassArrayList);
				}
				a.add(temp);
			}	
		}
		
		return a;
	}

}
