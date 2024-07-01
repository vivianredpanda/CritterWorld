package ast;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.List;

public class Action extends AbstractNode implements UpdateOrAction{
    private Actions a;
    private Maybe<Expr> child;

    /**
     * Creates an Action node by setting action type and Expr child
     * @param a the sensing action
     * @param child Maybe representation of Expr child
     */
    public Action(Actions a, Maybe<Expr> child){
        this.a = a;
        this.child = child;
    }

    /** An enumeration of all possible sensing actions. */
    public enum Actions{
        WAIT,
        FORWARD,
        BACKWARD,
        LEFT,
        RIGHT,
        EAT,
        ATTACK,
        GROW,
        BUD,
        MATE,
        SERVE;
    }

    @Override
    public List<Node> getChildren() {
        try{
            ArrayList<Node> a = new ArrayList<>();
            a.add(child.get());
            return a;
        } catch (NoMaybeValue e) {
            return new ArrayList<>();
        }
    }

    /**
     * returns the sensing action of this node
     * @return sensing action of this node
     */
    public Actions getActions(){return a;}

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        sb.append(a.toString().toLowerCase());
        try {
            sb.append(" [ ");
            sb.append(child.get());
            sb.append(" ]");
        } catch (NoMaybeValue e) {
            sb.delete(sb.length()-3,sb.length());
        }
        return sb;
    }

    @Override
    public Node clone() {
        Action copy;
        try {
            Expr newChild = (Expr)child.get().clone();
            copy = new Action(a, Maybe.some(newChild));
            newChild.setParent(copy);
        } catch (NoMaybeValue e) {
            copy = new Action(a, Maybe.none());
        }
        return copy;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.ACTION;
    }

    @Override
    public boolean classInv() {
        //has to check only serve has expr that exists
        if(a != Actions.SERVE){
            assert(!child.isPresent());
        } else{
            assert(child.isPresent());
            try {
                Expr e = child.get();
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
        if(m != null && n instanceof Action) {
            a = ((Action)(n)).a;
            child =((Action)(n)).child;
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object n){
        if(!(n instanceof Action)){
            return false;
        }else{
            Action node = (Action) n;
            return node.getChildren().equals(getChildren()) && a==node.a;
        }
    }

}
