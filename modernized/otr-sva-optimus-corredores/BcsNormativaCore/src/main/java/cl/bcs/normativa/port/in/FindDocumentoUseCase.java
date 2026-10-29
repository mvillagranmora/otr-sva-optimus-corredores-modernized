package cl.bcs.normativa.port.in;

import cl.bcs.normativa.domain.Documento;

/**
 * Caso de uso: obtener y actualizar documentos normativos.
 */
public interface FindDocumentoUseCase {

    /**
     * Busca documento por id.
     * @throws IllegalArgumentException si no existe.
     */
    Documento findById(long id);

    /**
     * Actualiza el indicador EnProceso del documento.
     * @throws IllegalArgumentException si no existe.
     */
    Documento setEnProceso(long idDocumento, boolean enProceso);
}
