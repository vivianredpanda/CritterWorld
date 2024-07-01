package parse;

import ast.*;
import exceptions.SyntaxError;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.common.ParserOutput;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ParserImplTest {

    @Test
    void parseProgram() throws SyntaxError {
        Reader br = writeToFile("mem[3*(1+2)] != 17 --> mem[6] := 17;//hi this is a test comment\n" +
                "nearby[3] = 0 and mem[4] > 2500 --> bud;\n" +
                "{mem[4] > mem[3] * 400 and mem[3] < 7} --> grow;\n" +
                "ahead[1] < -1 and mem[4] < 500 * mem[3] --> eat;\n" +
                "// next line attacks only other species\n" +
                "(ahead[1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;\n" +
                "{ahead[2] < 10 or random[20] = 0} and ahead[1] = 0 --> forward;\n" +
                "ahead[3] < 15 and ahead[1] = 0 --> forward;\n" +
                "ahead[4] < 20 and ahead[1] = 0 --> forward;\n" +
                "nearby[0] > 0 and nearby[3] = 0 --> backward;\n" +
                "// karma action: donate food if we are too full or large enough\n" +
                "ahead[1] < 1 and { mem[4] > 2500 or mem[3] > 7 } --> serve[mem[3] / 42];//here is another comment");

        // Reader br = writeToFile("mem[2]>3*(1+2) --> mem[6] := 17;//hi");
        test(ParserImpl.parseProgram(new Tokenizer(br)));

        /*ProgramImpl p = ParserImpl.parseProgram(new Tokenizer(br));
        System.out.println("IN TEST");
        for(Node c: p.getChildren()){
            System.out.println((c).getParent().toString());
        }*/
    }

    @Test
    void parseRule1() throws SyntaxError {
        Reader br = writeToFile("1 > 0 --> mem[0] := 10\n mem[1] := 4\n mem[5] := 3;");
        test(ParserImpl.parseRule(new Tokenizer(br)));
    }

    @Test
    void parseRule2() throws SyntaxError {
        Reader br = writeToFile("(ahead[2-1] / 10 mod 100) != 17 and ahead[1] > 0 --> attack;");
        test(ParserImpl.parseRule(new Tokenizer(br)));
    }

    @Test
    void parseRule3() throws SyntaxError {
        Reader br = writeToFile("POSTURE != 17 --> POSTURE := 17;");
        test(ParserImpl.parseRule(new Tokenizer(br)));
    }

    @Test
    void parseCondition() throws SyntaxError {
        Reader br = writeToFile("1 > 2 or 3 <= 4 and 5 = 6 or 1 = 2");
        test(ParserImpl.parseCondition(new Tokenizer(br)));
    }

    @Test
    // parse an expression that contains a single term
    void parseExprTerm() throws SyntaxError {
        Reader br = writeToFile("1 * 2 * 3");
        test(ParserImpl.parseTerm(new Tokenizer(br)));
    }

    @Test
    // parse an expression that contains term (addop term)*
    void parseExprTermAddTerm() throws SyntaxError {
        Reader br = writeToFile("(5 - -2) + 3 * smell");
        test(ParserImpl.parseExpression(new Tokenizer(br)));
    }

    @Test
    // parse a term that only contains a factor
    void parseTermFactor() throws SyntaxError {
        Reader br = writeToFile("smell");
        test(ParserImpl.parseTerm(new Tokenizer(br)));
    }

    @Test
    // parse a term that contains factor (mulop factor)*
    void parseTermFactorMulFactor() throws SyntaxError {
        Reader br = writeToFile("smell * 2 mod (1 + 2)");
        test(ParserImpl.parseTerm(new Tokenizer(br)));
    }

    @Test
    // parse an expression that needs parentheses for binaryOp
    void parseBinaryOpPrecedence() throws SyntaxError {
        Reader br = writeToFile("1*(2 - 3)");
        test(ParserImpl.parseExpression(new Tokenizer(br)));
    }

    @Test
    // parse single num
    void parseFactorNum() throws SyntaxError {
        Reader br = writeToFile("11209");
        test(ParserImpl.parseFactor(new Tokenizer(br)));
    }

    @Test
    // parse single sensor through factor
    void parseFactorSensor() throws SyntaxError {
        Reader br = writeToFile("smell");
        test(ParserImpl.parseFactor(new Tokenizer(br)));
    }

    @Test
    // parse factor containing memsugar
    void parseFactorMemSugar() throws SyntaxError {
        Reader br = writeToFile("POSTURE");
        test(ParserImpl.parseFactor(new Tokenizer(br)));
    }

    @Test
    void parseCommand() {
    }

    @Test
    // parse basic update
    void parseUpdate() throws SyntaxError {
        Reader br = writeToFile("mem[1+2]:=2*3+1");
        test(ParserImpl.parseUpdate(new Tokenizer(br)));
    }

    @Test
    // parse update with memsugar
    void parseUpdateWithMemSugar() throws SyntaxError {
        Reader br = writeToFile("ENERGY:=2*3+1");
        test(ParserImpl.parseUpdate(new Tokenizer(br)));
    }

    @Test
    // try basic action without children
    void parseAction() throws SyntaxError {
        Reader br = writeToFile("wait");
        test(ParserImpl.parseAction(new Tokenizer(br)));
    }

    @Test
    // parse a relation
    void parseRel() throws SyntaxError {
        Reader br = writeToFile("1 < 2");
        test(ParserImpl.parseRelation(new Tokenizer(br)));
    }

    @Test
    // parse smell sensor
    void parseSensorSmell() throws SyntaxError {
        Reader br = writeToFile("smell");
        test(ParserImpl.parseSensor(new Tokenizer(br)));
    }

    @Test
    // parse sensor with child
    void parseSensorNearby() throws SyntaxError {
        Reader br = writeToFile("nearby[1+2]");
        test(ParserImpl.parseSensor(new Tokenizer(br)));
    }

    @Test
    // check throwing SyntaxErrors
    void parseSyntaxError1() {
        try {
            Reader br = writeToFile("{ 1 + 2 } * 3 > 2 --> mem[1] := 1;");
            test(ParserImpl.parseProgram(new Tokenizer(br)));
            fail("Did not throw SyntaxError 1");
        } catch (SyntaxError e){}
    }

    @Test
    // check throwing SyntaxErrors
    void parseSyntaxError2() {
        try {
            Reader br = writeToFile("1 > 2 and (3 <= 4 or 5 = 6) --> mem[1] := 1;");
            test(ParserImpl.parseProgram(new Tokenizer(br)));
            fail("Did not throw SyntaxError 2");
        } catch (SyntaxError e){}
    }

    @Test
    // check throwing SyntaxErrors
    void parseSyntaxError3() {
        try {
            Reader br = writeToFile("1 > 2 and 3 <= 4 --> mem[wait;");
            test(ParserImpl.parseProgram(new Tokenizer(br)));
            fail("Did not throw SyntaxError 3");
        } catch (SyntaxError e){}
    }

    @Test
    // check throwing SyntaxErrors
    void parseSyntaxError4() {
        try {
            Reader br = writeToFile("1 > 0 --> mem[0] := 1\n" +
                    "forward\n" +
                    "attack;");
            test(ParserImpl.parseProgram(new Tokenizer(br)));
            fail("Did not throw SyntaxError 4");
        } catch (SyntaxError e){}
    }

    @Test
    // check throwing SyntaxErrors
    void parseSyntaxError5() {
        try {
            Reader br = writeToFile("mem[1] > 3 { or mem[2] < 4 } --> mem[0] := 1;");
            test(ParserImpl.parseProgram(new Tokenizer(br)));
            fail("Did not throw SyntaxError 5");
        } catch (SyntaxError e){}
    }

    private void test(Node result){
        StringBuilder sb = new StringBuilder();
        sb = result.prettyPrint(sb);
        System.out.println(sb);
        cleanUp();
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

    private void cleanUp(){
        try {
            Files.deleteIfExists(Paths.get("./temp.txt"));
        } catch (Exception e){
            System.out.println("Error deleting file.");
        }
    }
}