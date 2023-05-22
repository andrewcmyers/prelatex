package prelatex;

import prelatex.tokens.Item;
import prelatex.tokens.MacroName;

public class Normal implements Activity {

    @Override
    public void handle(Item item, MacroProcessor mp) {
        if (item instanceof MacroName m) {
            try {
                Macro b = mp.lookup(m.name());
                MacroArgs ma = new MacroArgs(b);
                if (ma.complete()) {
                    ma.apply(mp);
                } else {
                    mp.pushActivity(ma);
                }
            } catch (Namespace.LookupFailure e) {
                mp.output(item.chars());
            }
        } else {
            mp.output(item.chars());
        }
    }
}