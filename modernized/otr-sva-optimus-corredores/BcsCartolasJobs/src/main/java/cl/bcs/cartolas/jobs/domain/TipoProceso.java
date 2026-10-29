package cl.bcs.cartolas.jobs.domain;

/**
 * Tipos de proceso de cartolas.
 *
 * Oracle: cl.bcs.normativa.cartolas.entidades.TipoProceso
 *
 * Determina qué pasos se ejecutan en ProcesoCartolasApp.procesar():
 *   PROGRAMADO  → solo guarda parámetros
 *   NORMAL      → guarda parámetros + pobla nómina + carga datos + emite
 *   RECURRENTE  → igual que NORMAL
 *   REPROCESO   → carga datos + emite (sin repoblar nómina ni parámetros)
 *   REEMPAQUETA → solo emite (re-empaqueta sin recargar datos)
 */
public final class TipoProceso {

    /** Solo guarda parámetros de programación. */
    public static final String PROGRAMADO  = "PROGRAMADO";

    /** Flujo completo: parámetros + nómina + carga + emisión. */
    public static final String NORMAL      = "NORMAL";

    /** Igual que NORMAL, para procesos recurrentes. */
    public static final String RECURRENTE  = "RECURRENTE";

    /** Vuelve a cargar datos y emite sin repoblar la nómina. */
    public static final String REPROCESO   = "REPROCESO";

    /** Solo re-empaqueta y emite sin recargar datos. */
    public static final String REEMPAQUETA = "REEMPAQUETA";

    private TipoProceso() {
    }
}
