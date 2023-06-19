package prelatex.macros;

import prelatex.Namespace;
import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.List;

public class ProcessOptions extends LaTeXMacro {
    public ProcessOptions() {
        super("ProcessOptions", 0, List.of(), List.of());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        assert arguments.size() == 0;
        String[] options;
        try {
            LaTeXMacro m = (LaTeXMacro) mp.lookup("package options");
            options = mp.flattenToString(m.body).split(",");
        } catch (Namespace.LookupFailure e) {
            return; // no options passed
        }
        for (String option : options) {
            if (option.isEmpty()) continue;
            try {
                List<Token> body = ((LaTeXMacro) mp.lookup("option " + option)).body;
                mp.prependTokens(body);
            } catch (Namespace.LookupFailure e) {
                mp.reportError("Unknown option " + option, location);
            }
        }
    }
}