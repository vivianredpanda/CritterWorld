package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.List;

public class Insert extends AbstractMutation{

    @Override
    public Maybe<Node> visit(Program n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Rule n){
        return Maybe.none();
    }

    @Override
    public Maybe<Program> apply(Program program, Node node){
        try {
            Node n = node.accept(this).get();
            for(int i=0;i<program.size();i++){
                Node original = program.nodeAt(i);
                Node parent = node.getParent().get();
                if(original == (parent)){
                    Node clone = n.clone();
                    parent.set(this,clone);
                    for(Node child : parent.getChildren()){
                        child.setParent(parent);
                    }
                    break;
                }
            }
            return Maybe.from(program);
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
    }

    /**
     * Helper method for inserting Rel and Binary condition
     */
    private Maybe<Node> newBinaryCond(Condition n){
        BinaryCondition.Operator[] options = BinaryCondition.Operator.values();
        BinaryCondition.Operator v = options[r.nextInt(options.length)];
        traceRoot(n);

        Node newParent, nclone;
        try {
            if (v == BinaryCondition.Operator.AND) {
                nclone = n.clone();
                Node otherChild = subTree(root, n).get();
                newParent = new AndCondition((Condition) nclone, v, (Condition)otherChild);
            } else {
                nclone = n.clone();
                Node otherChild = subTree(root, n).get();
                newParent = new OrCondition((Condition) nclone, v, (Condition)otherChild);
            }
            newParent.setParent(n.getParent().get());
            nclone.setParent(newParent);
            Node parent = n.getParent().get();
            List<Node> parentChildren = parent.getChildren();
            parentChildren.set(parentChildren.indexOf(n), newParent);
            if(parent instanceof AndCondition){
                parent = new AndCondition((Condition)parentChildren.get(0),
                        BinaryCondition.Operator.AND,(Condition)parentChildren.get(1));
            }else if(parent instanceof OrCondition){
                parent = new OrCondition((Condition)parentChildren.get(0),
                        BinaryCondition.Operator.OR,(Condition)parentChildren.get(1));
            }else if(parent instanceof Rule){
                parent = new Rule((Condition) parentChildren.get(0), (Command)parentChildren.get(1));
            }
            return Maybe.from(parent);
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
    }

    /**
     * Helper method for inserting expr, factor related nodes.
     */
    private Maybe<Node> newExprParent(Expr n){
        int choice = r.nextInt(2);
        traceRoot(n);
        assert(root != null);
        Node newParent;
        Node nclone = n.clone();
        try {
            if (choice == 0) {
                newParent = new MemFactor((Expr) nclone);
            } else {
                BinaryOp.BOperator[] options = BinaryOp.BOperator.values();
                BinaryOp.BOperator v = options[r.nextInt(options.length)];
                if (v == BinaryOp.BOperator.ADD || v == BinaryOp.BOperator.SUB) {
                    nclone = n.clone();
                    newParent = new AddOp((Expr) nclone, v, (Expr) subTree(root, n).get());
                    newParent.setParent(n.getParent().get());
                    nclone.setParent(newParent);
                } else {
                    nclone = n.clone();
                    newParent = new MulOp((Expr) nclone, v, (Expr) subTree(root, n).get());
                    newParent.setParent(n.getParent().get());
                    nclone.setParent(newParent);
                }

            }
            Node parent = n.getParent().get();
            List<Node> parentChildren = parent.getChildren();
            parentChildren.set(parentChildren.indexOf(n), newParent);

            if (parent instanceof Sensor) {
                parent = new Sensor(((Sensor) parent).getSense(), Maybe.from((Expr) parentChildren.get(0)));
            } else if (parent instanceof AddOp) {
                parent = new AddOp((Expr) parentChildren.get(0),
                        ((AddOp) parent).getOp().get(), (Expr) parentChildren.get(1));
            } else if (parent instanceof MulOp) {
                parent = new MulOp((Expr) parentChildren.get(0),
                        ((MulOp) parent).getOp().get(), (Expr) parentChildren.get(1));
            } else if (parent instanceof Rel) {
                parent = new Rel((Expr) parentChildren.get(0), ((Rel) parent).getOp(), (Expr) parentChildren.get(1));
            } else if (parent instanceof MemFactor) {
                parent = new MemFactor((Expr) parentChildren.get(0));
            } else if (parent instanceof Action) {
                parent = new Action(((Action) parent).getActions(), Maybe.from((Expr) parentChildren.get(0)));
            } else if (parent instanceof Update) {
                parent = new Update((Expr) parentChildren.get(0), (Expr) parentChildren.get(1));
            }
            return Maybe.from(parent);
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
    }

    @Override
    public Maybe<Node> visit(BinaryCondition n){
        return newBinaryCond(n);
    }

    @Override
    public Maybe<Node> visit(Command n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Action n){
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryOp n) {
       return newExprParent(n);
    }

    @Override
    public Maybe<Node> visit(Rel n){
        return newBinaryCond(n);
    }

    @Override
    public Maybe<Node> visit(Number n){
        return newExprParent(n);
    }

    @Override
    public Maybe<Node> visit(Sensor n){
       return newExprParent(n);
    }

    @Override
    public Maybe<Node> visit(Update n){
        return Maybe.none();
    }

    @Override
    public String getType(){return "Insert";}

    @Override
    public Maybe<Node> visit(MemFactor n){
        return newExprParent(n);
    }
}
