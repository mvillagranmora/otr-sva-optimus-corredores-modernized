package cl.bcs.cuentainversion.port.out;

import cl.bcs.cuentainversion.reportes.AgregarLogEntrada;

/**
 * Platform audit trail ({@code cl.bcs.plataforma.pla.AgregarLog}) as used by the reporting / commission services. The legacy call
 * returned an error counter that the callers ignore, and the real service swallowed its own failures (CTA-B-18), so the port
 * returns nothing. An entry without {@code p_Log} rows is not sent (legacy {@code AgregarLog.logica}).
 */
@FunctionalInterface
public interface AuditoriaReportes {

    /** Audit port for callers/tests that do not observe the audit trail: discards the entry. */
    AuditoriaReportes NINGUNA = entrada -> {
    };

    void agregarLog(AgregarLogEntrada entrada);
}
