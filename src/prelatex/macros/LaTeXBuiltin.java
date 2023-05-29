package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static cms.util.maybe.Maybe.none;
import static cms.util.maybe.Maybe.some;

/** A LaTeX-syntax macro with built-in behavior. Macro type 2B per Macro.java */
abstract public class LaTeXBuiltin extends BuiltinMacro {
    /** Possibly empty list of default arguments. */
    protected final List<List<Token>> defaultArgs;

    static final List<List<Token>> noDefaultArgs = new ArrayList<>();

    protected LaTeXBuiltin(String n, int numArgs, List<List<Token>> defaultArgs) {
        super(n, numArgs);
        this.defaultArgs = defaultArgs;
    }
    protected LaTeXBuiltin(String n, int numArgs) {
        super(n, numArgs);
        this.defaultArgs = noDefaultArgs;
    }

    @Override
    public void apply(MacroProcessor mp, Location location, Token[] delimiter) throws PrelatexError {
        try {
            List<List<Token>> arguments = new LinkedList<>();
            for (int i = 0; i < defaultArgs.size(); i++) {
                mp.skipBlanks();
                if (mp.peekToken() instanceof CharacterToken c && c.codepoint() == '[') {
                    arguments.add(mp.parseMacroArg(some(new CharacterToken(']', location))));
                } else {
                    arguments.add(defaultArgs.get(i));
                    break;
                }
            }
            while (arguments.size() < numArgs) {
                arguments.add(mp.parseMacroArg(none()));
            }
            mp.prependTokens(delimiter);
            applyArguments(arguments, mp, location);
        } catch (EOF e) {
            throw new MacroProcessor.SemanticError("Unexpected end of input in macro \\" + name, location);
        }
    }
}