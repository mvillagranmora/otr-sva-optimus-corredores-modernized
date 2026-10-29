package cl.bcs.socionegocio.jobs.domain;

import java.time.LocalDate;

/**
 * Represents a client document that may trigger automatic blocking.
 * Ported from BloquearClientePorFechaVencimientoJob.Documentos (legacy).
 */
public record DocumentoCliente(
        Long idCliente,
        Long idAnexo,
        String idEstado,
        String bloquearAlVencer,
        LocalDate fechaVigencia,
        LocalDate fechaModificacion
) {

    /**
     * Returns a copy with the given fechaVigencia applied.
     */
    public DocumentoCliente withFechaVigencia(LocalDate newFechaVigencia) {
        return new DocumentoCliente(
                idCliente, idAnexo, idEstado, bloquearAlVencer,
                newFechaVigencia, fechaModificacion);
    }
}
