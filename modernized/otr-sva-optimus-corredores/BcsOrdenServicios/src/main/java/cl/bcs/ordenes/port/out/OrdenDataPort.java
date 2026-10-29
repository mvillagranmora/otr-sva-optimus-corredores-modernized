package cl.bcs.ordenes.port.out;

/** Puerto de salida — acceso a datos de órdenes en HANA. */
public interface OrdenDataPort {
    void registrarOrden(String idPortafolio, String idTipoOperacion);
    String consultarEstadoModulo(String idModulo);
}
