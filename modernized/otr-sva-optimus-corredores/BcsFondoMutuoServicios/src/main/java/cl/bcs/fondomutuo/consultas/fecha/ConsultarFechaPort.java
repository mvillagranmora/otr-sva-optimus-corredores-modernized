package cl.bcs.fondomutuo.consultas.fecha;

/** Legacy {@code ConsultarFechaSolicitudAporteRescate.logica(con, entrada)} without the connection: the unit of work is the ambient read-only transaction. */
public interface ConsultarFechaPort {

    ConsultarFechaContrato.Salida logica(ConsultarFechaContrato.Entrada entrada) throws Exception;
}
