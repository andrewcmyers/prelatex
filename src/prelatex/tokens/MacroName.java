package prelatex.tokens;

import easyIO.BacktrackScanner.Location;

/** A token of the form \abCd...
 */
public class MacroName extends Token {
    private String name;
    public MacroName(String name, Location loc) {
        super(loc);
        this.name = name;
    }

    @Override
    public String toString() {
        return "\\" + name;
    }
}
