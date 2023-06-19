package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;

public class IfMMode extends Conditional {
    public IfMMode() {
        super("ifmmode");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.skipBlanks();
        mp.applyConditional(mp.mathMode, location);
    }
}
