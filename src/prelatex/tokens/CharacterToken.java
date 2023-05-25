package prelatex.tokens;

import prelatex.lexer.Location;

/** a token representing a single Unicode character. */
public class CharacterToken extends Token {
    private int codepoint;
    public CharacterToken(int codepoint, Location loc) {
        super(loc);
        assert codepoint >= 0;
        this.codepoint = codepoint;
    }

    public int codepoint() {
        return codepoint;
    }

    public String chars() { return toString(); }
    public String toString() {
        return Character.toString(codepoint);
    }
}
