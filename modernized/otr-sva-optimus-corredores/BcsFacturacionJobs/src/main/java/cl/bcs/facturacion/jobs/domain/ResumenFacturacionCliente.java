package cl.bcs.facturacion.jobs.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary of a billing document (comprobante) for a client.
 *
 * Adapted from legacy {@code cl.bcs.facturacion.jobs.pojo.ResumenFacturacionCliente}.
 * Fields are narrowed to those returned by the H2-compatible adapter query:
 * {@code SELECT ID_CLIENTE, ID_COMPROBANTE, MONTO_TOTAL, FECHA_COMPROBANTE FROM COMPROBANTES_T}.
 */
public record ResumenFacturacionCliente(
        Long idCliente,
        Long idComprobante,
        BigDecimal montoTotal,
        LocalDate fechaComprobante
) {}
