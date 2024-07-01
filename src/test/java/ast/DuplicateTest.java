package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateTest {
    Parser parser = ParserFactory.getParser();

    @Test
    void visitProgram() throws NoMaybeValue, SyntaxError {
        Duplicate d = new Duplicate();
        Reader br = writeToFile("1 > 2 --> mem[3] := 4 mem[5] := 6 mem[7] := 8 wait;\n" +
                "9 < 10 --> mem[11] := 12 mem[13] := 15 eat;" +
                "");
        Program prog = parser.parse(br);

        System.out.println("ORIGINAL PROGRAM");
        System.out.println(prog);

        System.out.println("MUTATED PROGRAM");
        System.out.println(d.apply(prog, prog));
    }

    @Test
    void visitCommandParse() throws SyntaxError, NoMaybeValue {
        Duplicate d = new Duplicate();
        Reader br = writeToFile("1 > 2 --> mem[3] := 4 mem[5] := 6 mem[7] := 8 wait;\n" +
                "9 < 10 --> mem[11] := 12 mem[13] := 15 eat;" +
                "");
        Program prog = parser.parse(br);
        Command c = (Command) (((prog.getChildren().get(0)).getChildren().get(1)));
        // System.out.println("--- ORIGINAL NODE ---");
        // System.out.println(c);
        System.out.println("--- ORIGINAL PROGRAM ---");
        System.out.println(prog);

        // Maybe<Node> mutated = d.visit(c);
        // System.out.println("--- MUTATED NODE ---");
        // System.out.println(mutated.get());
        System.out.println("--- MUTATED PROGRAM ---");
        System.out.println(d.apply(prog, c));
    }

    private Reader writeToFile(String s){
        try {
            FileWriter myWriter = new FileWriter("./temp.txt");
            myWriter.write(s);
            myWriter.close();
            return new BufferedReader(new FileReader("./temp.txt"));
        } catch (IOException e) {
            System.out.println("Error creating file.");
        }
        return null;
    }
}