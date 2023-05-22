package prelatex;

import prelatex.tokens.Item;
import prelatex.tokens.Token;

/** There are two kinds of macros:
 * 1. user-defined macros introduced by \newcommand or \def, \gdef, etc., for which there is
 *    a definition to be substituted into the output
 * 2. built-in macros whose behavior is simulated by prelatex.
 */
abstract public class Macro {
    protected Token[] pattern;

    /** Apply this macro to the state of proc using the specified arguments.
     *  Requires the length of arguments to match the macro's declared parameters.
     */
    abstract public void apply(Token[] arguments, MacroProcessor proc);
}