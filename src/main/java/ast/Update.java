package ast;

import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Update extends AbstractNode implements UpdateOrAction{
    Expr memExpr;
    Expr expression;

    /**
     * Constructs an Update node with the Expr in the mem and the Expr the mem location is set to
     * @param mem the Expr that references location in mem
     * @param ex the Expr that the value stored at mem will be set to
     */
    public Update(Expr mem, Expr ex){
        memExpr = mem;
        expression = ex;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<Node>(Arrays.asList(memExpr,expression));
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        sb.append("mem [ ");
        sb.append(memExpr.toString());
        sb.append(" ] := ");
        sb.append(expression.toString());
        return sb;
    }

    @Override
    public Node clone() {
        Expr lCopy = (Expr) memExpr.clone();
        Expr rCopy = (Expr) expression.clone();
        Update copy = new Update(lCopy, rCopy);
        lCopy.setParent(copy);
        rCopy.setParent(copy);
        return copy;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.UPDATE;
    }

    @Override
    public boolean classInv() {
        assert(memExpr != null);
        assert(expression != null);
        assert(memExpr.classInv());
        assert(memExpr.classInv());
        return true;
    }

    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Update) {
            memExpr = ((Update)(n)).memExpr;
            expression = ((Update)(n)).expression;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof Update)){
            return false;
        }else{
            Update node = (Update) n;
            return node.getChildren().equals(getChildren());
        }
    }
 }
