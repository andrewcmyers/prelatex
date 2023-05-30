package prelatex.tokens;

import prelatex.lexer.Location;

public class OpenBrace extends Token {
    public OpenBrace(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "{";
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof OpenBrace b && b.canEqual(this));
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof OpenBrace);
    }
}
