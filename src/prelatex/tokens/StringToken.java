package prelatex.tokens;

import prelatex.lexer.Location;

/** A StringToken is not produced by the lexer. It exists for
 * convenience in generating output. It behaves like
 */
public class StringToken extends Token {
    String chars;

    public StringToken(String s, Location l) {
        super(l);
        this.chars = s;
    }

    @Override
    public String toString() {
        return chars;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof StringToken s && chars.equals(s.chars) && s.canEqual(this));
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof StringToken);
    }
}
