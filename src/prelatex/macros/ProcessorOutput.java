package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.tokens.Token;

/** A place for the macro processor to send its output. */
public interface ProcessorOutput {
    void output(Token t);
}