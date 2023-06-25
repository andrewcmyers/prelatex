package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;

public class Uppercase extends LaTeXBuiltin {
    public Uppercase() {
        this("uppercase");
    }

    protected Uppercase(String n) {
        super(n, 1);
    }

    protected Token transformToken(Token t) {
        if (t instanceof CharacterToken c) {
            return new CharacterToken(Character.toUpperCase(c.codepoint()), t.location);
        } else {
            return t;
        }
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        assert arguments.size() == 1;
        List<Token> arg = arguments.get(0);
        ArrayList<Token> out = new ArrayList<>();
        for (Token t : arg) {
        }
        mp.prependTokens(out);
    }
}
