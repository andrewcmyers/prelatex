package prelatex.tokens;

import prelatex.lexer.Location;

public class MathToken extends Token {

    private boolean display;

    public MathToken(boolean display, Location location) {
        super(location);
    }

    public boolean display() {
        return display;
    }

    @Override
    public String toString() {
        return display ? "$$" : "$";
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MathToken t && t.canEqual(this)) {
            return t.display == display;
        }
        return false;
    }

    @Override
    public boolean canEqual(Object o) {
        return o instanceof MathToken;
    }
}
