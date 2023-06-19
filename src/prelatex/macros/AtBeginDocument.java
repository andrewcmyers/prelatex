package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

public class AtBeginDocument extends LaTeXBuiltin {
    public AtBeginDocument() {
        super("AtBeginDocument", 1, List.of());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        assert arguments.size() == 1;
        List<Token> arg = arguments.get(0);
        mp.atBeginDocument(arg);
    }
}
