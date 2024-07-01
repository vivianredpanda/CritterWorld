package controller;

import ast.*;
import ast.Number;
import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import model.Critter;

import java.util.List;

import static model.Constants.MAX_RULES_PER_TURN;

public class InterpreterImpl implements Interpreter {

    @Override
    public CritterAction getNextAction(Critter c) {
        // reset critter's mem[5] to 1
        c.resetPass();

        Maybe<CritterAction> action = Maybe.none();
        List<Node> rules = c.getProgramImpl().getChildren();

        // parse until Action found, exceeded 999 passes, or no true conditions
        for(int i = 0; i < rules.size(); i++) {
            if(c.getMemoryAt(5) > MAX_RULES_PER_TURN){
                break;
            }

            Rule cur = (Rule) rules.get(i);
            if(interpretCondition(c, (Condition) cur.getChildren().get(0))){
                action = interpretCommand(c, (Command) cur.getChildren().get(1));
                if(action.isPresent()){ break; }
                i = -1; // reset to first rule node
                c.incrementPass();
            }
        }

        // return Action if present, otherwise return WAIT
        try {
            return action.get();
        } catch (NoMaybeValue e) {
            return new CritterAction(Action.Actions.WAIT, Maybe.none());
        }
    }

    // returns whether the Command represented by node is true
    public boolean interpretCondition(Critter c, Condition node) { // Rel, OrCondition, or AndCondition
        if(node instanceof Rel){
            return interpretRel(c, (Rel) node);
        } else { // BinaryCondition
            boolean l = interpretCondition(c, (Condition) node.getChildren().get(0));
            boolean r = interpretCondition(c, (Condition) node.getChildren().get(1));
            if(node instanceof AndCondition){ return l && r; }
            else { return l || r; }
        }
    }

    public boolean interpretRel(Critter c, Rel node) {
        int l = interpretExpr(c, (Expr) node.getChildren().get(0));
        int r = interpretExpr(c, (Expr) node.getChildren().get(1));
        switch(node.getOp()){
            case LT:
                return l < r;
            case LE:
                return l <= r;
            case EQ:
                return l == r;
            case GE:
                return l >= r;
            case GT:
                return l > r;
            default: // NE
                return l != r;
        }
    }

    // performs all Updates contained in node and returns any Action to be performed
    public Maybe<CritterAction> interpretCommand(Critter c, Command node) {
        for(Node cur: node.getChildren()){
            if(cur.getCategory() == NodeCategory.UPDATE){
                interpretUpdate(c, (Update) cur);
            } else{
                return Maybe.some(interpretAction(c, (Action) cur));
            }
        }
        return Maybe.none();
    }

    public void interpretUpdate(Critter c, Update node) {
        int idx = interpretExpr(c, (Expr) node.getChildren().get(0));
        int val = interpretExpr(c, (Expr) node.getChildren().get(1));
        c.setMemoryAt(idx, val);
    }

    public CritterAction interpretAction(Critter c, Action node) {
        Integer num;
        try {
            num = interpretExpr(c, (Expr) node.getChildren().get(0));
        } catch (IndexOutOfBoundsException e){
            num = null;
        }
        return new CritterAction(node.getActions(), Maybe.from(num));
    }

    public int interpretExpr(Critter c, Expr node) {
        int ans = 0;

        if(node instanceof Factor){ // Number, MemFactor, or Sensor
            if(node instanceof Number){
                ans = ((Number) node).getVal();
            } else if(node instanceof MemFactor){
                ans = c.getMemoryAt(interpretExpr(c, (Expr) node.getChildren().get(0)));
            } else{
                switch(((Sensor) node).getSense()){
                    case NEARBY: {
                        ans = c.getNearby(interpretExpr(c, (Expr) node.getChildren().get(0)));
                        break;
                    }
                    case AHEAD: {
                        ans = c.getAhead(interpretExpr(c, (Expr) node.getChildren().get(0)));
                        break;
                    }
                    case RANDOM: {
                        ans = c.getRandom(interpretExpr(c, (Expr) node.getChildren().get(0)));
                        break;
                    }
                    default: { // SMELL
                        ans = c.getSmell();
                    }
                }
            }
        } else { // BinaryOp
            int l = interpretExpr(c, (Expr) node.getChildren().get(0));
            int r = interpretExpr(c, (Expr) node.getChildren().get(1));

            try {
                switch(((BinaryOp )node).getOp().get()){
                    case ADD: {
                        ans = l + r;
                        break;
                    }
                    case SUB: {
                        ans = l - r;
                        break;
                    }
                    case MUL: {
                        ans = l * r;
                        break;
                    }
                    case DIV: {
                        if(r==0){ ans = 0; }
                        else { ans = Math.floorDiv(l,r); }
                        break;
                    }
                    default: {
                        if(r==0){ ans = 0; }
                        else { ans = Math.floorMod(l,r); }
                    }
                }
            } catch (NoMaybeValue e) {
                return 0;
            }
        }

        return node.getNeg()? ans * -1: ans;
    }
}
