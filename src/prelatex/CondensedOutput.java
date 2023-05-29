package prelatex;

import prelatex.macros.ProcessorOutput;
import prelatex.tokens.MacroName;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.io.PrintWriter;

public class CondensedOutput implements ProcessorOutput {
    private PrintWriter out;
    private boolean removeComments;
    private boolean lastWasMacro = false;

    CondensedOutput(PrintWriter out, boolean removeComments) {
        this.out = out;
        this.removeComments = removeComments;
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
            if (removeComments && t instanceof Separator && s.charAt(0) == '%') {
                out.print("%\n");
            } else {
                out.print(s);
            }
        }
    }
}
