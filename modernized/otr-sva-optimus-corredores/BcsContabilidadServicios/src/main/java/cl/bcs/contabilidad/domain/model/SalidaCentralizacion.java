package cl.bcs.contabilidad.domain.model;

import java.util.List;

/**
 * Output of a single BcsCentralizacion.centralizar() call.
 *
 * Derived from legacy BcsCentralizacion.SalidaCentralizacion.
 * P_OUT_ERRORESCONTADOR initialises to 0 in the legacy no-arg constructor.
 * P_OUT_RESULTADO, P_OUT_MENSAJES, and the movement/event tables are always
 * non-null lists (empty by default).
 */
public record SalidaCentralizacion(
        /** Error count; starts at 0.  Incremented once per failed item. */
        long                       erroresContador,
        List<String>               mensajes,
        List<ResultadoCentralizacion> resultados,
        List<TabMovimientoRef>     tabMovimientos,
        List<TabEventoRef>         tabEventos,
        List<TabDatoMovimientoRef> tabDatosMovimiento,
        List<TabDatoEventoRef>     tabDatosEvento
) {
    /** Canonical empty result; counter = 0. */
    public static SalidaCentralizacion empty() {
        return new SalidaCentralizacion(
                0L,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    // ---------------------------------------------------------------------------
    // Nested lightweight movement/event references  (replaces raw POJO tables)
    // ---------------------------------------------------------------------------

    public record TabMovimientoRef(
            Long   idTipoCentralizacion,
            Long   idMovimiento,
            String idTipoMovimiento,
            Long   idEmpresa,
            Long   numero,
            Long   linea,
            String fecha,
            String documentoIdentidad,
            Long   idPortafolio,
            Long   ejercicio,
            String periodo,
            java.math.BigDecimal monto,
            String idMoneda,
            String estado
    ) {}

    public record TabEventoRef(
            Long   idTipoCentralizacion,
            Long   idMovimiento,
            String idEvento,
            String nombre,
            java.math.BigDecimal montoEvento,
            String codigoMoneda
    ) {}

    public record TabDatoMovimientoRef(
            Long   idTipoCentralizacion,
            Long   idMovimiento,
            String tipoDato,
            String valorVarChar,
            java.math.BigDecimal valorDecimal
    ) {}

    public record TabDatoEventoRef(
            Long   idTipoCentralizacion,
            Long   idMovimiento,
            String tipoDato,
            String tipoEvento,
            String valorVarChar,
            java.math.BigDecimal valorDecimal
    ) {}
}
