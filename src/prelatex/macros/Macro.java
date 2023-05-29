package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

/** Macros may be:
 * 1. user-defined macros introduced by \newcommand or \def, \gdef, etc., for which there is
 *    a definition to be substituted into the output
 * 2. built-in macros whose behavior is simulated by prelatex.
 *
 * Orthogonally, macros may be:
 * A. TeX-style macros with a pattern as understood by \def
 * B. LaTeX-style macros with more restricted calling syntax but possibly an optional argument
 */
abstract public class Macro {
    final String name;
    protected Macro(String name) {
        this.name = name;
    }
    abstract public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError;
    public boolean isConditional() {
        return false;
    }
    /** Is this a macro that simply expands into new tokens, or is it special? */
    public boolean isExpandable() {
        return false;
    }
}
