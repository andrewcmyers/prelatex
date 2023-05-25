package prelatex;

import cms.util.maybe.Maybe;
import prelatex.lexer.Lexer;
import prelatex.macros.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    Lexer lexer;
    MacroProcessor processor;
    List<String> inputFiles = new ArrayList<>();
    List<String> tex_inputs = new ArrayList<>();

    PrintWriter out;

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
        int optind = 0;
        Maybe<String> outputFile = Maybe.none();
        for (; optind < args.length; optind++) {
            String opt = args[optind];
            if (opt.charAt(0) != '-') break;
            if (opt.matches("^-o")) {
                if (opt.length() == 2) {
                    outputFile = Maybe.some(args[++optind]);
                } else {
                    outputFile = Maybe.some(args[++optind].substring(2));
                }
            } else {
                usage();
            }
        }
        for (int i = optind; i < args.length; i++) inputFiles.add(args[i]);
        if (inputFiles.isEmpty()) usage();

        if (!outputFile.isPresent() || outputFile.get().equals("-")) {
            out = new PrintWriter(System.out, true);
        } else {
            out = new PrintWriter(new FileOutputStream(outputFile.get(), true));
        }

        if (System.getenv("TEXINPUTS") != null)
            tex_inputs = Arrays.stream(System.getenv("TEXINPUTS").split(":")).toList();
    }

    private void initializeContext(MacroProcessor mp, List<String> searchPath) {
        mp.define("input", new InputMacro(searchPath));
        mp.define("def", new Def());
        mp.define("newcommand", new NewCommand());
        mp.define("renewcommand", new RenewCommand());
    }

    void run() {
        try {
            for (String filename : inputFiles) {
                String baseDir = new File(filename).getParent();
                ArrayList<String> searchPath = new ArrayList<>(tex_inputs);
                searchPath.add(baseDir);
                lexer = new Lexer(filename);
                processor = new MacroProcessor(lexer, out,
                        new PrintWriter(System.err, true));
                initializeContext(processor, searchPath);
                processor.run();
            }
        } catch (PrelatexError|FileNotFoundException e1) {
            System.err.println(e1.getMessage());
        }
        out.close();
    }
}