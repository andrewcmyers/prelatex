package prelatex.lexer;

import easyIO.BacktrackScanner;

public class ScannerLocn implements Location {
    BacktrackScanner.Location loc;

    public ScannerLocn(BacktrackScanner.Location location) {
        loc = location;
    }
    public String toString() {
        return loc.toString();
    }
}
