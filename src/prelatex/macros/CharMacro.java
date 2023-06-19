package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Lexer;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.StringToken;
import prelatex.tokens.Token;

import java.util.List;

public class CharMacro extends Macro {
    public CharMacro() {
        super("char");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        mp.skipBlanks();
        StringBuilder b = new StringBuilder();
        List<Token> num = mp.parseNumber(location);
        int codepoint = Integer.parseInt(mp.flattenToString(num));
        mp.prependTokens(new StringToken("\\char" + codepoint, location));
    }
}
