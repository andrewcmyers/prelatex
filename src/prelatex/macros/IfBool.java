package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

/** The \ifbool macro from etoolbox */
public class IfBool extends LaTeXBuiltin {
    public IfBool() {
        super("ifbool", 3);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws MacroProcessor.SemanticError {
        String ifName = mp.flattenToString(arguments.get(0));
        boolean b = mp.testCondition(ifName);
        mp.prependTokens(arguments.get(b ? 1 : 2));
    }
}
