package prelatex;

import prelatex.tokens.Item;

public interface Activity {

    /** Do the appropriate actions for this item on the given MacroProcessor. */
    void handle(Item item, MacroProcessor mp);
}