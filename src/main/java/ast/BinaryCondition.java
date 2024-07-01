package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.List;

/** A representation of a binary Boolean condition: 'and' or 'or' */
public abstract class BinaryCondition extends Condition {
    private Condition l;
    private Maybe<Condition> r;
    private Maybe<Operator> op;

    /**
     * Create an AST representation of l op r.
     *
     * @param l the Condition left of the operator
     * @param op a Maybe of Operator
     * @param r a Maybe of Condition
     */
    public BinaryCondition(Condition l, Maybe<Operator> op, Maybe<Condition> r) {
        this.l = l;
        this.op = op;
        this.r = r;
    }

    /** An enumeration of all possible binary condition operators. */
    public enum Operator {
        OR,
        AND;
    }

    @Override
    public List<Node> getChildren() {
        List<Node> temp = new ArrayList<>();
        temp.add(l);
        try{
            temp.add(r.get());
        } catch (NoMaybeValue e) {
        }
        return temp;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb){
        try{
            if(op.contains(Operator.AND) && l instanceof OrCondition){
                sb.append("{ " + l.toString() + " }");
            }else{
                sb.append(l.toString());
            }
            sb.append(" " + op.get().toString().toLowerCase() + " ");
            if(op.contains(Operator.AND) && r.get() instanceof OrCondition){
                sb.append("{ " + r.get().toString() + " } ");
            }else{
                sb.append(r.get().toString());
            }
            return sb;
        } catch (NoMaybeValue e) {
            sb.substring(0,sb.length()-1);
            return sb;
        }
    }

    @Override
    public Node clone() {
        Condition copy;
        Condition lCopy;
        Condition rCopy;

        try {
            lCopy = (Condition) l.clone();
            Operator opCopy = op.get();
            rCopy = (Condition) r.get().clone();

            if (this instanceof AndCondition) {
                copy = new AndCondition(lCopy, opCopy, rCopy);
            } else {
                copy = new OrCondition(lCopy, opCopy, rCopy);
            }
        } catch (NoMaybeValue e){
            return null;
        }

        lCopy.setParent(copy);
        rCopy.setParent(copy);
        return copy;
    }

    @Override
    public boolean classInv() {
        //makes sure if there is an op, there is an r and vice versa
        assert(l.classInv());
        if(op.isPresent() || r.isPresent()){
            assert(op.isPresent() && r.isPresent());
            try {
                Condition e = r.get();
                assert(e.classInv());
            } catch (NoMaybeValue ignored){}
        }
        return true;
    }
    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof BinaryCondition) {
            l = ((BinaryCondition)(n)).l;
            op =((BinaryCondition)(n)).op;
            r = ((BinaryCondition)(n)).r;
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object n){
        if(!(n instanceof BinaryCondition)){
            return false;
        }else{
            BinaryCondition node = (BinaryCondition) n;
            return node.getChildren().equals(getChildren()) && op==node.op;
        }
    }
}
