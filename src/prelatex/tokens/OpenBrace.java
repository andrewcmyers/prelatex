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
}
