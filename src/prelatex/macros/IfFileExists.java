package prelatex.macros;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.io.File;
import java.util.List;

public class IfFileExists extends LaTeXBuiltin {
    public IfFileExists() {
        super("IfFileExists", 2);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws MacroProcessor.SemanticError {
        String filename = mp.flattenToString(arguments.get(0));
        Maybe<String> mf = mp.findFile(filename, List.of("", ".tex"));
        try {
            if (new File(mf.get()).canRead()) {
                mp.prependTokens(arguments.get(1));
            }
        } catch (NoMaybeValue e) {
            // skip
        }
    }
}