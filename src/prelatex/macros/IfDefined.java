package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.Set;

public class IfDefined extends Conditional {
    public IfDefined() {
        super("ifdefined");
    }

    @Override
    public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError {
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
                LinkedList<Token> thenClause, elseClause = new LinkedList<>();
                thenClause = mp.parseMatched(Set.of(MacroProcessor.fi,
                                        MacroProcessor.elseToken));
                MacroName sep = (MacroName)thenClause.removeLast();
                if (sep.name().equals("else")) {
                    elseClause = mp.parseMatched(Set.of(MacroProcessor.fi));
                }
                if (defined) {
                    mp.prependTokens(thenClause);
                } else {
                    mp.prependTokens(elseClause);
                }
            } else {
                throw new MacroProcessor.SemanticError("\\ifdefined argument must be macro name", location);
            }
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\ifdefined", location);
        }
    }
}
