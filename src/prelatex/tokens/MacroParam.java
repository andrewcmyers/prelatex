package prelatex.tokens;

import easyIO.BacktrackScanner.Location;

/** A possibly nested macro parameter (#1 - #9, ##1-##9, etc.) */

public class MacroParam extends Token {
    Token token; // either 1-9 or another MacroParam token

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
}