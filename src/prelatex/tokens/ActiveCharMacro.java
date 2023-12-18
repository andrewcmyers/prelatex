package prelatex.tokens;

import prelatex.lexer.Location;

/** A macro whose name is a single active character (that is, it
 *  has no backslash).
 */
public class ActiveCharMacro extends MacroName {
    private final int codepoint;
    public ActiveCharMacro(int codepoint, Location loc) {
        super(Character.toString(codepoint), loc);
        this.codepoint = codepoint;
    }
    @Override public boolean active() {
        return true;
    }
    @Override public String toString() {
        return name();
    }

    @Override public String chars() {
        return Character.toString(codepoint);
    }

    public int codepoint() {
        return codepoint;
    }
}
