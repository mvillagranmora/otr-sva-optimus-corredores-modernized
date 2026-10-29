package cl.bcs.fondomutuo.normal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/** {@link NormalDatos} over the legacy SQL, verbatim (all SELECT but the INSERT of {@code FMM.SolicitudAporteRescate}, the accepted write). */
@Component
public class NormalJdbc implements NormalDatos {

    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
    private static final String SQL_SELECT_INDICADORES = SqlRecursos.modulo("ComunesSolicitudes_SelectIndicadoresPortafolio.sql");
    private static final String SQL_SELECT_PORTAFOLIO_APV = SqlRecursos.modulo("ComunesSolicitudes_SelectPortafolioAPV.sql");
    private static final String SQL_SELECT_CONFIGURACION = SqlRecursos.modulo("ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
    private static final String SQL_SELECT_TRASPASO_APV = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_SelectConsultaTraspasoAPV.sql");
    private static final String SQL_SELECT_COUNT_CUSTODIA = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_SelectCountCustodia.sql");
    private static final String SQL_SELECT_OPERA_SALDO_NEGATIVO = SqlRecursos.modulo("CrearSolicitudAporteRescateNormal_SelectParametroOperaConSaldoNegativo.sql");
    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("CertificarSolicitudAporteRescate_SelectSolicitudes.sql");
    private static final String SQL_INSERT_SOLICITUD = SqlRecursos.modulo("ComunesSolicitudes_InsertSolicitud.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public NormalJdbc(ConexionUnidadDeTrabajo conexion) {
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
    public List<PortafolioAPV> portafoliosApv(List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        return consultar(SQL_SELECT_PORTAFOLIO_APV, params, PortafolioAPV.class);
    }

    @Override
    public List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdRegimenFondoMutuo", idsRegimen);
        return consultar(SQL_SELECT_CONFIGURACION, params, ConfiguracionPropositoPortafolio.class);
    }

    @Override
    public List<TraspasoAPV> traspasosApv(List<Long> foliosContrato) {
        Map<String, Object> params = new HashMap<>();
        params.put("FolioContrato", foliosContrato);
        return consultar(SQL_SELECT_TRASPASO_APV, params, TraspasoAPV.class);
    }

    @Override
    public List<SalidaTraspasoTotal> custodiaTraspasoTotal(List<String> idsSerie, List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerie);
        params.put("IdPortafolio", idsPortafolio);
        return consultar(SQL_SELECT_COUNT_CUSTODIA, params, SalidaTraspasoTotal.class);
    }

    @Override
    public List<ParametroOperaCSN> parametrosOperaConSaldoNegativo(List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        return consultar(SQL_SELECT_OPERA_SALDO_NEGATIVO, params, ParametroOperaCSN.class);
    }

    @Override
    public List<SolicitudAporteRescate> solicitudes(Long idEmpresa, List<Long> idsSolicitud) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSolicitudAporteRescate", idsSolicitud);
        return consultar(SQL_SELECT_SOLICITUDES, params, SolicitudAporteRescate.class);
    }

    @Override
    public void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes) {
        cl.bcs.fondomutuo.kit.EscrituraConfirmada.escrituraLocalPendiente();   // M3: the lock DDL commits it on HANA
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_SOLICITUD, solicitudes));
    }
}
