package cl.bcs.apv.domain.port.out.contratos;

import java.util.List;

/** Persistence of the subscription folio counter per channel (legacy ObtenerFolioSuscripcion SQL). */
public interface FolioSuscripcionRepositorio {

    /** Row of {@code SCS.FolioSuscripcion}. */
    record FolioFila(String Canal, Long Folio) {
    }

    List<FolioFila> seleccionarPorCanal(String canal);

    void insertar(String canal, Long folio);
}
