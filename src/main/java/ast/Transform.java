package ast;

import cms.util.maybe.Maybe;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import cms.util.maybe.NoMaybeValue;

public class Transform extends AbstractMutation {

    @Override
    public Maybe<Node> visit(Program n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Rule n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryCondition n){
        // construct a new node with same children
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            Iterator<Node> iter = children.iterator();
            // generates a new random binary condition operator
            BinaryCondition.Operator[] options = BinaryCondition.Operator.values();
            BinaryCondition.Operator v = options[r.nextInt(options.length)];
            if(v==BinaryCondition.Operator.AND){
                n = new AndCondition((Condition) iter.next(), v, (Condition) iter.next());
            }else {
                n = new OrCondition((Condition) iter.next(), v, (Condition) iter.next());
            }
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    @Override
    public Maybe<Node> visit(Command n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Action n){
        // construct a new node with same children
        List<Node> children = n.getChildren();
        // 1 child means it has to stay as serve
        if(children.size()==1) {
            return Maybe.none();
        }else{
            // generates a new random action
            Action.Actions[] options = Action.Actions.values();
            Action.Actions v = options[r.nextInt(options.length)];
            n = new Action(v, Maybe.none());
            return Maybe.from(n);
        }
    }

    @Override
    public Maybe<Node> visit(BinaryOp n) {
        // construct a new node with same children
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            Iterator<Node> iter = children.iterator();
            // generates a new random rel operator
            BinaryOp.BOperator[] options = BinaryOp.BOperator.values();
            BinaryOp.BOperator v = options[r.nextInt(options.length)];
            if(v==BinaryOp.BOperator.ADD || v==BinaryOp.BOperator.SUB) {
                n = new AddOp((Expr) iter.next(), v, (Expr) iter.next());
            }else{
                n = new MulOp((Expr) iter.next(), v, (Expr) iter.next());
            }
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    @Override
    public Maybe<Node> visit(Rel n){
        // construct a new node with same children
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            Iterator<Node> iter = children.iterator();
            // generates a new random rel operator
            Rel.RelOperator[] options = Rel.RelOperator.values();
            Rel.RelOperator v = options[r.nextInt(options.length)];
            n = new Rel((Expr) iter.next(), v, (Expr) iter.next());
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    @Override
    public Maybe<Node> visit(Number n){
        return Maybe.from(new Number(Math.abs(Integer.MAX_VALUE/r.nextInt())));
    }

    @Override
    public Maybe<Node> visit(Sensor n){
        // construct a new node with same children
        List<Node> children = n.getChildren();
        //0 child means it has to stay as smell
        if(children.size()==0) {
            return Maybe.none();
        }else{
            Iterator<Node> iter = children.iterator();
            // generates a new random sensor
            Sensor.Sense[] options = Sensor.Sense.values();
            Sensor.Sense v = options[r.nextInt(options.length)];
            while(v==Sensor.Sense.SMELL){
                v = options[r.nextInt(options.length)];
            }
            n = new Sensor(v, Maybe.from((Expr)iter.next()));
            return Maybe.from(n);
        }
    }

    @Override
    public Maybe<Node> visit(Update n){
        return Maybe.none();
    }

    @Override
    public String getType(){return "Transform";}

    @Override
    public Maybe<Node> visit(MemFactor n){
        return Maybe.none();
    }
}
