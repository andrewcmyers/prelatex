package prelatex.tokens;

import prelatex.lexer.SyntheticLocn;

public class Delimiter extends Separator {
    public Delimiter() {
        super("", new SyntheticLocn("limiter token"));
    }
}
