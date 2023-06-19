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
        MacroName endName = new MacroName("end" + s, location);
        try {
            if (mp.lookup(endName) instanceof LaTeXMacro) {
                mp.prependTokens(endName);
                return;
            }
            // wrong kind of macro, fall through
        } catch (Namespace.LookupFailure e) {
            //  macro undefined, fall through
        }
        if (s.equals("document")) {
            mp.outputEpilogue();
        }
        mp.output(new MacroName("end", location), new OpenBrace(location));
        mp.output(new StringToken(s, location));
        mp.output(new CloseBrace(location));
    }
}
