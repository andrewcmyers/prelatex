package tokens;

import easyIO.BacktrackScanner.Location;

public class CloseBrace extends Token {
    public CloseBrace(Location location) {
        super(location);
    }

    @Override
    public String toString() {
        return "}";
    }
}
