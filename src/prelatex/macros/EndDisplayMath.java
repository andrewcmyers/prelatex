package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.List;

public class EndDisplayMath extends LaTeXBuiltin {
    public EndDisplayMath() {
        super("]", 0);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        mp.popContexts(mp.openBrace, location);
        mp.mathMode = false;
        mp.output(new MacroName("]", location));
    }
}
