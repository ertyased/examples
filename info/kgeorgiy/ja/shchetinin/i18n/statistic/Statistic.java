package info.kgeorgiy.ja.shchetinin.i18n.statistic;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * An abstract class for calculating statistics on a sequence of elements.
 * This class supports computing the minimum, maximum, sum, and average of the elements,
 * as well as counting the number of unique elements.
 *
 * @param <T> the type of elements being processed
 * @param <G> the type of the sum and average values
 * @param <U> the type of additional information collected during computation
 */
abstract public class Statistic<T, G, U> {

    private final Set<T> different;
    private final Comparator<T> compare;
    private final BiFunction<G, T, G> add;
    protected final String value;

    /**
     * Constructs a new Statistic with the specified parameters.
     *
     * @param s the string representing the sequence of elements to be processed
     * @param compare the comparator used to determine the order of the elements
     * @param add the function used to add elements to the sum
     */
    public Statistic(String s, Comparator<T> compare, BiFunction<G, T, G> add) {
        different = new HashSet<>();
        value = s;
        this.compare = compare;
        this.add = add;
    }

    /**
     * Computes the statistics for the sequence of elements.
     * <p>
     * This method calculates the minimum, maximum, sum, and average of the elements,
     * and counts the number of unique elements.
     *
     * @return a Result object containing the computed statistics
     */
    public Result<T, G> compute() {
        T min = null;
        T max = null;
        G sum = null;
        int amount = 0;

        while (true) {
            T next = getNext();
            if (next == null) {
                break;
            }

            amount++;
            additionalInfo(next);
            different.add(next);

            if (min == null) {
                min = next;
                max = next;
            } else {
                if (compare.compare(next, min) < 0) {
                    min = next;
                }
                if (compare.compare(max, next) < 0) {
                    max = next;
                }
            }
            sum = add.apply(sum, next);
        }
        G avg = getAverage(sum, amount);
        return new Result<>(amount, different.size(), min, max, avg);
    }

    /**
     * Processes additional information for each element.
     *
     * @param next the element to process
     */
    abstract void additionalInfo(T next);

    /**
     * Retrieves the additional information collected during computation.
     *
     * @return the additional information
     */
    abstract U getAdditionalInfo();

    /**
     * Computes the average of the elements.
     *
     * @param sum the sum of the elements
     * @param amount the number of elements
     * @return the average of the elements
     */
    abstract G getAverage(G sum, int amount);

    /**
     * Retrieves the next element in the sequence.
     *
     * @return the next element, or null if there are no more elements
     */
    abstract T getNext();
}