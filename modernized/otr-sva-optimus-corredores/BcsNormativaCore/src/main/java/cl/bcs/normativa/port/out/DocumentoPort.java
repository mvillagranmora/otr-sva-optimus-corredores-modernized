package cl.bcs.normativa.port.out;

import cl.bcs.normativa.domain.Documento;

import java.util.Optional;

/**
 * Puerto de salida para acceso a datos de Documento.
 */
public interface DocumentoPort {

    /** Busca documento por id. */
    Optional<Documento> buscarPorId(long id);

    /** Actualiza el indicador EnProceso y la fecha de inicio si enProceso=true. */
    void setEnProceso(long idDocumento, boolean enProceso);
}
