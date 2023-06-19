package prelatex.lexer;

import easyIO.BacktrackScanner;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.Context;
import prelatex.Namespace;
import prelatex.tokens.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import static prelatex.lexer.Lexer.CatCode.*;

public class ScannerLexer implements Lexer {
    static final boolean DEBUG_LEXING = false;
    Scanner input;
    BacktrackScanner.Location location;

    Context<CatCode> context;

    enum LexerMode { N, M, S } // XXX this should be implemented more fully and correctly
    LexerMode mode;


    private void setupCatcodes() {
        setCatcode('\\', ESCAPE);
        setCatcode('{', BEGIN);
        setCatcode('}', END);
        setCatcode('$', MATH);
        setCatcode('&', ALIGN);
        setCatcode('\n', NEWLINE);
        setCatcode('#', PARAMETER);
        setCatcode('~', ACTIVE);
        setCatcode('^', SUPERSCRIPT);
        setCatcode('_', SUBSCRIPT);
        setCatcode(0, IGNORED);
        setCatcode(255, INVALID);
        setCatcode(' ', SPACE);
        setCatcode('\t', SPACE);
        setCatcode('\r', SPACE);
        setCatcode('\f', SPACE);
        setCatcode('%', COMMENT);

        for (int i = 'A'; i <= 'Z'; i++) {
            setCatcode(i, LETTER);
        }
        for (int i = 'a'; i <= 'z'; i++) {
            setCatcode(i, LETTER);
        }
    }

    public ScannerLexer(List<String> filenames, Context<CatCode> catcodes) throws FileNotFoundException {
        LinkedList<String> reversed = new LinkedList<>();
        this.context  = catcodes;
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
        setupCatcodes();
    }

    public void setContext(Context<CatCode> context) {
        this.context = context;
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

    @Override
    public int skipChars(String s) {
        try {
            if (-1 != s.indexOf(input.peek())) {
                return input.next();
            }
        } catch (EOF e) {
            return -1;
        }
        return 0;
    }

    @Override
    public int nextChar() {
        try {
            if (input.peek() >= 0 && getCatcode(input.peek()) == SUPERSCRIPT) {
                throw new Error("^^ escapes not supported yet");
                // TODO: support ^^ escapes here
            }
            return input.nextCodePoint();
        } catch (EOF e) {
            return -1;
        }
    }

    @Override
    public void setCatcode(int c, CatCode code) {
        context.add(Character.toString(c), code);
    }

    @Override
    public CatCode getCatcode(int c) {
        try {
            return context.lookup(Character.toString(c));
        } catch (Namespace.LookupFailure e) {
            return OTHER;
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
        if ( c == -1) throw new EOF();
        switch (getCatcode(c)) {
            case ESCAPE: return parseMacroName();
            case COMMENT: return parseComment();
            case BEGIN: return parseBegin();
            case END: return parseEnd();
            case PARAMETER: return parseParameter();
            case IGNORED: return parseToken();
            // should handle ^^ character escapes
            case INVALID: throw new LexicalError("Invalid character " + c, new ScannerLocn(location));
            case ACTIVE: return new ActiveCharMacro(nextChar(), new ScannerLocn(location));
            case MATH: return parseMathMode();
            default:
                if (Character.isWhitespace(c)) {
                    return parseWhitespace();
                }
                return parseCharacter();
        }
    }

    private Token parseMathMode() throws LexicalError {
        Location start = expect("$");
        if (input.peek() == '$') {
            expect("$");
            return new MathToken(true, start); // display math
        } else {
            return new MathToken(false, start);
        }
    }

    Location expect(String s) throws LexicalError {
        try {
            Location result = new ScannerLocn(input.location());
            input.consume(s);
            return result;
        } catch (UnexpectedInput exc) {
            throw new LexicalError("Expected \"" + s + "\"", new ScannerLocn(location));
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of input, expected \"" + s + "\"", new ScannerLocn(location));
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
        if (legalLongMacroChar(ch)) {
            while (input.hasNext() && legalLongMacroChar(input.peek())) {
                b.append(input.next());
            }
            // skip following whitespace
            while (input.hasNext() && blank(input.peek())) input.next();
        } else { // single-character macro
            b.append(input.next());
        }
        return new MacroName(b.toString(), new ScannerLocn(location));
    }

    private boolean legalLongMacroChar(int c) {
        return Character.isAlphabetic(c) || c == '@';
    }

    private boolean blank(int c) {
        return c == ' ' || c == '\t' || c == '\r';
    }
}