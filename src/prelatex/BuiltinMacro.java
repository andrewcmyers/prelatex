package prelatex;

import java.util.List;

// A macro that is considered built-in to TeX/LaTeX and whose semantics need to be simulated.
public abstract class BuiltinMacro implements MacroBinding {
    private int numArgs;
    int numArguments() { return numArgs; }
    abstract void handleMacro(List<Item> arguments); // XXX need more args
}
