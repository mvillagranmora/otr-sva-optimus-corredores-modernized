package cl.bcs.cartolas.jobs.domain;

import java.util.Objects;

/**
 * Describe qué pasos deben ejecutarse para un TipoProceso dado.
 *
 * Esta clase es pura (sin dependencias Spring) y es fácilmente testeable.
 * Oracle: ProcesoCartolasApp.procesar() — ramas if/else por tipoProceso.
 *
 * B-CAR-01: Routing de pasos por TipoProceso
 */
public record PasosEjecucion(
        boolean guardarParametros,
        boolean poblarNomina,
        boolean cargarDatos,
        boolean emitir
) {

    /**
     * B-CAR-01: Determina los pasos de ejecución para el tipo de proceso dado.
     *
     * Oracle: ProcesoCartolasApp.procesar() — switch por tipoProceso
     *   PROGRAMADO  → solo guardarParametros
     *   NORMAL      → guardarParametros + poblarNomina + cargarDatos + emitir
     *   RECURRENTE  → igual que NORMAL
     *   REPROCESO   → cargarDatos + emitir
     *   REEMPAQUETA → solo emitir
     *
     * @param tipoProceso valor de TipoProceso (nunca null)
     * @return pasos de ejecución correspondientes
     * @throws IllegalArgumentException si tipoProceso es desconocido
     */
    public static PasosEjecucion paraTipoProceso(String tipoProceso) {
        Objects.requireNonNull(tipoProceso, "tipoProceso no puede ser null");
        return switch (tipoProceso) {
            case TipoProceso.PROGRAMADO  -> new PasosEjecucion(true,  false, false, false);
            case TipoProceso.NORMAL,
                 TipoProceso.RECURRENTE  -> new PasosEjecucion(true,  true,  true,  true);
            case TipoProceso.REPROCESO   -> new PasosEjecucion(false, false, true,  true);
            case TipoProceso.REEMPAQUETA -> new PasosEjecucion(false, false, false, true);
            default -> throw new IllegalArgumentException(
                    "TipoProceso desconocido: " + tipoProceso);
        };
    }
}
