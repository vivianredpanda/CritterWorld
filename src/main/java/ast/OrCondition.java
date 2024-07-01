package ast;

import cms.util.maybe.Maybe;

public class OrCondition extends BinaryCondition {
    /**
     * Create an AST representation of l op r.
     *
     * @param l the Condition left of the operator
     * @param op an Operator that represents OR or null if it doesn't exist
     * @param r the Condition right of the operator or null if it doesn't exist
     */
    public OrCondition(Condition l, Operator op, Condition r) {
        super(l, Maybe.from(op), Maybe.from(r));
    }
}
