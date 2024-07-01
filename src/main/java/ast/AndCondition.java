package ast;

import cms.util.maybe.Maybe;

public class AndCondition extends BinaryCondition {
    /**
     * Create an AST representation of l op r.
     *
     * @param l the Condition left of the operator
     * @param op an Operator that represents AND or null if it doesn't exist
     * @param r the Condition right of the operator or null if it doesn't exist
     */
    public AndCondition(Condition l, Operator op, Condition r) {
        super(l, Maybe.from(op), Maybe.from(r));
    }

}
