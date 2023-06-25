package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;

public class Lowercase extends Uppercase {
    public Lowercase() {
        super("lowercase");
    }

    @Override protected Token transformToken(Token t) {
        if (t instanceof CharacterToken c) {
            return new CharacterToken(Character.toLowerCase(c.codepoint()), t.location);
        } else {
            return t;
        }
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        assert arguments.size() == 1;
        List<Token> arg = arguments.get(0);
        ArrayList<Token> out = new ArrayList<>();
        for (Token t : arg) out.add(transformToken(t));
        mp.prependTokens(out);
    }
}
