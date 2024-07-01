package ast;

import cms.util.maybe.Maybe;

import java.util.*;

import cms.util.maybe.NoMaybeValue;

public class Replace extends AbstractMutation {

    public Maybe<Node> visit(Program n){
        return Maybe.none();
    }

    public Maybe<Node> visit(Rule n){
        try {
            traceRoot(n);
            Maybe<Node> newChild = subTree(root, n);
            List<Node> parentChildren = n.getParent().get().getChildren();
            parentChildren.set(parentChildren.indexOf(n),newChild.get());
            return Maybe.from(new ProgramImpl(parentChildren));
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryCondition n) {
        return visit((Condition) n);
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

    // shared implementation for BinaryCondition and Rel nodes
    public Maybe<Node> visit(Condition n){
        try {
            traceRoot(n);
            Node parent = n.getParent().get();
            Maybe<Node> newChild = subTree(root, n);
            List<Node> parentChildren = parent.getChildren();
            parentChildren.set(parentChildren.indexOf(n),newChild.get());
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
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();

    }

    public Maybe<Node> visit(Command n){
        try {
            traceRoot(n);
            Maybe<Node> newChild = subTree(root, n);
            List<Node> parentChildren = n.getParent().get().getChildren();
            Node child = newChild.get();
            if(newChild.get().getCategory() == NodeCategory.ACTION || newChild.get().getCategory() == NodeCategory.UPDATE){
                child = new Command(new ArrayList<Node>(Arrays.asList(newChild.get())));
            }
            parentChildren.set(parentChildren.indexOf(n),child);
            return Maybe.from(new Rule((Condition) parentChildren.get(0),(Command) parentChildren.get(1)));
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    // shared implementation for Update and Action nodes
    private Maybe<Node> visit(UpdateOrAction n){
        try{
            traceRoot(n);
            Maybe<Node> newChild = subTree(root, n);
            List<Node> parentChildren = n.getParent().get().getChildren();
            parentChildren.set(parentChildren.indexOf(n),newChild.get());
            return Maybe.from(new Command(parentChildren));
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    public Maybe<Node> visit(Action n){
        return visit((UpdateOrAction) n);
    }

    public Maybe<Node> visit(BinaryOp n) {
        return visit((Expr) n);
    }

    public Maybe<Node> visit(Rel n){
        return visit((Condition) n);
    }

    public Maybe<Node> visit(Number n){
        return visit((Expr)n);
    }

    public Maybe<Node> visit(Sensor n){
        return visit((Expr) n);
    }

    public Maybe<Node> visit(Update n){
        return visit((UpdateOrAction) n);
    }

    public Maybe<Node> visit(MemFactor n){
        return visit((Expr) n);
    }

    public Maybe<Node> visit(Expr n){
        //parent can be sensor, binaryop, rel, action(serve[expr]), update(mem[expr])
        try{
            traceRoot(n);
            Maybe<Node> newChild = subTree(root, n);
            Node parent = n.getParent().get();
            List<Node> parentChildren = parent.getChildren();
            parentChildren.set(parentChildren.indexOf(n),newChild.get());

                if(parent instanceof Sensor){
                    parent = new Sensor(((Sensor) parent).getSense(), Maybe.from((Expr)parentChildren.get(0)));
                }else if(parent instanceof AddOp){
                    parent = new AddOp((Expr)parentChildren.get(0),
                            ((AddOp) parent).getOp().get(), (Expr)parentChildren.get(1));
                }else if(parent instanceof MulOp){
                    parent = new MulOp((Expr)parentChildren.get(0),
                            ((MulOp) parent).getOp().get(), (Expr)parentChildren.get(1));
                }else if(parent instanceof Rel){
                    parent = new Rel((Expr)parentChildren.get(0),((Rel) parent).getOp(),(Expr) parentChildren.get(1));
                }else if(parent instanceof MemFactor){
                    parent = new MemFactor((Expr) parentChildren.get(0));
                }else if(parent instanceof Action){
                    parent = new Action(((Action) parent).getActions(),Maybe.from((Expr)parentChildren.get(0)));
                }else if(parent instanceof Update){
                    parent = new Update((Expr) parentChildren.get(0),(Expr) parentChildren.get(1));
                }
            return Maybe.from(parent);
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    public String getType(){return "Replace";}
}
