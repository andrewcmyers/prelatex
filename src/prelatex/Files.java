package prelatex;

import cms.util.maybe.Maybe;
import cms.util.maybe.NoMaybeValue;

import java.io.File;
import java.util.List;

import static cms.util.maybe.Maybe.some;

/** This class encapsulates the way that the macro processor finds files,
 * using a search path.
 */
public class Files {

    List<String> searchPath;

    public Files(List<String> searchpath) {
        this.searchPath = searchpath;
    }
    /** Find the file whose name starts with filename, using the current search path.
     */
    public Maybe<String> findFile(String filename, List<String> exts) {
        File f1 = new File(filename);
        if (!f1.isAbsolute()) {
            for (String base : searchPath) {
                try {
                    return some(findFileExt(base, filename, exts).get());
                } catch (NoMaybeValue exc) {
                    // keep looking
                }
            }
        }
        return findFileExt("", filename, exts);
    }

    /** Look for a file with one of the specified extensions in the specified location
     *  in the file system. */
    private Maybe<String> findFileExt(String base, String filename, List<String> extensions) {
        for (String ext : extensions) {
            if (base.isEmpty() && !new File(filename).isAbsolute()) base = System.getProperty("user.dir");
            File rel = new File(base, filename + ext);
            if (rel.canRead()) return some(rel.toString());
        }
        return Maybe.none();
    }
}
