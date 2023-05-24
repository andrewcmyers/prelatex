package prelatex.macros;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;
import prelatex.lexer.Location;
import prelatex.lexer.SyntheticLocn;
import prelatex.tokens.CharacterToken;
import prelatex.tokens.MacroParam;
import prelatex.tokens.Separator;
import prelatex.tokens.Token;

import java.io.File;
import java.util.List;

/** The \input macro */
public class InputMacro extends BuiltinMacro {

    List<String> searchPath;

    public InputMacro(List<String> searchPath) {
        super("input");
        Location loc = new SyntheticLocn("\\input parameter 1");
        this.pattern = new Token[] { new MacroParam(new CharacterToken('1', loc), loc),
                                     new Separator(" ", loc) };
        this.searchPath = searchPath;
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) {
        assert arguments.size() == 1;
        String filename = mp.flattenToString(arguments.get(0));
        try {
            filename = findFile(filename).get();
            mp.includeSource(filename);
        } catch (NoMaybeValue exc) {
            mp.reportError("Cannot find input file \"" + filename + "\"", location);
        }
    }

    /** Find the file whose name starts with filename, using the current search path.
     */
    Maybe<String> findFile(String filename) {
        File f1 = new File(filename);
        if (!f1.isAbsolute()) {
            for (String base : searchPath) {
                try {
                    return Maybe.some(findFileExt(base, filename).get());
                } catch (NoMaybeValue exc) {
                    // keep looking
                }
            }
        }
        return findFileExt("", filename);
    }

    private Maybe<String> findFileExt(String base, String filename) {
        String[] extensions = { ".tex", ".sty" };
        for (String ext : extensions) {
            File rel = new File(base, filename + ext);
            if (rel.canRead()) return Maybe.some(rel.toString());
        }
        return Maybe.none();
    }
}