package ast;

/** An abstract class representing a Boolean condition in a critter program. */
public abstract class Condition extends AbstractNode {

    /**
     * Returns category of any Condition to be NodeCategory.Condition
     * @return NodeCategory Condition as category
     */
    @Override
    public NodeCategory getCategory() {
        return NodeCategory.CONDITION;
    }
}
