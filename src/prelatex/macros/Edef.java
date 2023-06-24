package prelatex.macros;

public class Edef extends Def {
    public Edef() {
        super("edef");
    }

    @Override
    protected boolean expandBody() {
        return true;
    }
}
