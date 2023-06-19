package prelatex.tokens;

import prelatex.lexer.Location;

/** A macro whose name is a single active character.
 */
public class ActiveCharMacro extends MacroName {
    private int codepoint;
    public ActiveCharMacro(int codepoint, Location loc) {
        super(Character.toString(codepoint), loc);
        this.codepoint = codepoint;
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
