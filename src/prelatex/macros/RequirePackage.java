package prelatex.macros;

import prelatex.PrelatexError;
import prelatex.lexer.Location;
import prelatex.tokens.CloseBrace;
import prelatex.tokens.MacroName;
import prelatex.tokens.OpenBrace;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import static prelatex.Main.PackageDisposition.DROP;
import static prelatex.Main.PackageDisposition.EXPAND;

/** The \input macro */
public class RequirePackage extends LaTeXBuiltin {

    public RequirePackage(String name) {
        super(name, 2, List.of(List.of(), List.of()));
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location)
            throws PrelatexError {
        assert arguments.size() == 2;
        // TODO do something with the options in argument 1
        String pkgName = mp.flattenToString(arguments.get(1));
        if (mp.packageDisposition.get(pkgName) == EXPAND &&
                !mp.packagesRead.contains(pkgName)) {
            mp.packagesRead.add(pkgName);
            mp.includeFile(arguments.get(1), List.of(".sty"), location);
        } else {
            if (mp.packageDisposition.get(pkgName) != DROP) {
                mp.output(new MacroName(name, location), new OpenBrace(location));
                mp.output(mp.stringToTokens(pkgName, location));
                mp.output(new CloseBrace(location));
            }
        }
    }
}