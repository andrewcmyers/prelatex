package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.ScannerLexer;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import static cms.util.maybe.Maybe.none;
import static prelatex.Main.Disposition.DROP;
import static prelatex.Main.Disposition.KEEP;
import static prelatex.macros.MacroProcessor.LaTeXParams;
import prelatex.Main.Disposition;

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
            MacroName macroName = mp.parseMacroName(location);
            Disposition disposition = mp.macroDisposition.get(macroName);
            boolean expectSuffix = mp.hasPrefix("WithSuffix");
            boolean dropDefn = false;
            mp.clearPrefixes();
            mp.skipBlanks();
            MacroName definedName = macroName;
            if (expectSuffix) {
                Token t = mp.nextToken();
                definedName = new MacroName(macroName + t.chars(), macroName.location);
                mp.recordSuffix(macroName.chars(), t);
            }
            try {
                mp.lookup(definedName);
                switch (name) {
                    case "newcommand":
                        mp.reportWarning("Macro \\" + macroName + " already defined", location);
                        break;
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
            if (!dropDefn)
                makeDefinition(mp, definedName, parameters, body, disposition, location);
        } catch (EOF exc) {
            throw new ScannerLexer.LexicalError("Unexpected end of file in \\newcommand definition", location);
        }
    }

    private void makeDefinition(MacroProcessor mp, MacroName mname, LaTeXParams parameters, List<Token> body, Disposition disposition, Location location) {
        if (disposition == DROP) body = List.of();
        Macro m = new LaTeXMacro(mname, parameters.numArgs(), parameters.defaultArgs(), body);
        if (disposition == KEEP) {
            mp.output(new MacroName(name, location));
            mp.output(new OpenBrace(location));
            mp.output(mname);
            mp.output(new CloseBrace(location));
            if (parameters.numArgs() > 0) {
                mp.output(new CharacterToken('[', location));
                mp.output(new StringToken(Integer.toString(parameters.numArgs()), location));
                mp.output(new CharacterToken(']', location));
            }
            if (parameters.defaultArgs().size() > 0) {
                for (List<Token> arg : parameters.defaultArgs()) {
                    mp.output(new CharacterToken('[', location));
                    mp.output(arg);
                    mp.output(new CharacterToken(']', location));
                }
            }
            mp.output(new OpenBrace(location));
            mp.output(body);
            mp.output(new CloseBrace(location));
        } else {
            mp.define(mname.name(), m);
        }
    }
}
