package prelatex.lexer;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.tokens.Token;

/** A Lexer is a source of tokens. */
public interface Lexer {

    /** Read enough input to find the next token.
     */
    Token nextToken() throws LexicalError, EOF;

    /** Start reading tokens from the specified file until the end of file. */
    void includeSource(String filename);

    public static class LexicalError extends PrelatexError {

        public LexicalError(String message, Location loc) {
                                                        super(message, loc);
                                                                            }
    }
}
