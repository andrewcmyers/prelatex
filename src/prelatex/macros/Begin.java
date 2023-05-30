package prelatex.macros;

import easyIO.EOF;
import prelatex.Namespace;
import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.*;

import java.util.ArrayList;
import java.util.List;

import static cms.util.maybe.Maybe.some;
import static prelatex.macros.MacroProcessor.SemanticError;

import prelatex.lexer.Lexer.LexicalError;

public class Begin extends Macro {
    public Begin() {
        super("begin");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            mp.skipBlanks();
            Token open = mp.nextToken();
            if (!(open instanceof OpenBrace)) {
                throw new LexicalError("\\begin expects environment name in braces", open.location);
            }
            Location envLoc = mp.peekToken().location;
            String env = mp.parseMacroName(location);
            Token close = mp.nextNonblankToken();
            if (!(close instanceof CloseBrace)) {
                throw new LexicalError("\\begin expects environment name in braces", close.location);
            }
            try {
                Macro m1 = mp.lookup(env);
                if (m1 instanceof LaTeXMacro m) {
                    List<List<Token>> arguments = mp.parseLaTeXArguments(m.numArgs, m.defaultArgs, envLoc);
                    m.applyArguments(arguments, mp, envLoc);
                    return;
                } else {
                    mp.reportError("Macro \\" + env + " not defined by \\newenvironment", location);
                    // fall through
                }
            } catch (Namespace.LookupFailure e) {
                // fall through
            }
            mp.output(new MacroName("begin", location));
            mp.output(open);
            mp.output(new StringToken(env, envLoc));
            mp.output(close);
        } catch (EOF e) {
            throw new LexicalError("Unexpected end of input in \\begin", location);
        }
    }
}
