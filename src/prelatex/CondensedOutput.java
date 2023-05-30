package prelatex;

import prelatex.macros.ProcessorOutput;
import prelatex.tokens.MacroName;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.io.PrintWriter;

public class CondensedOutput implements ProcessorOutput {
    private PrintWriter out;
    private boolean removeComments;
    private boolean lastWasAlphaMacro = false;

    CondensedOutput(PrintWriter out, boolean removeComments) {
        this.out = out;
        this.removeComments = removeComments;
    }
    @Override
    public void output(Token t) throws PrelatexError {
        String s = t.chars();
        if (Character.isAlphabetic(s.charAt(0)) && lastWasAlphaMacro) {
            out.print(' ');
        }
        if (t instanceof MacroName) {
            s = t.toString();
            out.print(s);
            lastWasAlphaMacro = Character.isAlphabetic(s.charAt(s.length() - 1));
        } else {
            lastWasAlphaMacro = false;
            if (removeComments && t instanceof Separator && s.charAt(0) == '%') {
                out.print("%\n");
            } else {
                out.print(s);
            }
        }
    }
}
