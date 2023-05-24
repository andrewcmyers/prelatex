package prelatex.lexer;

/** A fake location that is not in any actual source file. Used for definitions
 * of built-in macros.
 */
public class SyntheticLocn implements Location {
    String description;
    public SyntheticLocn(String desc) {
        description = desc;
    }
    public String toString() {
        return description;
    }
}
