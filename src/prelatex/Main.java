package prelatex;

import easyIO.BacktrackScanner.Location;
import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;
import prelatex.tokens.*;
import tokens.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

import static prelatex.Main.ParseMode.*;

public class Main {

    static final boolean DEBUG_LEXING = true;
    Scanner input;
    Location location;

    Context<MacroBinding> context = new Context<>();

    private Deque<Activity> activities;

    enum ParseMode {
        MACRO,
        NORMAL,
        ARGUMENT
    }

    ParseMode mode = NORMAL;

    public static void main(String[] args) {
        try {
            new Main(args).run();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private Main(String[] args) throws Exception {
        parseArgs(args);
    }

    void usage() throws Exception {
        throw new Exception("Usage: prelatex <filename.tex>");
    }

    protected void parseArgs(String[] args) throws Exception {
        if (args.length != 1) usage();
        String filename = args[0];

        Reader r = new InputStreamReader(new FileInputStream(filename),
                StandardCharsets.UTF_8);
        input = new Scanner(r, filename);
    }

    void run() {
        try {
            while (true) {
                location = input.location();
                Item item = parseItem();
                if (DEBUG_LEXING) {
                    System.out.println("Saw: " + item);
                }
            }
        } catch (UnexpectedInput e) {
            System.err.println("Error at " + location + ":" + e.getMessage());
        } catch (EOF e) {
            // All done.
        }
    }

    private Item parseItem() throws UnexpectedInput, EOF {
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

    private MacroParam parseParameter() throws UnexpectedInput {
        input.consume("#");
        location = input.location();
        int n = input.peek();
        if (n == '#') {
            MacroParam p = parseParameter();
            return new MacroParam(p, location);
        } else if (Character.isDigit(n)) {
            input.consume(Character.toString(n));
            return new MacroParam(new CharacterToken(n, input.location()), location);
        } else {
            throw new UnexpectedInput("Expected macro parameter number 1-9");
        }
    }

    private Token parseBegin() throws UnexpectedInput {
        input.consume("{");
        return new OpenBrace(location);
    }
    private Token parseEnd() throws UnexpectedInput {
        input.consume("}");
        return new CloseBrace(location);
    }

    private Token parseCharacter() throws UnexpectedInput {
        try {
            return new CharacterToken(input.next(), location);
        } catch (EOF e) {
            throw new UnexpectedInput();
        }
    }

    private Separator parseWhitespace() {
        try {
            StringBuilder b = new StringBuilder();
            if (input.peek() == '%') { // comment
                do {
                    b.append((char) input.next());
                } while (input.peek() != '\n');
                b.append('\n');
            } else {
                while (Character.isWhitespace(input.peek())) {
                    b.append((char) input.next());
                }
            }
            return new Separator(b.toString(), location);
        } catch (EOF exc) {
            throw new Error("Not possible");
        }
    }


    private Separator parseComment() throws UnexpectedInput {
        StringBuilder b = new StringBuilder();
        try {
            while (input.peek() != '\n') {
                b.append(input.next());
            }
            return new Separator(b.toString(), location);
        } catch (EOF e) {
            throw new UnexpectedInput("Unexpected end of tile");
        }
    }

    private Item parseMacro() throws EOF, UnexpectedInput
    {
        input.consume("\\");
        StringBuilder b = new StringBuilder();
        if (Character.isAlphabetic(input.peek())) {
            while (input.hasNext() && Character.isAlphabetic(input.peek())) {
                b.append(input.next());
            }
        } else { // single-character macro
            b.append(input.next());
        }
        return new MacroName(b.toString(), location);
    }

    /** Send string s as output to the appropriate location based on the
     *  current input source. */
    private void output(String s) {
    }
}