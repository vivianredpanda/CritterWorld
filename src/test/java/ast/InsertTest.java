package ast;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class InsertTest {

    Random r = new Random();

    @Test
    void visit1() throws SyntaxError {
        Reader br = writeToFile("mem[4] > 2500 --> bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n") ;
        Parser p = ParserFactory.getParser();
        Program root = p.parse(br);
        System.out.println(root.size());
        Mutation m = new Insert();
        for(int i=0;i<40;i++){
            System.out.println(root.nodeAt(i).getCategory());
            System.out.println("Before: " + root.nodeAt(i));
            m.apply(root, root.nodeAt(i));
            System.out.println("After: " + root.nodeAt(i));
            System.out.println(root);
        }
    }

    @Test
    void visit2() throws SyntaxError {
        Reader br = writeToFile("POSTURE != 17 --> POSTURE := 17 mem[1] := 2 mem[3] := 4;\n" +
                "nearby[3] = 0 and ENERGY > 2500 --> bud;\n" +
                "{ENERGY > SIZE * 400 and SIZE < 7} --> grow;\n" +
                "ahead[0] < -1 and ENERGY < 500 * SIZE --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "1 = 2 --> wait;") ;
        Parser p = ParserFactory.getParser();
        Program root = p.parse(br);

        Mutation m = new Insert();

        for(int i = 0; i < 100; i++){
            Node nodeToMutate = root.nodeAt(r.nextInt(root.size()));
            System.out.println("Node to mutate: " + nodeToMutate);
            Maybe<Program> result = m.apply(root, nodeToMutate);
            try {
                System.out.println(result.get());
            } catch (NoMaybeValue e) {
                System.out.println("No maybe");
            }
        }
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
