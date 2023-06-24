package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.List;

public class LetMacro extends Macro {
    public LetMacro() {
        super("let");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            boolean globalLet = mp.hasPrefix("global");
            Token m = mp.nextNonblankToken();
            if (m instanceof MacroName nm) {
                Token t = mp.nextToken();
                if (t instanceof CharacterToken c && c.codepoint() == '=') {
                    t = mp.nextNonblankToken();
                }
                List<Token> replacement = List.of(t);
                if (globalLet) {
                    mp.globalDefine(nm.name(), new prelatex.macros.SimpleCommand(nm.name(),
                            mp1 -> mp1.prependTokens(replacement)));
                } else {
                    mp.define(nm.name(), new prelatex.macros.SimpleCommand(nm.name(),
                            mp1 -> mp1.prependTokens(replacement)));
                }
            } else {
                throw new SemanticError("\\let must be followed by macro name", m.location);
            }
        } catch (EOF e) {
            throw new SemanticError("Badly formed \\let definition", location);
        }
    }
}
