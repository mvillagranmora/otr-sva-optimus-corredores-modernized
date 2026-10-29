package cl.bcs.contabilidad.domain.model;

/**
 * Immutable input for a single centralisation run.
 *
 * Derived from legacy BcsCentralizacion.EntradaCentralizacion.
 * Default value of simulaOperacion is "N" (no simulation), matching the
 * legacy no-arg constructor.
 */
public record EntradaCentralizacion(
        Long   idEmpresa,
        Long   idUsuario,
        String idModulo,
        Long   idTipoCentralizacion,
        String fechaContable,
        String idInforme,
        Long   ejercicio,
        /** "S" = simulate (skip DB writes);  "N" = real run. Legacy default: "N". */
        String simulaOperacion
) {
    /** Convenience factory that sets simulaOperacion to "N" (real run). */
    public static EntradaCentralizacion realRun(
            Long   idEmpresa,
            Long   idUsuario,
            String idModulo,
            Long   idTipoCentralizacion,
            String fechaContable,
            String idInforme,
            Long   ejercicio) {
        return new EntradaCentralizacion(
                idEmpresa, idUsuario, idModulo, idTipoCentralizacion,
                fechaContable, idInforme, ejercicio, "N");
    }

    /** Convenience factory that sets simulaOperacion to "S". */
    public static EntradaCentralizacion simulation(
            Long   idEmpresa,
            Long   idUsuario,
            String idModulo,
            Long   idTipoCentralizacion,
            String fechaContable,
            String idInforme,
            Long   ejercicio) {
        return new EntradaCentralizacion(
                idEmpresa, idUsuario, idModulo, idTipoCentralizacion,
                fechaContable, idInforme, ejercicio, "S");
    }
}
