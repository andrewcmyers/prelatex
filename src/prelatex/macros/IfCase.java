package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class IfCase extends Conditional {
    private static final Token orToken = new MacroName("or", new SyntheticLocn("IfCase \\or"));

    public IfCase() {
        super("ifcase");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            int num = -1;
            mp.skipBlanks();
            ArrayList<Token> tokens = new ArrayList<>();
            collectTokens: for (;;) {
                // keep expanding until we pull two tokens off
                Token t = mp.nextToken();
                switch (t) {
                    case MacroName m:
                        try {
                            Macro b = mp.lookup(m);
                            if (b.isExpandable()) {
                                mp.macroCall(m);
                                continue;
                            }
                        } catch (Namespace.LookupFailure e) {
                            throw new SemanticError("Unexpandable macro " +
                                m + " in \\ifcase expression", m.location);
                        }
                        break;
                    case CharacterToken c:
                        if (Character.isDigit(c.codepoint())) {
                            tokens.add(c);
                        } else {
                            mp.prependTokens(List.of(c));
                            break collectTokens;
                        }
                        break;
                    default:
                        mp.prependTokens(List.of(t));
                        break collectTokens;
                }
                String numStr = mp.flattenToString(tokens);
                try {
                    num = Integer.parseInt(numStr);
                } catch (NumberFormatException exc) {
                    num = -1;
                }
                if (num < 0) {
                    throw new SemanticError("Illegal number " + numStr + " in \\ifcase", location);
                }
            }
            LinkedList<Token> selected = new LinkedList<>();
            for (int i = 0; ; i++) {
                Token delim;
                LinkedList<Token> case_i = mp.parseMatched(Set.of(MacroProcessor.fi, orToken));
                delim = case_i.removeLast();
                if (i == num) {
                    selected = case_i;
                }
                if (delim instanceof MacroName && delim.toString().equals("\\fi")) break;
            }
            mp.prependTokens(selected);
        } catch (EOF e) {
            throw new SemanticError("Unexpected EOF in \\ifcase", location);
        }
    }
}