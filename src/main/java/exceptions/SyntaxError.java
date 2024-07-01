package exceptions;

/** An exception indicating a syntax error. */
public class SyntaxError extends Exception {

    public SyntaxError(int lineNo, String message) {
        super("Parse error at line " + lineNo + ": " + message);
    }
}
