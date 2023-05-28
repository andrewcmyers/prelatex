package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/** The \input macro */
public class RequirePackage extends LaTeXBuiltin {

    public RequirePackage(String name) {
        super(name, 2, Arrays.asList(new List[] { new ArrayList<>() }));
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        assert arguments.size() == 2;
        // TODO do something with the options in argument 1
        String pkgName = mp.flattenToString(arguments.get(1));
        if (mp.localPackages.contains(pkgName) &&
                !mp.packagesRead.contains(pkgName)) {
            mp.packagesRead.add(pkgName);
            mp.includeFile(arguments.get(1), new String[]{".sty"}, location);
        } else {
            if (!mp.dropPackages.contains(pkgName)) {
                mp.output("\\" + name + "{" + pkgName + "}");
            }
        }
    }
}