package prelatex;

import prelatex.macros.*;
import prelatex.macros.SimpleMacro;

public class StandardContext {
    public static void initialize(MacroProcessor mp) {
        // builtin TeX macros
        mp.define("def", new Def());
        mp.define("gdef", new Gdef());
        mp.define("edef", new Edef());
        mp.define("let", new LetMacro());
        mp.define("input", new InputMacro());
        mp.define("relax", new NoopMacro("relax", 0));
        mp.define("char", new CharMacro());
        mp.define("newif", new Newif());
        mp.define("ifx", new Ifx());
        mp.define("if", new IfEq());
        mp.define("ifdefined", new IfDefined());
        mp.define("ifcase", new IfCase());
        mp.define("iffalse", new IfFalse());
        mp.define("iftrue", new IfTrue());
        mp.define("ifmmode", new IfMMode());
        mp.define("csname", new CSName());
        mp.define("expandafter", new ExpandAfter());
        mp.define("catcode", new Catcode());
        mp.define("begingroup", new BeginGroup());
        mp.define("endgroup", new EndGroup());
        mp.define("relax", new NoopMacro("relax", 0));
        mp.define("outer", new PrefixMacro("outer")); // Not enforced
        mp.define("long", new PrefixMacro("long"));
        mp.define("global", new PrefixMacro("global"));
        mp.define("uppercase", new Uppercase());
        mp.define("lowercase", new Lowercase());
        // builtin LaTeX macros
        mp.define("newcommand", new NewCommand());
        mp.define("DeclareRobustCommand", new RenewCommand());
        mp.define("providecommand", new ProvideCommand());
        mp.define("renewcommand", new RenewCommand());
        mp.define("newenvironment", new NewEnvironment());
        mp.define("begin", new Begin());
        mp.define("end", new End());
        mp.define("RequirePackage", new RequirePackage("RequirePackage"));
        mp.define("usepackage", new RequirePackage("usepackage"));
        mp.define("ProvidesPackage", new NoopMacro("ProvidesPackage", 1));
        mp.define("active", new SimpleMacro("active", "13"));
        mp.define("IfFileExists", new IfFileExists());
        mp.define("DeclareOption", new DeclareOption());
        mp.define("ProcessOptions", new ProcessOptions());
        mp.define("AtBeginDocument", new AtBeginDocument());
        mp.define("AtEndDocument", new AtEndDocument());
        mp.define("protect", new NoopMacro("protect", 0));
        mp.define("typeout", new Typeout());
        mp.define("ensuremath", new EnsureMath());
        mp.define("[", new BeginDisplayMath());
        mp.define("]", new EndDisplayMath());
        // standardish macros from LaTeX packages like etoolbox
        mp.define("ifbool", new IfBool());
        mp.define("WithSuffix", new PrefixMacro("WithSuffix"));
    }
}
