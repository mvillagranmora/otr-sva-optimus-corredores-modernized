package cl.bcs.fondomutuo.consultas.solicitudes;

/** Legacy {@code ConsultarSolicitudesFFMMWS.logica(con, entrada)} without the connection: the unit of work is the ambient read-only transaction. */
public interface ConsultarSolicitudesWsPort {

    ConsultarSolicitudesWsContrato.Salida logica(ConsultarSolicitudesWsContrato.Entrada entrada) throws Exception;
}
