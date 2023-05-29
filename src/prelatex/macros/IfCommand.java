package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

/** A macro introduced by \newif */
public class IfCommand extends Conditional {
    public IfCommand(String name) {
        super(name);
    }

    @Override
    public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError {
        try {
            mp.applyConditional(location, mp.testCondition(name));
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\" + name, location);
        }
    }
}