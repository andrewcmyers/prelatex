package prelatex.macros;

import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.*;

import java.util.List;

public class End extends LaTeXBuiltin {
    public End() {
        super("end", 1, List.of());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws PrelatexError {
        String s = mp.flattenToString(arguments.get(0));
        MacroName endname = new MacroName("end" + s, location);
        try {
            if (mp.lookup(endname) instanceof LaTeXMacro) {
                mp.prependTokens(endname);
                return;
            }
            // wrong kind of macro, fall through
        } catch (Namespace.LookupFailure e) {
            //  macro undefined, fall through
        }
        mp.output(new MacroName("end", location), new OpenBrace(location));
        mp.output(new StringToken(s, location));
        mp.output(new CloseBrace(location));
    }
}
