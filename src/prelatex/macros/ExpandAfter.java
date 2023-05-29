package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.Delimiter;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.LinkedList;

public class ExpandAfter extends Macro {
    public ExpandAfter() {
        super("expandafter");
    }

    @Override
    public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError {
        try {
            Token t = mp.nextNonblankToken(); // deferred token
            Token t2 = mp.nextToken();
            switch (t2) {
                case MacroName m2:
                    try {
                        mp.lookup(m2); // try it for the possible exception
                        Delimiter d  = new Delimiter();
                        if (delimiter.length > 0) {
                            Token[] nd = new Token[delimiter.length + 1];
                            System.arraycopy(delimiter, 0, nd, 1, delimiter.length);
                            nd[0] = d;
                            delimiter = nd;
                        } else {
                            delimiter = new Token[]{d};
                        }
                        mp.macroCall(m2, delimiter);
                        LinkedList<Token> expanded = new LinkedList<>();
                        collectTokens: for (;;) {
                            Token t3 = mp.nextToken();
                            switch (t3) {
                                case Delimiter d2:
                                    assert d2 == d;
                                    break collectTokens;
                                case MacroName m3:
                                    break;
                                default:
                                    expanded.add(t3);
                            }
                        }
                        mp.prependTokens(delimiter);
                        mp.prependTokens(expanded);
                        mp.prependTokens(t);
                    } catch (Namespace.LookupFailure e) {
                        mp.reportError("Can't \\expandafter unexpandable macro " + m2, location);
                        mp.prependTokens(delimiter);
                    }
                    break;
                default:
                    mp.prependTokens(delimiter);
                    mp.prependTokens(t2);
            }
        } catch (EOF exc) {
            throw new SemanticError("Unexpected end of input in \\expandafter", location);
        }

    }
}
