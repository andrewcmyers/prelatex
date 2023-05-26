package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.List;

abstract public class TeXMacro extends Macro {
    protected int numArgs;
    protected List<Token> pattern;

    /** Create a named macro. */
    public TeXMacro(String name, int numArgs) {
        super(name);
        this.numArgs = numArgs;
    }

    /** Set the pattern. Useful because some patterns are hard to build on the fly.
     *  TODO builder pattern instead? */
    protected void setPattern(List<Token> pattern) {
        this.pattern = pattern;
    }

    /** Parse and apply the macro to the parsed arguments. This method is overridden by some specialized
     *  macros like \newcommand and \def where the parsing needs to be done in a specialized way, and these
     *  overridden macros need not even use the applyArguments method to do their work.
     */
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        int position = 0;
        List<List<Token>> arguments = new LinkedList<>();
        while (position < pattern.size()) {
            if (pattern.get(position) instanceof MacroParam) {
                try {
                    Maybe<Token> delim = mp.delimiter(pattern, position);
                    arguments.add(mp.parseMatchedTokens(delim));
                    position++;
                    if (delim.isPresent()) position++;
                } catch (EOF exc) {
                    throw new MacroProcessor.SemanticError("File ended while scanning use of " + name, location);
                }
            } else {
                try {
                    Token t = mp.nextToken();
                    if (mp.matchesToken(t, pattern.get(position))) {
                        position++;
                    } else {
                        throw new MacroProcessor.SemanticError("Token does not match in macro \\" + name +
                                ": saw " + t + ", expected " + pattern.get(position),
                                t.location);
                    }
                } catch (EOF exc) {
                    throw new Error("not possible");
                }
            }
        }
        applyArguments(arguments, mp, location);
    }

    /**
     * Apply this macro to the state of proc using the specified
     * (already parsed) arguments. (Each argument is a list of tokens.)
     * Requires the length of arguments to the macro's declared parameters,
     * which must appear in the pattern. In the case of error, location is used
     * as the point in the code to blame.
     */
    abstract public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws PrelatexError;

}