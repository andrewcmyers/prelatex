package prelatex.macros;

public class Gdef extends Def {
    @Override
    protected void makeDefinition(MacroProcessor mp, String name, DefMacro m) {
        mp.globalDefine(name, m);
    }
}
