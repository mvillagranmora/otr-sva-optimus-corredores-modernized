package cl.bcs.fondomutuo.consultas.detalle;

/** Legacy {@code ConsultarDetalleSolicitud.logica(con, entrada)} without the connection: the unit of work is the ambient read-only transaction. */
public interface ConsultarDetallePort {

    ConsultarDetalleContrato.Salida logica(ConsultarDetalleContrato.Entrada entrada) throws Exception;
}
