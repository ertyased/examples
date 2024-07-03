package info.kgeorgiy.ja.shchetinin.i18n.test;


import info.kgeorgiy.ja.shchetinin.i18n.TextStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StatisticTest {
    private static final Locale OUTPUT_RU = Locale.of("ru", "ru");
    private static final Locale OUTPUT_EN = Locale.of("en", "us");
    private static final Locale[] AVAILABLE_INPUT_LOCALES = Locale.getAvailableLocales();
    private final Random random = new Random(1);

    private static final long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

    String randomWord(int maxLen, String[] alphabet) {
        int len = random.nextInt(1, maxLen + 1);
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < len; i++) {
            res.append(alphabet[random.nextInt(alphabet.length)]);
        }
        return res.toString();
    }

    @Test
    void emptyTest() throws IOException, ParseException {
        FullInfo fi = test("", OUTPUT_EN, OUTPUT_RU);
        assertEquals(fi.word.am, 0);
        assertEquals(fi.sentence.am, 0);
        assertEquals(fi.date.am, 0);
        assertEquals(fi.currency.am, 0);
        assertEquals(fi.number.am, 0);
    }

    @Test
    void oneWordTest() throws IOException, ParseException {
        FullInfo fi = test("Aboba", OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.am, 1);
        assertEquals(fi.word.diff, 1);
        assertEquals(fi.word.min, "Aboba");
        assertEquals(fi.word.min, fi.word.max);
        assertEquals(fi.word.shortest, 5);
        assertEquals(fi.word.longest, fi.word.shortest);
        assertEquals(fi.word.average, 5d);
    }

    @Test
    void manyIdenticalWords() throws IOException, ParseException {
        FullInfo fi = test(" hello".repeat(100), OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.am, 100);
        assertEquals(fi.word.diff, 1);
        assertEquals(fi.sentence.am, 1);
    }

    @Test
    void randomWords() throws IOException, ParseException {
        StringBuilder s = new StringBuilder();
        Set<String> diffs = new HashSet<>();
        int sumLen = 0;
        for (int i = 0; i < 1000; ++i) {
            String rs = randomWord(100, Alphabets.alphabets.get("en"));
            diffs.add(rs);
            sumLen += rs.length();
            s.append(" ").append(rs);
        }
        FullInfo fi = test(s.toString(), OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.am, 1000);
        assertEquals(fi.word.diff, diffs.size());
        assertEquals(fi.word.average, sumLen / 1000d);
    }

    private String randomWords(int amount, int maxLen, String[] alphabet) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < amount; ++i) {
            String rs = randomWord(maxLen, alphabet);
            s.append(rs).append(" ");
        }
        return s.toString();
    }

    @Test
    void longestWord() throws IOException, ParseException {
        String longWord = randomWord(1000, Alphabets.alphabets.get("en"));
        String s = randomWords(50, 10, Alphabets.alphabets.get("en"));
        s += longWord;
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.longest, longWord.length());
    }

    @Test
    void shortestWord() throws IOException, ParseException {
        String s = "bbbc bbbbbbbc bcmbn gfsjknf xzcz";
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.shortest, 4);
    }

    @Test
    void smallestWord() throws IOException, ParseException {
        String smallWord = "A";
        String s = randomWords(100, 10, Alphabets.alphabets.get("en"));
        s += smallWord;
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.min, smallWord);
    }

    @Test
    void biggestWord() throws IOException, ParseException {
        String bigWord = "zzzzzzzzzzzzz";
        String s = randomWords(100, 10, Alphabets.alphabets.get("en"));
        s += bigWord;
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.word.max, bigWord);
    }

    @Test
    void differentLocaleWords() throws IOException, ParseException {
        for (Map.Entry<String, String[]> entry : Alphabets.alphabets.entrySet()) {
            String country = entry.getKey();
            String[] alphabet = entry.getValue();
            String s;
            if (country.equals("ja") || country.equals("zh")) {
                s = randomWords(100, 1, alphabet);
            } else {
                s = randomWords(100, 10, alphabet);
            }

            FullInfo fi = test(s, Locale.of(entry.getKey()), OUTPUT_EN);
            assertEquals(fi.word.am, 100);
        }
    }

    @Test
    void simpleSentence() throws IOException, ParseException {
        String s = "Hello, World!";
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.sentence.am, 1);
        assertEquals(fi.sentence.shortest, s.length());
        assertEquals(fi.sentence.longest, s.length());
        assertEquals(fi.sentence.min, s);
        assertEquals(fi.sentence.max, s);
        assertEquals(fi.sentence.average, s.length());
    }

    @Test
    void twoSentences() throws IOException, ParseException {
        String s1 = "Hello, World!";
        String s2 = "My name is Egor.";
        String s = s1 + " " + s2;
        FullInfo fi = test(s, OUTPUT_EN, OUTPUT_EN);
        assertEquals(fi.sentence.am, 2);
        assertEquals(fi.sentence.diff, 2);
        assertEquals(fi.sentence.shortest, s1.length());
        assertEquals(fi.sentence.longest, s2.length());
        assertEquals(fi.sentence.min, s1);
        assertEquals(fi.sentence.max, s2);
        assertEquals(fi.sentence.average, (s1.length() + s2.length()) / 2d);
    }

    @Test
    void manySentences() throws IOException, ParseException {
        FullInfo fi = test(Text.crimeAndPunishment, OUTPUT_RU, OUTPUT_EN);
        assertEquals(fi.sentence.am, 59);
    }

    private void assertEqualsError(double a, double b) {
        assertTrue(Math.abs(a - b) / a < 0.0078);
    }

    private void testNumbers(int am, Format nf, double module_max, Locale input, Locale output, int type) throws IOException, ParseException {
        StringBuilder s = new StringBuilder();
        double sum = 0d;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        for (int i = 0; i < am; ++i) {
            double num;
            if (type == 2) {
                int n = random.nextInt((int) -module_max, (int) module_max);
                long numTmp = n * MILLISECONDS_IN_DAY;
                s.append(nf.format(numTmp)).append(" ");
                num = numTmp;
            } else {
                num = random.nextDouble(-module_max, module_max);
                s.append(nf.format(num)).append(" ");
            }
            sum += num;
            max = Double.max(max, num);
            min = Double.min(min, num);
        }
        FullInfo fi = test(s.toString(), input, output);
        if (type == 0) {
            assertEqualsError(fi.number.am, am);
            assertEqualsError(fi.number.max, max);
            assertEqualsError(fi.number.avg, sum / am);
            assertEqualsError(fi.number.min, min);
        } else if (type == 1) {
            assertEqualsError(fi.currency.am, am);
            assertEqualsError(fi.currency.max, max);
            assertEqualsError(fi.currency.avg, sum / am);
            assertEqualsError(fi.currency.min, min);
        } else if (type == 2) {
            assertEqualsError(fi.date.am, am);
            assertEqualsError(fi.date.max, max);
            assertEqualsError(fi.date.avg, sum / am);
            assertEqualsError(fi.date.min, min);
        }
    }

    @Test
    void oneNumber() throws IOException, ParseException {
        testNumbers(1, NumberFormat.getNumberInstance(OUTPUT_EN), 100, OUTPUT_EN, OUTPUT_EN, 0);
    }

    @Test
    void manyLargeNumber() throws IOException, ParseException {
        testNumbers(100, NumberFormat.getNumberInstance(OUTPUT_EN), 1000000000000D, OUTPUT_EN, OUTPUT_EN, 0);
    }

    @Test
    void maxDoubleNumber() throws IOException, ParseException {
        NumberFormat nf = NumberFormat.getNumberInstance(OUTPUT_RU);
        double a = Double.MAX_VALUE;
        String s = nf.format(a);
        FullInfo fi = test(s, OUTPUT_RU, OUTPUT_RU);
        assertEqualsError(fi.number.am, 1);
        assertEqualsError(fi.number.max, a);
        assertEqualsError(fi.number.avg, a);
        assertEqualsError(fi.number.min, a);
    }

    @Test
    void numbersDifferentLocale() throws IOException, ParseException {
        for (int i = 0; i < 50; ++i) {
            Locale locale = AVAILABLE_INPUT_LOCALES[i];
            testNumbers(5, NumberFormat.getNumberInstance(locale), 1000, locale, OUTPUT_EN, 0);
        }
    }

    @Test
    void oneCurrency() throws IOException, ParseException {
        testNumbers(1, NumberFormat.getCurrencyInstance(OUTPUT_RU), 100, OUTPUT_RU, OUTPUT_RU, 1);
    }

    @Test
    void manyLargeCurrency() throws IOException, ParseException {
        testNumbers(100, NumberFormat.getCurrencyInstance(OUTPUT_RU), 1000000000000D, OUTPUT_RU, OUTPUT_RU, 1);
    }

    @Test
    void currencyDifferentLocale() throws IOException, ParseException {
        for (int i = 0; i < 50; ++i) {
            Locale locale = AVAILABLE_INPUT_LOCALES[i];
            testNumbers(5, NumberFormat.getCurrencyInstance(locale), 1000, locale, OUTPUT_EN, 1);
        }
    }

    @Test
    void oneDate() throws IOException, ParseException {
        testNumbers(1, DateFormat.getDateInstance(DateFormat.DEFAULT, OUTPUT_RU), 100, OUTPUT_RU, OUTPUT_RU, 2);
    }

    @Test
    void manyDates() throws IOException, ParseException {
        testNumbers(1000,  DateFormat.getDateInstance(DateFormat.DEFAULT, OUTPUT_RU), 10000D, OUTPUT_RU, OUTPUT_EN, 2);
    }

    @Test
    void dateDifferentLocale() throws IOException, ParseException {
        for (int i = 0; i < 50; ++i) {
            Locale locale = AVAILABLE_INPUT_LOCALES[i];
            testNumbers(5, DateFormat.getDateInstance(DateFormat.DEFAULT, locale), 10000D, locale, OUTPUT_EN, 2);
        }
    }

    @Test
    void speedTest() throws IOException, ParseException {
        String s = Files.readString(Path.of("solutions\\java-solutions\\info\\kgeorgiy\\ja\\shchetinin\\i18n\\test\\Sherlock Holmes"));
        long startTime = System.currentTimeMillis();
        test(s, OUTPUT_EN, OUTPUT_RU);
        long endTime = System.currentTimeMillis();
        Assertions.assertTrue(endTime - startTime < 10000);
    }

    private String getOutput(String s, Locale inLocale, Locale outLocale) throws IOException {
        Path tmpFile = Path.of("tmp/input.txt");
        Files.createDirectories(tmpFile.getParent());
        BufferedWriter bw = Files.newBufferedWriter(tmpFile, StandardCharsets.UTF_8);
        bw.write(s);
        bw.close();
        Path outputTmpFile = Path.of("tmp/output.txt");
        TextStatistics.evaluateStatistic(inLocale, outLocale, tmpFile, outputTmpFile);
        String output = Files.readString(outputTmpFile, StandardCharsets.UTF_8);
        Files.delete(tmpFile);
        Files.delete(outputTmpFile);
        Files.delete(tmpFile.getParent());
        return output;
    }


    private record StringData(int am, int diff, String min, String max, int shortest, int longest, double average) {
    }


    private record NumericData(int am, int diff, Double min, Double max, Double avg) {
    }

    private record DateData(int am, int diff, Long min, Long max, Long avg) {
    }

    private String getValueFirst(String s) {
        String[] split1 = s.split(":");
        String[] b = split1[1].trim().split(" ");
        String res = b[0];
        if (res.charAt(res.length() - 1) == '.') {
            return res.substring(0, res.length() - 1);
        }
        return res;
    }

    private String getValueAll(String s) {
        String[] split1 = s.split(":");
        String res = split1[1];
        if (res.charAt(res.length() - 1) == '.') {
            return res.substring(1, res.length() - 1);
        }
        return res.substring(1);
    }

    private String getDifferent(String s) {
        String[] split1 = s.split(":");
        String[] b = split1[1].trim().split(" ");
        return b[1].substring(1);
    }

    private StringData stringData(List<String> lines, Locale outLocale) throws ParseException {
        NumberFormat nf = NumberFormat.getNumberInstance(outLocale);
        return new StringData(
                nf.parse(getValueFirst(lines.get(0))).intValue(),
                nf.parse(getDifferent(lines.get(0))).intValue(),
                getValueAll(lines.get(1)),
                getValueAll(lines.get(2)),
                nf.parse(getValueFirst(lines.get(3))).intValue(),
                nf.parse(getValueFirst(lines.get(4))).intValue(),
                nf.parse(getValueAll(lines.get(5))).doubleValue()
        );
    }

    private NumericData numericStatistic(List<String> lines, NumberFormat nf, Locale outLocale) throws ParseException {
        NumberFormat df = NumberFormat.getNumberInstance(outLocale);
        return new NumericData(
                df.parse(getValueFirst(lines.get(0))).intValue(),
                df.parse(getDifferent(lines.get(0))).intValue(),
                nf.parse(getValueAll(lines.get(1))).doubleValue(),
                nf.parse(getValueAll(lines.get(2))).doubleValue(),
                nf.parse(getValueAll(lines.get(3))).doubleValue()
        );
    }

    private NumericData numberStatistic(List<String> lines, Locale outLocale) throws ParseException {
        return numericStatistic(lines, NumberFormat.getNumberInstance(outLocale), outLocale);
    }

    private NumericData currencyStatistic(List<String> lines, Locale outLocale) throws ParseException {
        return numericStatistic(lines, NumberFormat.getCurrencyInstance(outLocale), outLocale);
    }

    private Long getMilliseconds(Date date) {
        return date.toInstant().toEpochMilli();
    }

    private DateData dataStatistic(List<String> lines, Locale outLocale) throws ParseException {
        NumberFormat nf = NumberFormat.getNumberInstance(outLocale);
        DateFormat df = DateFormat.getDateInstance(DateFormat.DEFAULT, outLocale);
        return new DateData(
                nf.parse(getValueFirst(lines.get(0))).intValue(),
                nf.parse(getDifferent(lines.get(0))).intValue(),
                getMilliseconds(df.parse(getValueAll(lines.get(1)))),
                getMilliseconds(df.parse(getValueAll(lines.get(2)))),
                getMilliseconds(df.parse(getValueAll(lines.get(3))))
        );
    }

    private record FullInfo(StringData sentence, StringData word, NumericData number, NumericData currency,
                            DateData date) {
    }

    private FullInfo test(String s, Locale inLocale, Locale outLocale) throws IOException, ParseException {
        NumberFormat nf = NumberFormat.getNumberInstance(outLocale);
        String output = getOutput(s, inLocale, outLocale);
        List<String> lines = List.of(output.split("\n"));
        assertEquals("tmp\\input.txt", lines.get(0).split(":")[1].substring(1));
        FullInfo fi = new FullInfo(
                stringData(lines.subList(8, 14), outLocale),
                stringData(lines.subList(15, 21), outLocale),
                numberStatistic(lines.subList(22, 26), outLocale),
                currencyStatistic(lines.subList(27, 31), outLocale),
                dataStatistic(lines.subList(32, 36), outLocale)
        );

        assertEquals(fi.sentence.am, nf.parse(getValueAll(lines.get(2))).intValue());
        assertEquals(fi.word.am, nf.parse(getValueAll(lines.get(3))).intValue());
        assertEquals(fi.number.am, nf.parse(getValueAll(lines.get(4))).intValue());
        assertEquals(fi.currency.am, nf.parse(getValueAll(lines.get(5))).intValue());
        assertEquals(fi.date.am, nf.parse(getValueAll(lines.get(6))).intValue());

        return fi;
    }
}
