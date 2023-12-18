package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.List;

/** A macro that is considered built-in to TeX/LaTeX and whose semantics need to be simulated.
 * Macro type 2A (see Macro) */
public abstract class BuiltinMacro extends TeXMacro {
    protected BuiltinMacro(String n, int numArgs) {
        super(new MacroName(n, new SyntheticLocn("Definition of " + n)), numArgs);
    }
    protected BuiltinMacro(MacroName n, int numArgs) {
        super(n, numArgs);
    }
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        throw new SemanticError("Unimplemented macro " + name, location);
    }
}
