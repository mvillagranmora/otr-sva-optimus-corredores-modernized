package cl.bcs.utils;

import java.util.List;

/**
 * UTIL-10 / UTIL-11: string helpers (legacy {@code StringUtils.java:6-26}).
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * UTIL-10: joins {@code items} with {@code separator}. Legacy quirk kept: the separator is only written once the
     * result is non-empty, so leading empty items vanish ({@code ["", "b"] -> "b"}); {@code null} items and a
     * {@code null} separator are written as the text {@code "null"}; a {@code null} list throws NPE.
     */
    public static String join(String separator, List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (!sb.isEmpty()) {
                sb.append(separator);
            }
            sb.append(item);
        }
        return sb.toString();
    }

    /** UTIL-11: {@code null} or empty after {@link String#trim()} (so NUL is blank, NBSP is not). */
    public static boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
