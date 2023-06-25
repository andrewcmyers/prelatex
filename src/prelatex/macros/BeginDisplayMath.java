package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.List;

public class BeginDisplayMath extends LaTeXBuiltin {
    public BeginDisplayMath() {
        super("[", 0);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        mp.pushContexts(mp.openBrace);
        mp.mathMode = true;
        mp.output(new MacroName("[", location));
    }
}
