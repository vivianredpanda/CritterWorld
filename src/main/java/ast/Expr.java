package ast;

/** A critter program expression that has an integer value. */
public abstract class Expr extends AbstractNode {
    protected boolean isNeg = false;

    /**
     * Sets if Expr is negative or not
     * @param neg true if Expr is negative, false if Expr is positive
     */
    public void setNeg(boolean neg){
        isNeg = neg;
    }

    /**
     * Returns if Expr is negative or not
     * @return true if Expr is negative, false if Expr is positive
     */
    public boolean getNeg(){
        return isNeg;
    }
    @Override
    public NodeCategory getCategory() {
        return NodeCategory.EXPRESSION;
    }
}
