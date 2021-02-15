package acute.loot;

import base.collections.IntegerChancePool;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class IntegerChancePoolTest {

    @Test
    @DisplayName("Chance pool add correct")
    public void chancePoolAddCorrect() {
        final IntegerChancePool<String> pool = new IntegerChancePool<>();
        assertThat(pool.values(), empty());
        assertThat(pool.max(), is(0));

        pool.add("test", 10);
        assertThat(pool.values(), containsInAnyOrder("test"));
        assertThat(pool.max(), is(10));

        pool.add("hello, world", 4);
        pool.add("AcuteLoot", 1);
        assertThat(pool.values(), containsInAnyOrder("test", "AcuteLoot", "hello, world"));
        assertThat(pool.max(), is(15));

        assertThrows(IllegalArgumentException.class, () -> pool.add("TestTest", 0));
        assertThrows(IllegalArgumentException.class, () -> pool.add("TestTest", -1));
        assertThrows(IllegalArgumentException.class, () -> pool.add("TestTest", -1234));

        pool.addDiscardingInvalid("TestTest", 0);
        assertThat(pool.values(), containsInAnyOrder("test", "AcuteLoot", "hello, world"));
        assertThat(pool.max(), is(15));

        pool.addDiscardingInvalid("TestTest2", -3);
        assertThat(pool.values(), containsInAnyOrder("test", "AcuteLoot", "hello, world"));
        assertThat(pool.max(), is(15));

        pool.clear();
        assertThat(pool.values(), empty());
        assertThat(pool.max(), is(0));
    }

    @Test
    @DisplayName("Chance pool draw correct")
    public void chancePoolDrawCorrect() {
        final IntegerChancePool<String> pool = new IntegerChancePool<>();
        //noinspection Convert2MethodRef
        assertThrows(NoSuchElementException.class, () -> pool.draw());
        assertThrows(NoSuchElementException.class, () -> pool.draw(0));
        assertThrows(NoSuchElementException.class, () -> pool.draw(-1));
        assertThrows(NoSuchElementException.class, () -> pool.draw(10));

        pool.add("test", 10);
        assertThrows(IllegalArgumentException.class, () -> pool.draw(-1));
        assertThrows(IllegalArgumentException.class, () -> pool.draw(10));
        for (int i = 0; i < 9; i++) {
            assertThat(pool.draw(i), is("test"));
        }

        pool.add("hello, world", 4);
        pool.add("AcuteLoot", 1);
        for (int i = 0; i < 9; i++) {
            assertThat(pool.draw(i), is("test"));
        }
        for (int i = 10; i < 14; i++) {
            assertThat(pool.draw(i), is("hello, world"));
        }
        assertThat(pool.draw(14), is("AcuteLoot"));

        assertThat(pool.draw(0.0), is(pool.draw(0)));
        assertThat(pool.draw(1.0), is(pool.draw(pool.max() - 1)));
        assertThat(pool.draw(0.25), is("test"));
        assertThat(pool.draw(0.80), is("hello, world"));
        assertThat(pool.draw(0.99), is("AcuteLoot"));
    }

    @Test
    @DisplayName("Chance pool filter and remove correct")
    public void chancePoolFilterAndRemoveCorrect() {
        final IntegerChancePool<String> pool = new IntegerChancePool<>();
        pool.add("hello, world!", 1);
        pool.add("hello", 2);
        pool.add("Foo", 3);
        pool.add("foobar", 4);
        pool.add("AcuteLoot", 5);

        final IntegerChancePool<String> filteredNoHello = pool.filter(s -> !s.startsWith("hello"));
        assertThat(filteredNoHello.max(), is(12));
        assertThat(filteredNoHello.values(), containsInAnyOrder("Foo", "foobar", "AcuteLoot"));

        final IntegerChancePool<String> filteredNoCaps = pool.filter(s -> !Character.isUpperCase(s.charAt(0)));
        assertThat(filteredNoCaps.max(), is(7));
        assertThat(filteredNoCaps.values(), containsInAnyOrder("hello, world!", "hello", "foobar"));

        final IntegerChancePool<String> filteredNoO = pool.filter(s -> !s.contains("o"));
        assertThat(filteredNoO.max(), is(0));
        assertThat(filteredNoO.values(), empty());

        pool.removeWithPredicate(s -> s.startsWith("hello"));
        assertThat(pool.max(), is(12));
        assertThat(pool.values(), containsInAnyOrder("Foo", "foobar", "AcuteLoot"));

        pool.removeWithPredicate(s -> s.contains("o"));
        assertThat(pool.max(), is(0));
        assertThat(pool.values(), empty());

        final IntegerChancePool<String> pool1 = new IntegerChancePool<>();
        final IntegerChancePool<String> pool2 = new IntegerChancePool<>();
        pool1.add("abc", 1);
        pool1.add("def", 1);
        pool1.add("ghi", 1);
        pool2.add("abc", 1);
        pool2.add("def", 1);
        pool2.add("ghi", 1);
        assertThat(pool1.max(), is(pool2.max()));
        // toArray() here to avoid pool2.values() being interpreted as our target item itself
        assertThat(pool1.values(), containsInAnyOrder(pool2.values().toArray()));
        pool1.clear();
        pool2.removeWithPredicate(s -> true);
        assertThat(pool1.max(), is(pool2.max()));
        assertThat(pool1.values(), containsInAnyOrder(pool2.values().toArray()));
    }

    @Test
    @DisplayName("Chance pool draw with predicate correct")
    public void test() {
        final IntegerChancePool<String> pool = new IntegerChancePool<>();
        pool.add("hello, world!", 1);
        pool.add("hello", 2);
        pool.add("Foo", 3);
        pool.add("foobar", 4);
        pool.add("AcuteLoot", 5);

        // drawWithPredicate() is random, so do these a few times
        for (int i = 0; i < 10; i++) {
            assertThat(pool.drawWithPredicate(s -> s.startsWith("hello")), startsWith("hello"));
            assertThat(pool.drawWithPredicate(s -> s.contains("u")), is("AcuteLoot"));
            assertThat(pool.drawWithPredicate(s -> s.length() > 5),
                    anyOf(is("hello, world!"), is("AcuteLoot"), is("foobar")));
        }

        assertThrows(NoSuchElementException.class, () -> pool.drawWithPredicate(s -> false));
    }

}
