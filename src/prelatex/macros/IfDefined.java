package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

public class IfDefined extends Conditional {
    public IfDefined() {
        super("ifdefined");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            Token t = mp.nextNonblankToken();
            if (t instanceof MacroName m) {
                boolean defined;
                try {
                    mp.lookup(m.name());
                    defined = true;
                } catch (Namespace.LookupFailure e) {
                    defined = false;
                }
                mp.applyConditional(defined, t.location);
            } else {
                throw new MacroProcessor.SemanticError("\\ifdefined argument must be macro name", location);
            }
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\ifdefined", location);
        }
    }
}
