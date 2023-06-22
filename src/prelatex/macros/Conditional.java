package prelatex.macros;

/** A conditional that must be terminated by \fi */
abstract public class Conditional extends Macro {

    protected Conditional(String name) {
        super(name);
    }

    @Override
    public boolean isConditional() {
        return true;
    }

    @Override
    public boolean isExpandable() { return true; }
}
