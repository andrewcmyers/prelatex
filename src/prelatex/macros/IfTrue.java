package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.Set;

public class IfTrue extends Conditional {
    public IfTrue() {
        super("iftrue");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.applyConditional(true, location);
    }
}
