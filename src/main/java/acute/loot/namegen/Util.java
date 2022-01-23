package acute.loot.namegen;

import java.util.stream.LongStream;

final class Util {

    private Util() {}

    public static LongStream nameCount(final NameGenerator nameGenerator) {
        return nameGenerator.countNumberOfNames().map(LongStream::of).orElseGet(LongStream::empty);
    }

}
