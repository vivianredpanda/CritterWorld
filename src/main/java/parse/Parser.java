package parse;

import ast.Program;
import cms.util.maybe.Maybe;
import exceptions.SyntaxError;

import java.io.Reader;

/** An interface for parsing a critter program. */
public interface Parser {

    /**
     * Parses a program in the given file.
     *
     * @param r A reader to read the program
     * @return The parsed program, unless there was a syntax error.
     * @throws SyntaxError if the input contains a syntax error.
     */
    Program parse(Reader r) throws SyntaxError;
}
