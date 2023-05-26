package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;

/** A macro introduced by \newif */
public class IfCommand extends Macro {
    public IfCommand(String name) {
        super(name);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.applyConditional(location, mp.testCondition(name));
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\" + name, location);
        }
    }
    @Override
    public boolean isConditional() {
        return true;
    }
}