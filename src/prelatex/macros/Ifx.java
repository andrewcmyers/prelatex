package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

public class Ifx extends Macro {
    protected Ifx() {
        super("ifx");
    }

    @Override
    public boolean isConditional() {
        return true;
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.skipBlanks();
            Token t1 = mp.nextToken();
            mp.skipBlanks();
            Token t2 = mp.nextToken();
        } catch (EOF exc) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\ifx", location);
        }
    }
}
