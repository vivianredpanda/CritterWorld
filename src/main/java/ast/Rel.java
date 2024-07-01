package ast;
import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** A representation of a binary relation operator: '<', '<=', '=', '>=', '>', or '!=' */
public class Rel extends Condition {
    private Expr l;
    private Expr r;
    private RelOperator op;

    /**
     * Create an AST representation of l op r.
     *
     * @param l the Expr left of the operator
     * @param op any RelOperator or null if it doesn't exist
     * @param r the Expr right of the operator or null if it doesn't exist
     */
    public Rel(Expr l, RelOperator op, Expr r){
        this.l = l;
        this.op = op;
        this.r = r;
    }

    /** An enumeration of all possible binary relation operators. */
    public enum RelOperator {
        LT,
        LE,
        EQ,
        GE,
        GT,
        NE;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> temp = new ArrayList<>();
        temp.add(l);
        temp.add(r);
        return temp;
    }

    /**
     * Returns the RelOperator of this instance
     * @return the RelOperator op
     */
    public RelOperator getOp(){
        return op;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb){
        sb.append(l.toString());

        switch(op){
            case LT: {
                sb.append(" < ");
                break;
            }
            case LE: {
                sb.append(" <= ");
                break;
            }
            case EQ: {
                sb.append(" = ");
                break;
            }
            case GE: {
                sb.append(" >= ");
                break;
            }
            case GT: {
                sb.append(" > ");
                break;
            }
            case NE: {
                sb.append(" != ");
                break;
            }
            default:
                sb.append(" ERROR RETRIEVING RELATION SYMBOL ");
        }

        sb.append(r.toString());
        return sb;
    }

    @Override
    public Node clone() {
        Expr lCopy = (Expr) l.clone();
        Expr rCopy = (Expr) r.clone();
        Node copy = new Rel(lCopy, op, rCopy);
        lCopy.setParent(copy);
        rCopy.setParent(copy);
        return copy;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.REL;
    }

    @Override
    public boolean classInv() {
        assert(l != null);
        assert(r != null);
        assert(op != null);
        assert(l.classInv());
        assert(r.classInv());
        return false;
    }

    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    // requires having a mutation to access the set method.

    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Rel) {
                l = ((Rel)(n)).l;
                op = ((Rel)(n)).op;
                r = ((Rel)(n)).r;
                return true;
            }
        return false;
    }
    @Override
    public boolean equals(Object n){
        if(!(n instanceof Rel)){
            return false;
        }else{
            Rel node = (Rel) n;
            return node.getChildren().equals(getChildren()) && op==node.op;
        }
    }
}
