package cl.bcs.apv.domain.port.out.traspasos;

/** Persistence port of {@code CrearOrdenesTraspaso} (legacy {@code CrearOrdenTraspaso}). */
public interface CrearOrdenesTraspasoRepositorio {

    /**
     * Inserts one order/traspaso relation; the row id comes from the database sequence inside the statement. Answers the affected row count
     * (legacy {@code resultado[0]}).
     */
    int insertar(Long idTraspaso, Long idOrden, Long idSolicitudAporteRescate);
}
