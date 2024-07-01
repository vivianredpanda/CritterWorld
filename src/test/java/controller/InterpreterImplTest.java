package controller;

import ast.*;
import cms.util.maybe.NoMaybeValue;
import exceptions.SyntaxError;
import model.Critter;
import org.junit.jupiter.api.Test;
import parse.ParserImpl;
import parse.Tokenizer;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterImplTest {
    InterpreterImpl interpreter = new InterpreterImpl();


    // --- GET NEXT ACTION TESTS ---
    @Test
    // get next Action for simple Critter
    void getNextAction() throws SyntaxError {
        Reader br = writeToFile("1 < 2 --> mem[0] := 1 backward;");
        Program node = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[7], node);
        assertEquals(Action.Actions.BACKWARD, interpreter.getNextAction(c).getAction());
    }

    @Test
    // get next Action for simple Critter
    void getNextAction1() throws SyntaxError {
        Reader br = writeToFile("2 != 2 --> mem[1] := 2;" +
                "1 < 2 --> left;" +
                "2 > 1 --> attack; ");
        Program node = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[7], node);
        assertEquals(Action.Actions.LEFT, interpreter.getNextAction(c).getAction());
    }

    @Test
    // get next Action for simple Critter with no action
    void getNextAction2() throws SyntaxError {
        Reader br = writeToFile("1 < 2 --> mem[0] := 1;");
        Program node = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[7], node);
        assertEquals(Action.Actions.WAIT, interpreter.getNextAction(c).getAction());
    }

    @Test
    // get next Action for simple Critter with condition that becomes true
    void getNextAction3() throws SyntaxError {
        Reader br = writeToFile("mem[13] > 2 --> mem[9] := 1 grow;" +
                "1 < 2 --> mem[13] := 10;");
        Program node = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[14], node);
        assertEquals(Action.Actions.GROW, interpreter.getNextAction(c).getAction());
    }


    // --- CONDITION TESTS ---

    @Test
    // parse single relation
    void interpretCondition() throws SyntaxError {
        Reader br = writeToFile("1 < 2");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertTrue(interpreter.interpretCondition(null, node));
    }

    @Test
    // parse an AndCondition
    void interpretCondition1() throws SyntaxError {
        Reader br = writeToFile("1 < 2 and 3 < 2");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertFalse(interpreter.interpretCondition(null, node));
    }

    @Test
    // parse an OrCondition
    void interpretCondition2() throws SyntaxError {
        Reader br = writeToFile("1 < 2 or 3 < 2");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertTrue(interpreter.interpretCondition(null, node));
    }

    @Test
    // parse compound condition
    void interpretCondition3() throws SyntaxError {
        Reader br = writeToFile("1 < 2 and 3 < 2 or 4 < 5");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertTrue(interpreter.interpretCondition(null, node));
    }

    @Test
    // parse compound condition with parentheses
    void interpretCondition4() throws SyntaxError {
        Reader br = writeToFile("1 < 2 and { 3 < 2 or 4 < 5 }");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertTrue(interpreter.interpretCondition(null, node));
    }

    @Test
    // parse compound condition with parentheses
    void interpretCondition5() throws SyntaxError {
        Reader br = writeToFile("2 < 1 and { 3 < 2 or 4 < 5 }");
        Condition node = ParserImpl.parseCondition(new Tokenizer(br));
        assertFalse(interpreter.interpretCondition(null, node));
    }


    // --- COMMAND TESTS ---
    @Test
    // parse Command with single Update
    void interpretCommand() throws SyntaxError {
        Reader br = writeToFile("1 = 1 --> mem[0] := 1;");
        Program prog = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[8], prog);
        Command node = (Command) prog.getChildren().get(0).getChildren().get(1);
        interpreter.interpretCommand(c, node);
        assertEquals(0, c.getMemoryAt(0));
    }

    @Test
    // parse Command with single Action
    void interpretCommand1() throws SyntaxError {
        Reader br = writeToFile("right");
        Command node = ParserImpl.parseCommand(new Tokenizer(br));
        interpreter.interpretCommand(null, node);
    }

    @Test
    // parse longer Command
    void interpretCommand2() throws SyntaxError {
        Reader br = writeToFile("1 = 1 --> mem[0] := 1 mem[1] := 2 mem[7] := 2 forward;");
        Program prog = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[8], prog);
        Command node = (Command) prog.getChildren().get(0).getChildren().get(1);
        interpreter.interpretCommand(c, node);
        assertEquals(0, c.getMemoryAt(0));
        assertEquals(0, c.getMemoryAt(1));
        assertEquals(2, c.getMemoryAt(7));
    }


    // --- UPDATE TESTS ---
    @Test
    // parse basic Update
    void interpretUpdate() throws SyntaxError {
        Reader br = writeToFile("1 = 1 --> mem[7] := 1;");
        Program prog = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[8], prog);
        Update node = (Update) prog.getChildren().get(0).getChildren().get(1).getChildren().get(0);
        interpreter.interpretUpdate(c, node);
        assertEquals(1, c.getMemoryAt(7));
    }

    @Test
    // parse basic Update with Expr
    void interpretUpdate1() throws SyntaxError {
        Reader br = writeToFile("1 = 1 --> mem[1+2/0+3*4] := 5;");
        Program prog = ParserImpl.parseProgram(new Tokenizer(br));
        Critter c = new Critter("small", new int[14], prog);
        Update node = (Update) prog.getChildren().get(0).getChildren().get(1).getChildren().get(0);
        interpreter.interpretUpdate(c, node);
        assertEquals(5, c.getMemoryAt(13));
    }


    // --- ACTION TESTS ---
    @Test
    // parse basic Action
    void interpretAction() throws SyntaxError {
        Reader br = writeToFile("eat");
        Action node = ParserImpl.parseAction(new Tokenizer(br));
        assertEquals(Action.Actions.EAT, interpreter.interpretAction(null, node).getAction());
    }

    @Test
    // parse serve Action
    void interpretAction1() throws SyntaxError, NoMaybeValue {
        Reader br = writeToFile("serve[10]");
        Action node = ParserImpl.parseAction(new Tokenizer(br));
        CritterAction action = interpreter.interpretAction(null, node);
        assertEquals(Action.Actions.SERVE, action.getAction());
        assertEquals(10, action.getNum().get());
    }


    // --- BASIC MATHEMATICAL EXPR TESTS ---

    @Test
    // parse basic mathematical expression
    void interpretExpr() throws SyntaxError {
        Reader br = writeToFile("1 / 2 + 3 * 2");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(6, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing division by 0
    void interpretExpr1() throws SyntaxError {
        Reader br = writeToFile("1 / 0");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(0, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing single number
    void interpretExpr2() throws SyntaxError {
        Reader br = writeToFile("2");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(2, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing different order of operations
    void interpretExpr3() throws SyntaxError {
        Reader br = writeToFile("2 * (1 + 3)");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(8, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing single negative number
    void interpretExpr4() throws SyntaxError {
        Reader br = writeToFile("-9");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(-9, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing negative mathematical expression
    void interpretExpr5() throws SyntaxError {
        Reader br = writeToFile("-( 1 + 2 ) * 3");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(-9, interpreter.interpretExpr(null, expr));
    }

    @Test
    // parse expression containing more complex negative mathematical expression
    void interpretExpr6() throws SyntaxError {
        Reader br = writeToFile("-( -( 1 + 2 ) * 3 + -4)");
        Expr expr = ParserImpl.parseExpression(new Tokenizer(br));
        assertEquals(13, interpreter.interpretExpr(null, expr));
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