package prelatex;

import java.io.PrintWriter;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import easyIO.EOF;
import prelatex.lexer.Lexer;
import prelatex.lexer.Location;
import prelatex.macros.InputMacro;
import prelatex.tokens.*;

public class MacroProcessor {
    private final PrintWriter err;
    Context<Macro> context = new Context<>();
    Lexer lexer;

    private final Deque<Activity> activities = new LinkedList<>();
    private final Deque<Item> pendingItems = new LinkedList<>();

    public void close() {
        out.close();
    }

    PrintWriter out;

    MacroProcessor(Lexer lexer, PrintWriter out, PrintWriter err) {
        this.out = out;
        this.err = err;
        this.lexer = lexer;
        activities.add(new Normal());
        context.add("input", new InputMacro());
    }
    void output(String s) {
        out.print(s);
    }

    public void handle(Item item) {
        currentActivity().handle(item, this);
    }

    Activity currentActivity() {
        return activities.getFirst();
    }
    void pushActivity(Activity a) {
        activities.addFirst(a);
    }
    void popActivity() {
        activities.removeFirst();
    }

    /** Start processing an included source file. Requires: no pending items. */
    public void includeSource(String filename) {
        assert pendingItems.isEmpty(); // If this fails, may need to create new Activity to save state
        lexer.includeSource(filename);
    }

    void prependToken(Token token) {
        pendingItems.addFirst(token);
    }

    /** Put the items in this list at the head of the input sequence. */
    void prependItems(List<Item> items) {
        Item[] a = new Item[items.size()];
        int j = 0;
        for (Item i : items) a[j++] = i;
        for (j = a.length - 1; j >= 0; j--) {
            pendingItems.addFirst(a[j]);
        }
    }

    public void run() {
        try {
            while (true) {
                Item item = pendingItems.isEmpty()
                        ? lexer.nextItem()
                        : pendingItems.removeFirst();
                handle(item);
            }
        } catch (Lexer.LexicalError e) {
            System.err.println(e.getMessage());
        } catch (EOF e) {
            // All done.
            close();
        }
    }

    public void reportError(String msg, Location l) {
        err.println(l + ":" + msg);
    }

    public void startGroup(Location l) {
        pushActivity(new Group(l));
    }

    public Macro lookup(String name) throws Namespace.LookupFailure {
        return context.lookup(name);
    }
}