package cl.bcs.contabilidad.domain.model.clienterelacionado;

import java.math.BigDecimal;

/** Pacto, termino or termino-cartera operation. Legacy {@code PactoTermino} (COCR:94-101). */
public record PactoTermino(
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
        String fechaInicio,
        String fechaTermino,
        BigDecimal montoFinal,
        BigDecimal montoInicial,
        BigDecimal perdida,
        BigDecimal ganancia) implements OperacionClienteRelacionado {
}
