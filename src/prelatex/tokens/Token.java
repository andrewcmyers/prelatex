package prelatex.tokens;

import prelatex.lexer.Location;

/** A token that is meaningful to TeX. */
public abstract class Token extends Item {
    public Token(Location loc) {
        super(loc);
    }

    @Override
    public boolean isSeparator() { return false; }

    abstract public String toString();
    public String chars() {
        return toString();
    }
}