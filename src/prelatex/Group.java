package prelatex;

import prelatex.lexer.Location;
import prelatex.tokens.CloseBrace;
import prelatex.tokens.Item;
import prelatex.tokens.Token;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class Group implements Activity {
    private LinkedList<Token> tokens = new LinkedList<>();
    private Location location;

    public Group(Location l) {
        this.location = l;
    }

    @Override
    public void handle(Item item, MacroProcessor mp) {
        if (item instanceof CloseBrace) {
            Token result;
            if (tokens.size() == 1) {
                result = tokens.getFirst();
            } else {
                result = new TokenGroup(tokens, location);
            }
            mp.prependToken(result);

        }
        if (item instanceof Token t) {
            tokens.add(t);
        }
    }
}
