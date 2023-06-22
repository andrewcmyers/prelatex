package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.Main;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CloseBrace;
import prelatex.tokens.MacroName;
import prelatex.tokens.OpenBrace;
import prelatex.tokens.Token;

import java.util.List;
import java.util.Set;

import static cms.util.maybe.Maybe.none;

public class Typeout extends LaTeXBuiltin {
    public Typeout() {
        super("typeout", 1, List.of());
    }
    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            List<Token> text = mp.parseMatched(Set.of(), true);
            text = mp.stripOuterBraces(text);
            if (!Main.Disposition.DROP.equals(mp.macroDisposition.get("typeout"))) {
                mp.reportError(mp.flattenToString(text), location);
            }
            mp.output(new MacroName("typeout", location));
            mp.output(new OpenBrace(location));
            mp.output(text);
            mp.output(new CloseBrace(location));
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in \\typeout argument", location);
        }
    }
}
