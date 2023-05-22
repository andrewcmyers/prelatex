package prelatex.tokens;

import prelatex.lexer.Location;

/** A LaTeX file is parsed into a sequence of Items. */
abstract public class Item {
    private final Location location;

    public Item(Location loc) {
        location = loc;
    }

    public Location location() {
        return location;
    }

    public abstract boolean isSeparator();

    /** The output form of this item. */
    public abstract String chars();
}