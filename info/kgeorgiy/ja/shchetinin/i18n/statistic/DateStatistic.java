package info.kgeorgiy.ja.shchetinin.i18n.statistic;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.Date;

/**
 * A class for calculating statistics on dates. It extends {@link Statistic}
 * to provide specialized handling for dates, including computing the minimum, maximum,
 * sum, and average of the dates.
 */
public class DateStatistic extends Statistic<Date, Long, Void> {

    private final DateFormat format;
    private final ParsePosition a = new ParsePosition(0);

    /**
     * Converts a {@link Date} to milliseconds since the epoch.
     *
     * @param date the date to convert
     * @return the number of milliseconds since the epoch
     */
    static private Long getMilliseconds(Date date) {
        return date.toInstant().toEpochMilli();
    }

    /**
     * Constructs a new DateStatistic with the specified parameters.
     *
     * @param s the string to analyze
     * @param dateFormat the DateFormat used to parse the dates from the string
     */
    public DateStatistic(String s, DateFormat dateFormat) {
        super(s, Comparator.comparing(DateStatistic::getMilliseconds, Long::compareTo),
                (aLong, date) -> {
                    if (aLong == null) {
                        aLong = 0L;
                    }
                    return aLong + getMilliseconds(date);
                });
        this.format = dateFormat;
    }

    /**
     * Processes additional information for the specified date. This implementation does nothing.
     *
     * @param next the date to process
     */
    @Override
    void additionalInfo(Date next) {
        // No additional information to process for dates
    }

    /**
     * Retrieves the additional information collected during computation. This implementation returns null.
     *
     * @return null, as no additional information is collected for dates
     */
    @Override
    Void getAdditionalInfo() {
        return null;
    }

    /**
     * Computes the average of the dates in milliseconds.
     *
     * @param sum the sum of the dates in milliseconds
     * @param amount the number of dates
     * @return the average of the dates in milliseconds, or 0L if the sum is null
     */
    @Override
    Long getAverage(Long sum, int amount) {
        if (sum == null) {
            return 0L;
        }
        return sum / amount;
    }

    @Override
    Date getNext() {
        while (a.getIndex() < value.length()) {
            Date res = format.parse(value, a);
            if (res == null) {
                a.setIndex(a.getIndex() + 1);
                continue;
            }
            return res;
        }
        return null;
    }
}
