package ast;

import cms.util.maybe.Maybe;

/** A mutation to the AST */
public interface Mutation {
    /**
     * Compares the type of this mutation to {@code m}
     *
     * @param m The mutation to compare with
     * @return Whether this mutation is the same type as {@code m}
     */
    boolean equals(Mutation m);

    /**
     * Applies this mutation to the given {@code Node} within this {@code
     * Program}
     *
     * @param program the program to be mutated.
     * @param node the specific node to perform mutation on.
     * @return a mutated program or {@code Maybe.none} if the mutation is
     *      unsuccessful.
     */
    Maybe<Program> apply(Program program, Node node);

    /**
     * Returns true if and only if this type of mutation can be applied to the
     *     given node.
     * @param n the node to mutate
     * @return whether this mutation can be applied to {@code n}
     */
    boolean canApply(Node n);

    /**
     * Implements mutations for Program
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Program n);

    /**
     * Implements mutations for Rule
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Rule n);

    /**
     * Implements mutations for Condition
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(BinaryCondition n);

    /**
     * Implements mutations for Command
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Command n);

    /**
     * Implements mutations for Action
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Action n);

    /**
     * Implements mutations for Number
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node>  visit(Number n);

    /**
     * Implements mutations for Sensor
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Sensor n);

    /**
     * Implements mutations for Update
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Update n);
    /**
     * Implements mutations for BinaryOp
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(BinaryOp n);

    /**
     * Implements mutations for Rel
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */
    Maybe<Node> visit(Rel n);

    /**
     * Implements mutations for MemFactor
     * @param n the node to be mutated
     * @return the mutated node if successfully mutated, Maybe.none() if not
     */

    Maybe<Node> visit(MemFactor n);

    /**
     * Returns what mutation we are doing
     * @return the type of mutation the current class is
     */
    String getType();

    /**
     * Identify whether there exists a node that can replace node exists in the subtree stemming from root
     * @param root the node to start searching
     * @param node the node that we want to be able to replace with
     * @return true if found, false if not found
     */
    boolean subTreeExist(Node root, Node node);

    /**
     * Find a node in the subtree stemming at root that can replace node.
     * @param root the node to start searching
     * @param node the node that we want to be able to replace with
     * @return the node that can replace node
     */
    Maybe<Node> subTree (Node root, Node node);


    /**
     * Finds the root of the tree and assign it to the instance variable root in AbstractMutation
     * @param n a node in the tree
     */
    void traceRoot(Node n);

}
