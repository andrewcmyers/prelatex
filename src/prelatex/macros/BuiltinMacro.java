package prelatex.macros;

import prelatex.macros.Macro;

/** A macro that is considered built-in to TeX/LaTeX and whose semantics need to be simulated. */
public abstract class BuiltinMacro extends Macro {
    protected BuiltinMacro(String n) {
        super(n);
    }
}
