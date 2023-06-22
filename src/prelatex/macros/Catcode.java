package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.lexer.Location;
import prelatex.tokens.ActiveCharMacro;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;

import java.util.List;

public class Catcode extends Macro {
    public Catcode() {
        super("catcode");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            Token t = mp.nextNonblankToken();
            if (!t.equals(new CharacterToken('`', location))) {
                throw new SemanticError("Expected ` after \\catcode", t.location);
            }
            Token ch = mp.nextToken();
            int codepoint;
            switch (ch) {
                case ActiveCharMacro m:
                    codepoint = m.codepoint();
                    break;
                case MacroName m:
                    if (m.name().length() > 1) {
                        throw new SemanticError("improper alphabetic sequence in \\catcode", ch.location);
                    }
                    codepoint = Character.codePointAt(m.name(), 0);
                    break;
                case CharacterToken ct:
                    codepoint = ct.codepoint();
                    break;
                default:
                    codepoint = Character.codePointAt(ch.chars(), 0); // TODO: handle other cases better
            }
            Token eq = mp.nextNonblankToken();
            if (!eq.equals(new CharacterToken('=', eq.location))) {
                throw new SemanticError("Expected = after character in \\catcode", eq.location);
            }
            List<Token> number = mp.parseNumber(eq.location);
            try {
                int code = Integer.parseInt(mp.flattenToString(number));
                mp.setCatcode(codepoint, code);
            } catch (NumberFormatException e) {
                throw new SemanticError("Illegal code number in \\catcode", eq.location);
            }
        } catch (EOF exc) {
            throw new SemanticError("Unexpected EOF in \\catcode", location);
        }
    }
}
