package prelatex.macros;

import cms.util.maybe.Maybe;
import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Lexer;
import prelatex.lexer.Location;
import prelatex.tokens.*;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.List;

public class NewCommand extends BuiltinMacro {

    public NewCommand() {
        super("newcommand");
    }

    protected NewCommand(String n) {
        super(n);
    }

    @Override
    public void apply(Macro binding, MacroProcessor mp, Location location) throws PrelatexError {
        try {
            boolean longdef = true;
            mp.skipBlanks();
            Token t = mp.peekToken();
            List<Token> nameTokens = mp.parseMacroArgument(Maybe.none());
            if (nameTokens.size() != 1 || !(nameTokens.get(0) instanceof MacroName)) {
                throw new SemanticError("Invalid macro name in " + this.name + ": " + mp.flattenToString(nameTokens),
                        t.location);
            }
            Token mname = nameTokens.get(0);
            String name_s = mname.chars().substring(1);
            // code above also appears in Def, sorry
            try {
                mp.context.lookup(name_s);
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
            if (mp.peekToken() instanceof CharacterToken c && c.codepoint() == '[') {
                mp.nextToken();
                mp.skipBlanks();
                Token args = mp.nextToken();
                try {
                    nargs = Integer.parseInt(args.chars());
                    if (nargs < 1 || nargs > 9) {
                        throw new SemanticError("Illegal number of parameters to \\newcommand: " + nargs, location);
                    }
                } catch (NumberFormatException exc) {
                    throw new SemanticError("Illegal number of parameters to \\newcommand: " + args.chars(), location);
                }
                mp.skipBlanks();
                Token t2 = mp.nextToken();
                if (!t2.chars().equals("]")) {
                    throw new SemanticError("Expected ] after number of parameters to \\newcommand: " + args.chars(),
                        t2.location);
                }
            }
            mp.skipBlanks();
            if (!(mp.peekToken() instanceof OpenBrace)) {
                throw new SemanticError("Macro body must be surrounded by braces", mp.peekToken().location);
            }
            List<Token> body = mp.parseMacroArgument(Maybe.none());
            UserMacro m = new UserMacro(mname.chars().substring(1));
            m.numArgs = nargs;
            m.pattern = new Token[nargs];
            for (int i = 0; i < nargs; i++) {
                m.pattern[i] = new MacroParam(new CharacterToken('1'+ i, location), location);
            }
            m.body = body;
            mp.define(name_s, m);
        } catch (EOF exc) {
            throw new Lexer.LexicalError("Unexpected end of file in \\newcommand definition", location);
        }
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        throw new Error("Unused");
    }
}
