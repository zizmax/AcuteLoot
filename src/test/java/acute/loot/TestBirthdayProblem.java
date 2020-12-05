package acute.loot;

import acute.loot.namegen.PermutationCounts;

/**
 * This class will run the birthday problem function for various inputs
 */
public class TestBirthdayProblem {

    public static void main(String[] args) {
        // normal birthday problem
        System.out.println(PermutationCounts.birthdayPercent(23, 365));

        System.out.println(PermutationCounts.birthdayProblem(365, 0.5, 0.00001));
        System.out.println(PermutationCounts.birthdayProblem(4000000000L, 0.5, 0.00001));
        System.out.println(PermutationCounts.birthdayProblem(17000000000L, 0.5, 0.00001));
    }

}
