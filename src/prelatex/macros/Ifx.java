package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.Iterator;
import java.util.List;

public class Ifx extends Conditional {
    public Ifx() {
        super("ifx");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.skipBlanks();
            Token t1 = mp.nextToken();
            mp.skipBlanks();
            Token t2 = mp.nextToken();
            boolean comparison = false;
            switch (t1) {
                case MacroName m1:
                    if (t2 instanceof MacroName m2) {
                        comparison = compareMacros(m1, m2, mp);
                    }
                    break;
                default:
                    comparison = t1.chars().equals(t2.chars());
                    break;
            }
            mp.applyConditional(comparison);
        } catch (EOF exc) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\ifx", location);
        }
    }

    private boolean compareMacros(MacroName m1, MacroName m2, MacroProcessor mp) {
        if (m1.name().equals(m2)) {
            return true;
        } else {
            try {
                Macro b1 = mp.lookup(m1.name());
                Macro b2 = mp.lookup(m2.name());
                if (b1 instanceof DefMacro d1 &&
                    b2 instanceof DefMacro d2) {
                    return compareTokens(d1.body, d2.body);
                } else if (b1 instanceof LaTeXMacro d1 &&
                           b2 instanceof LaTeXMacro d2) {
                    return compareTokens(d1.body, d2.body);
                }
                return false;
            } catch (Namespace.LookupFailure exc) {
                return false;
            }
        }
    }
    boolean compareTokens(List<Token> l1, List<Token> l2) {
        Iterator<Token> i2 = l2.iterator();
        for (Token t1 : l1) {
            if (!i2.hasNext()) return false;
            Token t2 = i2.next();
            if (!t1.toString().equals(t2.toString())) return false;
        }
        return true;
    }
}
