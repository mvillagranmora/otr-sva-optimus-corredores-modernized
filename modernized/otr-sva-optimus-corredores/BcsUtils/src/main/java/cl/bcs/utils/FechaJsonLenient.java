package cl.bcs.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * UTIL-23: the date parsing shared by {@link GsonDateDeserializer} and {@link GsonTimestampDeserializer} (legacy
 * {@code GsonDateDeserializer.java:21-33}, {@code GsonTimestampDeserializer.java:20-32}).
 *
 * <p>Legacy behavior kept as pinned: the pattern is chosen by the text length only (10: {@code yyyy-MM-dd}, 19:
 * {@code yyyy-MM-dd HH:mm:ss}, anything else {@code yyyy-MM-dd HH:mm:ss.SSS}); {@link SimpleDateFormat} is LENIENT
 * and reads a prefix ({@code 2024-02-30} is 2024-03-01; {@code .123456} is 123 456 ms); the default zone applies; an
 * unparseable text gives {@code null}, not an exception (logged at DEBUG instead of the legacy {@code System.err}).
 */
final class FechaJsonLenient {

    private static final Logger LOG = LoggerFactory.getLogger(FechaJsonLenient.class);

    private FechaJsonLenient() {
    }

    /** The parsed date, or {@code null} when the text cannot be parsed. */
    static Date leer(String texto) {
        String patron = switch (texto.length()) {
            case 10 -> "yyyy-MM-dd";
            case 19 -> "yyyy-MM-dd HH:mm:ss";
            default -> "yyyy-MM-dd HH:mm:ss.SSS";
        };
        try {
            return new SimpleDateFormat(patron).parse(texto);
        } catch (ParseException e) {
            LOG.debug("Failed to parse Date: {}", e.getMessage());
            return null;
        }
    }
}
