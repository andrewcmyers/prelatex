package prelatex.lexer;

import easyIO.BacktrackScanner;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.PrelatexError;
import prelatex.tokens.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Lexer {
    static final boolean DEBUG_LEXING = false;
    Scanner input;
    BacktrackScanner.Location location;

    enum LexerMode { N, M, S }; // XXX this should be implemented more fully and correctly
    LexerMode mode;

    public Lexer(String filename) throws FileNotFoundException {
        Reader r = new InputStreamReader(new FileInputStream(filename),
                StandardCharsets.UTF_8);
        input = new Scanner(r, filename);
        mode = LexerMode.N;
    }

    public void includeSource(String filename) {
        try {
            Reader r = new InputStreamReader(new FileInputStream(filename),
                    StandardCharsets.UTF_8);
            input.includeSource(r, filename);
        } catch (FileNotFoundException exc) {
            System.err.println(exc.getMessage() + filename);
        }
    }

    public static class LexicalError extends PrelatexError {

        public LexicalError(String message, Location loc) {
            super(message, loc);
        }
    }
    /** Read enough input to find the next token.
     */
    public Token nextToken() throws EOF, LexicalError {
        location = input.location();
        Token item = parseToken();
        if (DEBUG_LEXING) {
            System.out.printf("Line %d, column %d: %s\n", location.lineNo(), location.column(), item);
        }
        return item;
    }
    private Token parseToken() throws LexicalError, EOF {
        int c = input.peek();
        switch (c) {
            case -1: throw new EOF();
            case '\\': return parseMacroName();
            case '%': return parseComment();
            case '{': return parseBegin();
            case '}': return parseEnd();
            case '#': return parseParameter();
            default:
                if (Character.isWhitespace(c)) {
                    return parseWhitespace();
                }
                return parseCharacter();
        }
    }

    void expect(String s) throws LexicalError {
        try {
            input.consume(s);
        } catch (UnexpectedInput exc) {
            throw new LexicalError("Expected \"" + s + "\"", new ScannerLocn(location));
        }
    }

    private MacroParam parseParameter() throws LexicalError, EOF {
        expect("#");
        try {
            location = input.location();
        } catch (EOF e) {
            throw new LexicalError("Macro parameter ended early", new ScannerLocn(location));
        }
        int n = input.peek();
        if (n == '#') {
            MacroParam p = parseParameter();
            return new MacroParam(p, new ScannerLocn(location));
        } else if (Character.isDigit(n)) {
            expect(Character.toString(n));
            return new MacroParam(new CharacterToken(n, new ScannerLocn(input.location())), new ScannerLocn(location));
        } else {
            throw new LexicalError("Expected macro parameter number 1-9", new ScannerLocn(location));
        }
    }

    private Token parseBegin() throws LexicalError {
        expect("{");
        return new OpenBrace(new ScannerLocn(location));
    }
    private Token parseEnd() throws LexicalError {
        expect("}");
        return new CloseBrace(new ScannerLocn(location));
    }

    private Token parseCharacter() {
        try {
            return new CharacterToken(input.next(), new ScannerLocn(location));
        } catch (EOF e) {
            return notPossible();
        }
    }

    private <T> T notPossible() throws Error {
        throw new Error("Impossible");
    }

    private Separator parseWhitespace() {
        try {
            StringBuilder b = new StringBuilder();
            if (input.peek() == '%') { // comment
                do {
                    b.append(input.next());
                } while (input.peek() != '\n');
                b.append('\n');
            } else {
                while (Character.isWhitespace(input.peek())) {
                    b.append(input.next());
                }
            }
            return new Separator(b.toString(), new ScannerLocn(location));
        } catch (EOF exc) {
            throw new Error("Not possible");
        }
    }

    private Separator parseComment() throws LexicalError {
        StringBuilder b = new StringBuilder();
        try {
            while (input.peek() != '\n') {
                b.append(input.next());
            }
            return new Separator(b.toString(), new ScannerLocn(location));
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of file", new ScannerLocn(location));
        }
    }

    private Token parseMacroName() throws EOF, LexicalError {
        expect("\\");
        StringBuilder b = new StringBuilder();
        if (Character.isAlphabetic(input.peek())) {
            while (input.hasNext() && Character.isAlphabetic(input.peek())) {
                b.append(input.next());
            }
            // skip following whitespace
            while (input.hasNext() && blank(input.peek())) input.next();
        } else { // single-character macro
            b.append(input.next());
        }
        return new MacroName(b.toString(), new ScannerLocn(location));
    }

    private boolean blank(int c) {
        return c == ' ' || c == '\t' || c == '\r';
    }

}