package cl.bcs.contabilidad.domain.model.clienterelacionado;

/**
 * One operation of a related client, as read by legacy {@code ConsultarOperacionesClienteRelacionado}
 * ({@code OperacionesClienteRelacionadoBase}, ConsultarOperacionesClienteRelacionado.java:47-58).
 * There are three shapes: comprobantes, dollar operations (spot/forward) and pactos/terminos.
 */
public sealed interface OperacionClienteRelacionado permits Comprobante, OperacionDolar, PactoTermino {

    Long folioOperacion();

    /** yyyy-MM-dd as produced by the SQL ({@code TO_VARCHAR(.., 'YYYY-MM-DD')}). */
    String fechaOperacion();

    String nombre();

    String portafolio();

    String documentoIdentidad();

    String idTipoInstrumento();

    String tipoDocumento();

    String compraVenta();

    String idTipoOperacion();

    String idClasificacionTipoInstrumento();
}
