package prelatex.tokens;

import prelatex.lexer.Location;

/** A possibly nested macro parameter (#1 - #9, ##1-##9, etc.) */

public class MacroParam extends Token {
    private Token token; // either 1-9 or another MacroParam token

    public MacroParam(Token t, Location loc) {
        super(loc);
        token = t;
    }
    /** The parameter index */
    public Token token() {
        return token;
    }

    @Override
    public String toString() {
        return "#" + token;
    }

    @Override public boolean equals(Object o) {
        return (o instanceof MacroParam m && token.equals(m.token) && m.canEqual(this));
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof MacroParam);
    }


}