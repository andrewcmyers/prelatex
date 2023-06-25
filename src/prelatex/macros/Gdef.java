package prelatex.macros;

public class Gdef extends Def {
    public Gdef() {
        super("gdef");
    }
    @Override
    protected void defineMacro(MacroProcessor mp, String name, DefMacro m) {
        mp.globalDefine(name, m);
    }
}