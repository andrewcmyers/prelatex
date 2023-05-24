package prelatex.macros;

import prelatex.SemanticError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.List;

public class UserMacro extends Macro {

    protected UserMacro(String n) {
        super(n);
    }

    private List<Token> body;

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
        throws SemanticError {
        LinkedList<Token> tokens = new LinkedList<>();
        for (Token t : body) {
            switch (t) {
                case MacroParam p:
                    if (p.token().chars().equals("#")) {
                        tokens.addLast(p.token());
                    } else {
                        int parameter = Integer.parseInt(p.token().chars());
                        assert 1 <= parameter && parameter <= 9;
                        if (parameter > numArgs) {
                            throw new SemanticError("Illegal parameter " + parameter + " in body of " + name
                            + " at " + p.location, location);
                        }
                        tokens.addAll(arguments.get(parameter - 1));
                    }
                    break;
                default:
                    tokens.addLast(t);
                    break;
            }
        }
        mp.prependTokens(tokens);
    }
}
