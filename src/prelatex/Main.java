package prelatex;

import easyIO.EOF;
import prelatex.lexer.Lexer;
import prelatex.tokens.Item;

import java.io.PrintWriter;

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
        throw new Exception("Usage: prelatex [--config <configfile>] <filename.tex> ...");
    }

    protected void parseArgs(String[] args) throws Exception {
        if (args.length != 1) usage();
        String filename = args[0];
        lexer = new Lexer(filename);
        processor = new MacroProcessor(lexer, new PrintWriter(System.out, true));
    }

    void run() {
        processor.run();
    }
}