package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;

public class IfTrue extends Conditional {
    public IfTrue() {
        super("iftrue");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.applyConditional(true, location);
    }
}
