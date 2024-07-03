package info.kgeorgiy.ja.shchetinin.i18n.statistic;

import java.text.BreakIterator;
import java.util.regex.Pattern;

/**
 * A class for calculating statistics on strings, including the minimum and maximum length of the strings,
 * as well as the average length. It can be configured to process words or sentences.
 */
public class StringStatistic extends Statistic<String, Double, StringStatistic.MinMaxLength> {

    /**
     * A record representing the minimum and maximum length strings.
     */
    public record MinMaxLength(String min, String max) {
    }

    private String min;
    private String max;
    private final BreakIterator iter;
    private int prev = 0;
    private final Boolean isWord;

    /**
     * Constructs a new StringStatistic with the specified parameters.
     *
     * @param s the string to analyze
     * @param iter the BreakIterator used to divide the string into words or sentences
     * @param isWord a boolean indicating whether to process words (true) or sentences (false)
     */
    public StringStatistic(String s, BreakIterator iter, Boolean isWord) {
        super(s, String::compareTo, (doub, s1) -> {
            if (doub == null) {
                doub = 0.0;
            }
            return doub + s1.length();
        });
        this.isWord = isWord;
        this.iter = iter;
        iter.setText(s);
    }

    /**
     * Computes the average length of the strings.
     *
     * @param sum the sum of the lengths of the strings
     * @param amount the number of strings
     * @return the average length of the strings
     */
    @Override
    Double getAverage(Double sum, int amount) {
        if (amount == 0) {
            return 0.0;
        }
        return sum / amount;
    }

    /**
     * Retrieves the next string in the sequence. Skips empty strings, punctuation, and non-word strings if processing words.
     *
     * @return the next string, or null if there are no more strings
     */
    @Override
    String getNext() {
        String tmp = "";
        while (tmp.isEmpty() || Pattern.matches("[\\p{Punct}\\p{IsPunctuation}]", tmp) ||
                (tmp.chars().anyMatch(o -> !Character.isLetter(o)) && isWord)) {
            int nextInd = iter.next();
            if (nextInd == -1) {
                return null;
            }
            tmp = value.substring(prev, nextInd);
            tmp = tmp.trim();
            prev = nextInd;
        }
        return tmp;
    }

    /**
     * Processes additional information for the specified string, updating the minimum and maximum length strings.
     *
     * @param next the string to process
     */
    @Override
    void additionalInfo(String next) {
        if (min == null) {
            min = next;
            max = next;
            return;
        }
        if (min.length() > next.length()) {
            min = next;
        }
        if (max.length() < next.length()) {
            max = next;
        }
    }

    /**
     * Retrieves the additional information collected during computation, specifically the minimum and maximum length strings.
     *
     * @return the additional information as a MinMaxLength object
     */
    @Override
    public MinMaxLength getAdditionalInfo() {
        return new MinMaxLength(min, max);
    }
}
