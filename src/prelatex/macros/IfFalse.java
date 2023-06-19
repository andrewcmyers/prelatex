package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.Set;

public class IfFalse extends Conditional {
    public IfFalse() {
        super("iffalse");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.parseMatched(Set.of(mp.fi));
        } catch (EOF e) {
            throw new PrelatexError("Unexpected end of input in \\iffalse", location);
        }
    }
}