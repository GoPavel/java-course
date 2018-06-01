package ru.ifmo.rain.golovin.textstatistics;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;

public class TextStatistics {

    private static final String htmlType = "<!DOCTYPE html>";
    private static final String htmlHeadTag =  "<head>\n" +
                                        "    <meta charset=\"utf-8\">\n" +
                                        "    <title>Кодировка HTML-страницы</title>\n" +
                                        "</head>\n";


    private static String wrapTag(String tag, String str) {
        return "<" + tag + ">" + str + "</" + tag + ">";
    }


    /**
     * Calculate statistic of text.
     *<локаль текста> <локаль вывода> <файл с текстом> <файл отчета>
     * @param args :
     *             <ul>
     *             <li>Локаль текста</li>
     *             <li>Локаль вывода</li>
     *             <li>файл с текстом</li>
     *             <li>файл отчета</li>
     *             </ul>
     */
    public static void main(String[] args) {
        if (args == null || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.out.println("Excepted non-null argument.");
            return;
        }

        Locale inputLocale = getLocale(args[0]);
        Locale outputLocale = getLocale(args[1]);




    }

    static private Locale getLocale(String str) {
        String langAndCountry[] = str.split("_");
        return new Locale.Builder().setLanguage(langAndCountry[0]).setRegion(langAndCountry[1]).build();
    }

    static private void error(String message, Exception e) {
        System.out.println(message);
        System.out.println(e.getMessage());
    }
}
