package acute.loot;

import java.util.Random;

public class TestChancePool {

    public static void main(String[] args) {
        final Random random = new Random();

        final IntegerChancePool<Integer> chancePool = new IntegerChancePool<>();
        chancePool.add(0, 10);
        chancePool.add(1, 5);
        chancePool.add(2, 1);

        System.out.printf("Built chance pool:\n%s\n", chancePool.toString());

        System.out.println(chancePool.max() == 16);

        int[] counts = new int[3];

        final int trials = 1000000;
        for (int i = 0; i < trials; i++) {
            counts[chancePool.draw(random.nextInt(chancePool.max()))]++;
        }

        System.out.println("Draws");
        System.out.println("0: " + counts[0] + " expected " + (trials * 10 / 16));
        System.out.println("1: " + counts[1] + " expected " + (trials * 5 / 16));
        System.out.println("2: " + counts[2] + " expected " + (trials * 1 / 16));

        int[] countsPred = new int[3];
        for (int i = 0; i < trials; i++) {
            countsPred[chancePool.drawWithPredicate(e -> e < 2)]++;
        }

        System.out.println("Draws");
        System.out.println("0: " + countsPred[0] + " expected " + (trials * 10 / 15));
        System.out.println("1: " + countsPred[1] + " expected " + (trials * 5 / 15));
        System.out.println("2: " + countsPred[2] + " expected " + (trials * 0 / 16));
    }

}
