package prelatex.macros;

import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.List;

/** A LaTeX-syntax macro with user-defined behavior. Macro type 1B per Macro.java */
public class LaTeXMacro extends LaTeXBuiltin {
    List<Token> body;
    protected LaTeXMacro(String n, int numArgs, List<List<Token>> defaultArgs, List<Token> body) {
        super(n, numArgs, defaultArgs);
        this.body = body;
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws MacroProcessor.SemanticError {
        mp.substituteTokens(body, arguments, location);
    }
    @Override
    public boolean isExpandable() {
        return true;
    }
}