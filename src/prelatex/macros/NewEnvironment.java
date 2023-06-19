package prelatex.macros;

import easyIO.EOF;
import prelatex.PrelatexError;
import prelatex.lexer.Lexer;
import prelatex.lexer.Location;
import prelatex.tokens.CloseBrace;
import prelatex.tokens.OpenBrace;
import prelatex.tokens.Token;

import java.util.List;

import static cms.util.maybe.Maybe.*;
import static prelatex.macros.MacroProcessor.LaTeXParams;
import static prelatex.macros.MacroProcessor.SemanticError;

public class NewEnvironment extends Macro {
    public NewEnvironment() {
        super("newenvironment");
    }

    @Override
    public void apply(MacroProcessor mp, Location location) throws PrelatexError {
        try {
            Token t = mp.nextNonblankToken();
            if (!(t instanceof OpenBrace)) {
                throw new SemanticError("\\newenvironment must be followed by open brace", location);
            }
            String name = mp.parseLongMacroName(t.location);
            mp.skipBlanks();
            if (name.isEmpty()) {
                throw new SemanticError("\\newenvironment name must be nonempty", location);
            }
            if (!(mp.nextToken() instanceof CloseBrace)) {
                throw new SemanticError("\\newenvironment name must be in braces (no })", location);
            }
            mp.skipBlanks();
            LaTeXParams parameters = mp.parseLaTeXParameters(location);
            List<Token> beginEnv = mp.parseMacroArg(none());
            List<Token> endEnv = mp.parseMacroArg(none());
            mp.define(name, new LaTeXMacro(name, parameters.numArgs(), parameters.defaultArgs(), beginEnv));
            mp.define("end" + name, new LaTeXMacro("end" + name, 0, List.of(), endEnv));
        } catch (EOF e) {
            throw new Lexer.LexicalError("Unexpected end of input in \\newenvironment", location);
        }
    }
}