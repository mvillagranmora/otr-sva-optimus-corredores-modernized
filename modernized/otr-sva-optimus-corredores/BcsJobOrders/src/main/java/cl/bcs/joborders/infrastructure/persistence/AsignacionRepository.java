package cl.bcs.joborders.infrastructure.persistence;

import cl.bcs.joborders.domain.Asignacion;
import cl.bcs.joborders.domain.Orden;
import cl.bcs.joborders.domain.ParametrosDefault;
import cl.bcs.joborders.domain.Transaccion;
import cl.bcs.joborders.port.out.AsignacionDataPort;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Stub — todas las operaciones requieren HANA.
 * Se activan cuando el ambiente de QA HANA esté disponible.
 */
@Repository
public class AsignacionRepository implements AsignacionDataPort {

    @Override
    public String obtenerNombreLibroOrdenes(int id) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.LibroOrdenes");
    }

    @Override
    public String obtenerUltimaFechaLibroOrdenes(int id) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.LibroOrdenes");
    }

    @Override
    public String obtenerUltimaFechaEjecucionAsignacion(String idLibroOrdenes, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada");
    }

    @Override
    public int obtenerContadorUltimaEjecucion(String idLibroOrdenes, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::ObtenerUltimaFechaEjecucionTransaccionAsignada");
    }

    @Override
    public boolean prepararDatosAsignacion(long jobId, String idLibroOrdenes, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::PrepararAsignacionCronologica");
    }

    @Override
    public boolean prepararDatosAsignacionConFecha(long jobId, String idLibroOrdenes, String fechaCorte, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::PrepararAsignacionCronologicaConFecha");
    }

    @Override
    public List<Orden> obtenerOrdenes(long jobId) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.OrdenPaso (tabla temporal #orden_in_)");
    }

    @Override
    public List<Transaccion> obtenerTransacciones(long jobId) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.TransaccionPaso (tabla temporal #transaccion_in_)");
    }

    @Override
    public void bloquearDesbloquearOrdenes(String accion, long jobId, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::BloquearDesbloquearOrdenes");
    }

    @Override
    public void bloquearDesbloquearTransacciones(String accion, long jobId, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::BloquearDesbloquearTransacciones");
    }

    @Override
    public void anularMasivamente(long jobId, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::AnularAsignacionMasiva");
    }

    @Override
    public void eliminarMasivamente(String idLibroOrdenes) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.AsignacionPaso");
    }

    @Override
    public void asignarMasivamente(List<Asignacion> asignaciones, long jobId, ParametrosDefault param) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD.Servicios::ORD.AgregarAsignacion");
    }

    @Override
    public void crearTablaTemporalAsignaciones(long jobId, int idLibroOrdenes) {
        throw new UnsupportedOperationException("requires HANA: tabla temporal #created_asignacion_");
    }

    @Override
    public void eliminarTabla(String nombreTabla) {
        throw new UnsupportedOperationException("requires HANA: DROP TABLE " + nombreTabla);
    }

    @Override
    public void agregarLog(long jobId, String estado, String mensaje) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Plataforma.BD::PLA.Log");
    }

    @Override
    public boolean libroOrdenesActivo(String idLibroOrdenes) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Orden.BD::ORD.LibroOrdenes");
    }

    @Override
    public int existeAsignacionAutomaticaActiva(int idLibroOrdenes, String tipoPrograma) {
        throw new UnsupportedOperationException("requires HANA: BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas");
    }
}
