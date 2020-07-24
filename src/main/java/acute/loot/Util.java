package acute.loot;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

public final class Util {

    private static final Random random = AcuteLoot.random;

    private Util() {
    }

    /**
     * Draw a random element from a list. If the list is empty, throw a NoSuchElementException.
     *
     * @param list the List to draw from
     * @return a random element from the list
     */
    public static <T> T drawRandom(List<T> list) {
        if (list.isEmpty()) throw new NoSuchElementException();
        return list.get(random.nextInt(list.size()));
    }

}
