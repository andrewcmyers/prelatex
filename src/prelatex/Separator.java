package prelatex;

import easyIO.BacktrackScanner.Location;

/** Character from the file that have no significance to TeX, like
 *  whitespace or comments. Still, they should be preserved.
 */
public class Separator extends Item {
    String chars;
    public Separator(String chars, Location loc) {
        super(loc);
        this.chars = chars;
    }

    @Override
    public boolean isSeparator() {
        return true;
    }
}
