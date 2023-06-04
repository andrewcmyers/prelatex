package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.MacroName;
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
            mp.skipBlanks();
            Token t = mp.peekToken();
            List<Token> nameTokens = mp.parseMacroArg(Maybe.none());
            if (nameTokens.size() != 1 || !(nameTokens.get(0) instanceof MacroName)) {
                throw new SemanticError("Invalid macro name in " + this.name + ": " + mp.flattenToString(nameTokens),
                        t.location);
            }
            Token mname = nameTokens.get(0);
            String name_s = mname.toString().substring(1);
            // code above also appears in NewCommand, sorry
            List<Token> params = new ArrayList<>();
            int n = 0;
            while (!(mp.peekToken() instanceof OpenBrace)) {
                t = mp.nextToken();
                params.add(t);
                if (t instanceof MacroParam p) {
                    Token num = p.token();
                    try {
                        int i = Integer.parseInt(num.chars());
                        if (i == n + 1) n++;
                        else throw new SemanticError("Macro parameters must increase sequentially", num.location);
                    } catch (NumberFormatException exc) {
                        throw new SemanticError("Macro parameters must be numbers", num.location);
                    }
                }
            }
            List<Token> body = mp.parseMacroArg(Maybe.none());
            if (mp.macroDisposition.get(name_s) == DROP) body = List.of();
            mp.define(name_s, new DefMacro(name_s, n, params, body));
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of file in \\def definition", location);
        }
    }
}