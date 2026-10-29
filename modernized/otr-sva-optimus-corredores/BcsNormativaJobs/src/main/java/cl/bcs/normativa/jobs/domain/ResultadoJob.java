package cl.bcs.normativa.jobs.domain;

/**
 * Result of a normativa job execution.
 *
 * B-NJB-03: jobs return a success/failure flag plus an optional message.
 */
public record ResultadoJob(boolean exitoso, String mensaje) {

    public static ResultadoJob ok(String mensaje) {
        return new ResultadoJob(true, mensaje);
    }

    public static ResultadoJob error(String mensaje) {
        return new ResultadoJob(false, mensaje);
    }
}
