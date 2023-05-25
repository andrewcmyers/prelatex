package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.Token;

import java.util.List;

/** A macro that is considered built-in to TeX/LaTeX and whose semantics need to be simulated. */
public abstract class BuiltinMacro extends Macro {
    protected BuiltinMacro(String n) {
        super(n);
    }
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws SemanticError {
        throw new SemanticError("Unimplemented macro " + name, location);
    }
}
