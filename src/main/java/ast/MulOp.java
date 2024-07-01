package ast;

import cms.util.maybe.Maybe;

/** A representation of a binary multiplication operator: '*', '/', or 'mod' */
public class MulOp extends BinaryOp {

    /**
     * Creates a MulOp with an Expr and a potential BOperator and an additional potential Expr
     * @param l the Expr left of the operator
     * @param op a BOperator that represents *, /, or - or null if it doesn't exist
     * @param r the Expr right of the operator or null if it doesn't exist     */
    public MulOp(Expr l, BOperator op, Expr r) {
        super(l, Maybe.some(op), Maybe.some(r));
    }

}
