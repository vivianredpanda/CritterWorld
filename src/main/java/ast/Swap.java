package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.Random;
import java.util.List;

public class Swap extends AbstractMutation {

    // swap rule at index c1 and index c2 in children
    private void swap(int c1, int c2, List<Node> children){
        Node temp = children.get(c1);
        children.set(c1, children.get(c2));
        children.set(c2, temp);
    }

    public Maybe<Node> visit(Program n){
        List<Node> rules = n.getChildren();
        if(rules.size()<2) {
            return Maybe.none();
        }else{
            // choose two random rules that are not the same
            int rule1 = r.nextInt(rules.size());
            int rule2 = r.nextInt(rules.size());
            while(rule1==rule2){
                rule2 = r.nextInt(rules.size());
            }

            swap(rule1, rule2, rules);
            return Maybe.from(new ProgramImpl(rules));
        }
    }

    public Maybe<Node> visit(Rule n){
        return Maybe.none();
    }

    public Maybe<Node> visit(BinaryCondition n){
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            // swap children
            if(n instanceof AndCondition){
                n = new AndCondition((Condition) children.get(1), BinaryCondition.Operator.AND, (Condition) children.get(0));
            }else {
                n = new OrCondition((Condition) children.get(1), BinaryCondition.Operator.OR, (Condition) children.get(0));
            }
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    public Maybe<Node> visit(Command n){
        List<Node> children = n.getChildren();
        if(children.size()<2){
            return Maybe.none();
        }
        if(children.size()==2){
            if(children.get(0) instanceof Action || children.get(1) instanceof Action){
                return Maybe.none();
            }
        }
        int c1 = r.nextInt(children.size());
        int c2 = r.nextInt(children.size());

        while(children.get(c1) instanceof Action){
            c1 = r.nextInt(children.size());
        }

        while(c1 == c2 || children.get(c2) instanceof Action){
            c2 = r.nextInt(children.size());
        }

        swap(c1, c2, children);

        return Maybe.from(new Command(children));
    }

    public Maybe<Node> visit(Action n){
       return Maybe.none();
    }

    public Maybe<Node> visit(BinaryOp n) {
        List<Node> children = n.getChildren();
        if(children.size()==2) {
        try {
            if(n.getOp().get() == BinaryOp.BOperator.ADD || n.getOp().get()== BinaryOp.BOperator.SUB) {
                n = new AddOp((Expr) children.get(1), n.getOp().get(), (Expr) children.get(0));
            }else{
                n = new MulOp((Expr) children.get(1), n.getOp().get(), (Expr) children.get(0));
            }
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    public Maybe<Node> visit(Rel n){
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            n = new Rel((Expr) children.get(1), n.getOp(), (Expr) children.get(0));
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    public Maybe<Node> visit(Number n){
        return Maybe.none();
    }

    public Maybe<Node> visit(Sensor n){
        return Maybe.none();
    }

    public Maybe<Node> visit(Update n){
        List<Node> children = n.getChildren();
        if(children.size()==2) {
            n = new Update((Expr) children.get(1), (Expr) children.get(0));
            return Maybe.from(n);
        }else{
            return Maybe.none();
        }
    }

    public Maybe<Node> visit(MemFactor n){
        return Maybe.none();
    }

    public String getType(){return "Swap";}
}
