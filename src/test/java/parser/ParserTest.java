package parser;

import ast.Program;
import ast.ProgramImpl;
import cms.util.maybe.Maybe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import static org.junit.jupiter.api.Assertions.*;

/** This class contains tests for the Critter parser. */
public class ParserTest {

    /** Checks that a valid critter program is not {@code null} when parsed. */
    @Test
    public void testProgramIsNotNone() {
        InputStream in = ClassLoader.getSystemResourceAsStream("files/draw_critter.txt");
        Reader r = new BufferedReader(new InputStreamReader(in));
        Parser parser = ParserFactory.getParser();
        try{
            ProgramImpl prog = (ProgramImpl) parser.parse(r);
            System.out.println("--- ORIGINAL PROGRAM ---");
            System.out.println(prog);
            prog.mutate();
            System.out.println("--- MUTATED PROGRAM ---");
            System.out.println(prog);
        } catch(SyntaxError e){
            fail("A valid program should not have syntax errors");
        }
    }

}
