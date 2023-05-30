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

    @Override
    public String chars() {
        return "\\" + name + " ";
    }

    @Override public boolean equals(Object o) {
        return (o instanceof MacroName m && name.equals(m.name) && m.canEqual(this));
    }

    @Override
    public boolean canEqual(Object o) {
        return o instanceof MacroName;
    }

    public String name() { return name; }
}
