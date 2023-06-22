package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** The \input macro */
public class InputMacro extends Macro {
    public InputMacro() {
        super("input");
        Location loc = new SyntheticLocn("\\RequirePackage parameter 1");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            LinkedList<Token> parsedArg = mp.parseMatched(Set.of(new Separator(" ",
                            new SyntheticLocn("\\input definition"))),
                    true);
            parsedArg.removeLast();
            List<Token> arg =  mp.stripOuterBraces(parsedArg);
            mp.includeFile(arg, List.of("", ".tex"), location);
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of input in \\input", location);
        }
    }
}