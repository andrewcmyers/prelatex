package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.ScannerLexer;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.ArrayList;
import java.util.List;

public class NewCommand extends Macro {

    public NewCommand() {
        super("newcommand");
    }

    protected NewCommand(String n) {
        super(n);
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            boolean longdef = true;
            mp.skipBlanks();
            Token t = mp.peekToken();
            List<Token> nameTokens = mp.parseMacroArg(Maybe.none());
            if (nameTokens.size() != 1 || !(nameTokens.get(0) instanceof MacroName)) {
                throw new SemanticError("Invalid macro name in " + this.name + ": " + mp.flattenToString(nameTokens),
                        t.location);
            }
            MacroName mname = (MacroName)nameTokens.get(0);
            String name_s = mname.name();
            // code above also appears in Def, sorry
            try {
                mp.lookup(name_s);
                switch (name) {
                    case "newcommand":
                        throw new SemanticError("Macro \\" + name_s + " already defined", location);
                    case "renewcommand":
                        break;
                    default:
                        throw new Error("huh?");
                }
            } catch (Namespace.LookupFailure e) {
                switch (name) {
                    case "newcommand": break;
                    case "renewcommand": break; // might be defined already, who knows?
                    default:
                        throw new Error("huh?");
                }
            }
            mp.skipBlanks();
            if (mp.peekToken() instanceof CharacterToken c && c.codepoint() == '*') {
                longdef = false;
                mp.nextToken();
                mp.skipBlanks();
            }
            int nargs = 0;
            List<List<Token>> defaultArgs = new ArrayList<>();
            if (mp.peekToken() instanceof CharacterToken c && c.codepoint() == '[') {
                mp.nextToken();
                mp.skipBlanks();
                Token args = mp.nextToken();
                try {
                    nargs = Integer.parseInt(args.chars());
                    if (nargs < 0 || nargs > 9) {
                        throw new SemanticError("Illegal number of parameters to \\newcommand: " + nargs, location);
                    }
                } catch (NumberFormatException exc) {
                    throw new SemanticError("Illegal number of parameters to \\newcommand: " + args.chars(), location);
                }
                Token t2 = mp.nextNonblankToken();
                if (!t2.chars().equals("]")) {
                    throw new SemanticError("Expected ] after number of parameters to \\newcommand: " + args.chars(),
                        t2.location);
                }
                for (;;) {
                    mp.skipBlanks();
                    if (mp.peekToken() instanceof CharacterToken ct && ct.codepoint() == '[') {
                        mp.nextToken();
                        List<Token> arg = new ArrayList<>();
                        for (;;) {
                            Token at = mp.nextToken();
                            if (at instanceof CloseBrace) break;
                            arg.add(at);
                        }
                        defaultArgs.add(arg);
                    } else {
                        break;
                    }
                }
                nargs += defaultArgs.size();
            }
            mp.skipBlanks();
            if (!(mp.peekToken() instanceof OpenBrace)) {
                throw new SemanticError("Macro body must be surrounded by braces", mp.peekToken().location);
            }
            List<Token> body = mp.parseMacroArg(Maybe.none());
            Macro m = new LaTeXMacro(mname.chars().substring(1), nargs, defaultArgs, body);
            mp.define(name_s, m);
        } catch (EOF exc) {
            throw new ScannerLexer.LexicalError("Unexpected end of file in \\newcommand definition", location);
        }
    }
}
