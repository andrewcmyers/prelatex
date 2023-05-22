package prelatex;

import prelatex.tokens.*;

import java.util.LinkedList;
import java.util.List;

/** Activity of reading the arguments to a macro.
 */
public class MacroArgs implements Activity {
    private Macro binding;
    private int position;
    private Token[] arguments;
    public MacroArgs(Macro b) {
        binding = b;
        position = 0;
        arguments = new Item[b.pattern.length];
    }
    public boolean complete() {
        return (position == arguments.length);
    }

    public void apply(MacroProcessor mp) {
        assert position == arguments.length;
        Token[] subst = new Token[arguments.length];
        binding.apply(subst, mp);
    }

    public void matchArgument(Token tok, MacroProcessor mp) {
        switch (tok) {
            case MacroParam p:
                mp.reportError("Unexpected macro parameter", tok.location());
                break;
            case OpenBrace brace:
                mp.startGroup(brace.location());
                break;
            default:
                Token currentParam = binding.pattern[position];
                if (currentParam instanceof MacroParam p) {
                    arguments[position++] = tok;
                } else {

            default:

        }
    }

    @Override
    public void handle(Item item, MacroProcessor mp) {
        if (item instanceof Token tok) {
            matchArgument(tok, mp);
        }
    }
}