package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;

public class IfFalse extends Conditional {
    public IfFalse() {
        super("iffalse");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.applyConditional(false, location);
    }
}