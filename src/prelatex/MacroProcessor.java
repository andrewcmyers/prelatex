package prelatex;

import java.io.PrintWriter;
import java.util.Deque;

import prelatex.tokens.*;
import static prelatex.MacroProcessor.ParseMode.*;

public class MacroProcessor {
    Context<MacroBinding> context = new Context<>();

    private Deque<Activity> activities;

    public void close() {
        out.close();
    }

    enum ParseMode {
        MACRO,
        NORMAL,
        ARGUMENT
    }

    ParseMode mode = NORMAL;
    PrintWriter out;

    MacroProcessor(PrintWriter out) {
        this.out = out;
    }
    void output(String s) {
        out.print(s);
    }

    public void handle(Item item) {
        if (item.isSeparator()) {
            if (mode == NORMAL) {
                output(item.chars());
            } else {
                // ignore separator items that are not for output
            }
        }
        if (item instanceof Token tok) {
            processToken(tok);
        }
    }

    private void processToken(Token tok) {
        switch (mode) {
            case NORMAL:
                if (tok instanceof MacroName m) {
                    try {
                        MacroBinding b = context.lookup(m.name());
                        mode = MACRO;
                    } catch (Namespace.LookupFailure e) {
                        // just output it
                    }
                }
                output(tok.chars());
                break;
            case MACRO:
                break;
        }
    }


}