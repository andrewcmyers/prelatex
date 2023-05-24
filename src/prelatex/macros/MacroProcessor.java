package prelatex.macros;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

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

    /** Where macro definitions are looked up */
    Context<Macro> context = new Context<>();

    Lexer lexer;

    /** The directories to search in for source files */
    List<String> searchPath;

    /** Tokens that have been read from the lexer but are still waiting to be processed. */
    private final Deque<Token> pendingTokens = new LinkedList<>();

    public void close() {
        out.close();
    }

    PrintWriter out;

    public MacroProcessor(Lexer lexer, PrintWriter out, PrintWriter err, List<String> searchPath) {
        this.out = out;
        this.err = err;
        this.lexer = lexer;
        this.searchPath = searchPath;
        context.add("input", new InputMacro(searchPath));
    }

    void output(String s) {
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

    public void run() {
        try {
            normalMode();
        } catch (PrelatexError e) {
            System.err.println(e.getMessage());
        } catch (EOF e) {
            // All done.
            close();
        }
    }

    Token nextToken() throws EOF, LexicalError {
        if (pendingTokens.isEmpty()) {
            return lexer.nextToken();
        } else {
            return pendingTokens.removeFirst();
        }
    }

    private Token peekToken() throws EOF, LexicalError {
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
            binding = context.lookup(m.name());
        } catch (Namespace.LookupFailure exc) {
            output(m.chars());
            return;
        }
        binding.apply(binding, this, m.location);
    }

    List<Token> parseMacroArgument(Maybe<Token> delimiter) throws PrelatexError, EOF {
        if (!delimiter.isPresent()) {
            while (peekToken().isBlank()) nextToken();
        }
        LinkedList<Token> result = new LinkedList<>();
        int braceDepth = 0;
        boolean stripBraces = peekToken() instanceof OpenBrace; // do outer braces need to be stripped?
        boolean first = true;
        while (true) {
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
                        return result;
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
        err.println(l + ":" + msg);
    }

    public Macro lookup (String name) throws Namespace.LookupFailure {
        return context.lookup(name);
    }

    /** The delimiter, if any, for the macro parameter starting at position in
     *  the pattern.
     *  Requires: pattern[position] must be a parameter. */
    Maybe<Token> delimiter(Token[] pattern, int position) {
        assert pattern[position] instanceof MacroParam;
        if (position == pattern.length - 1) return Maybe.none();
        if (pattern[position + 1] instanceof MacroParam) return Maybe.none();
        return Maybe.some(pattern[position + 1]);
    }

    public String flattenToString(List<Token> tokens) {
        StringBuilder b = new StringBuilder();
        for (Token t : tokens) {
            b.append(t.chars());
        }
        return b.toString();
    }
}