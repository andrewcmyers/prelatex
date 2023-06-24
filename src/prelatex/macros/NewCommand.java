package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.ScannerLexer;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;
import static prelatex.Main.Disposition.DROP;
import static prelatex.macros.MacroProcessor.LaTeXParams;

import java.util.List;

/** This class implements the macros \newcommand, \renewcommand, and \providecommand.
 *  Since we not reading all packages, we can't really get the semantics exactly right --
 *  We don't know whether macros have been previously defined. In the case where
 *  the macro *is* known to be defined, we do the right thing for \newcommand and
 *  \providecommand. Otherwise, we muddle through as best we can.
 */
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
            if (mp.peekToken() instanceof CharacterToken c && c.codepoint() == '*') {
                longdef = false;
                mp.nextToken();
                mp.skipBlanks();
            }
            String name_s = mp.parseMacroName(location);
            boolean expectSuffix = mp.hasPrefix("WithSuffix");
            boolean dropDefn = false;
            mp.clearPrefixes();
            mp.skipBlanks();
            String definedName = name_s;
            if (expectSuffix) {
                Token t = mp.nextToken();
                definedName = name_s + t.chars();
                mp.recordSuffix(name_s, t);
            }
            try {
                mp.lookup(definedName);
                switch (name) {
                    case "newcommand":
                        throw new SemanticError("Macro \\" + name_s + " already defined", location);
                    case "renewcommand":
                    case "DeclareRobustCommand":
                        break;
                    case "providecommand":
                        dropDefn = true;
                        break; // already defined
                    default:
                        throw new Error("huh?");
                }
            } catch (Namespace.LookupFailure e) {
                // Just because we can't find it doesn't mean it isn't defined already.
            }
            mp.skipBlanks();
            LaTeXParams parameters = mp.parseLaTeXParameters(location);
            mp.skipBlanks();
            if (!(mp.peekToken() instanceof OpenBrace)) {
                throw new SemanticError("Macro body must be surrounded by braces", mp.peekToken().location);
            }
            List<Token> body = mp.parseMacroArg(none());
            if (!longdef) mp.forbidPar(body);
            if (mp.macroDisposition.get(name_s) == DROP) body = List.of();
            if (!dropDefn) {
                Macro m = new LaTeXMacro(definedName, parameters.numArgs(), parameters.defaultArgs(), body);
                mp.define(name_s, m);
            }
        } catch (EOF exc) {
            throw new ScannerLexer.LexicalError("Unexpected end of file in \\newcommand definition", location);
        }
    }
}
