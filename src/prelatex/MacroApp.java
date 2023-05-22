package prelatex;

import prelatex.tokens.Item;

public class MacroApp implements Activity {
    // The parameters expected by the macro, which may be a mix
    Item[] template;
    int position;

    @Override
    public boolean normal() {
        return false;
    }
}
