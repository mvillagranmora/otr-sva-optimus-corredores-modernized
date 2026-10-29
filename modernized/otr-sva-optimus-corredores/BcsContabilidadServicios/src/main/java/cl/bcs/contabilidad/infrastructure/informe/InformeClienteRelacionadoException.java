package cl.bcs.contabilidad.infrastructure.informe;

/**
 * The related-client report cannot be rendered from the data given (legacy GenerarInformeClienteRelacionado failed
 * with an exception in the same cases). The service reports it as UI_ERROR_GENERAR_INFORME.
 */
public class InformeClienteRelacionadoException extends RuntimeException {

    public InformeClienteRelacionadoException(String message) {
        super(message);
    }
}
