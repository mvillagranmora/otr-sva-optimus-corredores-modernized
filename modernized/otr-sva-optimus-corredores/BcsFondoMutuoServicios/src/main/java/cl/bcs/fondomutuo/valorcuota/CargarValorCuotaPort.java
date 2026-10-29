package cl.bcs.fondomutuo.valorcuota;

/** Legacy {@code CargarValorCuota.logica(con, entrada)} without the connection: the unit of work is the ambient READ WRITE transaction. */
@FunctionalInterface
public interface CargarValorCuotaPort {

    CargarValorCuotaContrato.Salida logica(CargarValorCuotaContrato.Entrada entrada) throws Exception;
}
