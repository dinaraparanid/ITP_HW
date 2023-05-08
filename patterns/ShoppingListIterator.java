import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Main class and start point of the program.
 * Note that this program requires unstable API
 * and Java 19 Preview version
 * with Virtual Threads support (new Java's async/await)
 */

public final class ShoppingListIterator {
    public static void main(final String[] args) throws ExecutionException, InterruptedException {
        final var mainShoppingListTask = MainShoppingList.loadShoppingListAsync();
        final var vasyaShoppingListTask = UserShoppingList.generateShoppingListAsync("https://raw.githubusercontent.com/UralmashFox/assignment_5/main/customer_1.txt");
        final var kolyaShoppingListTask = UserShoppingList.generateShoppingListAsync("https://raw.githubusercontent.com/UralmashFox/assignment_5/main/customer_2.txt");
        final var mishaShoppingListTask = UserShoppingList.generateShoppingListAsync("https://raw.githubusercontent.com/UralmashFox/assignment_5/main/customer_3.txt");

        final var mainShoppingList = mainShoppingListTask.get();

        System.out.println("Main Shopping List");
        System.out.println("------------------");
        mainShoppingList.log();
        System.out.println();

        final var vasyaShoppingList = vasyaShoppingListTask.get();

        System.out.println("Vasya's Shopping List");
        System.out.println("---------------------");
        vasyaShoppingList.log();
        System.out.println();

        final var kolyaShoppingList = kolyaShoppingListTask.get();

        System.out.println("Kolya's Shopping List");
        System.out.println("---------------------");
        kolyaShoppingList.log();
        System.out.println();

        final var mishaShoppingList = mishaShoppingListTask.get();

        System.out.println("Misha's Shopping List");
        System.out.println("---------------------");
        kolyaShoppingList.log();
        System.out.println();

        vasyaShoppingList.forEach(vasyaProduct ->
            mainShoppingList.tryDecrease(vasyaProduct.title(), vasyaProduct.quantity())
        );

        System.out.println();
        System.out.println("Main Shopping List after Vasya's purchase");
        System.out.println("-----------------------------------------");
        mainShoppingList.log();
        System.out.println();

        kolyaShoppingList.forEach(kolyaProduct ->
                mainShoppingList.tryDecrease(kolyaProduct.title(), kolyaProduct.quantity())
        );

        System.out.println();
        System.out.println("Main Shopping List after Kolya's purchase");
        System.out.println("-----------------------------------------");
        mainShoppingList.log();
        System.out.println();

        mishaShoppingList.forEach(mishaProduct ->
                mainShoppingList.tryDecrease(mishaProduct.title(), mishaProduct.quantity())
        );

        System.out.println();
        System.out.println("Main Shopping List after Misha's purchase");
        System.out.println("-----------------------------------------");
        mainShoppingList.log();
    }
}

/**
 * Product to buy
 * @param title product's title
 * @param quantity product's amount
 */

record Product(@NotNull String title, int quantity) {

    /**
     * Parses string #r`^\w+, \d+$` as the new product
     * @param s string to parse
     */

    public static Product parseProduct(final @NotNull String s) {
        final var arr = s.trim().split(", ");
        return new Product(arr[0], Integer.parseInt(arr[1]));
    }
}

/** Product list that allows iteration */

abstract class AbstractShoppingList implements Iterable<Product> {

    /** Thread pool for product list's initialization */

    @NotNull
    protected static final ExecutorService initExecutor = Executors.newVirtualThreadPerTaskExecutor();

    @NotNull
    protected final String fileUrl;

    @NotNull
    protected List<Product> products;

    protected AbstractShoppingList(final @NotNull String fileUrl) {
        this.fileUrl = fileUrl;

        try (final var reader = new BufferedReader(new InputStreamReader(new URL(fileUrl).openStream()))) {
            products = reader.lines()
                    .filter(s -> !s.isEmpty())
                    .map(Product::parseProduct)
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    protected final class Iter implements ListIterator<Product> {

        private int cursor;

        Iter(final int initialCursor) { cursor = initialCursor; }

        @Override
        public boolean hasNext() { return cursor < products.size(); }

        @Override
        public boolean hasPrevious() { return cursor > 0; }

        @Override
        @NotNull
        public Product next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return products.get(cursor++);
        }

        @Override
        @NotNull
        public Product previous() {
            if (!hasPrevious())
                throw new NoSuchElementException();
            return products.get(cursor--);
        }

        @Override
        public int nextIndex() { return cursor; }

        @Override
        public int previousIndex() { return cursor - 1; }

        @Override
        public void remove() { throw new UnsupportedOperationException(); }

        @Override
        public void set(final @Nullable Product product) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final @Nullable Product product) {
            throw new UnsupportedOperationException();
        }
    }

    @NotNull
    @Override
    public final Iterator<Product> iterator() { return new Iter(0); }

    public final void log() { products.forEach(System.out::println); }
}

/** Original shopping list parsed from the file */

final class MainShoppingList extends AbstractShoppingList {

    /** Reads and parses file synchronously */

    private MainShoppingList() {
        super("https://raw.githubusercontent.com/UralmashFox/assignment_5/main/shop.txt");
    }

    /** Reads and parses file synchronously */

    @NotNull
    public static synchronized MainShoppingList loadShoppingListSync() { return new MainShoppingList(); }

    /** Reads and parses file asynchronously */

    @NotNull
    public static synchronized Future<MainShoppingList> loadShoppingListAsync() {
        return initExecutor.submit(MainShoppingList::loadShoppingListSync);
    }

    /**
     * Tries to decrease product's quantity,
     * if this product is present and number of products is enough
     */

    public void tryDecrease(final @NotNull @NonNls String productTitle, final int amount) {
        final var productIndOpt = IntStream.range(0, products.size())
                .filter(i -> products.get(i).title().equals(productTitle))
                .findFirst();

        if (productIndOpt.isEmpty()) {
            System.out.printf("Product `%s` is sold out\n", productTitle);
            return;
        }

        final var productInd = productIndOpt.getAsInt();
        final var productQuantity = products.get(productInd).quantity();

        if (productQuantity < amount) {
            System.out.printf("There is not enough product `%s` for this purchase\n", productTitle);
            return;
        }

        final var newQuantity = productQuantity - amount;

        if (newQuantity == 0)
            products.remove(productInd);
        else
            products.set(productInd, new Product(productTitle, productQuantity - amount));
    }
}

/** User's shopping list randomly generated from the original file */

final class UserShoppingList extends AbstractShoppingList {

    /**
     * Reads and parses file synchronously,
     * shuffles all items and removes some of them
     */

    private UserShoppingList(final @NotNull String fileUrl) { super(fileUrl); }

    /**
     * Reads and parses file synchronously,
     * shuffles all items and removes some of them
     */

    @NotNull
    public static UserShoppingList createShoppingListSync(final @NotNull String fileUrl) {
        return new UserShoppingList(fileUrl);
    }

    /**
     * Reads and parses file asynchronously,
     * shuffles all items and removes some of them
     */

    @NotNull
    public static synchronized Future<UserShoppingList> generateShoppingListAsync(final @NotNull String fileUrl) {
        return initExecutor.submit(() -> createShoppingListSync(fileUrl));
    }
}
