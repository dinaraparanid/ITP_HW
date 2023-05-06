import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

/** Main class and start point of the program */

public final class FoodMenuSystem {
    public static void main(final String[] args) {
        final List<IMenu> appetizerMenuItems = List.of(
                new VegetarianDecorator(new MenuItem("Garlic bread", 5.5)),
                new SpicyDecorator(new MenuItem("Chicken wings", 12.5)),
                new SpicyDecorator(new VegetarianDecorator(new MenuItem("Tomato soup", 10.5)))
        );

        final var appetizerMenu = new CompositeMenu("Appetizer Menu", appetizerMenuItems);

        final List<IMenu> dessertMenuItems = List.of(
                new MenuItem("Pie", 4.5),
                new SpicyDecorator(new MenuItem("Pie", 4.5)),
                new MenuItem("Ice cream", 3.0)
        );

        final var dessertMenu = new CompositeMenu("Dessert Menu", dessertMenuItems);

        final var mainMenu = new CompositeMenu(
                "Main Menu",
                List.of(appetizerMenu, dessertMenu)
        );

        mainMenu.print();
    }
}

/**
 * Describes menu's item, which can be
 * either menu holder or menu item
 */

interface IMenu {
    void print();

    @NotNull
    String getName();

    double getPrice();
}

/** Menu item's holder implemented as Composite pattern */

final class CompositeMenu implements IMenu {

    @NotNull
    public final String name;

    @NotNull
    private final List<IMenu> menuItems;

    /**
     * Creates empty menu holder with given title
     * @param name menu's title
     */

    public CompositeMenu(final @NotNull String name) {
        this.name = name;
        menuItems = new ArrayList<>();
    }

    /**
     * Creates menu holder with given items
     * @param name menu's title
     * @param menuItems menu sub-items
     */

    public CompositeMenu(final @NotNull String name, final @NotNull List<IMenu> menuItems) {
        this.name = name;
        this.menuItems = new ArrayList<>(menuItems);
    }

    /**
     * Prints menu's content with the next format:
     * <p>
     * $Name [ $accumulatedPrice ]
     * <p>
     * -------------------------
     * <p> <p>
     * $item1's content ...
     * <p>
     * $item2's content ...
     */

    @Override
    public void print() {
        System.out.printf("%s [ %.1f ]\n", name, getPrice());
        System.out.println("-------------------------");

        menuItems.forEach(m -> {
            if (m instanceof CompositeMenu)
                System.out.println();
            m.print();
        });
    }

    @Override
    public @NotNull String getName() { return name; }

    /**
     * Accumulates prices as the sum of children's prices
     * @return children's price sum or zero if no children are present
     */

    @Override
    public double getPrice() {
        return menuItems
                .stream()
                .map(IMenu::getPrice)
                .reduce(Double::sum)
                .orElse(0.0);
    }

    public void add(final @NotNull IMenu menuItem) { menuItems.add(menuItem); }

    public void remove(final int menuItemIndex) { menuItems.remove(menuItemIndex); }

    public void removeFirst(final @NotNull @NonNls String menuItem) {
        IntStream.range(0, menuItems.size())
                .filter(i -> menuItems.get(i).getName().equals(menuItem))
                .findFirst()
                .ifPresent(menuItems::remove);
    }

    public void removeAll(final @NotNull @NonNls String menuItem) {
        menuItems.removeIf(p -> p.getName().equals(menuItem));
    }
}

/** Menu item that does not contain any sub-items */

final class MenuItem implements IMenu {
    @NotNull
    public String name;

    public double price;

    public MenuItem(final @NotNull String name, final double price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public @NotNull String getName() { return name; }

    @Override
    public double getPrice() { return price; }

    @Override
    public void print() { System.out.println(this); }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (null == o || getClass() != o.getClass()) return false;

        final var menuItem = (MenuItem) o;
        return Double.compare(menuItem.price, price) == 0 && Objects.equals(name, menuItem.name);
    }

    @Override
    public int hashCode() { return Objects.hash(name, price); }

    @Override
    public @NotNull String toString() { return String.format("\t%s, $%.1f", name, price); }
}

/** Decorator trait for the MenuItem */

interface MenuItemDecorator extends IMenu {
    @NotNull
    IMenu getMenuItem();

    @Override
    default @NotNull String getName() { return getMenuItem().getName(); }
}

/** Decorator trait for the spicy food */

final class SpicyDecorator implements MenuItemDecorator {
    private static final double SPICY_PRICE = 2.0;

    @NotNull
    private final IMenu menuItem;

    public SpicyDecorator(final @NotNull IMenu menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public void print() {
        menuItem.print();
        System.out.println("\t\t -- This item is spicy (+ $2)");
    }

    @Override
    public double getPrice() { return menuItem.getPrice() + SPICY_PRICE; }

    @Override
    public @NotNull IMenu getMenuItem() { return menuItem; }
}

/** Decorator trait for vegetarian food */

final class VegetarianDecorator implements MenuItemDecorator {
    private static final double VEGAN_PRICE = 4.0;

    @NotNull
    private final IMenu menuItem;

    public VegetarianDecorator(final @NotNull IMenu menuItem) {
        this.menuItem = menuItem;
    }

    @Override
    public void print() {
        menuItem.print();
        System.out.println("\t\t -- This item is vegetarian (+ $4)");
    }

    @Override
    public double getPrice() { return menuItem.getPrice() + VEGAN_PRICE; }

    @Override
    public @NotNull IMenu getMenuItem() { return menuItem; }
}
