package com.xdata.partialmarking.core;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.arithmetic.Addition;
import net.sf.jsqlparser.expression.operators.arithmetic.Multiplication;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ExpressionComparator {

    public static boolean areEqual(Expression e1, Expression e2) {
        return areEqual(e1, e2, null, null);
    }

    public static boolean areEqual(Expression e1, Expression e2, java.util.Map<String, String> aliasMap1, java.util.Map<String, String> aliasMap2) {
        if (e1 == e2) return true;
        if (e1 == null || e2 == null) return false;

        while (e1 instanceof Parenthesis) e1 = ((Parenthesis) e1).getExpression();
        while (e2 instanceof Parenthesis) e2 = ((Parenthesis) e2).getExpression();

        if (e1 instanceof Between && e2 instanceof AndExpression) {
            boolean res = compareBetweenWithAnd((Between) e1, (AndExpression) e2, aliasMap1, aliasMap2);
            if (res) return true;
        }
        if (e1 instanceof AndExpression && e2 instanceof Between) {
            boolean res = compareBetweenWithAnd((Between) e2, (AndExpression) e1, aliasMap2, aliasMap1);
            if (res) return true;
        }
        
        if (e1 instanceof NotExpression && (e2 instanceof AndExpression || e2 instanceof OrExpression)) {
            boolean res = checkDeMorgan((NotExpression) e1, e2, aliasMap1, aliasMap2);
            if (res) return true;
        }
        if (e2 instanceof NotExpression && (e1 instanceof AndExpression || e1 instanceof OrExpression)) {
            boolean res = checkDeMorgan((NotExpression) e2, e1, aliasMap2, aliasMap1);
            if (res) return true;
        }

        if (checkCommutative(e1, e2, aliasMap1, aliasMap2)) return true;

        if (e1 instanceof Column && e2 instanceof Column) {
            Column c1 = (Column) e1;
            Column c2 = (Column) e2;
            String col1 = c1.getColumnName().toUpperCase();
            String col2 = c2.getColumnName().toUpperCase();
            String tab1 = c1.getTable() != null ? c1.getTable().getName().toUpperCase() : null;
            String tab2 = c2.getTable() != null ? c2.getTable().getName().toUpperCase() : null;

            if (tab1 != null && aliasMap1 != null && aliasMap1.containsKey(tab1)) {
                tab1 = aliasMap1.get(tab1);
            }
            if (tab2 != null && aliasMap2 != null && aliasMap2.containsKey(tab2)) {
                tab2 = aliasMap2.get(tab2);
            }
            
            if (tab1 != null && tab2 != null) {
                return col1.equals(col2) && tab1.equals(tab2);
            }
            return col1.equals(col2);
        }

        if (e1.getClass() != e2.getClass()) return false;

        if (e1 instanceof ComparisonOperator && e2 instanceof ComparisonOperator) {
            ComparisonOperator op1 = (ComparisonOperator) e1;
            ComparisonOperator op2 = (ComparisonOperator) e2;
            return areEqual(op1.getLeftExpression(), op2.getLeftExpression(), aliasMap1, aliasMap2) &&
                   areEqual(op1.getRightExpression(), op2.getRightExpression(), aliasMap1, aliasMap2);
        }

        if (e1 instanceof AndExpression && e2 instanceof AndExpression) {
            AndExpression a1 = (AndExpression) e1;
            AndExpression a2 = (AndExpression) e2;
            List<Expression> list1 = flattenBinary(a1, AndExpression.class);
            List<Expression> list2 = flattenBinary(a2, AndExpression.class);
            return compareExpressionLists(list1, list2, aliasMap1, aliasMap2);
        }

        if (e1 instanceof OrExpression && e2 instanceof OrExpression) {
            OrExpression o1 = (OrExpression) e1;
            OrExpression o2 = (OrExpression) e2;
            List<Expression> list1 = flattenBinary(o1, OrExpression.class);
            List<Expression> list2 = flattenBinary(o2, OrExpression.class);
            return compareExpressionLists(list1, list2, aliasMap1, aliasMap2);
        }
        
        if (e1 instanceof BinaryExpression && e2 instanceof BinaryExpression && 
           (e1 instanceof Addition || e1 instanceof Multiplication)) {
            BinaryExpression b1 = (BinaryExpression) e1;
            BinaryExpression b2 = (BinaryExpression) e2;
            if (areEqual(b1.getLeftExpression(), b2.getLeftExpression(), aliasMap1, aliasMap2) &&
                areEqual(b1.getRightExpression(), b2.getRightExpression(), aliasMap1, aliasMap2)) return true;
            return areEqual(b1.getLeftExpression(), b2.getRightExpression(), aliasMap1, aliasMap2) &&
                   areEqual(b1.getRightExpression(), b2.getLeftExpression(), aliasMap1, aliasMap2);
        }

        if (e1 instanceof Between && e2 instanceof Between) {
            Between b1 = (Between) e1;
            Between b2 = (Between) e2;
            return areEqual(b1.getLeftExpression(), b2.getLeftExpression(), aliasMap1, aliasMap2) &&
                   areEqual(b1.getBetweenExpressionStart(), b2.getBetweenExpressionStart(), aliasMap1, aliasMap2) &&
                   areEqual(b1.getBetweenExpressionEnd(), b2.getBetweenExpressionEnd(), aliasMap1, aliasMap2) &&
                   b1.isNot() == b2.isNot();
        }

        if (e1 instanceof InExpression && e2 instanceof InExpression) {
            InExpression i1 = (InExpression) e1;
            InExpression i2 = (InExpression) e2;
            if (i1.isNot() != i2.isNot()) return false;
            if (!areEqual(i1.getLeftExpression(), i2.getLeftExpression(), aliasMap1, aliasMap2)) return false;
            
            if (i1.getRightExpression() instanceof ParenthesedExpressionList && 
                i2.getRightExpression() instanceof ParenthesedExpressionList) {
                List<Expression> list1 = ((ParenthesedExpressionList) i1.getRightExpression()).getExpressions();
                List<Expression> list2 = ((ParenthesedExpressionList) i2.getRightExpression()).getExpressions();
                return compareExpressionLists(list1, list2, aliasMap1, aliasMap2);
            }
            return i1.toString().trim().equalsIgnoreCase(i2.toString().trim());
        }

        if (e1 instanceof StringValue && e2 instanceof StringValue) {
            return ((StringValue) e1).getValue().equalsIgnoreCase(((StringValue) e2).getValue());
        }

        if (e1 instanceof LongValue && e2 instanceof LongValue) {
            return ((LongValue) e1).getValue() == ((LongValue) e2).getValue();
        }
        
        if (e1 instanceof LongValue || e1 instanceof DoubleValue || e1 instanceof StringValue || e1 instanceof JdbcParameter) {
             String s1 = e1.toString().replaceAll("'", "");
             String s2 = e2.toString().replaceAll("'", "");
             try {
                 double d1 = Double.parseDouble(s1);
                 double d2 = Double.parseDouble(s2);
                 return d1 == d2;
             } catch (NumberFormatException ex) {
             }
        }

        if (e1 instanceof DoubleValue && e2 instanceof DoubleValue) {
            return ((DoubleValue) e1).getValue() == ((DoubleValue) e2).getValue();
        }

        if (e1 instanceof Addition && e2 instanceof Addition) {
            Addition a1 = (Addition) e1;
            Addition a2 = (Addition) e2;
            List<Expression> list1 = flattenBinary(a1, Addition.class);
            List<Expression> list2 = flattenBinary(a2, Addition.class);
            return compareExpressionLists(list1, list2, aliasMap1, aliasMap2);
        }

        if (e1 instanceof Multiplication && e2 instanceof Multiplication) {
            Multiplication m1 = (Multiplication) e1;
            Multiplication m2 = (Multiplication) e2;
            List<Expression> list1 = flattenBinary(m1, Multiplication.class);
            List<Expression> list2 = flattenBinary(m2, Multiplication.class);
            return compareExpressionLists(list1, list2, aliasMap1, aliasMap2);
        }

        if (e1 instanceof NotExpression && e2 instanceof NotExpression) {
             return areEqual(((NotExpression) e1).getExpression(), ((NotExpression) e2).getExpression(), aliasMap1, aliasMap2);
        }

        return e1.toString().trim().equalsIgnoreCase(e2.toString().trim());
    }

    private static boolean compareBetweenWithAnd(Between b, AndExpression and, Map<String, String> aliasMapB, Map<String, String> aliasMapAnd) {
        if (b.isNot()) return false;
        List<Expression> parts = flattenBinary(and, AndExpression.class);
        
        Expression left = b.getLeftExpression();
        while (left instanceof Parenthesis) left = ((Parenthesis) left).getExpression();
        Expression start = b.getBetweenExpressionStart();
        while (start instanceof Parenthesis) start = ((Parenthesis) start).getExpression();
        Expression end = b.getBetweenExpressionEnd();
        while (end instanceof Parenthesis) end = ((Parenthesis) end).getExpression();

        boolean part1Ok = false;
        boolean part2Ok = false;
        
        for (Expression p : parts) {
            while (p instanceof Parenthesis) p = ((Parenthesis) p).getExpression();
            if (p instanceof GreaterThanEquals) {
                GreaterThanEquals gte = (GreaterThanEquals) p;
                if (areEqual(gte.getLeftExpression(), left, aliasMapAnd, aliasMapB) && areEqual(gte.getRightExpression(), start, aliasMapAnd, aliasMapB)) {
                    part1Ok = true;
                } else if (areEqual(gte.getLeftExpression(), end, aliasMapAnd, aliasMapB) && areEqual(gte.getRightExpression(), left, aliasMapAnd, aliasMapB)) {
                    part2Ok = true;
                } else if (areEqual(gte.getLeftExpression(), left, aliasMapAnd, aliasMapB) && areEqual(gte.getRightExpression(), end, aliasMapAnd, aliasMapB)) {
                    part2Ok = true;
                } else if (areEqual(gte.getLeftExpression(), start, aliasMapAnd, aliasMapB) && areEqual(gte.getRightExpression(), left, aliasMapAnd, aliasMapB)) {
                    part1Ok = true;
                }
            } else if (p instanceof MinorThanEquals) {
                MinorThanEquals lte = (MinorThanEquals) p;
                if (areEqual(lte.getLeftExpression(), left, aliasMapAnd, aliasMapB) && areEqual(lte.getRightExpression(), end, aliasMapAnd, aliasMapB)) {
                    part2Ok = true;
                } else if (areEqual(lte.getLeftExpression(), start, aliasMapAnd, aliasMapB) && areEqual(lte.getRightExpression(), left, aliasMapAnd, aliasMapB)) {
                    part1Ok = true;
                } else if (areEqual(lte.getLeftExpression(), left, aliasMapAnd, aliasMapB) && areEqual(lte.getRightExpression(), start, aliasMapAnd, aliasMapB)) {
                    part1Ok = true;
                } else if (areEqual(lte.getLeftExpression(), end, aliasMapAnd, aliasMapB) && areEqual(lte.getRightExpression(), left, aliasMapAnd, aliasMapB)) {
                    part2Ok = true;
                }
            }
        }
        
        if (part1Ok && part2Ok) return true;

        String sAnd = and.toString().toLowerCase();
        String sB = b.getLeftExpression().toString().toLowerCase();
        return sAnd.contains(sB) && (sAnd.contains(">=") || sAnd.contains("<=")) && (sAnd.contains("and") || sAnd.contains("&&"));
    }

    private static boolean checkDeMorgan(NotExpression not, Expression binary, Map<String, String> aliasMapNot, Map<String, String> aliasMapBin) {
        Expression inner = not.getExpression();
        while (inner instanceof Parenthesis) inner = ((Parenthesis) inner).getExpression();
        
        if (inner instanceof AndExpression && binary instanceof OrExpression) {
            List<Expression> andParts = flattenBinary(inner, AndExpression.class);
            List<Expression> orParts = flattenBinary(binary, OrExpression.class);
            if (andParts.size() != orParts.size()) return false;
            
            return compareNotExpressionLists(andParts, orParts, aliasMapNot, aliasMapBin);
        }
        if (inner instanceof OrExpression && binary instanceof AndExpression) {
            List<Expression> orParts = flattenBinary(inner, OrExpression.class);
            List<Expression> andParts = flattenBinary(binary, AndExpression.class);
            if (orParts.size() != andParts.size()) return false;

            return compareNotExpressionLists(orParts, andParts, aliasMapNot, aliasMapBin);
        }
        return false;
    }

    private static boolean compareNotExpressionLists(List<Expression> innerParts, List<Expression> outerParts, Map<String, String> aliasMapInner, Map<String, String> aliasMapOuter) {
        if (innerParts.size() != outerParts.size()) return false;
        List<Expression> remainingOuter = new ArrayList<>(outerParts);
        for (Expression inP : innerParts) {
            boolean found = false;
            for (int i = 0; i < remainingOuter.size(); i++) {
                Expression outP = remainingOuter.get(i);
                while (outP instanceof Parenthesis) outP = ((Parenthesis) outP).getExpression();
                
                if (outP instanceof NotExpression) {
                    if (areEqual(inP, ((NotExpression) outP).getExpression(), aliasMapInner, aliasMapOuter)) {
                        remainingOuter.remove(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }

    private static boolean checkCommutative(Expression e1, Expression e2, java.util.Map<String, String> aliasMap1, java.util.Map<String, String> aliasMap2) {
        if (e1 instanceof GreaterThan && e2 instanceof MinorThan) {
            return areEqual(((GreaterThan) e1).getLeftExpression(), ((MinorThan) e2).getRightExpression(), aliasMap1, aliasMap2) &&
                   areEqual(((GreaterThan) e1).getRightExpression(), ((MinorThan) e2).getLeftExpression(), aliasMap1, aliasMap2);
        }
        if (e1 instanceof MinorThan && e2 instanceof GreaterThan) {
            return areEqual(((MinorThan) e1).getLeftExpression(), ((GreaterThan) e2).getRightExpression(), aliasMap1, aliasMap2) &&
                   areEqual(((MinorThan) e1).getRightExpression(), ((GreaterThan) e2).getLeftExpression(), aliasMap1, aliasMap2);
        }
        if (e1 instanceof GreaterThanEquals && e2 instanceof MinorThanEquals) {
            return areEqual(((GreaterThanEquals) e1).getLeftExpression(), ((MinorThanEquals) e2).getRightExpression(), aliasMap1, aliasMap2) &&
                   areEqual(((GreaterThanEquals) e1).getRightExpression(), ((MinorThanEquals) e2).getLeftExpression(), aliasMap1, aliasMap2);
        }
        if (e1 instanceof MinorThanEquals && e2 instanceof GreaterThanEquals) {
            return areEqual(((MinorThanEquals) e1).getLeftExpression(), ((GreaterThanEquals) e2).getRightExpression(), aliasMap1, aliasMap2) &&
                   areEqual(((MinorThanEquals) e1).getRightExpression(), ((GreaterThanEquals) e2).getLeftExpression(), aliasMap1, aliasMap2);
        }
        if (e1 instanceof EqualsTo && e2 instanceof EqualsTo) {
             return areEqual(((EqualsTo) e1).getLeftExpression(), ((EqualsTo) e2).getRightExpression(), aliasMap1, aliasMap2) &&
                    areEqual(((EqualsTo) e1).getRightExpression(), ((EqualsTo) e2).getLeftExpression(), aliasMap1, aliasMap2);
        }
        return false;
    }

    private static List<Expression> flattenBinary(Expression expr, Class<? extends BinaryExpression> type) {
        List<Expression> list = new ArrayList<>();
        while (expr instanceof Parenthesis) expr = ((Parenthesis) expr).getExpression();

        if (type.isInstance(expr)) {
            BinaryExpression be = (BinaryExpression) expr;
            list.addAll(flattenBinary(be.getLeftExpression(), type));
            list.addAll(flattenBinary(be.getRightExpression(), type));
        } else {
            list.add(expr);
        }
        return list;
    }

    private static boolean compareExpressionLists(List<Expression> list1, List<Expression> list2, Map<String, String> aliasMap1, Map<String, String> aliasMap2) {
        if (list1.size() != list2.size()) return false;
        List<Expression> remaining2 = new ArrayList<>(list2);
        for (Expression e1 : list1) {
            boolean found = false;
            for (int i = 0; i < remaining2.size(); i++) {
                if (areEqual(e1, remaining2.get(i), aliasMap1, aliasMap2)) {
                    remaining2.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (int i = 0; i < remaining2.size(); i++) {
                    if (e1.toString().trim().equalsIgnoreCase(remaining2.get(i).toString().trim())) {
                        remaining2.remove(i);
                        found = true;
                        break;
                    }
                }
            }
            if (!found) return false;
        }
        return true;
    }
}
