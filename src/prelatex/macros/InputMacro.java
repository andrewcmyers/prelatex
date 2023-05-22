package prelatex.macros;

import prelatex.BuiltinMacro;
import prelatex.MacroProcessor;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Token;

/** The \input macro */
public class InputMacro extends BuiltinMacro {

    public InputMacro() {
        Location loc = new SyntheticLocn("\\input parameter 1");
        this.pattern = new Token[] { new MacroParam(new CharacterToken('1', loc), loc) };
    }

    @Override
    public void apply(Token[] arguments, MacroProcessor mp) {
        assert arguments.length == 1;
        String filename = arguments[0].chars();
        filename = findFile(filename);
        mp.includeSource(filename);
    }

    /** Find the file whose name starts with filename in TEXINPUTS.
     */
    String findFile(String filename) {
        return filename + ".tex"; // XXX
    }

}