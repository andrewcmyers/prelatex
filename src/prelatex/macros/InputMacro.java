package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.util.Arrays;
import java.util.List;

/** The \input macro */
public class InputMacro extends BuiltinMacro {
    public InputMacro() {
        super("input", 1);
        Location loc = new SyntheticLocn("\\RequirePackage parameter 1");
        setPattern(Arrays.asList(new MacroParam(new CharacterToken('1', loc), loc),
                new Separator(" ", loc)));
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        assert arguments.size() == 1;
        mp.includeFile(arguments.get(0), List.of("", ".tex"), location);
    }
}