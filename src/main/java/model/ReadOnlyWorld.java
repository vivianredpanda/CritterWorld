package model;

import cms.util.maybe.Maybe;

public interface ReadOnlyWorld {
    /** @return number of steps */
    int getSteps();

    /** @return number of alive critters. */
    int getNumberOfAliveCritters();

    /**
     * @param c column id.
     * @param r row id.
     * @return the critter at the specified hex.
     */
    Maybe<ReadOnlyCritter> getReadOnlyCritter(int c, int r);

    /**
     * @param c column id.
     * @param r row id.
     * @return 0 if the cell is empty. -1 if it is rock, -(X+1) if it is X food, X+1 if it contains a critter facing 
     *         in direction X. Treat out-of-bound or invalid hex as rock.
     */
    int getTerrainInfo(int c, int r);

    /**
     * @return int[] representing number of rows at index 0 and number of columns at index 1
     */
    int[] toHexSize();
}
