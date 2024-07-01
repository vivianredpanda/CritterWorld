package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static ast.Sensor.Sense.SMELL;

public class Sensor extends Factor {
    private Sense s;
    private Maybe<Expr> e;

    /**
     * Constructs a Sensor using a Sense and a potential Expr
     * @param s the sensing action
     * @param e the potential Expr used in the sensing action
     */
    public Sensor(Sense s, Maybe<Expr> e){
        this.s = s;
        this.e = e;
    }

    /** An enumeration of all possible sensing actions. */
    public enum Sense{
        NEARBY,
        AHEAD,
        RANDOM,
        SMELL;
    }

    /**
     * Returns the sensing action of this instance
     * @return the Sense s in this instance
     */
    public Sense getSense(){
        return s;
    }


    @Override
    public List<Node> getChildren() {
        List<Node> temp = new ArrayList<>();
        try{
            temp.add(e.get());
        } catch (NoMaybeValue ex) {
        }
        return temp;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb){
        if(getNeg()){ sb.append("-("); }

        if(s == SMELL){
            return sb.append("smell");
        }

        try{
            Expr temp = e.get();
            switch(s){
                case NEARBY:
                    sb.append("nearby");
                    break;
                case AHEAD:
                    sb.append("ahead");
                    break;
                case RANDOM:
                    sb.append("random");
                    break;
            }

            sb.append(" [ ");
            sb.append(temp.toString());
            sb.append(" ]");

            if(getNeg()){ sb.append(")"); }
            return sb;

        } catch (NullPointerException e){
            throw new IllegalArgumentException(e); // decide on type of exception to return
        } catch (NoMaybeValue e) {
            throw new RuntimeException();
        }
    }

    @Override
    public Node clone() {
        Expr copy;
        try {
            Expr eCopy = (Expr) e.get().clone();
            copy = new Sensor(s, Maybe.some(eCopy));
            eCopy.setParent(copy);
            return copy;
        } catch (NoMaybeValue ex) {
            copy = new Sensor(s, Maybe.none());
        }
        if(isNeg){ copy.setNeg(true); }
        return copy;
    }

    @Override
    public boolean classInv(){
        // check e is Maybe.none() if s is SMELL
        if(s == SMELL){
            assert(!e.isPresent());
            try {
                Expr eCopy = e.get();
                assert(eCopy.classInv());
            } catch (NoMaybeValue ignored){}
        } else{
            assert(e.isPresent());
        }
        return true;
    }

    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Sensor) {
            s = ((Sensor)(n)).s;
            e = ((Sensor)(n)).e;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof Sensor)){
            return false;
        }else{
            Sensor node = (Sensor) n;
            return node.getChildren().equals(getChildren()) && s==node.s  && (node.getNeg() == getNeg());
        }
    }


}
