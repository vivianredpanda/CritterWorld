package controller;

import model.Critter;

public interface Interpreter {

    /**
     * Interprets a Critter and returns its next valid action
     *
     * @param c Critter to interpret
     * @return next Action for Critter c according to c's program
     */
    CritterAction getNextAction(Critter c);
}
