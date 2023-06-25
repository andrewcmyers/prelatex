package prelatex;

import prelatex.macros.ProcessorOutput;
import prelatex.tokens.MacroName;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.io.PrintWriter;
import java.util.ArrayList;

public class CondensedOutput implements ProcessorOutput {
    private final PrintWriter out;
    private final boolean removeComments;
    private boolean lastWasAlphaMacro = false;
    private boolean lastWasNewline = false;

    private ArrayList<Token> outputLog; // null if debugging turned off
    private final static boolean DEBUG_OUTPUT = false;

    CondensedOutput(PrintWriter out, boolean removeComments) {
        this.out = out;
        this.removeComments = removeComments;
        if (DEBUG_OUTPUT) outputLog = new ArrayList<>();
    }
    @Override
    public void output(Token t) {
        if (DEBUG_OUTPUT) outputLog.add(t);
        if (t instanceof Separator && t.chars().contains("\n")) {
            if (lastWasNewline) {
                out.print(t.chars().replace("\n", ""));
                return; // don't create fake paragraph break
            }
            lastWasNewline = true;
        } else {
            lastWasNewline = false;
        }
        String s = t.chars();
        if (Character.isAlphabetic(s.charAt(0)) && lastWasAlphaMacro) {
            out.print(' ');
        }
        if (' ' == s.charAt(0) && lastWasAlphaMacro) {
            out.print("{}"); // prevent previous macro from eating real whitespace
        }
        if (t instanceof MacroName) {
            s = t.toString();
            out.print(s);
            lastWasAlphaMacro = Character.isAlphabetic(s.charAt(s.length() - 1));
        } else {
            lastWasAlphaMacro = false;
            if (removeComments && t instanceof Separator && s.charAt(0) == '%') {
                out.print("%");
            } else {
                out.print(s);
            }
        }
    }
}
