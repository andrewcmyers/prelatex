package prelatex.tokens;

import prelatex.lexer.Location;

public class ParagraphBreak extends Token {
    String text;
    public ParagraphBreak(String text, Location location) {
        super(location);
        this.text = text;
    }

    @Override
    public String toString() {
        return "\\par";
    }

    public String chars() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ParagraphBreak);
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean canEqual(Object o) {
        return (o instanceof ParagraphBreak);
    }
}
