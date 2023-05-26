package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.MacroName;
import prelatex.tokens.Token;
import prelatex.macros.MacroProcessor.SemanticError;

import java.util.List;

public class Newif extends LaTeXBuiltin {
    public Newif() {
        super("newif", 1);
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) throws SemanticError {
        assert arguments.size() == 1;
        if (arguments.get(0).size() != 1) {
            throw new SemanticError("\\newif requires a single macro token as an argument", location);
        }
        Token ifname = arguments.get(0).get(0);
        if (ifname instanceof MacroName) {
            String s = ifname.toString();
            if (!s.matches("^\\\\if.*$")) {
                throw new SemanticError("\\newif requires an argument starting with \\if", ifname.location);
            }
            String condition = s.substring(3);
            mp.define(ifname.toString(), new IfCommand(condition));
            mp.setConditionFalse(condition);
            mp.define(condition + "true",
                new SimpleCommand(condition + "true", mp1 -> mp1.setConditionTrue(condition)));
            mp.define(condition + "false",
                new SimpleCommand(condition + "false", mp1 -> mp1.setConditionFalse(condition)));
        } else {
            throw new SemanticError("\\newif requires a single macro token as an argument", ifname.location);
        }
    }
}
