package prelatex;

import easyIO.BacktrackScanner.Location;

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
}
