package application.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Math utility methods.
 * 
 * @author Sid Botvin
 */
public class MathUtil {

    /**
     * Returns the values found in a given sum.
     * 
     * <p>
     * Given the sum {@code 54} and values {@code [2, 4, 8, 16, 32, 64]}, it
     * would return {@code [2, 4, 16, 32]}.
     * </p>
     * 
     * <p>
     * Note: There's probably a much cleaner way to accomplish this, if you're
     * reading this and know how, please tell.
     * </p>
     * 
     * @param sum
     *            The sum of the values.
     * @param values
     *            An array of possible values for the given sum. E.g.
     *            {@code 2, 4,
     *            8...}
     * 
     * @throws ArithmeticException
     *             If the given sum cannot be found using the given values.
     */
    public static List<Integer> getValuesFromSum(int sum, int[] values) {
        List<Integer> foundValues = new ArrayList<Integer>();

        // Loop backwards
        for (int i = values.length - 1; i > -1; i--) {
            int x = values[i];

            // If target is bigger than or equal, this value contains it
            if (sum >= x) {
                sum -= x;
                foundValues.add(x);
            }

            // If we ended up below zero someone made a mistake
            if (sum < 0)
                throw new ArithmeticException();

            // If targetValue has reached 0 we've found all our values
            if (sum == 0)
                break;
        }

        return foundValues;
    }

}
