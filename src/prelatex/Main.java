package prelatex;

import cms.util.maybe.Maybe;
import prelatex.lexer.ScannerLexer;
import prelatex.macros.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;

import static prelatex.Main.PackageDisposition.DROP;
import static prelatex.Main.PackageDisposition.EXPAND;

public class Main {
    /** The core macro processing engine */
    MacroProcessor processor;

    /** The list of files to read */
    List<String> inputFiles = new ArrayList<>();

    /** The lexer for reading input tokens */
    ScannerLexer in;

    /** Where to look for external files */
    List<String> tex_inputs = new ArrayList<>();

    /** Where output goes */
    PrintWriter outWriter;

    /** Whether to remove comments */
    private boolean removeComments = false;

    /** How to handle packages used */
    public enum PackageDisposition { EXPAND, KEEP, DROP }

    Map<String, PackageDisposition> packageDisposition = new HashMap<>();

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
        throw new Exception("Usage: prelatex [--nocomments] [ --drop <pkg> ] ... [ --local <pkg> ] ...  <filename.tex> ...");
    }

    protected void parseArgs(String[] args) throws Exception {
        int optind = 0;
        Maybe<String> outputFile = Maybe.none();
        for (; optind < args.length; optind++) {
            String opt = args[optind];
            if (opt.codePointAt(0) != '-') break;
            if (opt.matches("^-o")) {
                if (opt.length() == 2) {
                    outputFile = Maybe.some(args[++optind]);
                } else {
                    outputFile = Maybe.some(args[++optind].substring(2));
                }
            } else if (opt.equals("--local")) {
                packageDisposition.put(args[++optind], EXPAND);
            } else if (opt.equals("--drop")) {
                packageDisposition.put(args[++optind], DROP);
            } else if (opt.equals("--nocomments")) {
                removeComments = true;
            } else if (opt.equals("--")) {
                optind++;
                break;
            } else if (opt.equals("-")) {
                break;
            } else {
                usage();
            }
        }
        for (int i = optind; i < args.length; i++) inputFiles.add(args[i]);
        if (inputFiles.isEmpty()) usage();

        if (!outputFile.isPresent() || outputFile.get().equals("-")) {
            outWriter = new PrintWriter(System.out, true);
        } else {
            outWriter = new PrintWriter(new FileOutputStream(outputFile.get(), true));
        }

        if (System.getenv("TEXINPUTS") != null)
            tex_inputs = Arrays.stream(System.getenv("TEXINPUTS").split(":")).toList();
    }

    private void initializeContext(MacroProcessor mp) {
        mp.define("def", new Def());
        mp.define("newcommand", new NewCommand());
        mp.define("providecommand", new RenewCommand());
        mp.define("renewcommand", new RenewCommand());
        mp.define("let", new LetMacro());
        mp.define("input", new InputMacro());
        mp.define("RequirePackage", new RequirePackage("RequirePackage"));
        mp.define("usepackage", new RequirePackage("usepackage"));
        mp.define("ProvidesPackage", new NoopMacro("ProvidesPackage", 1));
        mp.define("relax", new NoopMacro("relax", 0));
        mp.define("newif", new Newif());
        mp.define("ifx", new Ifx());
        mp.define("if", new IfEq());
        mp.define("ifdefined", new IfDefined());
        mp.define("ifcase", new IfCase());
        mp.define("csname", new CSName());
        mp.define("expandafter", new ExpandAfter());
        mp.define("IfFileExists", new IfFileExists());
        mp.define("ifbool", new IfBool());
    }

    void run() {
        try {
            in = new ScannerLexer(inputFiles);
            ProcessorOutput out = new CondensedOutput(outWriter, removeComments);
            PrintWriter err = new PrintWriter(System.err, true);
            processor = new MacroProcessor(in, out, err, tex_inputs);
            initializeContext(processor);
            processor.setPackageDisposition(packageDisposition);
            processor.run();
        } catch (PrelatexError|FileNotFoundException e1) {
            System.err.println(e1.getMessage());
        }
        outWriter.close();
    }
}