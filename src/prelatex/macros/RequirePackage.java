package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.*;

import java.util.ArrayList;
import java.util.List;

import static prelatex.Main.Disposition.DROP;
import static prelatex.Main.Disposition.EXPAND;

/** The \input macro */
public class RequirePackage extends LaTeXBuiltin {

    public RequirePackage(String name) {
        super(name, 2, List.of(List.of()));
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws PrelatexError {
        assert arguments.size() == 2;
        // TODO do something with the options in argument 1
        List<Token> options = arguments.get(0);
        String pkgArg = mp.flattenToString(arguments.get(1));
        String[] pkgs = pkgArg.split("\\s*,\\s*");
        List<String> includes = new ArrayList<>();
        for (String pkgName : pkgs) {
            if (mp.packageDisposition.get(pkgName) == EXPAND &&
                    !mp.packagesRead.contains(pkgName)) {
                mp.packagesRead.add(pkgName);
                if (!includes.isEmpty()) {
                    outputIncludes(mp, name, includes, arguments.get(0), location);
                    includes.clear();
                }
                mp.includeFile(arguments.get(1), List.of(".sty"), location);
            } else {
                if (mp.packageDisposition.get(pkgName) != DROP) {
                    includes.add(pkgName);
                }
            }
        }
        if (!includes.isEmpty()) {
            outputIncludes(mp, name, includes, arguments.get(0), location);
        }
    }

    private void outputIncludes(MacroProcessor mp, String macroName, List<String> includes, List<Token> arguments, Location location) throws PrelatexError {
        mp.output(new MacroName(name, location));
        if (arguments.size() > 0) {
            mp.output(new CharacterToken('[', location));
            mp.output(arguments.toArray(n -> new Token[n]));
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