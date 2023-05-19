package prelatex.tokens;

import easyIO.BacktrackScanner.Location;

public class OpenBrace extends Token {
    public OpenBrace(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "{";
    }
}
