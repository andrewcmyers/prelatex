package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.ScannerLexer;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import static prelatex.Main.Disposition.DROP;
import static prelatex.macros.MacroProcessor.LaTeXParams;

import java.util.List;

/** \newcommand */
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
            boolean longdef = true; // TODO: actually enforce this
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
            LaTeXParams parameters = mp.parseLaTeXParameters(location);

            mp.skipBlanks();
            if (!(mp.peekToken() instanceof OpenBrace)) {
                throw new SemanticError("Macro body must be surrounded by braces", mp.peekToken().location);
            }
            List<Token> body = mp.parseMacroArg(Maybe.none());
            if (mp.macroDisposition.get(name_s) == DROP) body = List.of();
            Macro m = new LaTeXMacro(mname.name(), parameters.numArgs(), parameters.defaultArgs(), body);
            mp.define(name_s, m);
        } catch (EOF exc) {
            throw new ScannerLexer.LexicalError("Unexpected end of file in \\newcommand definition", location);
        }
    }
}
