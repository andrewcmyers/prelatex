package prelatex.lexer;

import easyIO.BacktrackScanner;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.tokens.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static prelatex.lexer.ScannerLexer.CatCode.*;

public class ScannerLexer implements Lexer {
    static final boolean DEBUG_LEXING = false;
    Scanner input;
    BacktrackScanner.Location location;

    enum LexerMode { N, M, S } // XXX this should be implemented more fully and correctly
    LexerMode mode;

    enum CatCode {
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

    CatCode[] catcodes = {
        ESCAPE, BEGIN, END, MATH, ALIGN, NEWLINE, PARAMETER,
        SUPERSCRIPT, SUBSCRIPT, IGNORED, SPACE, LETTER, OTHER, ACTIVE,
        COMMENT, INVALID
    };
    static CatCode[] charCatcodes = new CatCode[256];
    static {
        for (int i = 0; i <= 255; i++) {
            charCatcodes[i] = OTHER;
        }
        charCatcodes['\\'] = ESCAPE;
        charCatcodes['{'] = BEGIN;
        charCatcodes['}'] = END;
        charCatcodes['$'] = MATH;
        charCatcodes['&'] = ALIGN;
        charCatcodes['\n'] = NEWLINE;
        charCatcodes['#'] = PARAMETER;
        charCatcodes[]
        for (int i = 'A'; i <= 'Z'; i++) {
            charCatcodes[i] = LETTER;
        }
        for (int i = 'a'; i <= 'z'; i++) {
            charCatcodes[i] = LETTER;
        }
    }


    public ScannerLexer(List<String> filenames) throws FileNotFoundException {
        LinkedList<String> reversed = new LinkedList<>();
        for (String f: filenames) {
            reversed.addFirst(f);
        }
        String filename = reversed.removeFirst();
        Charset utf8 = StandardCharsets.UTF_8;
        if (filename.equals("-")) {
            input = new Scanner(new InputStreamReader(System.in), "standard input");
        } else {
            Reader r = new InputStreamReader(new FileInputStream(filename), utf8);
            input = new Scanner(r, filename);
        }
        for (String f : reversed) {
            input.includeSource(new InputStreamReader(new FileInputStream(f), utf8), f);
        }
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
        int ch = input.peek();
        if (legalMacroChar(ch)) {
            while (input.hasNext() && legalMacroChar(input.peek())) {
                b.append(input.next());
            }
            // skip following whitespace
            while (input.hasNext() && blank(input.peek())) input.next();
        } else { // single-character macro
            b.append(input.next());
        }
        return new MacroName(b.toString(), new ScannerLocn(location));
    }

    private boolean legalMacroChar(int c) {
        return Character.isAlphabetic(c) || c == '@';
    }

    private boolean blank(int c) {
        return c == ' ' || c == '\t' || c == '\r';
    }
}