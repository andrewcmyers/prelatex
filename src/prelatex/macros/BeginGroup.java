package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.MacroName;

import javax.naming.NoPermissionException;

public class BeginGroup extends Macro {
    public static final Macro opener = new NoopMacro("begingroup opener", 0);

    public BeginGroup() {
        super("begingroup");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.pushContexts(opener);
        mp.output(new MacroName("begingroup", location));
    }
}
