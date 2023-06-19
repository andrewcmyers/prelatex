package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;

/** A macro introduced by \newif */
public class IfCommand extends Conditional {
    public IfCommand(String name) {
        super(name);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.applyConditional(mp.testCondition(name), location);
    }
}