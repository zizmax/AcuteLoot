package acute.loot.namegen;

public class JPKanaNameGenerator {

    public static final NameGenerator jpKanaNameGenerator = new RepeatedNameGenerator(new NamePoolNameGenerator(FixedListNamePool.kanaPool()), 2, 5);

}
