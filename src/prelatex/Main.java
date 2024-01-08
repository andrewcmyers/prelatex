package prelatex;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import easyIO.EOF;
import lwon.data.Array;
import lwon.data.DataObject;
import lwon.data.Dictionary;
import lwon.data.Text;
import lwon.parse.Parser;
import prelatex.lexer.Lexer;
import prelatex.lexer.ScannerLexer;
import prelatex.macros.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import easyIO.Scanner;

import java.util.*;


import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;
import static prelatex.Main.Disposition.DROP;
import static prelatex.Main.Disposition.EXPAND;
import static prelatex.Main.Disposition.KEEP;

public class Main {
    /** The core macro processing engine */
    MacroProcessor processor;

    /** The list of files to read */
    List<String> inputFiles = new ArrayList<>();

    /** The lexer for reading input tokens */
    ScannerLexer in;

    /** Where to look for external files */
    Files searchPath;
    List<String> tex_inputs = new ArrayList<>();

    /** Where output goes */
    PrintWriter outWriter;

    /** Whether to remove comments */
    private boolean noComments = false;

    /** Whether to print out all the defined macros */
    boolean dump_macros = false;

    /** How to handle packages and macro named.
     *  EXPAND: read the package or expand the macro using its definition
     *  KEEP: leave the package unread, or don't expand the macro even if the definition is known.
     *  DROP: delete the use of the package or uses of the macro.
     */
    public enum Disposition { EXPAND, KEEP, DROP }

    private final Map<String, Disposition> packageDisposition = new HashMap<>();
    private final Map<String, Disposition> macroDisposition = new HashMap<>();

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
        throw new Exception("Usage: prelatex [--nocomments] [--dump_macros] [--config <config file>] [ --drop <pkg> ] ... [ --expand <pkg> ] ...  <filename.tex> ...");
    }

    protected void parseArgs(String[] args) throws Exception {
        int optind = 0;
        Maybe<String> outputFile = none();
        for (; optind < args.length; optind++) {
            String opt = args[optind];
            if (opt.codePointAt(0) != '-') break;
            if (opt.matches("^-o")) {
                if (opt.length() == 2) {
                    outputFile = some(args[++optind]);
                } else {
                    outputFile = some(args[++optind].substring(2));
                }
            } else if (opt.equals("--expand")) {
                packageDisposition.put(args[++optind], EXPAND);
            } else if (opt.equals("--drop")) {
                packageDisposition.put(args[++optind], DROP);
            } else if (opt.equals("--dump_macros")) {
                dump_macros = true;
            } else if (opt.equals("--nocomments")) {
                noComments = true;
            } else if (opt.equals("--config")) {
                String configFile = args[++optind];
                processConfiguration(configFile);
            } else if (opt.equals("--")) {
                optind++;
                break;
            } else if (opt.equals("-")) {
                break;
            } else {
                usage();
            }
        }

        if (!outputFile.isPresent() || outputFile.get().equals("-")) {
            outWriter = new PrintWriter(System.out, true);
        } else {
            outWriter = new PrintWriter(new FileOutputStream(outputFile.get(), false));
        }

        if (System.getenv("TEXINPUTS") != null)
            tex_inputs = Arrays.stream(System.getenv("TEXINPUTS").split(":")).toList();

        searchPath = new Files(tex_inputs);
        for (int i = optind; i < args.length; i++) {
            Maybe<String> f = searchPath.findFile(args[i], List.of("", ".tex"));
            try {
                inputFiles.add(f.get());
            } catch (NoMaybeValue e) {
                System.err.println("File not found: " + args[i]);
            }
        }
        if (inputFiles.isEmpty()) usage();
    }

    private void processConfiguration(String configFile) {
        try {
            Scanner scanner = new Scanner(configFile);
            Parser lwonParser = new Parser(scanner);
            DataObject data = lwonParser.parseDictionary();
            if (data instanceof Dictionary config) {
                List<DataObject> lst = config.get("comments");
                if (!lst.isEmpty() && lst.get(0) instanceof Text t) {
                    noComments = !Boolean.parseBoolean(t.value());
                }
                for (DataObject o : config.get("drop package")) {
                    if (o instanceof Text name) packageDisposition.put(name.value(), DROP);
                }
                for (DataObject o : config.get("expand package")) {
                    if (o instanceof Text name) packageDisposition.put(name.value(), EXPAND);
                }
                for (DataObject o : config.get("drop input")) {
                    if (o instanceof Text name) packageDisposition.put("input " + name.value(), DROP);
                }
                for (DataObject o : config.get("keep input")) {
                    if (o instanceof Text name) packageDisposition.put("input " + name.value(), KEEP);
                }
                for (DataObject o : config.get("drop macro")) {
                    if (o instanceof Text name) macroDisposition.put(name.value(), DROP);
                }
                for (DataObject o : config.get("keep macro")) {
                    if (o instanceof Text name) macroDisposition.put(name.value(), KEEP);
                }
                for (DataObject o : config.get("TEXINPUTS")) {
                    switch (o) {
                        case Text t:
                            tex_inputs = List.of(t.value());
                            break;
                        case Array a:
                            tex_inputs = new LinkedList<>();
                            for (DataObject o2 : a) {
                                if (o2 instanceof Text t) {
                                    tex_inputs.add(t.value());
                                }
                            }
                            break;
                        default:
                            System.err.println("Bad directory in TEXINPUTS: " + o);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open configuration file: " + e.getMessage());
        } catch (Parser.SyntaxError e) {
            System.err.println(e.getMessage());
        } catch (EOF e) {
            System.err.println("Configuration file " + configFile + " is empty.");
        }
    }

    void run() {
        try {
            Context<Lexer.CatCode> catcodes = new Context<>();
            in = new ScannerLexer(inputFiles, catcodes);
            ProcessorOutput out = new CondensedOutput(outWriter, noComments);
            PrintWriter err = new PrintWriter(System.err, true);
            processor = new MacroProcessor(in, out, err, searchPath, catcodes);
            StandardContext.initialize(processor);
            if (dump_macros) {
                processor.dumpMacros();
                return;
            }
            processor.setDispositions(packageDisposition, macroDisposition);
            processor.run();
        } catch (PrelatexError|FileNotFoundException e1) {
            System.err.println(e1.getMessage());
        }
        outWriter.close();
    }
}