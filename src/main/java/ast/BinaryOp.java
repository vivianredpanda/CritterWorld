package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.List;

/** A representation of a binary math operator: '+', '-', '*', '/' or 'mod' */
public abstract class BinaryOp extends Expr {
    private Expr l;
    private Maybe<Expr> r;
    private Maybe<BOperator> op;

    /**
     * Create an AST representation of l op r.
     *
     * @param l the Expr left of the operator
     * @param op a Maybe of the BOperator
     * @param r a Maybe of the Expr right of the operator
     */
    public BinaryOp(Expr l, Maybe<BOperator> op, Maybe<Expr> r){
        this.l = l;
        this.r = r;
        this.op = op;
    }

    /** An enumeration of all possible binary math operators. */
    public enum BOperator {
        ADD,
        SUB,
        MUL,
        DIV,
        MOD;
    }

    /**
     * Returns the BOperator value
     * @return value of BOperator op
     */
    public Maybe<BOperator> getOp(){
        return op;
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
    public StringBuilder prettyPrint(StringBuilder sb) {
        if(this.getNeg()){ sb.append("-("); }

        try{
            switch(op.get()){
                case ADD: {
                    addParenLeft(sb);
                    sb.append(" + ");
                    addParenRight(sb);
                    break;
                }
                case SUB: {
                    addParenLeft(sb);
                    sb.append(" - ");
                    addParenRight(sb);
                    break;
                }
                case MUL: {
                    addParenLeft(sb);
                    // sb.append(l);
                    sb.append(" * ");
                    // sb.append(r);
                    addParenRight(sb);
                    break;
                }
                case DIV: {
                    addParenLeft(sb);
                    sb.append(" / ");
                    addParenRight(sb);
                    break;
                }
                case MOD: {
                    addParenLeft(sb);
                    sb.append(" mod ");
                    addParenRight(sb);
                    break;
                }
                default:
                    sb.append(" ERROR RETRIEVING BINARYOP SYMBOL ");
            }
        } catch (NoMaybeValue e){
        }

        if(getNeg()){ sb.append(")"); }
        return sb;
    }

    private StringBuilder addParenRight(StringBuilder sb){
        //checks and adds parentheses on Expr to the right of the op
        try{
            if(op.contains(BOperator.SUB) && r.get() instanceof AddOp){
                sb.append("( " + r.get().toString() + " )");
            }else if(this instanceof MulOp && r.get() instanceof AddOp){
                sb.append("( " + r.get().toString() + " )");
            }else if((op.contains(BOperator.MOD) || op.contains(BOperator.DIV)) && r.get() instanceof BinaryOp){
                sb.append("( " + r.get().toString() + " )");
            }else if(op.contains(BOperator.MUL) && r.get() instanceof MulOp){
                MulOp temp = (MulOp) r.get();
                if(temp.getOp().contains(BOperator.MOD)){
                    sb.append("( " + r.get().toString() + " )");
                } else{
                    sb.append(r.get().toString());
                }
            }else{
                sb.append(r.get().toString());
            }
        } catch (NoMaybeValue e) {
        }
        return sb;
    }

    private StringBuilder addParenLeft(StringBuilder sb){
        //checks and adds parenthesis on Expr to the left of op
        if(this instanceof MulOp && l instanceof AddOp){
            sb.append("( " + l.toString() + " )");
        }else{
            sb.append(l.toString());
        }
        return sb;
    }

    @Override
    public Node clone() {
        Expr copy;
        Expr lCopy;
        Expr rCopy;

        try {
            lCopy = (Expr) l.clone();
            BOperator opCopy = op.get();
            rCopy = (Expr) r.get().clone();

            if(this instanceof AddOp){
                copy = new AddOp(lCopy, opCopy, rCopy);
            }else{
                copy = new MulOp(lCopy, opCopy, rCopy);
            }
        } catch (NoMaybeValue e){
            return null;
        }

        lCopy.setParent(copy);
        rCopy.setParent(copy);
        if(isNeg){ copy.setNeg(true); }
        return copy;
    }

    @Override
    public boolean classInv() {
        assert(l.classInv());
        if(op.isPresent() || r.isPresent()){
            assert(op.isPresent() && r.isPresent());
            try {
                Expr e = r.get();
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
        if(m != null && n instanceof BinaryOp) {
            l = ((BinaryOp)(n)).l;
            op =((BinaryOp)(n)).op;
            r = ((BinaryOp)(n)).r;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof BinaryOp)){
            return false;
        }else{
            BinaryOp node = (BinaryOp) n;
            return node.getChildren().equals(getChildren()) && op==node.op  && (node.getNeg() == getNeg());
        }
    }
}
