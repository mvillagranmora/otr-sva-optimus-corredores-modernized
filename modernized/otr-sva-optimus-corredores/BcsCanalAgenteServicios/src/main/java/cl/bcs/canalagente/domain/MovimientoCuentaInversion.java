package cl.bcs.canalagente.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain model representing a single investment-account movement enriched with
 * comprobante (voucher) data.
 *
 * Derived from ConsultarHistorialMovimientosCuentaInversion.MovimientoCuentaInversion
 * in the legacy module.
 */
public record MovimientoCuentaInversion(
        Long idCargoAbonoMovimiento,
        String nombreCliente,
        Long idCliente,
        Long idPortafolio,
        LocalDate fechaMovimiento,
        LocalDate fechaLiquidacion,
        String idMoneda,
        String idConcepto,
        String nombreConcepto,
        BigDecimal cargo,
        BigDecimal abono,
        String folioComprobante,
        String idEstado,
        String nombreEstado,
        String nombreTipoComprobante,
        String nombreTipoProducto,
        String nombreTipoOperacion,
        String nombreTipoMovimiento,
        BigDecimal saldoSimulado,
        String codigoPortafolio,
        Long idComprobante,
        Long idRespuestaDTE,
        String respuestaPdfDTE,
        String documentoIdentidad,
        Long idMatrizAgrupador,
        String esADC
) {}
