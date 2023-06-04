package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Lexer;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;

public class CharMacro extends Macro {
    public CharMacro() {
        super("char");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.skipBlanks();
            StringBuilder b = new StringBuilder();
            while (mp.peekToken() instanceof CharacterToken c && Character.isDigit(c.codepoint())) {
                mp.nextToken();
                b.appendCodePoint(c.codepoint());
            }
            int codepoint = Integer.parseInt(b.toString());
            mp.prependTokens(new CharacterToken(codepoint, location));
        } catch (EOF e) {
            throw new Lexer.LexicalError("Unexpected end of input in \\char", location);
        }
    }
}
