package ast;

import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import parse.Parser;
import parse.ParserFactory;

import java.io.*;

class ReplaceTest {

    @Test
    public void subTreeExist() throws SyntaxError {
        Reader br = writeToFile("mem[4] > 2500 --> bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n") ;
        Parser p = ParserFactory.getParser();

        Mutation m = new Replace();
        Program root = p.parse(br);
        System.out.println(root.nodeAt(6).getCategory());
        System.out.println("Before: " + root.nodeAt(6));
        System.out.println(m.subTreeExist(root,root.nodeAt(6)));
        for(int i=0;i<root.size();i++){
            System.out.println(root.nodeAt(i).getCategory());
            System.out.println("Before: " + root.nodeAt(i));
            System.out.println(m.subTree(root,root.nodeAt(i)));

        }

        System.out.println("New tree+ \n" + root);
    }

    @Test
    void visit() throws SyntaxError {
        Reader br = writeToFile("mem[4] > 2500 --> bud;\n"
                + "mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n") ;
        Parser p = ParserFactory.getParser();
        Program root = p.parse(br);

        Mutation m = new Replace();
        for(int i=7;i<root.size();i++){
            System.out.println(root.nodeAt(i).getCategory());
            System.out.println(i);
            System.out.println("Before: " + root.nodeAt(i));
            m.apply(root, root.nodeAt(i));
            System.out.println("After: " + root.nodeAt(i));
            System.out.println(root.size());
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