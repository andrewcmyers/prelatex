package prelatex;

import prelatex.lexer.Location;

public class SemanticError extends PrelatexError {
    public SemanticError(String m, Location l) {
        super(m, l);
    }
}
