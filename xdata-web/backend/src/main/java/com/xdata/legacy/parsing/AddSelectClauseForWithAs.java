

// package parsing;

// import java.util.Iterator;
// import java.util.logging.Level;
// import java.util.logging.Logger;

// import net.sf.jsqlparser.expression.AllValue;
// // import net.sf.jsqlparser.expression.Expression;
// import net.sf.jsqlparser.expression.AnalyticExpression;
// import net.sf.jsqlparser.expression.AnyComparisonExpression;
// import net.sf.jsqlparser.expression.ArrayConstructor;
// import net.sf.jsqlparser.expression.ArrayExpression;
// import net.sf.jsqlparser.expression.BinaryExpression;
// import net.sf.jsqlparser.expression.CaseExpression;
// import net.sf.jsqlparser.expression.CastExpression;
// import net.sf.jsqlparser.expression.CollateExpression;
// import net.sf.jsqlparser.expression.ConnectByRootOperator;
// import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
// import net.sf.jsqlparser.expression.DateValue;
// import net.sf.jsqlparser.expression.DoubleValue;
// import net.sf.jsqlparser.expression.Expression;
// import net.sf.jsqlparser.expression.ExpressionVisitor;
// import net.sf.jsqlparser.expression.ExtractExpression;
// import net.sf.jsqlparser.expression.Function;
// import net.sf.jsqlparser.expression.HexValue;
// import net.sf.jsqlparser.expression.IntervalExpression;
// import net.sf.jsqlparser.expression.JdbcNamedParameter;
// import net.sf.jsqlparser.expression.JdbcParameter;
// import net.sf.jsqlparser.expression.JsonAggregateFunction;
// import net.sf.jsqlparser.expression.JsonExpression;
// import net.sf.jsqlparser.expression.JsonFunction;
// import net.sf.jsqlparser.expression.KeepExpression;
// import net.sf.jsqlparser.expression.LambdaExpression;
// import net.sf.jsqlparser.expression.LongValue;
// import net.sf.jsqlparser.expression.MySQLGroupConcat;
// import net.sf.jsqlparser.expression.NextValExpression;
// import net.sf.jsqlparser.expression.NotExpression;
// import net.sf.jsqlparser.expression.NullValue;
// import net.sf.jsqlparser.expression.NumericBind;
// import net.sf.jsqlparser.expression.OracleHierarchicalExpression;
// import net.sf.jsqlparser.expression.OracleHint;
// import net.sf.jsqlparser.expression.OracleNamedFunctionParameter;
// import net.sf.jsqlparser.expression.OverlapsCondition;
// import net.sf.jsqlparser.expression.Parenthesis;
// import net.sf.jsqlparser.expression.RangeExpression;
// import net.sf.jsqlparser.expression.RowConstructor;
// import net.sf.jsqlparser.expression.RowGetExpression;
// import net.sf.jsqlparser.expression.SignedExpression;
// import net.sf.jsqlparser.expression.StringValue;
// import net.sf.jsqlparser.expression.StructType;
// import net.sf.jsqlparser.expression.TimeKeyExpression;
// import net.sf.jsqlparser.expression.TimeValue;
// import net.sf.jsqlparser.expression.TimestampValue;
// import net.sf.jsqlparser.expression.TimezoneExpression;
// import net.sf.jsqlparser.expression.TranscodingFunction;
// import net.sf.jsqlparser.expression.TrimFunction;
// import net.sf.jsqlparser.expression.UserVariable;
// import net.sf.jsqlparser.expression.VariableAssignment;
// import net.sf.jsqlparser.expression.WhenClause;
// import net.sf.jsqlparser.expression.XMLSerializeExpr;
// // import net.sf.jsqlparser.expression.WithinGroupExpression;
// import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
// import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseAnd;
// import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseLeftShift;
// import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseOr;
// import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseRightShift;
// import net.sf.jsqlparser.expression.operators.arithmetic.BitwiseXor;
// import net.sf.jsqlparser.expression.operators.arithmetic.Concat;
// import net.sf.jsqlparser.expression.operators.arithmetic.Division;
// import net.sf.jsqlparser.expression.operators.arithmetic.IntegerDivision;
// import net.sf.jsqlparser.expression.operators.arithmetic.Modulo;
// import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
// import net.sf.jsqlparser.expression.operators.arithmetic.Subtraction;
// import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
// import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
// import net.sf.jsqlparser.expression.operators.conditional.XorExpression;
// import net.sf.jsqlparser.expression.operators.relational.Between;
// import net.sf.jsqlparser.expression.operators.relational.ContainedBy;
// import net.sf.jsqlparser.expression.operators.relational.Contains;
// import net.sf.jsqlparser.expression.operators.relational.DoubleAnd;
// import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
// import net.sf.jsqlparser.expression.operators.relational.ExcludesExpression;
// import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
// import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
// import net.sf.jsqlparser.expression.operators.relational.FullTextSearch;
// import net.sf.jsqlparser.expression.operators.relational.GeometryDistance;
// import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
// import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
// import net.sf.jsqlparser.expression.operators.relational.InExpression;
// import net.sf.jsqlparser.expression.operators.relational.IncludesExpression;
// import net.sf.jsqlparser.expression.operators.relational.IsBooleanExpression;
// import net.sf.jsqlparser.expression.operators.relational.IsDistinctExpression;
// import net.sf.jsqlparser.expression.operators.relational.IsNullExpression;
// // import net.sf.jsqlparser.expression.operators.relational.ItemsListVisitor;
// import net.sf.jsqlparser.expression.operators.relational.JsonOperator;
// import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
// import net.sf.jsqlparser.expression.operators.relational.Matches;
// import net.sf.jsqlparser.expression.operators.relational.MemberOfExpression;
// import net.sf.jsqlparser.expression.operators.relational.MinorThan;
// import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
// // import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
// import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
// import net.sf.jsqlparser.expression.operators.relational.RegExpMatchOperator;
// // import net.sf.jsqlparser.expression.operators.relational.RegExpMySQLOperator;
// import net.sf.jsqlparser.expression.operators.relational.SimilarToExpression;
// import net.sf.jsqlparser.expression.operators.relational.TSQLLeftJoin;
// import net.sf.jsqlparser.expression.operators.relational.TSQLRightJoin;
// import net.sf.jsqlparser.schema.Column;
// import net.sf.jsqlparser.schema.Table;
// import net.sf.jsqlparser.statement.select.AllColumns;
// import net.sf.jsqlparser.statement.select.AllTableColumns;
// import net.sf.jsqlparser.statement.select.FromItem;
// import net.sf.jsqlparser.statement.select.FromItemVisitor;
// import net.sf.jsqlparser.statement.select.Join;
// // import net.sf.jsqlparser.statement.select.LateralParenthesedSelect;
// import net.sf.jsqlparser.statement.select.LateralSubSelect;
// import net.sf.jsqlparser.statement.select.ParenthesedFromItem;
// import net.sf.jsqlparser.statement.select.PlainSelect;
// import net.sf.jsqlparser.statement.select.Select;
// import net.sf.jsqlparser.statement.select.SelectItem;
// import net.sf.jsqlparser.statement.select.SelectItemVisitor;
// import net.sf.jsqlparser.statement.select.SelectVisitor;
// import net.sf.jsqlparser.statement.select.SetOperationList;
// // import net.sf.jsqlparser.statement.select.SubJoin;
// import net.sf.jsqlparser.statement.select.ParenthesedSelect;
// import net.sf.jsqlparser.statement.select.TableFunction;
// import net.sf.jsqlparser.statement.select.TableStatement;
// import net.sf.jsqlparser.statement.select.Values;
// // import net.sf.jsqlparser.statement.select.ValuesList;
// import net.sf.jsqlparser.statement.select.WithItem;

// public class AddSelectClauseForWithAs implements ExpressionVisitor {

// 	private static Logger logger = Logger.getLogger(AddSelectClauseForWithAs.class.getName()); 
	
// 	public AddSelectClauseForWithAs(){
		
// 	}
// 	public AddSelectClauseForWithAs(Select select){
// 		select.accept(this);
// 	}
// 	// public void getNewFromItem(FromItem frm){
// 	// 	frm.accept(this);
// 	// }
// 	/**
// 	 * This method gets the parsed WITH-AS fromItem and replaces the alias in the 
// 	 * given query 
// 	 * 
// 	 * @param existingQuery
// 	 * @param subS
// 	 * @return
// 	 */
// 	public String getNewQuery(Select existingQuery,FromItem subS){
		
// 		// subS.accept(this);
// 		String newQuery = "";
// 		AddSelectClauseForWithAs.logger.log(Level.INFO,"WITH AS - Original Query : \n"+existingQuery.toString());
		
// 		((PlainSelect)existingQuery).setFromItem(subS);
// 		newQuery = ((PlainSelect)existingQuery).toString();
		
// 		AddSelectClauseForWithAs.logger.log(Level.INFO,"WITH AS - Modified Query : \n"+newQuery);
// 		return newQuery;
// 	}
	
// 	/**
// 	 * This method gets the parsed WITH-AS in the join item and replaces the alias
// 	 * in the join with the SQL query part
// 	 * 
// 	 * @param index
// 	 * @param joinItem
// 	 * @param existingQuery
// 	 * @param subS
// 	 * @return
// 	 */
// 	public String getNewQuery(int index,Join joinItem, Select existingQuery,FromItem subS){
		
// 		// subS.accept(this);
// 		String newQuery = ""; 
// 		AddSelectClauseForWithAs.logger.log(Level.INFO,"WITH AS - Original JOIN Query : \n"+existingQuery.toString());
		
// 		// joinItem.getRightItem().accept(this);
		
// 		((PlainSelect)existingQuery).getJoins().get(index).setRightItem(subS);
// 		newQuery = ((PlainSelect)existingQuery).toString();
// 		AddSelectClauseForWithAs.logger.log(Level.INFO,"WITH AS - Modified JOIN Query : \n"+newQuery);
// 		return newQuery;
// 	}
	
	
// 	/**methods for fromItem **/
// 	// @Override
// 	// public void visit(Table tableName) {
// 	// 	String tableWholeName = tableName.getFullyQualifiedName();
// 	// }

// 	// @Override
// 	// public void visit(ParenthesedSelect ParenthesedSelect) {
// 	// 	ParenthesedSelect.getSelect().accept(this);
// 	// }


// 	// @Override
// 	// public void visit(WithItem arg0) {
// 	// 	// TODO Auto-generated method stub
// 	// 	arg0.accept(this);
// 	// }
	
// 	@Override
// 	public void visit(Select subjoin) {
// 		// TODO Auto-generated method stub
// 		subjoin.getPlainSelect().getJoin(0).getFromItem().accept(this);
// 		subjoin.getJoin().getRightItem().accept(this);
// 	}

// 	@Override
// 	public void visit(LateralParenthesedSelect arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(ValuesList arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	/**Methods for FromItemVisitor Ends **/
	
// 	@Override
// 	public void visit(PlainSelect plainSelect) {
// 		// TODO Auto-generated method stub
// 		plainSelect.getFromItem().accept(this);
		
// 		if (plainSelect.getJoins() != null) {
// 			for (Iterator joinsIt = plainSelect.getJoins().iterator(); joinsIt.hasNext();) {
// 				Join join = (Join) joinsIt.next();
// 				join.getRightItem().accept(this);
// 			}
// 		}
// 		if (plainSelect.getWhere() != null)
// 			plainSelect.getWhere().accept(this);
// 	}

	
// 	@Override
// 	public void visit(Addition addition) {
// 		visitBinaryExpression(addition);
// 	}

// 	@Override
// 	public void visit(AndExpression andExpression) {
// 		visitBinaryExpression(andExpression);
// 	}

// 	@Override
// 	public void visit(Between between) {
// 		between.getLeftExpression().accept(this);
// 		between.getBetweenExpressionStart().accept(this);
// 		between.getBetweenExpressionEnd().accept(this);
// 	}

// 	@Override
// 	public void visit(Column tableColumn) {
// 	}

// 	@Override
// 	public void visit(Division division) {
// 		visitBinaryExpression(division);
// 	}

// 	@Override
// 	public void visit(DoubleValue doubleValue) {
// 	}

// 	@Override
// 	public void visit(EqualsTo equalsTo) {
// 		visitBinaryExpression(equalsTo);
// 	}

// 	@Override
// 	public void visit(Function function) {
// 	}

// 	@Override
// 	public void visit(GreaterThan greaterThan) {
// 		visitBinaryExpression(greaterThan);
// 	}

// 	@Override
// 	public void visit(GreaterThanEquals greaterThanEquals) {
// 		visitBinaryExpression(greaterThanEquals);
// 	}

// 	@Override
// 	public void visit(InExpression inExpression) {
// 		inExpression.getLeftExpression().accept(this);
// 		inExpression.getRightExpression().accept(this);
// 	}


// 	@Override
// 	public void visit(IsNullExpression isNullExpression) {
// 	}

// 	@Override
// 	public void visit(JdbcParameter jdbcParameter) {
// 	}

// 	@Override
// 	public void visit(LikeExpression likeExpression) {
// 		visitBinaryExpression(likeExpression);
// 	}

// 	@Override
// 	public void visit(ExistsExpression existsExpression) {
// 		existsExpression.getRightExpression().accept(this);
// 	}

// 	@Override
// 	public void visit(LongValue longValue) {
// 	}

// 	@Override
// 	public void visit(MinorThan minorThan) {
// 		visitBinaryExpression(minorThan);
// 	}

// 	@Override
// 	public void visit(MinorThanEquals minorThanEquals) {
// 		visitBinaryExpression(minorThanEquals);
// 	}

// 	@Override
// 	public void visit(Multiplication multiplication) {
// 		visitBinaryExpression(multiplication);
// 	}

// 	@Override
// 	public void visit(NotEqualsTo notEqualsTo) {
// 		visitBinaryExpression(notEqualsTo);
// 	}

// 	@Override
// 	public void visit(NullValue nullValue) {
// 	}

// 	@Override
// 	public void visit(OrExpression orExpression) {
// 		visitBinaryExpression(orExpression);
// 	}

// 	@Override
// 	public void visit(Parenthesis parenthesis) {
// 		parenthesis.getExpression().accept(this);
// 	}

// 	@Override
// 	public void visit(StringValue stringValue) {
// 	}

// 	@Override
// 	public void visit(Subtraction subtraction) {
// 		visitBinaryExpression(subtraction);
// 	}

// 	public void visitBinaryExpression(BinaryExpression binaryExpression) {
// 		binaryExpression.getLeftExpression().accept(this);
// 		binaryExpression.getRightExpression().accept(this);
// 	}

// 	@Override
// 	public void visit(ExpressionList expressionList) {
// 		for (Iterator iter = expressionList.getExpressions().iterator(); iter.hasNext();) {
// 			Expression expression = (Expression) iter.next();
// 			expression.accept(this);
// 		}

// 	}

// 	@Override
// 	public void visit(DateValue dateValue) {
// 	}
	
// 	@Override
// 	public void visit(TimestampValue timestampValue) {
// 	}
	
// 	@Override
// 	public void visit(TimeValue timeValue) {
// 	}

// 	@Override
// 	public void visit(CaseExpression caseExpression) {
// 	}

// 	@Override
// 	public void visit(WhenClause whenClause) {
// 	}

// 	@Override
// 	public void visit(Expression Expression) {
// 		Expression.getParenthesedSelect().getSelect().accept(this);
// 	}

// 	@Override
// 	public void visit(AnyComparisonExpression anyComparisonExpression) {
// 		anyComparisonExpression.getParenthesedSelect().getSelect().accept(this);
// 	}

// 	// @Override
// 	// public void visit(MultiExpressionList arg0) {
// 	// 	// TODO Auto-generated method stub
		
// 	// }

// 	@Override
// 	public void visit(SignedExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(JdbcNamedParameter arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(Concat arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(Matches arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(BitwiseAnd arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(BitwiseOr arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(BitwiseXor arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(CastExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(Modulo arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(AnalyticExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	// @Override
// 	// public void visit(WithinGroupExpression arg0) {
// 	// 	// TODO Auto-generated method stub
		
// 	// }

// 	@Override
// 	public void visit(ExtractExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(IntervalExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(OracleHierarchicalExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(RegExpMatchOperator arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(JsonExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	// @Override
// 	// public void visit(RegExpMySQLOperator arg0) {
// 	// 	// TODO Auto-generated method stub
		
// 	// }

// 	@Override
// 	public void visit(UserVariable arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override 
// 	public void visit(NumericBind arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(KeepExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	@Override
// 	public void visit(MySQLGroupConcat arg0) {
// 		// TODO Auto-generated method stub
		
// 	}

// 	// @Override
// 	// public void visit(SetOperationList arg0) {
// 	// 	// TODO Auto-generated method stub
		
// 	// }
// 	@Override
// 	public void visit(HexValue arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(RowConstructor arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(OracleHint arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(TimeKeyExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(DateTimeLiteralExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	// @Override
// 	// public void visit(TableFunction arg0) {
// 	// 	// TODO Auto-generated method stub
		
// 	// }
// 	@Override
// 	public void visit(DoubleAnd arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(Contains arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(ContainedBy arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(JsonOperator arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	@Override
// 	public void visit(NotExpression arg0) {
// 		// TODO Auto-generated method stub
		
// 	}
// 	// @Override
// 	// public Object visit(SelectItem arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	@Override
// 	public Object visit(BitwiseRightShift arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(BitwiseLeftShift arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(NullValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Function arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(SignedExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JdbcParameter arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JdbcNamedParameter arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(DoubleValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(LongValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(HexValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(DateValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TimeValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TimestampValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(StringValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Addition arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Division arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IntegerDivision arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Multiplication arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Subtraction arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AndExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(OrExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(XorExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Between arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(OverlapsCondition arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(EqualsTo arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(GreaterThan arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(GreaterThanEquals arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(InExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IncludesExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ExcludesExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(FullTextSearch arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IsNullExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IsBooleanExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(LikeExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(MinorThan arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(MinorThanEquals arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(NotEqualsTo arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(DoubleAnd arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Contains arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ContainedBy arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Column arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(CaseExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(WhenClause arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ExistsExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(MemberOfExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AnyComparisonExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Concat arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Matches arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(BitwiseAnd arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(BitwiseOr arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(BitwiseXor arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(CastExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Modulo arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AnalyticExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ExtractExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IntervalExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(OracleHierarchicalExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(RegExpMatchOperator arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JsonExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JsonOperator arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(UserVariable arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(NumericBind arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(KeepExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(MySQLGroupConcat arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ExpressionList arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(RowConstructor arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(RowGetExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(OracleHint arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TimeKeyExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(DateTimeLiteralExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(NotExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(NextValExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(CollateExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(SimilarToExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ArrayExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ArrayConstructor arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(VariableAssignment arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(XMLSerializeExpr arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TimezoneExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JsonAggregateFunction arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(JsonFunction arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(ConnectByRootOperator arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(OracleNamedFunctionParameter arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AllColumns arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AllTableColumns arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(AllValue arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(IsDistinctExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(GeometryDistance arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(Select arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TranscodingFunction arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TrimFunction arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(RangeExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TSQLLeftJoin arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(TSQLRightJoin arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(StructType arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	@Override
// 	public Object visit(LambdaExpression arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	// @Override
// 	// public Object visit(Table arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(TableFunction arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(ParenthesedFromItem arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	@Override
// 	public Object visit(ParenthesedSelect arg0, Object arg1) {
// 		// TODO Auto-generated method stub
// 		throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	}
// 	// @Override
// 	// public Object visit(PlainSelect arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(SetOperationList arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(WithItem arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(Values arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(LateralSubSelect arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }
// 	// @Override
// 	// public Object visit(TableStatement arg0, Object arg1) {
// 	// 	// TODO Auto-generated method stub
// 	// 	throw new UnsupportedOperationException("Unimplemented method 'visit'");
// 	// }

	



// }

