package cn.wwl.radio.utils;

import java.util.Random;

public class RandomStringGenerator {
    private static final String CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final String UNICODE_CHAR = "0123456789ABCDEF";

    public static String generateRandomString(int maxChar) {
        if (maxChar == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        char[] chars = CHARS.toCharArray();
        for (int i = 0; i < maxChar;i++) {
            Random random = new Random();
            builder.append(chars[random.nextInt(CHARS.length() - 1)]);
        }
        return builder.toString();
    }

    public static String generateRandomUnicode(int maxChar) {
        if (maxChar == 0) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        char[] chars = UNICODE_CHAR.toCharArray();
        for (int i = 0; i < maxChar;i++) {
            builder.append("\\u");
            for (int n = 0; n < 4; n++) {
                Random random = new Random();
                builder.append(chars[random.nextInt(UNICODE_CHAR.length() - 1)]);
            }
        }
        return builder.toString();
    }
}
