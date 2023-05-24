package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.List;

/** There are two kinds of macros:
 * 1. user-defined macros introduced by \newcommand or \def, \gdef, etc., for which there is
 *    a definition to be substituted into the output
 * 2. built-in macros whose behavior is simulated by prelatex.
 */
abstract public class Macro {
    final String name;
    protected Token[] pattern;
    protected int numArgs;

    /**
     * Create a named macro.
     */
    public Macro(String name) {
        this.name = name;
    }

    /**
     * Apply this macro to the state of proc using the specified arguments.
     * Requires the length of arguments to match the macro's declared parameters,
     * which must appear in the pattern. In the case of error, location is used
     * as the point in the code to blame.
     */
    abstract public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws MacroProcessor.SemanticError;

    public void apply(Macro binding, MacroProcessor mp, Location location) throws PrelatexError {
        int position = 0;
        List<List<Token>> arguments = new LinkedList<>();
        while (position < binding.pattern.length) {
            if (binding.pattern[position] instanceof MacroParam) {
                try {
                    Maybe<Token> delim = mp.delimiter(binding.pattern, position);
                    arguments.add(mp.parseMacroArgument(delim));
                    position++;
                    if (delim.isPresent()) position++;
                } catch (EOF exc) {
                    throw new MacroProcessor.SemanticError("File ended while scanning use of " + binding.name, location);
                }
            } else{
                try {
                    Token t = mp.nextToken();
                    if (mp.matchesToken(t, binding.pattern[position])) {
                        position++;
                    } else {
                        throw new MacroProcessor.SemanticError("Token does not match in macro \\" + binding +
                                ": saw " + t + ", expected " + binding.pattern[position],
                                t.location);
                    }
                } catch (EOF exc) {
                    throw new Error("not possible");
                }
            }
        }
        binding.applyArguments(arguments, mp, location);
    }
}