package ast;

import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Command extends AbstractNode {
    List<Node> children;

    /**
     * Creates Command node with list of children
     * @param children list of nodes that should be instances of UpdateOrAction
     */
    public Command(List<Node> children){ this.children = children; }

    @Override
    public List<Node> getChildren() {
        return children;
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb) {
        for(Node c: children){
            sb.append(c.toString());
            sb.append(" ");
        }
       return sb.deleteCharAt(sb.length()-1);
    }

    @Override
    public Node clone() {
        List<Node> temp = new ArrayList<>();
        for(Node c: children){
            temp.add(c.clone());
        }
        Command copy = new Command(temp);
        for(Node c: temp){
            c.setParent(copy);
        }
        return copy;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.COMMAND;
    }


    @Override
    public boolean classInv() {
        // has to check there is at most one action, must be at the end of children
        // children can only contain UpdateOrAction nodes
        assert(children.size() > 0);
        for(int i = 0; i < children.size(); i++){
            Node c = children.get(i);
            assert(c.classInv());
            assert(c instanceof UpdateOrAction);
            if(c instanceof Action){
                assert(i == children.size()-1);
            }
        }
        return true;
    }
    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Command) {
            children = ((Command)(n)).children;
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object n){
        if(!(n instanceof Command)){
            return false;
        }else{
            Command node = (Command) n;
            return node.children.equals(children) ;
        }
    }
}
