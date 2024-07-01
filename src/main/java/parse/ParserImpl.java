package parse;

import ast.*;
import ast.Number;
import cms.util.maybe.Maybe;
import exceptions.SyntaxError;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import static parse.TokenCategory.*;
import static parse.TokenType.*;

public class ParserImpl implements Parser {

    @Override
    public Program parse(Reader r) throws SyntaxError {
        Tokenizer t = new Tokenizer(r);
        return parseProgram(t);
    }

    /**
     * Parses a program from the stream of tokens provided by the Tokenizer,
     * consuming tokens representing the program. All following methods with a
     * name "parseX" have the same spec except that they parse syntactic form
     * X.
     *
     * @return the created AST
     * @throws SyntaxError if there the input tokens have invalid syntax
     */
    public static ProgramImpl parseProgram(Tokenizer t) throws SyntaxError {
        List<Node> rules = new ArrayList<>();
        while(t.hasNext()){
            if(t.peek().getType() == COMMENT){ t.next(); }
            rules.add(parseRule(t));
        }

        // set node as children's parent
        ProgramImpl p = new ProgramImpl(rules);
        for(Node c: p.getChildren()){
            c.setParent(p);
        }

        return p;
    }

    public static Rule parseRule(Tokenizer t) throws SyntaxError {
        Condition c = parseCondition(t);
        consume(t, TokenType.ARR);
        Command com = parseCommand(t);
        consume(t, TokenType.SEMICOLON);

        // set node as children's parent
        Rule r = new Rule(c, com);
        c.setParent(r);
        com.setParent(r);

        return r;
    }

    public static Condition parseCondition(Tokenizer t) throws SyntaxError {
        return parseOrCondition(t);
    }

    public static Expr parseExpression(Tokenizer t) throws SyntaxError {
        Expr e = parseTerm(t);
        while(t.peek().getType().category() == ADDOP){
            Token n = t.next();
            Expr e1 = parseTerm(t);
            AddOp op;
            if(n.getType() == PLUS){
                op = new AddOp(e, BinaryOp.BOperator.ADD, e1);
            } else{
                op = new AddOp(e, BinaryOp.BOperator.SUB, e1);
            }

            // set node as children's parent
            e.setParent(op);
            e1.setParent(op);
            e = op;
        }
        return e;
    }

    public static Expr parseTerm(Tokenizer t) throws SyntaxError {
        Expr e = parseFactor(t);
        while(t.peek().getType().category() == MULOP){
            Token n = t.next();
            Expr e1 = parseTerm(t);
            MulOp op;
            if(n.getType() == MUL){
                op = new MulOp(e, BinaryOp.BOperator.MUL, e1);
            } else if(n.getType() == DIV){
                op = new MulOp(e, BinaryOp.BOperator.DIV, e1);
            } else {
                op = new MulOp(e, BinaryOp.BOperator.MOD, e1);
            }

            // set node as children's parent
            e.setParent(op);
            e1.setParent(op);
            e = op;
        }
        return e;
    }

    public static Expr parseFactor(Tokenizer t) throws SyntaxError {
        switch(t.peek().getType()){
            case NUM:
                return new Number(t.next().toNumToken().getValue());
            case LPAREN:
                consume(t, LPAREN);
                Expr e1 = parseExpression(t);
                consume(t, RPAREN);
                return e1;
            case MINUS:
                consume(t, MINUS);
                Expr e2 = parseFactor(t);
                e2.setNeg(true);
                return e2;
            default:
                // sensor
                if(t.peek().getType().category() == SENSOR){
                    return parseSensor(t);
                }

                // mem or memsugar
                Expr e = parseMemSugar(t);

                // set node as children's parent
                MemFactor m = new MemFactor(e);
                e.setParent(m);

                return m;
        }
    }

    public static Command parseCommand(Tokenizer t) throws SyntaxError {
        List<Node> uoas = new ArrayList<>();

        // parse as many Update as possible
        while(t.peek().getType() == MEM || t.peek().getType().category() == MEMSUGAR){
            Update u = parseUpdate(t);
            uoas.add(u);
        }

        // try to parse an Action
        if(t.peek().isAction()){
            Action a = parseAction(t);
            uoas.add(a);
        }

        // set node as children's parent
        Command command = new Command(uoas);
        for(Node c: command.getChildren()){
            c.setParent(command);
        }

        return command;
    }

    public static Update parseUpdate(Tokenizer t) throws SyntaxError {
        Expr memExpr = parseMemSugar(t);

        consume(t, TokenType.ASSIGN);
        Expr e = parseExpression(t);

        // set node as children's parent
        Update u = new Update(memExpr, e);
        memExpr.setParent(u);
        e.setParent(u);

        return u;
    }

    public static Action parseAction(Tokenizer t) throws SyntaxError {
        if(t.peek().getType().category() != TokenCategory.ACTION){
            throw new SyntaxError(t.lineNumber(), "Expected action, received " + t.peek().getType().category());
        }

        Token n = t.next();
        Action.Actions a = Action.Actions.SERVE;
        if(n.getType() != TokenType.SERVE){
            switch(n.getType()){
                case WAIT:
                    a = Action.Actions.WAIT;
                    break;
                case FORWARD:
                    a = Action.Actions.FORWARD;
                    break;
                case BACKWARD:
                    a = Action.Actions.BACKWARD;
                    break;
                case LEFT:
                    a = Action.Actions.LEFT;
                    break;
                case RIGHT:
                    a = Action.Actions.RIGHT;
                    break;
                case EAT:
                    a = Action.Actions.EAT;
                    break;
                case ATTACK:
                    a = Action.Actions.ATTACK;
                    break;
                case GROW:
                    a = Action.Actions.GROW;
                    break;
                case BUD:
                    a = Action.Actions.BUD;
                    break;
                default:
                    a = Action.Actions.MATE;
            }
            return new Action(a, Maybe.none());
        }
        consume(t, TokenType.LBRACKET);
        Expr e = parseExpression(t);
        consume(t, TokenType.RBRACKET);

        // set node as children's parent
        Action action = new Action(a, Maybe.some(e));
        e.setParent(action);

        return action;
    }

    public static Condition parseRelation(Tokenizer t) throws SyntaxError {
        // { condition }
        if(t.peek().getType() == TokenType.LBRACE){
            consume(t, TokenType.LBRACE);
            Condition c = parseCondition(t);
            consume(t, TokenType.RBRACE);
            return c;
        }

        // expr rel expr
        Expr l = parseExpression(t);
        if(t.peek().getType().category() != RELOP){
            throw new SyntaxError(t.lineNumber(), "Expected RELOP, received " + t.peek().getType());
        }

        TokenType n = t.next().getType();
        Rel.RelOperator op = Rel.RelOperator.LT;
        switch(n){
            case LE:
                op = Rel.RelOperator.LE;
                break;
            case EQ:
                op = Rel.RelOperator.EQ;
                break;
            case GE:
                op = Rel.RelOperator.GE;
                break;
            case GT:
                op = Rel.RelOperator.GT;
                break;
            case NE:
                op = Rel.RelOperator.NE;
        }

        Expr r = parseExpression(t);

        // set node as children's parent
        Rel rel = new Rel(l, op, r);
        l.setParent(rel);
        r.setParent(rel);

        return rel;
    }

    public static Condition parseOrCondition(Tokenizer t) throws SyntaxError {
        // parseAndCondition while possible, check if next is an or, repeat
        Condition r = parseAndCondition(t);
        while(t.peek().getType() == TokenType.OR){
            consume(t, TokenType.OR);
            Condition r1 = parseOrCondition(t);

            // set node as children's parent
            OrCondition or = new OrCondition(r, BinaryCondition.Operator.OR, r1);
            r.setParent(or);
            r1.setParent(or);
            r = or;
        }
        return r;
    }

    public static Condition parseAndCondition(Tokenizer t) throws SyntaxError {
        // parseRel, check if next is an and, repeat
        Condition r = parseRelation(t);
        while(t.peek().getType() == TokenType.AND){
            consume(t, TokenType.AND);
            Condition r1 = parseAndCondition(t);

            // set node as children's parent
            AndCondition and = new AndCondition(r, BinaryCondition.Operator.AND, r1);
            r.setParent(and);
            r1.setParent(and);
            r = and;
        }
        return r;
    }

    public static Sensor parseSensor(Tokenizer t) throws SyntaxError {
        if(t.peek().getType().category() != SENSOR){
            System.out.println(t.peek());
            throw new SyntaxError(t.lineNumber(), "Expected sensor, received " + t.peek().getType().category());
        }

        Token n = t.next();
        if(n.getType() == TokenType.SMELL){
            return new Sensor(Sensor.Sense.SMELL, Maybe.none());
        }
        consume(t, TokenType.LBRACKET);
        Expr e = parseExpression(t);
        consume(t, TokenType.RBRACKET);
        Sensor.Sense s = Sensor.Sense.NEARBY;
        switch(n.getType()){
            case AHEAD:
                s = Sensor.Sense.AHEAD;
                break;
            case RANDOM:
                s = Sensor.Sense.RANDOM;
                break;
        }

        // set node as children's parent
        Sensor sensor = new Sensor(s, Maybe.some(e));
        e.setParent(sensor);

        return sensor;
    }

    public static Expr parseMemSugar(Tokenizer t) throws SyntaxError {
        Expr memExpr;
        try {
            consume(t, TokenType.MEM);
            consume(t, TokenType.LBRACKET);
            memExpr = parseExpression(t);
            consume(t, TokenType.RBRACKET);
        } catch (SyntaxError e){
            if(t.peek().getType().category() != MEMSUGAR){
                throw new SyntaxError(t.lineNumber() ,"Expected MEMSUGAR, received " + t.peek().getType().category());
            }

            Token n = t.next();
            switch(n.getType()){
                case ABV_MEMSIZE:
                    memExpr = new Number(0);
                    break;
                case ABV_DEFENSE:
                    memExpr = new Number(1);
                    break;
                case ABV_OFFENSE:
                    memExpr = new Number(2);
                    break;
                case ABV_SIZE:
                    memExpr = new Number(3);
                    break;
                case ABV_ENERGY:
                    memExpr = new Number(4);
                    break;
                case ABV_PASS:
                    memExpr = new Number(5);
                    break;
                default: // POSTURE
                    memExpr = new Number(6);
            }
        }
        return memExpr;
    }

    /**
     * Consumes a token of the expected type.
     *
     * @throws SyntaxError if the wrong kind of token is encountered.
     */
    public static void consume(Tokenizer t, TokenType tt) throws SyntaxError {
        if(!(t.peek().getType() == tt)){
            throw new SyntaxError(t.lineNumber(), "Expected TokenType " + tt.toString() + ", received TokenType " + t.peek().getType());
        }
        t.next();
    }
}
