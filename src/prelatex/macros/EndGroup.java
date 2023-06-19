package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;

public class EndGroup extends Macro {
    public EndGroup() {
        super("endgroup");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.popContexts(BeginGroup.opener, location);
        mp.output(new MacroName("endgroup", location));
    }
}
