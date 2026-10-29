package cl.bcs.canalagente.domain;

/**
 * Input for the consultar-historial-movimientos use case.
 *
 * Derived from ConsultarHistorialMovimientosCuentaInversion.EntradaConsultarHistorialMovimientosCuentaInversion.
 */
public record HistorialMovimientosRequest(
        Long idEmpresa,
        Long idUsuario,
        String idModulo,
        Long idPortafolio,
        String fechaDesde,
        String fechaHasta,
        String idMoneda,
        Long idMatrizAgrupador,
        String esADC
) {}
