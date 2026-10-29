package cl.bcs.utils;

import java.io.Serial;

/**
 * UTIL-53: an input and output path that must differ are the same (legacy {@code BadFilePathException.java}).
 */
public class BadFilePathException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BadFilePathException(String message) {
        super(message);
    }
}
