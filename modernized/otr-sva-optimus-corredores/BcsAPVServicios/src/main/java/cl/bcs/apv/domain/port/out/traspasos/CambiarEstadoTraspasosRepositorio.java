package cl.bcs.apv.domain.port.out.traspasos;

import java.util.List;

/** Persistence port of {@code CambiarEstadoTraspasos} (legacy {@code CambiarEstadoTraspaso}). */
public interface CambiarEstadoTraspasosRepositorio {

    /** Legacy {@code EstadoTraspaso}: the parameters of the UPDATE (public fields named as the SQL parameters). */
    class EstadoTraspaso {
        public String IdEstado;
        public Long IdEmpresa;
        public Long IdTraspaso;
        public Long IdContratoSAAPV;
        public java.sql.Date CierreSolicitud;
    }

    /** Runs the UPDATE once per element of the list as a JDBC batch and answers the batch counts (legacy {@code SQL.batch}); SQLException is thrown as is. */
    int[] cambiarEstado(List<EstadoTraspaso> traspasos);
}
