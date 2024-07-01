package model;

import ast.Program;
import cms.util.maybe.Maybe;

public interface ReadOnlyCritter {
    /** @return critter species. */
    String getSpecies();

    /**
     * Hint: you should consider making a defensive copy of the array.
     *
     * @return an array representation of critter's memory.
     */
    int[] getMemory();

    /**
     * Returns value of memory at a given index, or 0 if index is invalid
     *
     * @param idx idx of memory to return
     * @return value of memory at index idx, or 0 if index is invalid
     */
    int getMemoryAt(int idx);

    /** @return current program string of the critter. */
    String getProgramString();

    /**
     * @return last rule executed by the critter on its previous turn, or {@code Maybe.none()} if it has not
     *     executed any.
     */
    Maybe<String> getLastRuleString();

    /** @return critter program node. */
    Program getProgramImpl();

    int[] getPosition();

    int getDirection();

    /* Returns the appearance of this critter, defined as size * 1,000 + posture * 10 + direction */
    int getAppearance();
}
