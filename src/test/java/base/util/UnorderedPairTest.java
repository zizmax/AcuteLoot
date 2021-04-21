package base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;

public class UnorderedPairTest {

    @Test
    @DisplayName("UnorderedPair correct")
    public void unorderedPairCorrect() {
        final UnorderedPair<?> nullPair = UnorderedPair.of(null, null);
        final UnorderedPair<String> firstNull = UnorderedPair.of(null, "Foo");
        final UnorderedPair<String> secondNull = UnorderedPair.of("Foo", null);
        final UnorderedPair<String> pairOne = UnorderedPair.of("Foo", "Bar");
        final UnorderedPair<String> pairTwo = UnorderedPair.of("Bar", "Foo");
        final UnorderedPair<String> pairThree = UnorderedPair.of("Hello", "World");

        assertThat(firstNull, is(secondNull));
        assertThat(firstNull.hashCode(), is(secondNull.hashCode()));
        assertThat(pairOne, is(pairTwo));
        assertThat(pairOne.hashCode(), is(pairTwo.hashCode()));

        assertThat(nullPair, not(secondNull));
        assertThat(firstNull, not(pairOne));
        assertThat(pairTwo, not(pairThree));
        assertThat(pairTwo.hashCode(), not(pairThree.hashCode()));
    }

}
