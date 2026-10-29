package cl.bcs.utils;

import java.io.Serial;

/**
 * UTIL-30: a text could not be read as a date by {@link DateUtil} (legacy {@code DateFormatException.java}).
 * Unchecked, as in the legacy; BcsNormativaCore catches it.
 */
public class DateFormatException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DateFormatException(String message) {
        super(message);
    }

    public DateFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
