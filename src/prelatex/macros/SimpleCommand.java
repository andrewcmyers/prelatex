package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

public class SimpleCommand extends LaTeXBuiltin {

    interface MPAction {
        void act(MacroProcessor mp);
    }

    MPAction action;

    public SimpleCommand(String name, MPAction action) {
        super(name, 0);
        this.action = action;
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws MacroProcessor.SemanticError {
        action.act(mp);
    }
}
