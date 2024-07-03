package info.kgeorgiy.ja.shchetinin.googler;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;

public class Googler {
    final static Scanner scanner = new Scanner(System.in);
    final static Locale standardLocale = Locale.of("en", "US");

    private static ResourceBundle bundle;
    private static Locale getLocalFromString(String s) {
        String[] parts = s.split("_");
        return switch (parts.length) {
            case 1 -> Locale.of(parts[0]);
            case 2 -> Locale.of(parts[0], parts[1]);
            case 3 -> Locale.of(parts[0], parts[1], parts[2]);
            default -> standardLocale;
        };
    }

    private static void printTranslate(String s) {
        if (bundle.containsKey(s)) {
            System.out.println(bundle.getString(s));
            return;
        }
        System.out.println(s);
    }

    // Locale given as a parametr, if no locale given it chooses standardLocale
    // Я не успел написать парсер для результатов запросов :(. Пожалуйста не бейте.
    public static void main(String[] args) {
        Locale locale = standardLocale;
        if (args.length >= 1) {
            locale = getLocalFromString(args[0]);
        }
        if (locale.getCountry().equals("ru")) {
            bundle = ResourceBundle.getBundle("ruGoogler");
        } else {
            locale = standardLocale;
            bundle = ResourceBundle.getBundle("enGoogler");
        }
        NumberFormat nf = NumberFormat.getNumberInstance(locale);
        GoogleRequest currentRequest = null;
        while (true) {
            String req = scanner.nextLine();
            String[] strings = req.split(" ", 2);
            String task = strings[0];
            try {
                if (task.equals("quit")) {
                    break;
                } else if (task.equals("request")) {
                    if (strings.length < 2) {
                        printTranslate("not.enough.arguments");
                        continue;
                    }
                    currentRequest = new GoogleRequest(strings[1]);
                    System.out.println(currentRequest.goToPage(0).toString());
                } else if (task.equals("next")) {
                    if (currentRequest == null) {
                        printTranslate("no.current.request");
                        continue;
                    }
                    System.out.println(currentRequest.goToPage(currentRequest.getPageNumber() + 1).toString());
                } else if (task.equals("prev")) {
                    if (currentRequest == null) {
                        printTranslate("no.current.request");
                        continue;
                    }
                    if (currentRequest.getPageNumber() == 0) {
                        printTranslate("no.previous.in.request");
                        continue;
                    }
                    System.out.println(currentRequest.goToPage(currentRequest.getPageNumber() - 1).toString());
                } else if (task.equals("go")) {
                    if (strings.length < 2) {
                        printTranslate("not.enough.arguments");
                        continue;
                    }
                    int numPage = 0;
                    try {
                        numPage = nf.parse(strings[1]).intValue();
                    } catch (ParseException e) {
                        printTranslate("unable.parse");
                        continue;
                    }
                    if (numPage < 0) {
                        printTranslate("no.such.page");
                        continue;
                    }
                    System.out.println(currentRequest.goToPage(numPage).toString());
                } else {
                    printTranslate("no.such.command");
                }
            } catch (IOException e) {
                printTranslate("error.receiving.data");
            }
        }
    }
}
