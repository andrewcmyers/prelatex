package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.*;

import java.util.ArrayList;
import java.util.List;

import static prelatex.Main.Disposition.DROP;
import static prelatex.Main.Disposition.EXPAND;

/** The \input macro */
public class RequirePackage extends LaTeXBuiltin {

    public RequirePackage(String name) {
        super(new MacroName(name, new SyntheticLocn("RequirePackage definition")), 2, List.of(List.of()));
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws PrelatexError {
        assert arguments.size() == 2;
        // TODO do something with the options in argument 1
        String pkgArg = mp.flattenToString(arguments.get(1));
        String[] packages = pkgArg.split("\\s*,\\s*");
        List<String> includes = new ArrayList<>();
        for (String pkgName : packages) {
            if (mp.packageDisposition.get(pkgName) == EXPAND &&
                    !mp.packagesRead.contains(pkgName)) {
                mp.packagesRead.add(pkgName);
                if (!includes.isEmpty()) {
                    outputIncludes(mp, includes, arguments.get(0), location);
                    includes.clear();
                }
                mp.define("package options",
                    new LaTeXMacro("package options", 0, List.of(), arguments.get(0)));
                mp.includeFile(arguments.get(1), List.of(".sty"), location);
            } else {
                if (mp.packageDisposition.get(pkgName) != DROP) {
                    includes.add(pkgName);
                }
            }
        }
        if (!includes.isEmpty()) {
            outputIncludes(mp, includes, arguments.get(0), location);
        }
    }

    private void outputIncludes(MacroProcessor mp, List<String> includes, List<Token> arguments, Location location) throws PrelatexError {
        mp.output(new MacroName(name, location));
        if (arguments.size() > 0) {
            mp.output(new CharacterToken('[', location));
            mp.output(arguments.toArray(Token[]::new));
            mp.output(new CharacterToken(']', location));
        }
        mp.output(new OpenBrace(location));
        boolean first = true;
        for (String pkgName : includes) {
            if (!first) mp.output(new StringToken(", ", location));
            first = false;
            mp.output(mp.stringToTokens(pkgName, location));
        }
        mp.output(new CloseBrace(location));
    }
}