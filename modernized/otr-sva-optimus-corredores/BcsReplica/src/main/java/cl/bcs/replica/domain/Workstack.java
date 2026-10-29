package cl.bcs.replica.domain;

import java.time.Instant;

/**
 * Represents a WORKSTACK_T row.
 */
public record Workstack(
        Long idStack,
        String key,
        String table,
        String action,
        Integer status,
        Instant stamp,
        String parameters
) {
}
