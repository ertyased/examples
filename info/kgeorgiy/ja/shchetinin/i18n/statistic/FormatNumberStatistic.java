package info.kgeorgiy.ja.shchetinin.i18n.statistic;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Comparator;

/**
 * A class for calculating statistics on formatted numbers. It extends {@link Statistic}
 * to provide specialized handling for numbers, including computing the minimum, maximum,
 * sum, and average of the numbers.
 */
public class FormatNumberStatistic extends Statistic<Number, Double, Void> {
    private final NumberFormat format;
    private final ParsePosition a = new ParsePosition(0);

    /**
     * Constructs a new FormatNumberStatistic with the specified parameters.
     *
     * @param s the string to analyze
     * @param numberFormat the NumberFormat used to parse the numbers from the string
     */
    public FormatNumberStatistic(String s, NumberFormat numberFormat) {
        super(s, Comparator.comparingDouble(Number::doubleValue), (a, b) -> {
            if (a == null) {
                return b.doubleValue();
            }
            return a + b.doubleValue();
        });
        this.format = numberFormat;
    }

    /**
     * Processes additional information for the specified number. This implementation does nothing.
     *
     * @param next the number to process
     */
    @Override
    void additionalInfo(Number next) {
        // No additional information to process for numbers
    }

    /**
     * Retrieves the additional information collected during computation. This implementation returns null.
     *
     * @return null, as no additional information is collected for numbers
     */
    @Override
    Void getAdditionalInfo() {
        return null;
    }

    /**
     * Computes the average of the numbers.
     *
     * @param sum the sum of the numbers
     * @param amount the number of elements
     * @return the average of the numbers, or 0.0 if the amount is 0
     */
    @Override
    Double getAverage(Double sum, int amount) {
        if (amount == 0) {
            return 0.0;
        }
        return sum / amount;
    }

    @Override
    Double getNext() {
        while (a.getIndex() < value.length()) {
            Number res = format.parse(value, a);
            if (res == null) {
                a.setIndex(a.getIndex() + 1);
                continue;
            }
            return res.doubleValue();
        }
        return null;
    }
}
