package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

public class AtEndDocument extends LaTeXBuiltin {
    public AtEndDocument() {
        super("AtEndDocument", 1, List.of());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        assert arguments.size() == 1;
        mp.atEndDocument(arguments.get(0));
    }
}
