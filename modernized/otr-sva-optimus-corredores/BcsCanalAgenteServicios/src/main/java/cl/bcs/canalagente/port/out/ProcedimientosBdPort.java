package cl.bcs.canalagente.port.out;

/**
 * SQLScript text of the procedures of {@code BD/Servicios}: {@code ConsultaAvanzadaClientes}, {@code ConsultarHistorialMovimientosCuentaInversion},
 * {@code ConsultarHistorialMovimientosCustodia}. The legacy Java never calls them; the text is ported verbatim and HANA only runs it.
 */
public interface ProcedimientosBdPort {

    String texto(String nombre);
}
