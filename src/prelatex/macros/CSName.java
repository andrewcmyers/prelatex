package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroName;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.ArrayList;
import java.util.List;

public class CSName extends Macro {
    public CSName() {
        super("csname");
    }

    @Override
    public boolean isExpandable() {
        return true;
    }

    @Override
    public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError {
        try {
            ArrayList<Token> tokens = new ArrayList<>();
            collectTokens: for (;;) { Token t = mp.nextNonblankToken();
                switch (t) {
                    case Separator s:
                        break;
                    case MacroName m:
                        if (m.name().equals("endcsname")) break collectTokens;
                        try {
                            Macro b = mp.lookup(m);
                            if (b.isExpandable()) {
                                mp.macroCall(m);
                                continue;
                            }
                        } catch (Namespace.LookupFailure e) {
                            // fall through, ignore
                        }
                        mp.reportError("Invalid token in \\csname: " + t, location);
                        break;
                    case CharacterToken c:
                        tokens.add(t);
                        break;
                    default:
                        mp.reportError("Invalid token in \\csname: " + t, location);
                }
            }
            String name = mp.flattenToString(tokens);
            mp.prependTokens(delimiter);
            mp.prependTokens(List.of(new MacroName(name, location)));
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of input in \\csname", location);
        }
    }
}