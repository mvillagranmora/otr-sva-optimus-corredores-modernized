package cl.bcs.fondomutuo.comisionnn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/** {@link ComisionNnDatos} over the legacy SQL, verbatim (the INSERT of {@code FMM.SolicitudAporteRescate} is the one accepted write of this class). */
@Component
public class ComisionNnJdbc implements ComisionNnDatos {

    private static final String SQL_SELECT_SERIE_FONDO = SqlRecursos.modulo("ComunesSolicitudes_SelectSerieFondo.sql");
    private static final String SQL_SELECT_TIPO_MOVIMIENTO = SqlRecursos.modulo("Comunes_SelectTipoMovimientoFondoMutuo.sql");
    private static final String SQL_SELECT_MONEDA = SqlRecursos.modulo("ComunesSolicitudes_SelectMoneda.sql");
    private static final String SQL_INSERT_SOLICITUD = SqlRecursos.modulo("ComunesSolicitudes_InsertSolicitud.sql");
    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ComisionNnJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<SerieFondoSolicitudes> series(Long idEmpresa, List<String> idsSerie) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", idsSerie);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIE_FONDO, params, SerieFondoSolicitudes.class));
    }

    @Override
    public List<TipoMovimientoFondoMutuo> tiposMovimiento(List<String> idsTipoMovimiento) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTipoMovimientoFondoMutuo", idsTipoMovimiento);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_TIPO_MOVIMIENTO, params, TipoMovimientoFondoMutuo.class));
    }

    @Override
    public List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", idsSerie);
        params.put("IdRegimenTributario", idsRegimen);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class));
    }

    @Override
    public List<Moneda> monedas(List<String> idsMoneda) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdMoneda", idsMoneda);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_MONEDA, params, Moneda.class));
    }

    @Override
    public void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes) {
        cl.bcs.fondomutuo.kit.EscrituraConfirmada.escrituraLocalPendiente();   // M3: a later lock DDL commits it on HANA
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_SOLICITUD, solicitudes));
    }
}
