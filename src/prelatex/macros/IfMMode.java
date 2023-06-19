package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import javax.crypto.Mac;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static prelatex.macros.MacroProcessor.elseToken;

public class IfMMode extends Conditional {
    public IfMMode() {
        super("ifmmode");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        int num = -1;
        mp.skipBlanks();
        LinkedList<Token> selected = new LinkedList<>();
        mp.applyConditional(mp.mathMode, location);
    }
}
