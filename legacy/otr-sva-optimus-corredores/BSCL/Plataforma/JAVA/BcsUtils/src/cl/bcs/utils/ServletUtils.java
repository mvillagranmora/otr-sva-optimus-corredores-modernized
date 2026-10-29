package cl.bcs.utils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

public final class ServletUtils {
    private static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    public static BigDecimal getBigDecimal(
            final HttpServletRequest request,
            final String name) {
        final String str = request.getParameter(name);
        if (str == null) {
            return null;
        }

        return new BigDecimal(str);
    }

    public static Date getDate(
            final HttpServletRequest request,
            final String name) {
        final String str = request.getParameter(name);
        if (str == null) {
            return null;
        }

        try {
            return DF.parse(str);
        } catch (final ParseException e) {
            return null;
        }
    }

    public static int getInteger(
            final HttpServletRequest request,
            final String name,
            final int defval) {
        final String str = request.getParameter(name);
        if (str == null) {
            return defval;
        }

        return Integer.parseInt(str);
    }

    public static int getInteger(
            final String value,
            final int defval) {
        if (value == null) {
            return defval;
        }

        try {
            return Integer.valueOf(value);
        } catch (final NumberFormatException ex) {
            return defval;
        }
    }

    private ServletUtils() {
    }

}
