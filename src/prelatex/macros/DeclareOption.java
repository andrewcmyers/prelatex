package prelatex.macros;

import prelatex.Namespace;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.List;

public class DeclareOption extends LaTeXMacro {

    public DeclareOption() {
        super("DeclareOption", 2, List.of(), List.of());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        List<Token> options;
        List<Token> optionName = arguments.get(0);
        List<Token> action = arguments.get(1);
        try {
            DefMacro m = (DefMacro) mp.lookup("declared options");
            options = m.body;
            options.add(new CharacterToken(',', location));
            options.addAll(optionName);
        } catch (Namespace.LookupFailure e) {
            options = List.of();
        }
        mp.define("declared options",
            new LaTeXMacro("declared options", 0, List.of(), options));
        String optionName_s = "option " + mp.flattenToString(optionName);
        mp.define(optionName_s, new LaTeXMacro(optionName_s, 0, List.of(), action));
    }
}
