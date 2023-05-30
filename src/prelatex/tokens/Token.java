package prelatex.tokens;

import prelatex.lexer.Location;

/** A LaTeX file is parsed into a sequence of Tokens, some of which are blank. */
public abstract class Token {

    public final Location location;

    public Token(Location loc) {
        location = loc;
    }

    /* A blank token is some variety of whitespace or a comment */
    public boolean isBlank() { return false; }

    abstract public String toString();

    /** The output form of this item. */
    public String chars() {
        return toString();
    }

    @Override
    public abstract boolean equals(Object o);
    public abstract boolean canEqual(Object o);
}