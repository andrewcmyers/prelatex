package prelatex.tokens;

import easyIO.BacktrackScanner;
import org.apache.commons.text.StringEscapeUtils;
import prelatex.lexer.Location;
import prelatex.lexer.ScannerLocn;

/** Character from the file that have no significance to TeX, like
 *  whitespace or comments. Still, they should be preserved.
 */
public class Separator extends Token {
    private final String chars;
    public Separator(String chars, Location loc) {
        super(loc);
        this.chars = chars;
    }

    @Override
    public boolean isBlank() {
        return true;
    }

    public String toString() {
        return "Separator \"" + StringEscapeUtils.escapeJava(chars) + "\"";
    }
    @Override
    public String chars() {
        return chars;
    }
}