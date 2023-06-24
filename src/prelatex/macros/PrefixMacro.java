package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

public class PrefixMacro extends LaTeXBuiltin {
    public PrefixMacro(String name) {
        super(name, 0);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        mp.setPrefix(name);
    }
}
