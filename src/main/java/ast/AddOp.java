package ast;

import cms.util.maybe.Maybe;

/** A representation of a binary addition operator: '+' or '-' */
public class AddOp extends BinaryOp {

    /**
     * Creates an AddOp with an Expr and a potential BOperator and an additional potential Expr
     * @param l the Expr left of the operator
     * @param op a BOperator that represents + or - or null if it doesn't exist
     * @param r the Expr right of the operator or null if it doesn't exist
     */
    public AddOp(Expr l, BOperator op, Expr r) {
        super(l, Maybe.from(op), Maybe.from(r));
    }
    
}
