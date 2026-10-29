package cl.bcs.joborders.port.out;

import cl.bcs.joborders.domain.Asignacion;
import cl.bcs.joborders.domain.Orden;
import cl.bcs.joborders.domain.ParametrosDefault;
import cl.bcs.joborders.domain.Transaccion;

import java.util.List;

/**
 * Puerto de acceso a datos HANA para asignación cronológica.
 * Réplica estrecha de ConnectorsOrders.java — un método por operación HANA.
 */
public interface AsignacionDataPort {

    String obtenerNombreLibroOrdenes(int idLibroOrdenes);

    String obtenerUltimaFechaLibroOrdenes(int idLibroOrdenes);

    String obtenerUltimaFechaEjecucionAsignacion(String idLibroOrdenes, ParametrosDefault param);

    int obtenerContadorUltimaEjecucion(String idLibroOrdenes, ParametrosDefault param);

    boolean prepararDatosAsignacion(long jobId, String idLibroOrdenes, ParametrosDefault param);

    boolean prepararDatosAsignacionConFecha(long jobId, String idLibroOrdenes, String fechaCorte, ParametrosDefault param);

    List<Orden> obtenerOrdenes(long jobId);

    List<Transaccion> obtenerTransacciones(long jobId);

    void bloquearDesbloquearOrdenes(String accion, long jobId, ParametrosDefault param);

    void bloquearDesbloquearTransacciones(String accion, long jobId, ParametrosDefault param);

    void anularMasivamente(long jobId, ParametrosDefault param);

    void eliminarMasivamente(String idLibroOrdenes);

    void asignarMasivamente(List<Asignacion> asignaciones, long jobId, ParametrosDefault param);

    void crearTablaTemporalAsignaciones(long jobId, int idLibroOrdenes);

    void eliminarTabla(String nombreTabla);

    void agregarLog(long jobId, String estado, String mensaje);

    boolean libroOrdenesActivo(String idLibroOrdenes);

    int existeAsignacionAutomaticaActiva(int idLibroOrdenes, String tipoPrograma);
}
