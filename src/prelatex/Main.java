package prelatex;

import prelatex.lexer.Lexer;
import prelatex.macros.MacroProcessor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<String> tex_inputs = new ArrayList<>();
        if (System.getenv("TEXINPUTS") != null)
            tex_inputs = Arrays.stream(System.getenv("TEXINPUTS").split(":")).toList();
        for (int i = 0; i < args.length; i++) {
            String filename = args[i];
            String baseDir = new File(filename).getParent();
            ArrayList<String> searchPath = new ArrayList<>(tex_inputs);
            searchPath.add(baseDir);
            lexer = new Lexer(filename);
            processor = new MacroProcessor(lexer, new PrintWriter(System.out, true),
                                           new PrintWriter(System.err), searchPath);
        }
    }

    void run() {
        processor.run();
    }
}