package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.util.ArrayList;
import java.util.List;

public class Duplicate extends AbstractMutation {

    // return the root of Node n
    private Maybe<Node> findRoot(Node n){
        if(n.getCategory() == NodeCategory.PROGRAM){ return Maybe.some(n); }
        while(n.getCategory() != NodeCategory.PROGRAM){
            try {
                return findRoot(n.getParent().get());
            } catch (NoMaybeValue e) {
                return Maybe.none();
            }
        }
        return Maybe.none();
    }

    // get a random node of type NodeCategory in AST rooted at Node n
    // could also make this take a list of NodeCategory
    private Maybe<Node> getRandNodeOfType(Node n, NodeCategory c){
        // loop through all nodes, add all the nodes of category c to lst
        List<Node> options = new ArrayList<>();
        for(int i = 0; i < n.size(); i++){
            Node cur = n.nodeAt(i);
            if(cur.getCategory() == c){ options.add(cur); }
        }
        // if size is 0, return Maybe.none() --> no node of category c rooted at n
        if(options.size()==0){ return Maybe.none(); }

        // otherwise, return a random element of lst
        return Maybe.from(options.get(r.nextInt(options.size())));
    }

    @Override
    public Maybe<Node> visit(Program n) {
        List<Node> newChildren;
        try {
            // get to root to get program
            newChildren = new ArrayList<>();
            for(Node c: n.getChildren()){
                newChildren.add(c.clone());
            }
            Node root = findRoot(n).get();

            // get any rule in the program
            Maybe<Node> addNode = getRandNodeOfType(root, NodeCategory.RULE);

            // add new rule to newChildren
            if(!addNode.isPresent()){ return Maybe.none(); }
            newChildren.add(addNode.get());
        } catch (NoMaybeValue e){ return Maybe.none();}

        // return new Program(newChildren)
        return Maybe.some(new ProgramImpl(newChildren));
    }

    @Override
    public Maybe<Node> visit(Rule n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryCondition n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Command n) {
        List<Node> newChildren;
        try {
            // get to root to get program
            newChildren = new ArrayList<>();
            for(Node c: n.getChildren()){
                newChildren.add(c.clone());
            }
            Node root = findRoot(n).get();

            // get any update in the program
            Maybe<Node> addNode = getRandNodeOfType(root, NodeCategory.UPDATE);

            // add new rule to newChildren
            if(!addNode.isPresent()){ return Maybe.none(); }
            // check if last child is action
            boolean hasAction = newChildren.get(newChildren.size()-1).getCategory() == NodeCategory.ACTION;
            newChildren.add(hasAction? newChildren.size()-1: newChildren.size(), addNode.get());
        } catch (NoMaybeValue e){ return Maybe.none();}

        // return new Program(newChildren)
        return Maybe.some(new Command(newChildren));
    }

    @Override
    public Maybe<Node> visit(Action n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Number n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Sensor n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Update n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(BinaryOp n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(Rel n) {
        return Maybe.none();
    }

    @Override
    public Maybe<Node> visit(MemFactor n) {
        return Maybe.none();
    }

    @Override
    public String getType() {
        return "Duplicate";
    }
}
