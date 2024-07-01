package ast;

import java.util.List;

public abstract class Factor extends Expr {

    @Override
    public abstract List<Node> getChildren();

    @Override
    public abstract StringBuilder prettyPrint(StringBuilder sb);

    @Override
    public abstract boolean classInv();

    @Override
    public boolean set(Mutation m, Node n) {
        return false;
    }
}
