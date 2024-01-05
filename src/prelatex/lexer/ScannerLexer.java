package prelatex.lexer;

import easyIO.BacktrackScanner;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.Context;
import prelatex.Namespace;
import prelatex.tokens.*;

import java.io.*;
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

    enum LexerState {
        N, // beginning a new line
        M, // in the middle of a line
        S // skipping blanks
    }

    /** The TeX lexer is supposed to be in one of the above three modes at any given time.
     */
    LexerState state = LexerState.N;

    private void setupCatcodes() {
        setCatcode('\\', ESCAPE);
        setCatcode('{', BEGIN);
        setCatcode('}', END);
        setCatcode('$', MATH);
        setCatcode('&', ALIGN);
        setCatcode('\r', IGNORED);
        setCatcode('\n', NEWLINE);
        setCatcode('#', PARAMETER);
        setCatcode('~', ACTIVE);
        setCatcode('^', SUPERSCRIPT);
        setCatcode('_', SUBSCRIPT);
        setCatcode(0, IGNORED);
        setCatcode(255, INVALID);
        setCatcode(' ', SPACE);
        setCatcode('\t', SPACE);
        setCatcode('\f', SPACE);
        setCatcode('%', COMMENT);
        setCatcode('@', LETTER); // Not strictly correct but simplifies package handling

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
        state = LexerState.N;
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
        StringBuilder chars = new StringBuilder();
        boolean paragraph = false;
        repeat:
        while (true) {
            int c = input.peek();
            if (c == -1) throw new EOF();
            CatCode cc = getCatcode(c);
            if (cc == IGNORED) {
                input.next();
                continue repeat;
            }
            Location where = new ScannerLocn(this.location);
            switch (state) {
                case N:
                    if (cc == NEWLINE) {
                        paragraph = true;
                        input.next();
                        chars.append((char)c);
                        continue repeat;
                    } else if (cc == SPACE) {
                        state = LexerState.S;
                        continue repeat;
                    }
                    if (paragraph) {
                        return new ParagraphBreak(chars.toString(), where);
                    }
                    state = LexerState.M;
                    break;
                case S:
                    if (cc == SPACE) {
                        input.next();
                        continue repeat;
                    }
                    if (cc == NEWLINE) {
                        state = LexerState.N;
                        return new Separator(String.valueOf(input.next()), where);
                    }
                    state = LexerState.M;
                    break;
                case M:
                    if (cc == NEWLINE) {
                        state = LexerState.N;
                        return new Separator(String.valueOf(input.next()), where);
                    }
                    break;
            }
            switch (cc) {
                case ESCAPE:
                    return parseMacroName();
                case COMMENT:
                    return parseComment();
                case BEGIN:
                    return parseBegin();
                case END:
                    return parseEnd();
                case PARAMETER:
                    return parseParameter();
                case SUPERSCRIPT:
                    try {
                        input.mark();
                        int c1 = input.next();
                        if (peekCatcode() == SUPERSCRIPT) {
                            int c2 = input.next();
                            int c3 = input.next();
                            if (lowercaseHex(c3) && lowercaseHex(input.peek())) {
                                int c4 = input.next();
                                int cout = Integer.parseInt(String.valueOf((char) c3) +
                                        String.valueOf((char)c4), 16);
                                input.accept();
                                return new CharacterToken(cout, where);
                            } else if (c3 < 128) {
                                int cout = c3 ^ 64;
                                input.accept();
                                return new CharacterToken(cout, where);
                            }
                        }
                    } catch (EOF e) {
                        // not a special escape sequence: just fall through
                    }
                    input.abort();
                    return parseCharacter();
                case INVALID:
                    throw new LexicalError("Invalid character " + c, new ScannerLocn(location));
                case ACTIVE:
                    return new ActiveCharMacro(nextChar(), new ScannerLocn(location));
                case MATH:
                    return parseMathMode();
                case SPACE:
                    return parseWhitespace();
                default: // OTHER, LETTER, SUBSCRIPT
                    return parseCharacter();
            }
        }
    }

    private boolean lowercaseHex(int c) {
        if ('0' <= c && c <= '9') return true;
        if ('a' <= c && c <= 'f') return true;
        return false;
    }

    class CharSource implements easyIO.BacktrackScanner.Source {
        int character;
        String filename;
        int lineNo, column;
        boolean done = false;

        CharSource(int ch, String name, int lineNo, int column) {
            character = ch;
            filename = name;
            this.lineNo = lineNo;
            this.column = column;
        }

        @Override public String name() { return filename; }
        @Override public int lineNo() { return lineNo; }
        @Override public int column() { return column; }
        @Override public void close() throws IOException { done = true; }

        @Override
        public BacktrackScanner.Location read() throws IOException {
            if (done) return null;
            done = true;
            return new BacktrackScanner.Location(this, lineNo, column, character);
        }
    }

    private Token parseWhitespace() throws EOF, LexicalError {
        StringBuilder chars = new StringBuilder();
        Location where = new ScannerLocn(input.location());
        chars.append(input.next());
        state = LexerState.S;
        while (input.hasNext()) {
            CatCode c = peekCatcode();
            switch (c) {
                case IGNORED: continue;
                case SPACE:
                    chars.append(input.next());
                    continue;
                case NEWLINE:
                    return parseToken();
                default:
                    return new Separator(chars.toString(), where);
            }
        }
        return syntheticReturn();
    }

    private Token syntheticReturn() throws EOF, LexicalError {
        state = LexerState.N;
        BacktrackScanner.Source source = input.currentSource();
        input.includeSource(new CharSource('\r', source.name(), source.lineNo(), source.column()));
        return parseToken();
    }

    private Token parseMathMode() throws LexicalError {
        Location start = expect(MATH);
        if (peekCatcode() == MATH) { // $$
            expect(MATH);
            return new MathToken(true, start); // display math
        } else {
            return new MathToken(false, start);
        }
    }

    Location expect(CatCode cc) throws LexicalError {
        try {
            Location result = new ScannerLocn(input.location());
            if (peekCatcode() == cc)  {
                input.next();
                return result;
            } else {
                throw new LexicalError("Expected \"" + cc + "\"", new ScannerLocn(location));
            }
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of input, expected \"" + cc + "\"", new ScannerLocn(location));
        }
    }

    private MacroParam parseParameter() throws LexicalError, EOF {
        expect(PARAMETER);
        try {
            location = input.location();
        } catch (EOF e) {
            throw new LexicalError("Macro parameter ended early", new ScannerLocn(location));
        }
        if (peekCatcode() == PARAMETER) {
            MacroParam p = parseParameter();
            return new MacroParam(p, new ScannerLocn(location));
        }
        int n = input.next();
        if (getCatcode(n) == OTHER && Character.isDigit(n)) {
            return new MacroParam(new CharacterToken(n, new ScannerLocn(input.location())), new ScannerLocn(location));
        } else {
            throw new LexicalError("Expected macro parameter number 1-9", new ScannerLocn(location));
        }
    }

    private Token parseBegin() throws LexicalError {
        expect(BEGIN);
        return new OpenBrace(new ScannerLocn(location));
    }
    private Token parseEnd() throws LexicalError {
        expect(END);
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

    private Separator parseComment() throws LexicalError {
        StringBuilder b = new StringBuilder();
        try {
            while (input.peek() != '\n') {
                b.append(input.next());
            }
            input.next(); // consume newline
            state = LexerState.N;
            return new Separator(b.toString(), new ScannerLocn(location));
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of file", new ScannerLocn(location));
        }
    }

    private Token parseMacroName() throws EOF, LexicalError {
        expect(ESCAPE);
        StringBuilder b = new StringBuilder();
        if (peekCatcode() == LETTER) {
            while (input.hasNext() && peekCatcode() == LETTER) {
                b.append(input.next());
            }
            state = LexerState.S;
        } else { // single-character macro
            if (peekCatcode() != NEWLINE)
                b.append(input.next());
            state = LexerState.M;
        }
        return new MacroName(b.toString(), new ScannerLocn(location));
    }

    CatCode peekCatcode() {
        return getCatcode(input.peek());
    }
}