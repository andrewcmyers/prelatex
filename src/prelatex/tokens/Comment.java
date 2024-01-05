package prelatex.tokens;

import prelatex.lexer.Location;

public class Comment extends Token {
    String chars;

    public Comment(String s, Location loc) {
        super(loc);
        chars = s;
    }

    @Override
    public String toString() {
        return chars + "\n";
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Comment c && c.canEqual(this) && chars.equals(c.chars));
    }

    @Override
    public boolean canEqual(Object o) {
        return o instanceof Comment;
    }
}
