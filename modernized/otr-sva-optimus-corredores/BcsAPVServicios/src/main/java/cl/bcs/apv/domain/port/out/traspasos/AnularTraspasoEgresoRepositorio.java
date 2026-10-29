package cl.bcs.apv.domain.port.out.traspasos;

import java.util.List;

/** Persistence port of {@code AnularTraspasoEgreso}: the three batches of the legacy class, one execution per traspaso id. */
public interface AnularTraspasoEgresoRepositorio {

    /** Marks the traspasos as ANU. */
    void anularTraspasos(List<Long> idsTraspaso, Long idTrabajo);

    /** Marks the origin detail of the traspasos as ANU. */
    void anularDetalleTraspasos(List<Long> idsTraspaso, Long idTrabajo);

    /** Sets back to VIG the TRA contracts of the traspasos. */
    void reactivarContratos(List<Long> idsTraspaso, Long idTrabajo);
}
