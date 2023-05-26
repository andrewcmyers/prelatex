package prelatex.macros;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import easyIO.EOF;
import prelatex.*;
import prelatex.lexer.Lexer;
import prelatex.lexer.Lexer.LexicalError;
import prelatex.lexer.Location;
import prelatex.tokens.*;

public class MacroProcessor {
    private final PrintWriter err;
    private static final boolean DEBUG_MACROS = true;
    private StringBuilder debugOutput = new StringBuilder();

    /** Where macro definitions are looked up */
    private Context<Macro> context = new Context<>();

    private Lexer lexer;

    /** The directories to search in for source files */
    private List<String> searchPath;

    /** Tokens that have been read from the lexer but are still waiting to be processed. */
    private final Deque<Token> pendingTokens = new LinkedList<>();

    public void close() {
        out.close();
    }

    private PrintWriter out;

    /** Packages that should be read and expanded. */
    Set<String> localPackages = new HashSet<>();

    /** Packages that have been read. */
    Set<String> packagesRead = new HashSet<>();

    public MacroProcessor(Lexer lexer, PrintWriter out, PrintWriter err, List<String> searchPath) {
        this.out = out;
        this.err = err;
        this.lexer = lexer;
        this.searchPath = searchPath;
    }

    public void addLocalPackage(String pkgName) {
        localPackages.add(pkgName);
    }

    public void define(String name, Macro m) {
        context.add(name, m);
    }

    void output(String s) {
        if (DEBUG_MACROS) debugOutput.append(s);
        out.print(s);
    }

    /**
     * Start processing an included source file. Requires: no pending items.
     */
    public void includeSource(String filename) {
        assert pendingTokens.isEmpty(); // If this fails, may need to create new Activity to save state
        lexer.includeSource(filename);
    }

    /**
     * Put the items in this list at the head of the input sequence.
     */
    void prependTokens(List<Token> items) {
        Token[] a = new Token[items.size()];
        int j = 0;
        for (Token t : items) a[j++] = t;
        for (j = a.length - 1; j >= 0; j--) {
            pendingTokens.addFirst(a[j]);
        }
    }

    public void run() throws PrelatexError {
        try {
            normalMode();
        } catch (EOF e2) {
            // All done.
        }
    }

    Token nextToken() throws EOF, LexicalError {
        if (pendingTokens.isEmpty()) {
            return lexer.nextToken();
        } else {
            return pendingTokens.removeFirst();
        }
    }

    Token peekToken() throws EOF, LexicalError {
        if (pendingTokens.isEmpty()) {
            Token t = lexer.nextToken();
            pendingTokens.addFirst(t);
            return t;
        } else {
            return pendingTokens.getFirst();
        }
    }

    private void normalMode() throws EOF, PrelatexError {
        int braceDepth = 0;
        while (true) {
            Token t = nextToken();
            switch (t) {
                case OpenBrace b:
                    context.push();
                    braceDepth++;
                    output("{");
                    break;
                case CloseBrace b:
                    if (braceDepth == 0) {
                        throw new LexicalError("Closing brace with no matching opening brace", b.location);
                    }
                    context.pop();
                    output("}");
                    break;
                case MacroName n:
                    macroCall(n);
                    break;
                default:
                    output(t.chars());
            }
        }
    }

    private void macroCall(MacroName m) throws PrelatexError {
        Macro binding;
        try {
            binding = lookup(m.name());
        } catch (Namespace.LookupFailure exc) {
            output(m.chars());
            return;
        }
        binding.apply(this, m.location);
    }

    void skipBlanks() throws EOF, LexicalError {
        while (peekToken().isBlank()) nextToken();
    }

    /** Parse a sequence of matched tokens. The sequence may be either
     * delimited by the specified delimiter, in which case the shortest
     * properly matched sequence of tokens up to the delimiter is returned,
     * or not delimited, in which case the first token or first
     * properly matched sequence is returned.
     *
     * A sequence is properly matched if each open brace is followed after
     * properly matched tokens by a corresponding closing brace, and each
     * conditional is followed similarly by a corresponding \fi
     */
    List<Token> parseMatchedTokens(Maybe<Token> delimiter) throws PrelatexError, EOF {
        if (!delimiter.isPresent()) skipBlanks();
        LinkedList<Token> result = new LinkedList<>();
        int braceDepth = 0;
        boolean stripBraces = peekToken() instanceof OpenBrace; // do outer braces need to be stripped?
        for (boolean first = true;; first = false) {
            switch (peekToken()) {
                case CloseBrace b:
                    if (braceDepth == 0) {
                        throw new SemanticError("Unexpected close brace", b.location);
                    }
                    braceDepth--;
                    result.addLast(nextToken());
                    if (braceDepth == 0 && stripBraces && !delimiter.isPresent()) {
                        assert result.getFirst() instanceof OpenBrace;
                        result.removeFirst();
                        result.removeLast();
                        return result;
                    }
                    break;
                case OpenBrace b:
                    if (!first && braceDepth == 0) stripBraces = false;
                    braceDepth++;
                    result.addLast(nextToken());
                    break;
                default:
                    Token t = nextToken();
                    try {
                        if (matchesToken(delimiter.get(), t)) {
                            if (stripBraces) {
                                assert result.getFirst() instanceof OpenBrace;
                                assert result.getLast() instanceof CloseBrace;
                                result.removeFirst();
                                result.removeLast();
                            }
                            return result;
                        }
                        if (!first && braceDepth == 0) stripBraces = false;
                        result.add(t);
                    } catch (NoMaybeValue exc) {
                        result.add(t);
                        if (braceDepth == 0) return result;
                    }
            }
        }
    }

    boolean matchesToken(Token expected, Token received) {
        switch (expected) {
            case OpenBrace b: return received instanceof OpenBrace;
            case CloseBrace b: return received instanceof CloseBrace;
            case Separator s: return received instanceof Separator;
            case MacroName m:
                if (received instanceof MacroName m2) {
                    return m.name().equals(m2.name());
                } else {
                    return false;
                }
            case MacroParam p:
                throw new Error("Can't match on macro parameter");
            case CharacterToken c:
                if (received instanceof CharacterToken c2) {
                    return c.chars().equals(c2.chars());
                } else {
                    return false;
                }
            default:
                throw new Error("Unrecognized token parameter");
        }
    }

    public void reportError (String msg, Location l){
        err.println(l + ": " + msg);
    }

    public Macro lookup (String name) throws Namespace.LookupFailure {
        return context.lookup(name);
    }

    /** The delimiter, if any, for the macro parameter starting at position in
     *  the pattern.
     *  Requires: pattern[position] must be a parameter. */
    Maybe<Token> delimiter(List<Token> pattern, int position) {
        assert pattern.get(position) instanceof MacroParam;
        if (position == pattern.size() - 1) return Maybe.none();
        if (pattern.get(position + 1) instanceof MacroParam) return Maybe.none();
        return Maybe.some(pattern.get(position + 1));
    }

    public String flattenToString(List<Token> tokens) {
        StringBuilder b = new StringBuilder();
        for (Token t : tokens) {
            b.append(t.chars());
        }
        return b.toString();
    }

    public void substituteTokens(List<Token> body, List<List<Token>> arguments, Location location) throws SemanticError {
        LinkedList<Token> tokens = new LinkedList<>();
        for (Token t : body) {
            switch (t) {
                case MacroParam p:
                    if (p.token() instanceof MacroParam) {
                        tokens.addLast(p.token());
                    } else {
                        int parameter = Integer.parseInt(p.token().chars());
                        assert 1 <= parameter && parameter <= 9;
                        if (parameter > arguments.size()) {
                            throw new MacroProcessor.SemanticError("Illegal parameter " + parameter +
                                " in macro body at " + p.location, location);
                        }
                        tokens.addAll(arguments.get(parameter - 1));
                    }
                    break;
                default:
                    tokens.addLast(t);
                    break;
            }
        }
        prependTokens(tokens);
    }

    public void includeFile(List<Token> fileTokens, String[] exts, Location location) {
        String filename = flattenToString(fileTokens);
        try {
            filename = findFile(filename, exts).get();
            includeSource(filename);
        } catch (NoMaybeValue exc) {
            reportError("Cannot find input file \"" + filename + "\"", location);
        }
    }

    static Macro falseValue = new NoopMacro("false", 0);
    static Macro trueValue = new NoopMacro("true", 0);

    public void setConditionFalse(String condition) {
        define(condition + " value", falseValue);
    }
    public void setConditionTrue(String condition) {
        define(condition + " value", trueValue);
    }
    public boolean testCondition(String condition) {
        try {
            if (context.lookup(condition + " value") == trueValue) {
                return true;
            }
        } catch (Namespace.LookupFailure e) {
            // undefined
        }
        return false;
    }

    /** Read the rest of a conditional whose value is b */
    public void applyConditional(Location location, boolean b) throws EOF, PrelatexError {
        List<Token> kept = new ArrayList<>();
        boolean ifelse = false;
        for (;;) {
            Token t = nextToken();
            if (t instanceof MacroName m) {
                if (m.name().equals("else")) {
                    ifelse = true;
                    break;
                }
                if (m.name().equals("fi")) {
                    break;
                }
            }
            if (b) kept.add(t);
        }
        if (ifelse) {
            for (;;) {
                Token t = nextToken();
                if (t instanceof MacroName m) {
                    if (m.name().equals("fi")) {
                        break;
                    }
                }
                if (!b) kept.add(t);
            }
        }
        prependTokens(kept);
    }

    public static class SemanticError extends PrelatexError {
        public SemanticError(String m, Location l) {
            super(m, l);
        }
    }

    /** Find the file whose name starts with filename, using the current search path.
     */
    Maybe<String> findFile(String filename, String[] exts) {
        File f1 = new File(filename);
        if (!f1.isAbsolute()) {
            for (String base : searchPath) {
                try {
                    return Maybe.some(findFileExt(base, filename, exts).get());
                } catch (NoMaybeValue exc) {
                    // keep looking
                }
            }
        }
        return findFileExt("", filename, exts);
    }

    private Maybe<String> findFileExt(String base, String filename, String[] extensions) {
        for (String ext : extensions) {
            File rel = new File(base, filename + ext);
            if (rel.canRead()) return Maybe.some(rel.toString());
        }
        return Maybe.none();
    }
}