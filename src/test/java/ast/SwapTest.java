package ast;

import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class SwapTest {
    @Test
    public void swap() throws SyntaxError {
        Reader br = writeToFile("mem[4] > 2500 --> mem[1] := 0 mem[6] := 17 bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n") ;
        Parser p = ParserFactory.getParser();

        Mutation m = new Swap();
        Program root = p.parse(br);

        for(int i=0;i<root.size();i++){
            System.out.println(root.nodeAt(i).getCategory());
            System.out.println("Before: " + root.nodeAt(i));
            m.apply(root, root.nodeAt(i));
            System.out.println("After: " + root.nodeAt(i));
            System.out.println(root);

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