package prelatex.lexer;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.tokens.Token;

/** A Lexer is a source of tokens. */
public interface Lexer {

    void setCatcode(int c, ScannerLexer.CatCode code);

    CatCode getCatcode(int c);

    /** Read enough input to find the next token.
     */
    Token nextToken() throws LexicalError, EOF;

    /** Start reading tokens from the specified file until the end of file. */
    void includeSource(String filename);

    /** Skip past any of the characters in s */
    int skipChars(String s);

    /** The next character, ignoring potential tokens. */
    int nextChar();

    class LexicalError extends PrelatexError {
        public LexicalError(String message, Location loc) { super(message, loc); }
    }
    public enum CatCode {
        ESCAPE, // 0 (\)
        BEGIN, // 1 ({)
        END, // 2 (})
        MATH, // 3 ($)
        ALIGN, // 4 (&)
        NEWLINE, // 5 (\n)
        PARAMETER, // 6 (#)
        SUPERSCRIPT, // 7 (^)
        SUBSCRIPT, // 8 (_)
        IGNORED, // 9 (\0)
        SPACE, // 10 ( )
        LETTER, // 11 (a-z, A-Z)
        OTHER, // 12
        ACTIVE, // 13 (~)
        COMMENT, // 14 (%)
        INVALID // 15
    }
}
