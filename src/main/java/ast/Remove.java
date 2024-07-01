package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.List;
import java.util.Random;

public class Remove extends AbstractMutation{
    @Override
    public Maybe<Node> visit(Program n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Rule n) {
        try{
            List<Node> parentChildren = n.getParent().get().getChildren();
            if(parentChildren.size()>1) {
                parentChildren.remove(n);
                Node parent = new ProgramImpl(parentChildren);
                return Maybe.from(parent);
            }
        }catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryCondition n) {
        //parent can be of type binary condition or rule
        try{
            Node parent = n.getParent().get();
            List<Node> parentChildren = parent.getChildren();
            int index = parentChildren.indexOf(n);
            parentChildren.remove(n);
            Random r = new Random();
            int random = r.nextInt(n.getChildren().size());
            parentChildren.add(index,n.getChildren().get(random));
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

    @Override
    public Maybe<Node> visit(Command n) {
        return Maybe.none();
    }

    // shared visit implementation for Update and Action nodes
    private Maybe<Node> visit(UpdateOrAction n){
        try{
            List<Node> parentChildren = n.getParent().get().getChildren();
            if(parentChildren.size() > 1){
                parentChildren.remove(n);
                Node parent = new Command(parentChildren);
                return Maybe.from(parent);
            }
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Action n) {
        return visit((UpdateOrAction) n);
    }

    @Override
    public Maybe<Node> visit(Number n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Sensor n) {
        //parent can be another sensor, binaryop, rel, action(serve[expr]), update(mem[expr])
        if(n.getSense() == Sensor.Sense.SMELL){
            return Maybe.none();
        }
        return visit((Expr) n);
    }

    @Override
    public Maybe<Node> visit(MemFactor n) {
        //parent can be another sensor, binaryop, rel, action(serve[expr]), update(mem[expr])
        return visit((Expr) n);
    }

    // share visit implementation for all Expr nodes
    public Maybe<Node> visit(Expr n){
        //parent can be sensor, binaryop, rel, action(serve[expr]), update(mem[expr])
        try{
            Node parent = n.getParent().get();
            List<Node> parentChildren = parent.getChildren();
            if(n.getChildren().size()>0){
                int index = parentChildren.indexOf(n);
                parentChildren.remove(n);
                Random r = new Random();
                int random = r.nextInt(n.getChildren().size());
                parentChildren.add(index,n.getChildren().get(random));
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
            }
            return Maybe.from(parent);
        } catch (NoMaybeValue e) {
        }
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Update n) {
        return visit((UpdateOrAction) n);
    }

    @Override
    public Maybe<Node> visit(BinaryOp n) {
        return visit((Expr) n);
    }

    @Override
    public Maybe<Node> visit(Rel n) {
        return Maybe.none();
    }

    @Override
    public String getType() {
        return "Remove";
    }

    @Override
    public Maybe<Program> apply(Program program, Node node){
        try {
            Node n = node.accept(this).get();
            for(int i=0;i<program.size();i++){
                Node original = program.nodeAt(i);
                Node parent = node.getParent().get();
                if(original == parent){
                    program.nodeAt(i).set(this,n);
                    Node changed = program.nodeAt(i);
                    break;
                }
            }
            return Maybe.from(program);
        }catch(NoMaybeValue e){
            return Maybe.none();
        }
    }
}
