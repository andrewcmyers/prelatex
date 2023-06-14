package prelatex.macros;

import java.io.PrintWriter;
import java.util.*;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import easyIO.EOF;
import prelatex.Context;
import prelatex.Files;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Lexer;
import prelatex.lexer.Lexer.LexicalError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.*;
import prelatex.Main.Disposition;

import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;

/** The core macro processing algorithm. */
public class MacroProcessor {
    /** Where error messages are sent. */
    private final PrintWriter err;
    private static final boolean DEBUG_MACROS = false;

    /**
     * Where macro definitions are looked up
     */
    private Context<Macro> context = new Context<>();

    /** Where token input comes from. */
    private Lexer lexer;

    /** Where processed output is sent. */
    private final ProcessorOutput out;

    /**
     * The way to search for source files
     */
    private Files files;

    /**
     * Tokens that have been read from the lexer, or generated by
     * macro expansion, and are still waiting to be processed.
     */
    private final Deque<Token> pendingTokens = new LinkedList<>();

    /**
     * Packages that have been read already.
     */
    Set<String> packagesRead = new HashSet<>();

    /** Packages and macros requiring special treatment. */
    Map<String, Disposition> packageDisposition;
    Map<String, Disposition> macroDisposition;

    public MacroProcessor(Lexer lexer, ProcessorOutput out, PrintWriter err, Files searchPath) {
        this.out = out;
        this.err = err;
        this.lexer = lexer;
        this.files = searchPath;
    }

    /** Add a macro definition. */
    public void define(String name, Macro m) {
        context.add(name, m);
        reportDebug("Defining macro \"\\" + name + "\"");
    }

    /** Generate output. (Convenience method) */
    void output(Token ...s) throws PrelatexError {
        output(List.of(s));
    }
    /** Generate output. */
    void output(List<Token> lst) throws PrelatexError {
        for (Token t : lst) {
            out.output(t);
        }
    }

    /**
     * Start processing an included source file. Requires: no pending items.
     */
    public void includeSource(String filename) {
        assert pendingTokens.isEmpty(); // If this fails, may need to create new Activity to save state
        lexer.includeSource(filename);
    }

    /**
     * Put the tokens in this list at the head of the input sequence.
     */
    void prependTokens(List<Token> items) {
        Token[] a = new Token[items.size()];
        int j = 0;
        for (Token t : items) a[j++] = t;
        prependTokens(a);
    }

    /**
     * Put tokens at the head of the input sequence.
     */
    void prependTokens(Token ...tokens) {
        for (int j = tokens.length - 1; j >= 0; j--) {
            pendingTokens.addFirst(tokens[j]);
        }
    }

    /** Run the macro processor until the end of input is reached or
     * an error is encountered. */
    public void run() throws PrelatexError {
        try {
            normalMode();
        } catch (EOF e2) {
            // All done.
        }
    }

    /** Get the next token to be processed. */
    Token nextToken() throws EOF, LexicalError {
        if (pendingTokens.isEmpty()) {
            return lexer.nextToken();
        } else {
            return pendingTokens.removeFirst();
        }
    }

    /** The next token to be processed. Does not remove it from
     * the input. */
    Token peekToken() throws EOF, LexicalError {
        if (pendingTokens.isEmpty()) {
            Token t = lexer.nextToken();
            pendingTokens.addFirst(t);
            return t;
        } else {
            return pendingTokens.getFirst();
        }
    }

    /** The main loop for processing tokens, until either
     *  the end of input is reached or an error is. */
    private void normalMode() throws EOF, PrelatexError {
        int braceDepth = 0;
        while (true) {
            Token t = nextToken();
            switch (t) {
                case OpenBrace b:
                    context.push();
                    braceDepth++;
                    output(b);
                    break;
                case CloseBrace b:
                    if (braceDepth == 0) {
                        throw new LexicalError("Closing brace with no matching opening brace", b.location);
                    }
                    context.pop();
                    braceDepth--;
                    output(b);
                    break;
                case MacroName n:
                    macroCall(n);
                    break;
                default:
                    output(t);
            }
        }
    }

    /**
     * Expand the macro m (if that is the disposition).
     */
    void macroCall(MacroName m) throws PrelatexError {
        switch (macroDisposition.get(m)) {
            case KEEP: output(m); return;
            case DROP:
            case null:
            case EXPAND: break;
        }
        Macro binding;
        try {
            binding = lookup(m.name());
        } catch (Namespace.LookupFailure exc) {
            output(m);
            return;
        }
        binding.apply(this, m.location);
    }

    /** Skip past tokens that TeX considers 'blank' */
    void skipBlanks() throws EOF, LexicalError {
        while (peekToken().isBlank()) nextToken();
    }

    /** Get the next token that is not blank. */
    Token nextNonblankToken() throws EOF, LexicalError {
        skipBlanks();
        return nextToken();
    }

    public static final MacroName fi = new MacroName("fi", new SyntheticLocn("expected \\fi"));

    /**
     * Parse a sequence of matched tokens. The sequence may be either
     * delimited by the specified delimiter, in which case the shortest
     * properly matched sequence of tokens up to the delimiter is returned,
     * or not delimited, in which case the first token or first
     * properly matched sequence is returned, plus the final delimiter token;
     * callers may need to remove the delimiter.
     * <p>
     * A sequence is properly matched if each open brace is followed after
     * properly matched tokens by a corresponding closing brace, and each
     * conditional is followed similarly by a corresponding \fi
     */
    LinkedList<Token> parseMatched(Set<Token> delimiters) throws PrelatexError, EOF {
        LinkedList<Token> result = new LinkedList<>();
        for (;;) {
            if (!delimiters.isEmpty()) {
                for (Token d : delimiters) {
                    if (matchesToken(d, peekToken())) {
                        result.addLast(nextToken());
                        return result;
                    }
                }
            }
            Token t = nextToken();
            switch (t) {
                case CloseBrace b:
                    throw new SemanticError("Unexpected close brace", b.location);
                case OpenBrace b:
                    result.add(t);
                    List<Token> more = parseMatched(Set.of(new CloseBrace(b.location)));
                    result.addAll(more);
                    break;
                case MacroName m:
                    if (m.name().equals("fi"))
                        throw new SemanticError("Unexpected \\fi", m.location);
                    try {
                        Macro binding = lookup(m.name());
                        if (binding.isConditional()) {
                            result.add(m);
                            List<Token> cond = parseMatched(Set.of(fi));
                            result.addAll(cond);
                        } else {
                            result.add(m);
                        }
                    } catch (Namespace.LookupFailure e) {
                        result.add(m);
                    }
                    break;
                default:
                    result.add(t);
                    break;
            }
            if (delimiters.isEmpty()) return result;
        }
    }

    /** If {@code tokens} begins and ends with matching braces, return a new list in
     * which both braces have been removed. */
    List<Token> stripOuterBraces(List<Token> tokens) {
        if (tokens.size() == 0 || !(tokens.get(0) instanceof OpenBrace)) return tokens;
        int depth = 0;
        ArrayList<Token> stripped = new ArrayList<>();
        int i = 0, n = tokens.size();
        for (Token t : tokens) {
            if (t instanceof OpenBrace) depth++;
            if (t instanceof CloseBrace) depth--;
            if (i == 0) { i++; continue; }
            if (i == n - 1 && t instanceof CloseBrace) {
                assert depth == 0;
                return stripped;
            }
            if (depth == 0) return tokens; // outer braces don't match
            i++;
        }
        return tokens;
    }

    /** Parse a macro argument. The sequence may be either
     * delimited by the specified delimiter, in which case the shortest
     * properly matched sequence of tokens up to the delimiter is returned,
     * or not delimited, in which case the first token or first
     * properly matched sequence is returned.
     * <p>A sequence is properly matched if each open brace is followed after
     * properly matched tokens by a corresponding closing brace, and each
     * conditional is followed similarly by a corresponding \fi
     */
    List<Token> parseMacroArg(Maybe<Token> delimiter) throws PrelatexError, EOF {
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
                        if (t instanceof MacroName m) {
                            try {
                                Macro binding = lookup(m.name());
                                if (binding.isConditional()) {
                                    result.add(m);
                                    List<Token> cond = parseMatched(Set.of(fi));
                                    result.addAll(cond);
                                }
                            } catch (Namespace.LookupFailure e) {
                                result.add(m);
                            }
                        } else {
                            result.add(t);
                        }
                    } catch (NoMaybeValue exc) {
                        result.add(t);
                        if (braceDepth == 0) return result;
                    }
            }
        }
    }

    /** Whether the token received matches what was expected. */
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

    /** Report an error or warning. */
    public void reportError (String msg, Location l){
        err.println(l + ": " + msg);
    }

    /** Report a message if debugging. */
    public void reportDebug (String msg){
        if (DEBUG_MACROS) err.println("debugging: " + msg);
    }

    /** Look up a macro or other binding. */
    public Macro lookup (String name) throws Namespace.LookupFailure {
        return context.lookup(name);
    }

    /** Look up a macro. */
    public Macro lookup (MacroName m) throws Namespace.LookupFailure {
        return context.lookup(m.name());
    }

    /** The delimiter, if any, for the macro parameter starting at position in
     *  the pattern.
     *  Requires: pattern[position] must be a parameter. */
    Maybe<Token> delimiter(List<Token> pattern, int position) {
        assert pattern.get(position) instanceof MacroParam;
        if (position == pattern.size() - 1) return Maybe.none();
        if (pattern.get(position + 1) instanceof MacroParam) return Maybe.none();
        return some(pattern.get(position + 1));
    }

    /** Generate a string representation of a token sequence. */
    public String flattenToString(List<Token> tokens) {
        StringBuilder b = new StringBuilder();
        for (Token t : tokens) {
            b.append(t.chars());
        }
        return b.toString();
    }

    /** Expand a token sequence, replacing occurrences of macro parameters with the corresponding arguments.
     *  The resulting sequence is prepended to the input.
     */
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

    /** Start reading input from the specified file until its end is reached. */
    public void includeFile(List<Token> fileTokens, List<String> exts, Location location) {
        String filename = flattenToString(fileTokens);
        try {
            filename = files.findFile(filename, exts).get();
            includeSource(filename);
            if (DEBUG_MACROS) {
                reportDebug("Including file " + filename);
            }
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

    static final Token elseToken = new MacroName("else", new SyntheticLocn("\\else terminator"));
    static final Token fiToken = new MacroName("fi", new SyntheticLocn("\\fi terminator"));

    /** Read the rest of a conditional whose value is b */
    public void applyConditional(Location location, boolean b) throws EOF, PrelatexError {
        List<Token> kept = new ArrayList<>();
        LinkedList<Token> thenClause = parseMatched(Set.of(elseToken, fiToken));
        Token sep = thenClause.removeLast();
        if (b) kept.addAll(thenClause);
        if (sep.toString().equals("\\else")) {
            LinkedList<Token> elseClause = parseMatched(Set.of(fiToken));
            elseClause.removeLast();
            if (!b) kept.addAll(elseClause);
        }
        prependTokens(kept);
    }

    /** Tell the processor about special macros and packages. */
    public void setDispositions(Map<String, Disposition> packageDisposition,
                                Map<String, Disposition> macroDisposition) {
        this.packageDisposition = packageDisposition;
        this.macroDisposition = macroDisposition;
    }

    /** Convert a string into an array of character tokens */
    public Token[] stringToTokens(String pkgName, Location location) {
        return pkgName.codePoints()
                .mapToObj(i -> new CharacterToken(i, location))
                .toArray(n -> new Token[n]);
    }

    /** Parse a string of legal macro name characters */
    public String parseMacroName(Location location) throws LexicalError {
        try {
            StringBuilder b = new StringBuilder();
            skipBlanks();
            for (;;) {
                Token t = peekToken();
                switch (t) {
                    case CharacterToken c:
                        nextToken();
                        b.appendCodePoint(c.codepoint());
                        break;
                    default:
                        return b.toString();
                }
            }
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of input parsing macro name", location);
        }
    }

    /** Standard parsing of LaTeX arguments given a list of default arguments (normally length 0 or 1)
     * @param numArgs The total number of arguments to the macro including default arguments
     * @param defaultArgs The list of default arguments, which must be supplied explicitly in brackets to override
     * @return A list of lists of tokens, with one element per parameter.
     */
    public List<List<Token>> parseLaTeXArguments(int numArgs, List<List<Token>> defaultArgs, Location location) throws PrelatexError {
        try {
            List<List<Token>> arguments = new LinkedList<>();
            for (List<Token> defaultArg : defaultArgs) {
                skipBlanks();
                if (peekToken() instanceof CharacterToken c && c.codepoint() == '[') {
                    nextToken();
                    arguments.add(parseMacroArg(some(new CharacterToken(']', location))));
                } else {
                    arguments.add(defaultArg);
                    break;
                }
            }
            while (arguments.size() < numArgs) {
                arguments.add(parseMacroArg(none()));
            }
            return arguments;
        } catch (EOF exc) {
            throw new LexicalError("Unexpected end of input while parsing macro arguments", location);
        }
    }

    public Maybe<String> findFile(String filename, List<String> exts) {
        return files.findFile(filename, exts);
    }

    public record LaTeXParams(int numArgs, List<List<Token>> defaultArgs) { }

    /** Standard parsing of LaTeX parameters ala \newcommand or \newenvironment. Returns
     *  the total number of parameters and the list of default arguments, if any. */
    LaTeXParams parseLaTeXParameters(Location location) throws PrelatexError {
        int nargs = 0;
        List<List<Token>> defaultArgs = new ArrayList<>();
        try {
            if (peekToken() instanceof CharacterToken c && c.codepoint() == '[') {
                nextToken();
                skipBlanks();
                Token args = nextToken();
                try {
                    nargs = Integer.parseInt(args.chars());
                    if (nargs < 0 || nargs > 9) {
                        throw new SemanticError("Illegal number of parameters to \\newcommand: " + nargs, location);
                    }
                } catch (NumberFormatException exc) {
                    throw new SemanticError("Illegal number of parameters to \\newcommand: " + args.chars(), location);
                }
                Token t2 = nextNonblankToken();
                if (!t2.chars().equals("]")) {
                    throw new SemanticError("Expected ] after number of parameters to \\newcommand: " + args.chars(),
                            t2.location);
                }
                for (; ; ) {
                    skipBlanks();
                    if (peekToken() instanceof CharacterToken ct && ct.codepoint() == '[') {
                        nextToken();
                        List<Token> arg = new ArrayList<>();
                        for (; ; ) {
                            Token at = nextToken();
                            if (at instanceof CharacterToken ct2 && ct2.codepoint() == ']') break;
                            arg.add(at);
                        }
                        defaultArgs.add(arg);
                    } else {
                        break;
                    }
                }
            }
            return new LaTeXParams(nargs, defaultArgs);
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of input while parsing macro parameters",
                    location);
        }
    }

    /** An input error that is considered semantic rather than lexical in nature. */
    public static class SemanticError extends PrelatexError {
        public SemanticError(String m, Location l) {
            super(m, l);
        }
    }

}