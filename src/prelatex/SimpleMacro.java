package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.lexer.ScannerLocn;
import prelatex.lexer.SyntheticLocn;
import prelatex.macros.LaTeXMacro;
import prelatex.macros.Macro;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SimpleMacro extends LaTeXMacro {
    public SimpleMacro(String name, String number) {
        super(name, 0, List.of(), explode(number, new SyntheticLocn("macro defn of " + name)));
    }

    private static List<Token> explode(String s, Location l) {
        List<Token> result = new ArrayList<>();
        s.codePoints().forEach(c -> {
            result.add(new CharacterToken(c, l));
        });
        return result;
    }
}