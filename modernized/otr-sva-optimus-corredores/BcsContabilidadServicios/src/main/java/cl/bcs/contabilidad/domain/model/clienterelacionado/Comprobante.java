package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.math.BigDecimal;

/** Invoice-like operation (a comprobante or a prestamo/emprestito row). Legacy {@code Comprobante} (COCR:82-92). */
public record Comprobante(
        Long folioOperacion,
        String fechaOperacion,
        String nombre,
        String portafolio,
        String documentoIdentidad,
        String idTipoInstrumento,
        String tipoDocumento,
        String compraVenta,
        String idTipoOperacion,
        String idClasificacionTipoInstrumento,
        String idMoneda,
        BigDecimal montoOperado,
        BigDecimal montoTotal,
        BigDecimal montoCobro,
        BigDecimal totalComision,
        BigDecimal totalGasto,
        BigDecimal totalImpto,
        BigDecimal totalRetencion,
        BigDecimal totalDerecho) implements OperacionClienteRelacionado {

    /** Every amount negated; nulls stay null (COCR:334-341, 358-360). */
    public Comprobante negado() {
        return new Comprobante(folioOperacion, fechaOperacion, nombre, portafolio, documentoIdentidad, idTipoInstrumento,
                tipoDocumento, compraVenta, idTipoOperacion, idClasificacionTipoInstrumento, idMoneda,
                negar(montoOperado), negar(montoTotal), negar(montoCobro), negar(totalComision), negar(totalGasto),
                negar(totalImpto), negar(totalRetencion), negar(totalDerecho));
    }

    static BigDecimal negar(BigDecimal v) {
        return v == null ? null : v.negate();
    }
}
