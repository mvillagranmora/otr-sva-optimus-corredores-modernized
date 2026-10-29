package cl.bcs.apv.domain.port.out.traspasos;

/** Persistence port of {@code ModificarOrigenDestinoTransicion} (legacy {@code ModificarOrigenDestino}). */
public interface ModificarOrigenDestinoRepositorio {

    /** Legacy {@code OrigenDestino}: the parameters of the UPDATE (public fields named as the SQL parameters). */
    class OrigenDestino {
        public Long IdOrigenDestinoTransicion;
        public String Operacion;
        public Long OrigenCtaBco;
        public Long SocioNegocioCtaBco;
        public Long DestinoCtaBco;
        public String FechaModificacion;
        public Long IdUsuarioModificador;
        public String IdEstado;
    }

    /** Runs the UPDATE as a one-row JDBC batch and answers the batch counts (legacy {@code SQL.batch}). */
    int[] modificar(OrigenDestino fila);
}
