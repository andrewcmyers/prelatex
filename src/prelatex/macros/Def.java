package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.macros.MacroProcessor.SemanticError;
import prelatex.tokens.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import prelatex.Main.Disposition;
import static prelatex.Main.Disposition.DROP;
import static prelatex.Main.Disposition.KEEP;

/** The core 'def' macro of TeX. */
public class Def extends BuiltinMacro {
    public Def() {
        this("def");
    }
    protected Def(String name) {
        super(name, 2);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            MacroName name_s = mp.parseMacroName(location);
            boolean longDef = mp.hasPrefix("long");
            mp.clearPrefixes();
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
                        else throw new SemanticError("Macro parameters must increase sequentially", num.location);
                    } catch (NumberFormatException exc) {
                        throw new SemanticError("Macro parameters must be numbers", num.location);
                    }
                }
            }
            List<Token> body;
            if (expandBody()) {
                body = mp.parseMatched(Set.of(), true);
            } else {
                body = mp.parseMacroArg(Maybe.none());
            }
            body = mp.filterComments(body);
            if (!longDef) mp.forbidPar(body);
            Disposition disposition = mp.macroDisposition.get(name_s.toString());
            makeDefinition(mp, name_s, n, params, body, disposition, location);
        } catch (EOF e) {
            throw new SemanticError("Unexpected end of file in \\def definition", location);
        }
    }

    /** Should the macro body be expanded? Overridden by \edef */
    protected boolean expandBody() {
        return false;
    }

    /** Store this definition in the right place. */
    protected void makeDefinition(MacroProcessor mp, MacroName mname, int numArgs, List<Token> params,
                                  List<Token> body, Disposition disposition, Location location) {
        if (disposition == DROP) body = List.of();
        if (disposition == KEEP) {
            mp.output(mname);
            mp.output(params);
            mp.output(new OpenBrace(location));
            mp.output(body);
            mp.output(new CloseBrace(location));
        } else {
            defineMacro(mp, mname, new DefMacro(mname, numArgs, params, body));
        }
    }

    protected void defineMacro(MacroProcessor mp, MacroName mname, DefMacro macro) {
        mp.define(mname.name(), macro);
    }
}