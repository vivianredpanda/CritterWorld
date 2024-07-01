package ast;

import cms.util.maybe.Maybe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** A representation of a critter rule. */
public class Rule extends AbstractNode {
    private Condition condition;
    private Command command;

    /**
     * Constructs a Rule using a Condition and a Command
     * @param a a Condition node that is not null
     * @param b a Command node that is not null
     */
    public Rule(Condition a, Command b){
        condition = a;
        command = b;
    }

    @Override
    public NodeCategory getCategory() {
        return NodeCategory.RULE;
    }

    @Override
    public List<Node> getChildren() {
        return new ArrayList<Node>(Arrays.asList(condition,command));
    }

    @Override
    public StringBuilder prettyPrint(StringBuilder sb){
        sb.append(condition);
        sb.append(" --> ");
        sb.append(command);
        sb.append(";");
        return sb;
    }

    @Override
    public Node clone() {
        Condition lCopy = (Condition)condition.clone();
        Command rCopy = (Command)command.clone();
        Node copy = new Rule(lCopy, rCopy);
        lCopy.setParent(copy);
        rCopy.setParent(copy);
        return copy;
    }

    @Override
    public boolean classInv() {
        assert(condition != null);
        assert(command != null);
        assert(condition.classInv());
        assert(command.classInv());
        return true;
    }
    @Override
    public Maybe<Node> accept(Mutation v){
        return v.visit(this);
    }

    @Override
    public boolean set(Mutation m, Node n){
        if(m != null && n instanceof Rule) {
            command = ((Rule)(n)).command;
            condition = ((Rule)(n)).condition;
            return true;
        }
        return false;
    }
    @Override
    public boolean equals(Object n){
        if(!(n instanceof Rule)){
            return false;
        }else{
            Rule node = (Rule) n;
            return node.getChildren().equals(getChildren());
        }
    }
}
