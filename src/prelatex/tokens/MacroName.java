package prelatex.tokens;

import easyIO.BacktrackScanner.Location;
import prelatex.Main;

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

    @Override
    public void normalProcess(Main main) {
    }

    public String name() { return name; }
}
