package prelatex.macros;

/** A conditional that must be terminated by \fi */
abstract public class Conditional extends Macro {

    protected Conditional(String name) {
        super(name);
    }

    public boolean isConditional() {
        return true;
    }
}
