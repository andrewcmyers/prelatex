package prelatex.macros;

import easyIO.EOF;
import prelatex.Main;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;

/** The \input macro */
public class InputMacro extends Macro {
    public InputMacro() {
        super("input");
        Location loc = new SyntheticLocn("\\RequirePackage parameter 1");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            List<Token> parsedArg;
            if (mp.peekToken() instanceof OpenBrace) { // LaTeX-style \input
                parsedArg = mp.parseMacroArg(none(), true);
            } else {
                parsedArg = mp.parseMacroArg(some(new Separator(" ",
                                new SyntheticLocn("\\input definition"))),
                        true);
                mp.nextToken();
            }

            List<Token> arg = mp.stripOuterBraces(parsedArg);
            String s = mp.flattenToString(arg);
            if (mp.packageDisposition.get("input " + s) == Main.Disposition.DROP) return;
            if (mp.packageDisposition.get("input " + s) == Main.Disposition.KEEP) {
                mp.output(new MacroName("input"));
                mp.output(new CharacterToken('{', location));
                mp.output(new StringToken(s, location));
                mp.output(new CharacterToken('}', location));
                mp.output(new Separator("\n", location));
                return;
            }
            mp.includeFile(arg, List.of("", ".tex"), location);
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of input in \\input", location);
        }
    }
}