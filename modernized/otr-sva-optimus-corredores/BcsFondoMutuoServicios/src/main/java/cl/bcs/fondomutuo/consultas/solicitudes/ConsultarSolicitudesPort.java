package cl.bcs.fondomutuo.consultas.solicitudes;

/** Legacy {@code ConsultarSolicitudesFFMM.logica(con, entrada)} without the connection: the unit of work is the ambient read-only transaction. */
public interface ConsultarSolicitudesPort {

    ConsultarSolicitudesContrato.Salida logica(ConsultarSolicitudesContrato.Entrada entrada) throws Exception;
}
