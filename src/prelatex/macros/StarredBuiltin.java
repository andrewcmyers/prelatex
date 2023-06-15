package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.List;

/** A LaTeX builtin macro that permits an optional star */
public class StarredBuiltin extends LaTeXBuiltin {

    protected StarredBuiltin(String name, int numArgs, List<List<Token>> defaultArgs) {
        super(name, numArgs, defaultArgs);
    }
    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        boolean star = mp.skipStar();
        List<List<Token>> arguments = mp.parseLaTeXArguments(numArgs, defaultArgs, location);
        applyArguments(arguments, mp, location);
    }
}
