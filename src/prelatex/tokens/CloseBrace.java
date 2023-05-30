package prelatex.tokens;

import prelatex.lexer.Location;

public class CloseBrace extends Token {
    public CloseBrace(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "}";
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof CloseBrace b && b.canEqual(this));
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof CloseBrace);
    }
}
