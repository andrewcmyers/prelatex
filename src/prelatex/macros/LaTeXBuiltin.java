package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;

/** A LaTeX-syntax macro with built-in behavior. Macro type 2B per Macro.java */
abstract public class LaTeXBuiltin extends BuiltinMacro {
    /** Possibly empty list of default arguments. */
    protected final List<List<Token>> defaultArgs;

    static final List<List<Token>> noDefaultArgs = new ArrayList<>();

    protected LaTeXBuiltin(String n, int numArgs, List<List<Token>> defaultArgs) {
        this(new MacroName(n, new SyntheticLocn("Definition of " +n)), numArgs, defaultArgs);
    }
    protected LaTeXBuiltin(MacroName n, int numArgs, List<List<Token>> defaultArgs) {
        super(n, numArgs);
        this.defaultArgs = defaultArgs;
    }
    protected LaTeXBuiltin(MacroName n, int numArgs) {
        super(n, numArgs);
        this.defaultArgs = noDefaultArgs;
    }
    protected LaTeXBuiltin(String n, int numArgs) {
        this(new MacroName(n, new SyntheticLocn("Definition of " + n)), numArgs);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        List<List<Token>> arguments = mp.parseLaTeXArguments(numArgs, defaultArgs, location);
        applyArguments(arguments, mp, location);
    }
}