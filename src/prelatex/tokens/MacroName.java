package prelatex.tokens;

import prelatex.lexer.Location;

/** A token of the form \abCd...
 */
public class MacroName extends Token {
    private final String name;
    public MacroName(String name, Location loc) {
        super(loc);
        this.name = name;
    }

    @Override
    public String toString() {
        return "\\" + name;
    }

    public String name() { return name; }
}
