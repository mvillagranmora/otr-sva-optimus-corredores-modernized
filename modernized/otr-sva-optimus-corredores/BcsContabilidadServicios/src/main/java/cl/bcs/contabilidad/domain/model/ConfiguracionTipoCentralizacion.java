package cl.bcs.contabilidad.domain.model;

/**
 * Configuration record for a single TipoCentralizacion.
 *
 * Derived from legacy CentralizaMultiSeleccion.ConfiguracionCentralizacionTipo.
 * The "Tipo" field drives dispatch:
 *   "J" = Java bean (Spring-managed service)
 *   "P" = stored procedure call
 * A null Tipo means the type is unconfigured and must be skipped with a warning.
 */
public record ConfiguracionTipoCentralizacion(
        Long   idTipoCentralizacion,
        String nombre,
        String vistaMovimiento,
        String vistaEvento,
        String vistaMovimientoDato,
        /** Bean name / class name (J) or stored-procedure name (P). */
        String servicio,
        /** "J" for Java, "P" for stored procedure, null when unconfigured. */
        String tipo,
        String idInforme,
        Long   idTipoCentralizacionOrigen,
        String requiereFechaConsulta,
        String informeAsociado,
        String llaveInformeAsociado
) {}
