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
        MacroProcessor.thenElse thenElse = mp.parseThenElse(location);
        mp.prependTokens(thenElse.elseClause());
    }
}