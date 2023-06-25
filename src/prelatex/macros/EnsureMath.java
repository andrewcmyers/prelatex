package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MathToken;
import prelatex.tokens.Token;

import java.util.List;

public class EnsureMath extends LaTeXBuiltin {
    public EnsureMath() {
        super("ensuremath", 1);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        if (!mp.mathMode) {
            mp.prependTokens(new MathToken(false, location));
        }
        mp.prependTokens(arguments.get(0));
        if (!mp.mathMode) {
            mp.prependTokens(new MathToken(false, location));
        }
    }
}
