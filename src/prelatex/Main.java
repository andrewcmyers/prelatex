package prelatex;

import easyIO.EOF;
import easyIO.Scanner;
import easyIO.UnexpectedInput;

import java.io.PrintWriter;
import java.io.Writer;

public class Main {
    Lexer lexer;
    MacroProcessor processor;

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
        lexer = new Lexer(filename);
        processor = new MacroProcessor(new PrintWriter(System.out, true));
    }

    void run() {
        try {
            while (true) {
                Item item = lexer.nextItem();
                processor.handle(item);
            }
        } catch (Lexer.LexicalError e) {
            System.err.println(e.getMessage());
        } catch (EOF e) {
            // All done.
            processor.close();
        }
    }

    /** Send string s as output to the appropriate location based on the
     *  current input source. */
    public void output(String s) {
        System.out.print(s);
    }
}