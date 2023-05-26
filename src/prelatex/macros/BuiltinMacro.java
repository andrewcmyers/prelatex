package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.Token;

import java.util.List;

/** A macro that is considered built-in to TeX/LaTeX and whose semantics need to be simulated. */
/** Macro type 2A (see Macro) */
public abstract class BuiltinMacro extends TeXMacro {
    protected BuiltinMacro(String n, int numArgs) {
        super(n, numArgs);
    }
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws SemanticError {
        throw new SemanticError("Unimplemented macro " + name, location);
    }
}
