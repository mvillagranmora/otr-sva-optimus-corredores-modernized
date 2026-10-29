package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.math.BigDecimal;

/** Spot or forward dollar operation. Legacy {@code OperacionDolar} (COCR:77-80). */
public record OperacionDolar(
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
        BigDecimal montoCompra,
        BigDecimal montoVenta) implements OperacionClienteRelacionado {

    /** Purchase and sale amounts negated; nulls stay null (COCR:344-348). */
    public OperacionDolar negado() {
        return new OperacionDolar(folioOperacion, fechaOperacion, nombre, portafolio, documentoIdentidad,
                idTipoInstrumento, tipoDocumento, compraVenta, idTipoOperacion, idClasificacionTipoInstrumento,
                Comprobante.negar(montoCompra), Comprobante.negar(montoVenta));
    }
}
