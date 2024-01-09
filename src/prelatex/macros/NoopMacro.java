package prelatex.macros;

import prelatex.lexer.Location;
import prelatex.tokens.Token;

import java.util.ArrayList;
import java.util.List;

/** A macro that is just deleted */
public class NoopMacro extends LaTeXBuiltin {

    public NoopMacro(String name, int numArgs) {
        super(name, numArgs, new ArrayList<>());
    }

    @Override
    public void applyArguments(List<List<Token>> arguments, MacroProcessor mp, Location location) { }

    @Override public boolean equals(Object o) {
        return (o instanceof NoopMacro m && m.name.equals(name));
    }
}