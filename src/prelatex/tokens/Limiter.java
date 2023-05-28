package prelatex.tokens;

import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;

public class Limiter extends Separator {
    public Limiter() {
        super("", new SyntheticLocn("limiter token"));
    }
}
