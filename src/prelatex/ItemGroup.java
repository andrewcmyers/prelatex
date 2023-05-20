package prelatex;

import prelatex.tokens.MacroName;

import java.util.List;

public class ItemGroup extends Item {
    List<Item> items;
    public ItemGroup(List<Item> items) {
        super(items.listIterator().next().location());
        this.items = items;
    }
    @Override
    public boolean isSeparator() {
        return false;
    }
    public String chars() {
        StringBuilder b = new StringBuilder();
        b.append('{');
        boolean mayNeedSep = false;
        for (Item i : items) {
            String s = i.chars();
            if (mayNeedSep && Character.isAlphabetic(s.charAt(0))) {
                b.append(' ');
                mayNeedSep = false;
            }
            b.append(i.chars());
            if (i instanceof MacroName) mayNeedSep = true;
        }
        b.append('}');
        return b.toString();
    }
}