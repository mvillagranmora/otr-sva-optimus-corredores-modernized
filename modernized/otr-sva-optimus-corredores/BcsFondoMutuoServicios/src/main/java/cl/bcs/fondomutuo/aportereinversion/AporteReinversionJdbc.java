package cl.bcs.fondomutuo.aportereinversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.aportereinversion.AporteReinversionContrato.ConsultaRegimenReinversion;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.requisitos.SqlLote;
import cl.bcs.fondomutuo.rescatereinversion.RreDatos.ReinversionSerieFondoDestino;

/** {@link AporteReinversionDatos} over the legacy SQL, verbatim (SELECTs, the INSERT of {@code FMM.SolicitudAporteRescate} and the UPDATE of the final reinversion date). */
@Component
public class AporteReinversionJdbc implements AporteReinversionDatos {

    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
    private static final String SQL_SELECT_INDICADORES = SqlRecursos.modulo("ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
    private static final String SQL_SELECT_INDICADORES_FFMM = SqlRecursos.modulo("ComunesSolicitudes_SelectIndicadoresPortafolioFFMM.sql");
    private static final String SQL_SELECT_CONFIGURACION = SqlRecursos.modulo("ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
    private static final String SQL_SELECT_REGIMEN_REINVERSION = SqlRecursos.modulo("CrearSolicitudAporteReinversion_SelectRegimenDestino.sql");
    private static final String SQL_SELECT_REINVERSION_DESTINO = SqlRecursos.modulo("CrearSolicitudAporteReinversion_SelectReinversionSerieFondoDestino.sql");
    private static final String SQL_UPDATE_FECHA_FINAL = SqlRecursos.modulo("CrearSolicitudAporteReinversion_UpdateFechaFinalReinversion.sql");
    private static final String SQL_INSERT_SOLICITUD = SqlRecursos.modulo("ComunesSolicitudes_InsertSolicitud.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public AporteReinversionJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    private <T> List<T> consultar(String sql, Map<String, Object> params, Class<T> tipo) {
        return conexion.usar(con -> SqlNombrado.consultar(con, sql, params, tipo));
    }

    @Override
    public List<RegimenSerieDeFondo> regimenes(Long idEmpresa, List<String> idsSerie, List<String> idsRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", idsSerie);
        params.put("IdRegimenTributario", idsRegimen);
        return consultar(SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
    }

    @Override
    public List<IndicadoresPortafolio> indicadoresPortafolio(List<Long> idsPortafolio, String codigoTipoIndicador) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        params.put("CodigoTipoIndicador", codigoTipoIndicador);
        return consultar(SQL_SELECT_INDICADORES, params, IndicadoresPortafolio.class);
    }

    @Override
    public List<IndicadoresPortafolioFondoMutuo> indicadoresPortafolioFondoMutuo(List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        return consultar(SQL_SELECT_INDICADORES_FFMM, params, IndicadoresPortafolioFondoMutuo.class);
    }

    @Override
    public List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdRegimenFondoMutuo", idsRegimen);
        return consultar(SQL_SELECT_CONFIGURACION, params, ConfiguracionPropositoPortafolio.class);
    }

    @Override
    public List<ConsultaRegimenReinversion> regimenesDeLosRescates(List<Long> idsRescate) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSolicitudAporteRescate", idsRescate);
        return consultar(SQL_SELECT_REGIMEN_REINVERSION, params, ConsultaRegimenReinversion.class);
    }

    @Override
    public List<ReinversionSerieFondoDestino> reinversionesAActualizar(List<Long> idsRescate) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSolicitudAporteRescate", idsRescate);
        return consultar(SQL_SELECT_REINVERSION_DESTINO, params, ReinversionSerieFondoDestino.class);
    }

    @Override
    public void actualizarFechaFinalReinversion(List<ReinversionSerieFondoDestino> filas) {
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_UPDATE_FECHA_FINAL, filas));
    }

    @Override
    public void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes) {
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_SOLICITUD, solicitudes));
    }
}
