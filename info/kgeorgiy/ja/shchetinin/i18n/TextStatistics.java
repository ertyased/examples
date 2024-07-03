package info.kgeorgiy.ja.shchetinin.i18n;

import info.kgeorgiy.ja.shchetinin.i18n.statistic.DateStatistic;
import info.kgeorgiy.ja.shchetinin.i18n.statistic.FormatNumberStatistic;
import info.kgeorgiy.ja.shchetinin.i18n.statistic.Result;
import info.kgeorgiy.ja.shchetinin.i18n.statistic.StringStatistic;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;


/**
 * A class for calculating various statistics on text, including sentences, words, numbers, currencies, and dates.
 */
public class TextStatistics {
    private static Locale getLocalFromString(String s) {
        String[] parts = s.split("_");
        return switch (parts.length) {
            case 1 -> Locale.of(parts[0]);
            case 2 -> Locale.of(parts[0], parts[1]);
            case 3 -> Locale.of(parts[0], parts[1], parts[2]);
            default -> null;
        };
    }

    /**
     * The main method to execute the text statistics evaluation.
     * Expects four arguments: the locale of the text, the locale for the output, the input file path, and the output file path.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length < 4) {
            System.out.println("Not enough arguments");
            return;
        }
        Locale textLocale = getLocalFromString(args[0]);
        Locale outLocale = getLocalFromString(args[1]);
        Path inputFile =  Path.of(args[2]);
        Path outputFile = Path.of(args[3]);
        evaluateStatistic(textLocale, outLocale, inputFile, outputFile);

    }

    /**
     * Evaluates statistics on the text from the specified input file and writes the results to the specified output file.
     * <p>
     * This method computes statistics such as the number of sentences, words, numbers, currencies, and dates,
     * and writes a detailed report to the output file.
     *
     * @param textLocale the locale of the input text
     * @param outLocale the locale for the output text
     * @param inputFile the path to the input text file
     * @param outputFile the path to the output text file
     */
    public static void evaluateStatistic(Locale textLocale, Locale outLocale, Path inputFile, Path outputFile) {
        String text;

        try {
            text = Files.readString(inputFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Unable to read file");
            return;
        }

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT,textLocale);
        StringStatistic sentences = new StringStatistic(text, BreakIterator.getSentenceInstance(textLocale), false);
        StringStatistic words = new StringStatistic(text, BreakIterator.getWordInstance(textLocale), true);
        NumberFormat numberFormat = NumberFormat.getNumberInstance(textLocale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(textLocale);
        FormatNumberStatistic numberStatistic = new FormatNumberStatistic(text, numberFormat);
        FormatNumberStatistic currencyStatistic = new FormatNumberStatistic(text, currencyFormat);
        DateStatistic dateStatistic = new DateStatistic(text, dateFormat);


        Result<String, Double> sentecesResult = sentences.compute();
        StringStatistic.MinMaxLength sentenceLength = sentences.getAdditionalInfo();
        Result<String, Double> wordResult = words.compute();
        StringStatistic.MinMaxLength wordLength = words.getAdditionalInfo();
        Result<Number, Double> numberResult = numberStatistic.compute();
        Result<Number, Double> currencyResult = currencyStatistic.compute();
        Result<Date, Long> dateResult = dateStatistic.compute();

        String file;
        if (outLocale.getLanguage().equals("ru") && outLocale.getCountry().equals("RU")) {
            file = "solutions\\properties\\ru.properties";
        } else {
            outLocale = Locale.of("en", "US");
            file = "solutions\\properties\\en.properties";
        }
        ResourceBundle bundle;
        try (FileInputStream input = new FileInputStream(file)) {
            bundle =  new PropertyResourceBundle(input);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to locate property file");
            return;
        } catch (IOException e) {
            System.out.println("Unable to read from property file");
            return;
        }

        String globalStatistic = new TranslateStringBuilder(bundle)
                .appendTranslate("analyzed.file").append(": " + inputFile).append("\n")
                .appendTranslate("global.statistic").append("\n")
                .append("\t").appendTranslate("amount.sentence").append(": ").append(sentecesResult.am()).append("\n")
                .append("\t").appendTranslate("amount.word").append(": ").append(wordResult.am()).append("\n")
                .append("\t").appendTranslate("amount.number").append(": ").append(numberResult.am()).append("\n")
                .append("\t").appendTranslate("amount.sum").append(": ").append(currencyResult.am()).append("\n")
                .append("\t").appendTranslate("amount.date").append(": ").append(dateResult.am()).append("\n").toString();



        String sentence = printStatisticString(sentecesResult, "sentence",  sentenceLength, bundle);
        String word = printStatisticString(wordResult, "word",  wordLength, bundle);
        String number = printStatisticNumber(numberResult, "number",  NumberFormat.getNumberInstance(outLocale), bundle);
        String sum = printStatisticNumber(currencyResult, "sum",  NumberFormat.getCurrencyInstance(outLocale), bundle);
        String date = printStatisticNumber(dateResult, "date",  DateFormat.getDateInstance(DateFormat.DEFAULT,outLocale), bundle);
        String finalResult = new StringBuilder(globalStatistic).append(sentence).append(word).
                append(number).append(sum).append(date).toString();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            bufferedWriter.write(finalResult);
        } catch (IOException e) {
            System.out.println("Unable to write to output file");
        }
    }


    private static <T, G> String  printStatistic(Result<T, G> res, String name, ResourceBundle bundle) {
        TranslateStringBuilder ans = new TranslateStringBuilder(bundle).appendTranslate("statistic." + name).append("\n\t")
                .appendTranslate("amount."+name).append(": ").append(res.am()).append(" (").append(res.unique() + " ");
        if (res.unique() == 1) {
            ans.appendTranslate("different");
        } else {
            ans.appendTranslate("different.m");
        }
        ans.append(")\n");
        return ans.toString();
    }

    private static <T, G> String printStatisticNumber(Result<T, G> res, String name, Format format, ResourceBundle bundle) {
        String resMin = format.format(0);
        String resMax = format.format(0);
        String resAvg = format.format(0);
        if (res.max() != null) {
            resMin = format.format(res.min());
            resMax = format.format(res.max());
            if (res.avg() instanceof Long) {
                Date date = Date.from(Instant.ofEpochMilli((Long) res.avg()));
                resAvg = format.format(date);
            } else {
                resAvg = format.format(res.avg());
            }
        }
        TranslateStringBuilder ans = new TranslateStringBuilder(bundle).append(printStatistic(res, name, bundle));
        ans.append("\t").appendTranslate("minimal." + name).append(": ").append(resMin).append(".\n");
        ans.append("\t").appendTranslate("maximum." + name).append(": ").append(resMax).append(".\n");
        ans.append("\t").appendTranslate("average." + name).append(": ").append(resAvg).append(".\n");
        return ans.toString();
    }

    private static String printStatisticString(Result<String, Double> res, String name, StringStatistic.MinMaxLength lens, ResourceBundle bundle) {
        TranslateStringBuilder ans = new TranslateStringBuilder(bundle).append(printStatistic(res, name, bundle));
        String resMin = "";
        String resMax = "";
        if (res.max() != null) {
            resMin = res.min();
            resMax = res.max();
        }
        ans.append("\t").appendTranslate("minimal." + name).append(": ").append(resMin.replace("\n", "")).append(".\n");
        ans.append("\t").appendTranslate("maximum." + name).append(": ").append(resMax.replace("\n", "")).append(".\n");
        if (lens.max() == null) {
            ans.append("\t").appendTranslate("minimal.length." + name).append(": 0").append(" (\"").append("\").\n");
            ans.append("\t").appendTranslate("maximal.length." + name).append(": 0").append(" (\"").append("\").\n");
        } else {
            ans.append("\t").appendTranslate("minimal.length." + name).append(": ").append(lens.min().length()).append(" (\"").
                    append(lens.min().replace("\n", "")).append("\").\n");
            ans.append("\t").appendTranslate("maximal.length." + name).append(": ").append(lens.max().length()).append(" (\"").
                    append(lens.max().replace("\n", "")).append("\").\n");
        }
        ans.append("\t").appendTranslate("average." + name).append(": ").append(res.avg()).append(".\n");
        return ans.toString();
    }


}
