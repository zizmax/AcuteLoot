package base.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EitherTest {

    @Test
    @DisplayName("Either construction correct")
    public void eitherConstructionCorrect() {
        assertThrows(NullPointerException.class, () -> Either.ofLeft(null));
        assertThrows(NullPointerException.class, () -> Either.ofRight(null));

        assertThat(Either.ofLeft("Foo").left(), is(Optional.of("Foo")));
        assertThat(Either.ofLeft("Bar").right(), is(Optional.empty()));
        assertThat(Either.ofRight(12345).left(), is(Optional.empty()));
        assertThat(Either.ofRight(67890).right(), is(Optional.of(67890)));
    }

    @Test
    @DisplayName("Either acceptOr() correct")
    public void eitherAcceptCorrect() {
        class Monitor implements Consumer<Object> {
            int hits = 0;
            @Override
            public void accept(Object o) {
                hits++;
            }
        }

        final Monitor left = new Monitor();
        final Monitor right = new Monitor();

        final Either<Object, Object> lEither = Either.ofLeft("Hello");
        final Either<Object, Object> rEither = Either.ofRight(1337);

        assertThat(left.hits, is(0));
        assertThat(right.hits, is(0));

        lEither.acceptOr(left, right);
        assertThat(left.hits, is(1));
        assertThat(right.hits, is(0));

        rEither.acceptOr(left, right);
        assertThat(left.hits, is(1));
        assertThat(right.hits, is(1));
    }

}
