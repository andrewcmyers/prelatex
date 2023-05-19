package prelatex.tokens;

import easyIO.BacktrackScanner.Location;

/** a token representing a single Unicode character. */
public class CharacterToken extends Token {
    int codepoint;
    public CharacterToken(int codepoint, Location loc) {
        super(loc);
        this.codepoint = codepoint;
    }

    public String toString() {
        return Character.toString(codepoint);
    }
}
