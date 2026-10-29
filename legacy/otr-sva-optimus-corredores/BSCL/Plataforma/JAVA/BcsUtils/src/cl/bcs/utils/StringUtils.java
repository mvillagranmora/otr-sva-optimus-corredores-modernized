package cl.bcs.utils;

import java.util.List;

public final class StringUtils {
    public static String join(
            final String separator,
            final List<String> items) {
        String ret = "";
        for (final String item : items) {
            if (!ret.isEmpty()) {
                ret += separator;
            }

            ret += item;
        }

        return ret;
    }

    private StringUtils() {
    }

    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
