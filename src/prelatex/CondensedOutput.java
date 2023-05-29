package prelatex;

import prelatex.macros.ProcessorOutput;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.io.PrintWriter;

public class CondensedOutput implements ProcessorOutput {
    PrintWriter out;
    boolean lastWasMacro = false;

    CondensedOutput(PrintWriter out) {
        this.out = out;
    }
    @Override
    public void output(Token t) throws PrelatexError {
        String s = t.chars();
        if (Character.isAlphabetic(s.charAt(0)) && lastWasMacro) {
            out.print(' ');
        }
        if (t instanceof MacroName) {
            out.print(t.toString());
            lastWasMacro = true;
        } else {
            lastWasMacro = false;
            out.print(s);
        }
    }
}
