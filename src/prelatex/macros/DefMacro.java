package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

/** Macro type 1A (see Macro) */
public class DefMacro extends TeXMacro {
    protected List<Token> body;

    protected DefMacro(String name, int numArgs, List<Token> pattern, List<Token> body) {
        super(name, numArgs);
        this.body = body;
        setPattern(pattern);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
        throws MacroProcessor.SemanticError {
        mp.substituteTokens(body, arguments, location);
    }
}