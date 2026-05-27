package com.xdata.partialmarking.core;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QueryStructure {
    private List<Expression> projectionExpressions = new ArrayList<>();
    private List<Expression> predicateExpressions = new ArrayList<>();
    private List<Expression> groupByExpressions = new ArrayList<>();
    private Expression havingExpression = null;
    private List<String> projections = new ArrayList<>();
    private List<String> relations = new ArrayList<>();
    private List<String> predicates = new ArrayList<>();
    private List<String> joins = new ArrayList<>();
    private List<Expression> joinExpressions = new ArrayList<>();
    private List<String> groupBy = new ArrayList<>();
    private List<String> orderBy = new ArrayList<>();
    private String having = null;
    private boolean distinct = false;
    private TableMap tableMap;
    private List<QueryStructure> subqueries = new ArrayList<>();
    private Map<String, String> aliasMap = new java.util.HashMap<>();
    private int level = 1;

    public QueryStructure(String sql, TableMap tableMap) throws Exception {
        this(sql, tableMap, 1);
    }

    public QueryStructure(String sql, TableMap tableMap, int level) throws Exception {
        this.tableMap = tableMap;
        this.level = level;
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            if (stmt instanceof Select) {
                processSelectBody((Select) stmt, true);
                normalizeColumns();
            }
        } catch (Throwable e) {
            String errorMsg = e.getMessage();
            if (e.getClass().getName().contains("TokenMgrException")) {
                errorMsg = "Lexical error: " + e.getMessage();
            }
            throw new Exception("SQL Syntax Error: " + errorMsg);
        }
    }

    private void normalizeColumns() {
        for (Expression e : projectionExpressions) normalize(e);
        for (Expression e : predicateExpressions) normalize(e);
        for (Expression e : groupByExpressions) normalize(e);
        for (Expression e : joinExpressions) normalize(e);
        if (havingExpression != null) normalize(havingExpression);
        
        projections = projectionExpressions.stream().map(Object::toString).collect(Collectors.toList());
        predicates = predicateExpressions.stream().map(Object::toString).collect(Collectors.toList());
        groupBy = groupByExpressions.stream().map(Object::toString).collect(Collectors.toList());
        if (havingExpression != null) having = havingExpression.toString();
    }

    private void normalize(Expression expr) {
        if (expr == null) return;
        expr.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(net.sf.jsqlparser.schema.Column column) {
                if (column.getTable() != null && column.getTable().getName() != null) {
                    String alias = column.getTable().getName();
                    String tableName = aliasMap.get(alias.toUpperCase());
                    if (tableName != null) {
                        column.getTable().setName(tableName);
                    }
                } else if (tableMap != null) {
                    // Spalte ohne Tabellen-Präfix: Suche in beteiligten Relationen
                    String colName = column.getColumnName().toUpperCase();
                    for (String relName : relations) {
                        com.xdata.partialmarking.core.Table t = tableMap.getTable(relName);
                        if (t != null && t.getColumns().containsKey(colName)) {
                            column.setTable(new net.sf.jsqlparser.schema.Table(t.getTableName()));
                            break;
                        }
                    }
                }
            }
        });
    }

    private void processSelectBody(Select select, boolean isMain) {
        if (select == null) return;
        if (select instanceof PlainSelect) {
            processPlainSelect((PlainSelect) select, isMain);
        } else if (select instanceof SetOperationList) {
            SetOperationList setOpList = (SetOperationList) select;
            if (setOpList.getSelects() != null) {
                for (Select body : setOpList.getSelects()) {
                    processSelectBody(body, isMain);
                }
            }
        } else if (select instanceof ParenthesedSelect) {
            processSelectBody(((ParenthesedSelect) select).getSelect(), isMain);
        }
    }

    private void processPlainSelect(PlainSelect plainSelect, boolean isMain) {
        if (plainSelect == null) return;
            
        if (plainSelect.getFromItem() != null) {
            if (plainSelect.getFromItem() instanceof Table) {
                relations.add(((Table) plainSelect.getFromItem()).getName().toUpperCase());
            } else {
                relations.add(plainSelect.getFromItem().toString());
            }
            handleFromItem(plainSelect.getFromItem());
            extractAlias(plainSelect.getFromItem());
        }

        if (plainSelect.getJoins() != null) {
            for (Join join : plainSelect.getJoins()) {
                joins.add(join.toString());
                if (join.getOnExpressions() != null && !join.getOnExpressions().isEmpty()) {
                    Expression onExpr = join.getOnExpressions().iterator().next();
                    joinExpressions.add(onExpr);
                    
                    String joinStr = (join.isInner() || (!join.isLeft() && !join.isRight() && !join.isFull() && !join.isCross())) ? "JOIN " : join.toString().split(" ON")[0] + " ";
                    joinStr += (join.getRightItem() instanceof Table ? ((Table)join.getRightItem()).getName() : join.getRightItem().toString()) + " ON " + onExpr.toString();
                    joins.set(joins.size() - 1, joinStr);
                } else {
                    joinExpressions.add(null);
                }
                extractAlias(join.getRightItem());
                if (join.getRightItem() instanceof Table) {
                    String tableName = ((Table) join.getRightItem()).getName().toUpperCase();
                    if (!relations.contains(tableName)) {
                        relations.add(tableName);
                    }
                }
            }
        }

        for (int i = 0; i < relations.size(); i++) {
            String rel = relations.get(i);
            for (Map.Entry<String, String> entry : aliasMap.entrySet()) {
                 if (rel.toUpperCase().endsWith(" " + entry.getKey())) {
                     relations.set(i, entry.getValue());
                     break;
                 }
            }
        }
        
        if (isMain) {
            this.distinct = (plainSelect.getDistinct() != null);
        }

        if (plainSelect.getSelectItems() != null) {
            for (SelectItem item : plainSelect.getSelectItems()) {
                projections.add(item.toString());
                projectionExpressions.add(item.getExpression());
            }
        }

        if (plainSelect.getWhere() != null) {
            processExpression(plainSelect.getWhere());
        }

        if (plainSelect.getGroupBy() != null) {
            for (Object exprObj : plainSelect.getGroupBy().getGroupByExpressions()) {
                if (exprObj instanceof Expression) {
                    Expression e = (Expression) exprObj;
                    groupBy.add(e.toString());
                    groupByExpressions.add(e);
                }
            }
        }

        if (plainSelect.getHaving() != null) {
            this.having = plainSelect.getHaving().toString();
            this.havingExpression = plainSelect.getHaving();
        }

        if (plainSelect.getOrderByElements() != null) {
            for (OrderByElement obe : plainSelect.getOrderByElements()) {
                orderBy.add(obe.toString());
            }
        }
    }

    private void extractAlias(FromItem item) {
        if (item instanceof Table) {
            Table t = (Table) item;
            String tableName = t.getName().toUpperCase();
            if (t.getAlias() != null) {
                aliasMap.put(t.getAlias().getName().toUpperCase(), tableName);
            }
            aliasMap.put(tableName, tableName);
        }
    }

    private void handleFromItem(FromItem fromItem) {
        if (fromItem instanceof ParenthesedSelect) {
            processSelectBody(((ParenthesedSelect) fromItem).getSelect(), false);
        }
    }

    private void processExpression(Expression expr) {
        if (expr instanceof net.sf.jsqlparser.expression.operators.conditional.AndExpression) {
            BinaryExpression be = (BinaryExpression) expr;
            processExpression(be.getLeftExpression());
            processExpression(be.getRightExpression());
        } else if (expr instanceof Parenthesis) {
            processExpression(((Parenthesis) expr).getExpression());
        } else {
            predicates.add(expr.toString());
            predicateExpressions.add(expr);
        }
    }

    public List<Expression> getProjectionExpressions() { return projectionExpressions; }
    public List<Expression> getPredicateExpressions() { return predicateExpressions; }
    public List<Expression> getJoinExpressions() { return joinExpressions; }
    public List<Expression> getGroupByExpressions() { return groupByExpressions; }
    public Expression getHavingExpression() { return havingExpression; }
    public Map<String, String> getAliasMap() { return aliasMap; }

    public int getLevel() { return level; }
    public List<String> getProjections() { return projections; }
    public List<String> getRelations() { return relations; }
    public List<String> getPredicates() { return predicates; }
    public List<String> getJoins() { return joins; }
    public List<String> getGroupBy() { return groupBy; }
    public List<String> getOrderBy() { return orderBy; }
    public String getHaving() { return having; }
    public boolean isDistinct() { return distinct; }
    public List<QueryStructure> getSubqueries() { return subqueries; }
}
