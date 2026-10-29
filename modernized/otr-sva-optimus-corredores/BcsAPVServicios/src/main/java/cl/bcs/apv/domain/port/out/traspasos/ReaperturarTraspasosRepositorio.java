package cl.bcs.apv.domain.port.out.traspasos;

import java.util.List;

/** Persistence port of {@code ReaperturarTraspasos} (legacy {@code cl.bcs.APV.ReaperturarTraspaso}): the only read it makes. */
public interface ReaperturarTraspasosRepositorio {

    /** Legacy {@code EstadoDetalleTraspasoReapertura}: the select of the SQL does not list {@code IdEstado}, so the value is always null. */
    record EstadoDetalleTraspaso(String IdEstado) {
    }

    /** Legacy {@code IngresoRecaudacionTraspaso_SelectDetalleTraspasos.sql} for one traspaso. */
    List<EstadoDetalleTraspaso> detalleDelTraspaso(Long idTraspaso);
}
