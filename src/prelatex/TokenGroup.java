package prelatex;

import prelatex.lexer.Location;
import prelatex.tokens.Item;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.List;

public class TokenGroup extends Token {
    private final Token[] tokens;

    public TokenGroup(List<Token> tokens, Location l) {
        super(l);
        this.tokens = tokens.toArray(new Token[0]);
    }

    @Override
    public String toString() {
        return "{" + chars() + "}";
    }

    public String chars() {
        StringBuilder b = new StringBuilder();
        b.append('{');
        boolean mayNeedSep = false;
        for (Item i : tokens) {
            String s = i.chars();
            if (mayNeedSep && Character.isAlphabetic(s.charAt(0))) {
                b.append(' ');
                mayNeedSep = false;
            }
            b.append(i.chars());
            if (i instanceof MacroName) mayNeedSep = true;
        }
        b.append('}');
        return b.toString();
    }
}