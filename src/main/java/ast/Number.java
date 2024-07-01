package ast;

import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.List;

/** A representation of an integer value */
public class Number extends Factor {
    private int val;

    /**
     * Creates a Number with value val
     * @param val the integer value of Number
     */
    public Number(int val){
        this.val = val;
    }

    /**
     * @return value stored in this number
     */
    public int getVal(){ return val; }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        if(getNeg()){ sb.append("-"); }
        sb.append(val);
        return sb;
    }
    @Override
    public Node clone() {
        Number copy = new Number(val);
        if(isNeg){ copy.setNeg(true); }
        return copy;
    }

    @Override
    public boolean classInv() {
        assert(val >= 0);
        return true;
    }
    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Number) {
            val = ((Number)(n)).val;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof Number)){
            return false;
        }else{
            Number node = (Number) n;
            return val == node.val  && (node.getNeg() == getNeg());
        }
    }
}
