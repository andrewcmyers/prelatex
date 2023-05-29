package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.LinkedList;

public class ExpandAfter extends Macro {
    public ExpandAfter() {
        super("expandafter");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            Token t = mp.nextNonblankToken(); // deferred token
            Token t2 = mp.nextToken();
            if (t2 instanceof MacroName m2) {
                mp.macroCall(m2);
                mp.prependTokens(t);
            } else {
                mp.prependTokens(t, t2);
            }
        } catch (EOF exc) {
            throw new SemanticError("Unexpected end of input in \\expandafter", location);
        }

    }
}
