package prelatex.lexer;

import easyIO.BacktrackScanner.Location;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.tokens.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class Lexer {
    static final boolean DEBUG_LEXING = false;
    Scanner input;
    Location location;
    public Lexer(String filename) throws FileNotFoundException {
        Reader r = new InputStreamReader(new FileInputStream(filename),
                StandardCharsets.UTF_8);
        input = new Scanner(r, filename);
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

    public static class LexicalError extends Exception {
        Location location;

        public LexicalError(String message, Location loc) {
            super(loc + ":" + message);
            location = loc;
        }
    }
    /** Read enough input to find the next token.
     */
    public Item nextItem() throws EOF, LexicalError {
        location = input.location();
        Item item = parseItem();
        if (DEBUG_LEXING) {
            System.out.printf("Line %d, column %d: %s\n", location.lineNo(), location.column(), item);
        }
        return item;
    }
    private Item parseItem() throws LexicalError, EOF {
        int c = input.peek();
        switch (c) {
            case -1: throw new EOF();
            case '\\': return parseMacro();
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
            try {
                throw new LexicalError("Expected \"" + s + "\"", input.location());
            } catch (EOF exc2) {
                throw new LexicalError("Unexpected end of input, expected \"" + s + "\"", null);
            }
        }
    }

    private MacroParam parseParameter() throws LexicalError, EOF {
        expect("#");
        try {
            location = input.location();
        } catch (EOF e) {
            throw new LexicalError("Macro parameter ended early", location);
        }
        int n = input.peek();
        if (n == '#') {
            MacroParam p = parseParameter();
            return new MacroParam(p, new ScannerLocn(location));
        } else if (Character.isDigit(n)) {
            expect(Character.toString(n));
            return new MacroParam(new CharacterToken(n, new ScannerLocn(input.location())), new ScannerLocn(location));
        } else {
            throw new LexicalError("Expected macro parameter number 1-9", location);
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
            return new Separator(b.toString(), location);
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
            return new Separator(b.toString(), location);
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of file", location);
        }
    }

    private Item parseMacro() throws EOF, LexicalError {
        expect("\\");
        StringBuilder b = new StringBuilder();
        if (Character.isAlphabetic(input.peek())) {
            while (input.hasNext() && Character.isAlphabetic(input.peek())) {
                b.append(input.next());
            }
        } else { // single-character macro
            b.append(input.next());
        }
        return new MacroName(b.toString(), new ScannerLocn(location));
    }

}
