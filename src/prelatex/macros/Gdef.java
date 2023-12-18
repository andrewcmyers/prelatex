package prelatex.macros;

import prelatex.tokens.MacroName;

public class Gdef extends Def {
    public Gdef() {
        super("gdef");
    }
    @Override
    protected void defineMacro(MacroProcessor mp, MacroName name, DefMacro m) {
        mp.globalDefine(name.toString(), m);
    }
}