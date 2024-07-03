package info.kgeorgiy.ja.shchetinin.iterative;

import java.util.List;
import java.util.function.BiFunction;

/**
 * The AbstractThread class extends Thread and provides functionality for iterating over a list
 * with a specified step size and applying a reduction function.
 *
 * @param <T> the type of elements in the list
 * @param <U> the type of the accumulator
 */
class AbstractThread<T, U> extends Thread {
    private final List<T> list;
    private final int step;
    private final BiFunction<U, T, U> function;
    private U accumulator;

    /**
     * Constructs a new AbstractThread object with the specified list, step size, initial accumulator value,
     * and reduce function.
     *
     * @param list     the list of elements to iterate over
     * @param step     the step size indicating how many elements to skip in each iteration
     * @param identity the initial value of the accumulator
     * @param function the function to reduce
     */
    public AbstractThread(List<T> list, int step, U identity, BiFunction<U, T, U> function) {
        this.list = list;
        this.step = step;
        this.function = function;
        accumulator = identity;
    }

    /**
     * Executes the thread, iterating over the list with the specified step size and applying the function
     * on each element to update the accumulator.
     */
    @Override
    public void run() {
        for (int i = 0; i < list.size(); i += step) {
            accumulator = function.apply(accumulator, list.get(i));
        }
    }

    /**
     * Gets the accumulator value after the thread has finished executing.
     *
     * @return the final value of the accumulator after iteration
     */
    public U getAccumulator() {
        return accumulator;
    }
}
