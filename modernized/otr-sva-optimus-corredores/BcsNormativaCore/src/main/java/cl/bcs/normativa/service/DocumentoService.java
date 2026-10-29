package cl.bcs.normativa.service;

import cl.bcs.normativa.domain.Documento;
import cl.bcs.normativa.port.in.FindDocumentoUseCase;
import cl.bcs.normativa.port.out.DocumentoPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * Servicio de documentos normativos.
 * Migrado desde legacy cl.bcs.normativa.dal.DocumentoDAO.
 */
@Service
public class DocumentoService implements FindDocumentoUseCase {

    private final DocumentoPort documentoPort;

    public DocumentoService(DocumentoPort documentoPort) {
        this.documentoPort = documentoPort;
    }

    @Override
    public Documento findById(long id) {
        return documentoPort.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado id: " + id));
    }

    @Override
    @Transactional
    public Documento setEnProceso(long idDocumento, boolean enProceso) {
        Objects.requireNonNull(documentoPort.buscarPorId(idDocumento)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado id: " + idDocumento)));
        documentoPort.setEnProceso(idDocumento, enProceso);
        return documentoPort.buscarPorId(idDocumento)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado id: " + idDocumento));
    }
}
