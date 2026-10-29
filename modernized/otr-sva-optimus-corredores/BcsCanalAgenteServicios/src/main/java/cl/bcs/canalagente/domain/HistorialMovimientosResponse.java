package cl.bcs.canalagente.domain;

import java.math.BigDecimal;
import java.util.List;

/**
 * Output for the consultar-historial-movimientos use case.
 *
 * Derived from ConsultarHistorialMovimientosCuentaInversion.SalidaConsultarHistorialMovimientosCuentaInversion.
 */
public record HistorialMovimientosResponse(
        BigDecimal saldoEfectivoInicial,
        BigDecimal saldoEfectivoFinal,
        List<MovimientoCuentaInversion> resultado,
        Long advertenciasContador,
        List<AdvertenciasDetalle> advertenciasDetalle,
        Long erroresContador,
        List<ErroresDetalle> erroresDetalle
) {
    /** Convenience factory for an empty success response. */
    public static HistorialMovimientosResponse empty() {
        return new HistorialMovimientosResponse(
                null, null,
                List.of(), 0L, List.of(), 0L, List.of()
        );
    }

    /** Convenience factory for a "cliente no encontrado" error response. */
    public static HistorialMovimientosResponse clienteNoEncontrado() {
        return new HistorialMovimientosResponse(
                null, null,
                List.of(), 0L, List.of(),
                1L,
                List.of(new ErroresDetalle("ERROR", "Cliente no encontrado."))
        );
    }
}
