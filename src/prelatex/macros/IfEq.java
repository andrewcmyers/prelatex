package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.*;

import java.util.ArrayList;

/** The basic \if macro that compares two expanded tokens */
public class IfEq extends Conditional {
    public IfEq() {
        super("if");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.skipBlanks();
            ArrayList<Token> tokens = new ArrayList<>();
            while (tokens.size() < 2) {
                // keep expanding until we pull two tokens off
                Token t = mp.nextToken();
                if (t instanceof MacroName m) {
                    try {
                        Macro b = mp.lookup(m);
                        if (b.isExpandable()) {
                            mp.macroCall(m);
                            continue;
                        }
                    } catch (Namespace.LookupFailure e) {
                        // just add
                    }
                }
                tokens.add(t);
            }
            boolean comparison = false;
            assert tokens.size() == 2;
            Token t1 = tokens.get(0), t2 = tokens.get(1);
            switch (t1) {
                case MacroName m1:
                    if (t2 instanceof MacroName m2) {
                        comparison = true;
                    }
                    break;
                case CharacterToken c1:
                    if (t2 instanceof CharacterToken c2 &&
                            c1.codepoint() == c2.codepoint())
                       comparison = true;
                    break;
                case Separator s1:
                    if (t2 instanceof Separator) comparison = true;
                    break;
                case OpenBrace b1:
                    if (t2 instanceof OpenBrace) comparison = true;
                    break;
                case CloseBrace b1:
                    if (t2 instanceof CloseBrace) comparison = true;
                    break;
                case MacroParam p1:
                    if (t2 instanceof MacroParam p2 &&
                            p1.toString().equals(p2.toString())) comparison = true;
                default:
                    break;
            }
            mp.applyConditional(location, comparison);
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\if", location);
        }
    }
}
