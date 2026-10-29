package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.domain.Programa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * Pure-logic service for individual job lifecycle decisions.
 *
 * All business-logic methods are static so they can be tested without
 * a Spring context. The @Service annotation on the class allows Spring
 * to autowire it where needed for non-static convenience.
 */
public final class JobManagerService {

    private static final Logger log = LoggerFactory.getLogger(JobManagerService.class);

    private JobManagerService() {}

    /**
     * Computes the delay in milliseconds a job should wait between
     * daemon cycles based on the "Minuto" field.
     *
     * @param minuto the Minuto field value from the agenda
     * @return delay in milliseconds (minimum 5000 ms)
     */
    public static long computeDelayMillis(String minuto) {
        Objects.requireNonNull(minuto, "minuto must not be null");
        if ("**".equalsIgnoreCase(minuto.trim())) {
            return 5_000L;
        }
        try {
            int minutes = Integer.parseInt(minuto.trim());
            if (minutes <= 0) {
                return 5_000L;
            }
            return (long) minutes * 60 * 1_000L;
        } catch (NumberFormatException e) {
            log.warn("Invalid minuto value '{}', defaulting to 5000 ms", minuto);
            return 5_000L;
        }
    }

    /**
     * Truncates a description to the HANA column width limit.
     *
     * @param description raw description
     * @return description truncated to at most 2000 characters
     */
    public static String truncateDescription(String description) {
        Objects.requireNonNull(description, "description must not be null");
        return description.length() > 2000 ? description.substring(0, 2000) : description;
    }

    /**
     * Determines if a program has exhausted all its retry attempts.
     *
     * @param programa the program
     * @return true if no more attempts are available
     */
    public static boolean isExhausted(Programa programa) {
        Objects.requireNonNull(programa, "programa must not be null");
        return programa.getIntento() >= programa.getMaximoIntentos();
    }

    /**
     * Normalizes a node or job-type name by stripping hyphens so that
     * "ORD-01" and "ORD01" are treated as equivalent.
     *
     * @param raw raw name possibly containing hyphens
     * @return normalized name without hyphens; empty string if blank
     */
    public static String normalizeName(String raw) {
        Objects.requireNonNull(raw, "raw name must not be null");
        String cleaned = raw.replace("-", "").trim();
        return cleaned.equalsIgnoreCase("*") ? "" : cleaned;
    }

    /**
     * Builds the result parameter map for a job execution from the
     * raw parameters map.
     *
     * @param raw     raw parameters map from the DB
     * @return safe parameter map (never null, values never null)
     */
    public static Map<String, String> sanitizeParams(Map<String, String> raw) {
        Objects.requireNonNull(raw, "raw params must not be null");
        Map<String, String> safe = new java.util.HashMap<>();
        raw.forEach((k, v) -> safe.put(
                k == null ? "" : k,
                v == null ? "" : v));
        return safe;
    }
}
