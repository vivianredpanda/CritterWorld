package ast;

import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.List;

public class MemFactor extends Factor {
    private Expr e;

    /**
     * Creates a representation of mem[e] that can be evaluated to a number
     * @param e the Expr contained in the mem
     */
    public MemFactor(Expr e){
        this.e = e;
    }

    @Override
    public Node clone() {
        Expr eCopy = (Expr) e.clone();
        Expr copy = new MemFactor(eCopy);
        copy.setNeg(e.getNeg());
        eCopy.setParent(copy);
        if(isNeg){ copy.setNeg(true); }
        return copy;
    }

    @Override
    public List<Node> getChildren() {
        ArrayList<Node> children = new ArrayList<>();
        children.add(e);
        return children;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        sb.append("mem [ ");
        sb = e.prettyPrint(sb);
        sb.append(" ]");
        return sb;
    }

    @Override
    public boolean classInv() {
        // e cannot be null
        // technically e must evaluate to value in range [1,6]
        assert(e != null);
        assert(e.classInv());
        return true;
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof MemFactor) {
            e = ((MemFactor)(n)).e;
            return true;
        }
        return false;
    }

    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof MemFactor)){
            return false;
        }else{
            MemFactor node = (MemFactor) n;
            return e.equals(node.e) && (node.getNeg() == getNeg());
        }
    }
}
