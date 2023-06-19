package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.MacroParam;
import prelatex.tokens.OpenBrace;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;

import static prelatex.Main.Disposition.DROP;

public class Def extends BuiltinMacro {
    public Def() {
        super("def", 2);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            String name_s = mp.parseMacroName(location);
            List<Token> params = new ArrayList<>();
            int n = 0;
            while (!(mp.peekToken() instanceof OpenBrace)) {
                Token t = mp.nextToken();
                params.add(t);
                if (t instanceof MacroParam p) {
                    Token num = p.token();
                    try {
                        int i = Integer.parseInt(num.chars());
                        if (i == n + 1) n++;
                        else throw new PrelatexError("Macro parameters must increase sequentially", num.location);
                    } catch (NumberFormatException exc) {
                        throw new PrelatexError("Macro parameters must be numbers", num.location);
                    }
                }
            }
            List<Token> body = mp.parseMacroArg(Maybe.none());
            if (mp.macroDisposition.get(name_s) == DROP) body = List.of();
            makeDefinition(mp, name_s, new DefMacro(name_s, n, params, body));
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of file in \\def definition", location);
        }
    }

    /** Store this definition in the right place. */
    protected void makeDefinition(MacroProcessor mp, String name, DefMacro m) {
        mp.define(name, m);
    }
}