package prelatex.tokens;

import prelatex.lexer.Location;

/** A token representing a single Unicode character. */
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

    private static final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'a', 'b', 'c', 'd', 'e', 'f'};

    public String chars() {
        if (codepoint >= ' ') return toString();
        return "^^" + digits[codepoint/16] + digits[codepoint % 16];
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CharacterToken c && c.canEqual(this) && this.codepoint == c.codepoint);
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof CharacterToken);
    }

    public String toString() {
        return Character.toString(codepoint);
    }
}
