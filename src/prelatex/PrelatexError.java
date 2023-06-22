package prelatex;

import prelatex.lexer.Location;

public class PrelatexError extends Exception {
    Location location;
    protected PrelatexError(String message, Location loc) {
        super(loc + ": " + message);
        location = loc;
    }
}
