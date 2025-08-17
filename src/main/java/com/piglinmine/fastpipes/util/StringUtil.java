package com.piglinmine.fastpipes.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Random;

public class StringUtil {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##", DecimalFormatSymbols.getInstance(Locale.US));

    public static String formatNumber(float f) {
        return FORMATTER.format(f);
    }

    public static String formatNumber(int i) {
        return FORMATTER.format(i);
    }

    public static String randomString(Random random, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
} 