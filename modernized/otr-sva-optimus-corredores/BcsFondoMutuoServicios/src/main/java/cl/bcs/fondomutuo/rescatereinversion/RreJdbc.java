package cl.bcs.fondomutuo.rescatereinversion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.kit.EscrituraConfirmada;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/** {@link RreDatos} over the legacy SQL, verbatim (five SELECT, the INSERT of the destination rows and the INSERT of the requests: the accepted writes). */
@Component
public class RreJdbc implements RreDatos {

    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ComunesSolicitudes_SelectRegimenSerieDeFondo.sql");
    private static final String SQL_SELECT_REGIMEN_MANDATO = SqlRecursos.modulo("ComunesSolicitudes_SelectRegimenSerieDeFondoMandato.sql");
    private static final String SQL_CONFIGURACION_REGIMEN = SqlRecursos.modulo("ComunesSolicitudes_SelectConfiguracionRegimenFondoMutuo.sql");
    private static final String SQL_SELECT_CONFIGURACION_PROPOSITO = SqlRecursos.modulo("ComunesSolicitudes_SelectConfiguracionPropositoPortafolio.sql");
    private static final String SQL_SELECT_PORTAFOLIO_APV = SqlRecursos.modulo("ComunesSolicitudes_SelectPortafolioAPV.sql");
    private static final String SQL_INSERT_REINVERSION_SOLICITUD = SqlRecursos.modulo("ComunesSolicitudes_InsertReinversionSolicitud.sql");
    private static final String SQL_INSERT_SOLICITUD = SqlRecursos.modulo("ComunesSolicitudes_InsertSolicitud.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public RreJdbc(ConexionUnidadDeTrabajo conexion) {
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
    public List<RegimenMandato> regimenesMandato(Long idEmpresa, List<String> idsSerie) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", idsSerie);
        return consultar(SQL_SELECT_REGIMEN_MANDATO, params, RegimenMandato.class);
    }

    @Override
    public List<ConfiguracionRegimenFondoMutuo> configuracionRegimen(List<String> idsRegimenOrigen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdRegimenFondoMutuoOrigen", idsRegimenOrigen);
        return consultar(SQL_CONFIGURACION_REGIMEN, params, ConfiguracionRegimenFondoMutuo.class);
    }

    @Override
    public List<ConfiguracionPropositoPortafolio> configuracionProposito(List<String> idsRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdRegimenFondoMutuo", idsRegimen);
        return consultar(SQL_SELECT_CONFIGURACION_PROPOSITO, params, ConfiguracionPropositoPortafolio.class);
    }

    @Override
    public List<PortafolioAPV> portafoliosApv(List<Long> idsPortafolio) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPortafolio", idsPortafolio);
        return consultar(SQL_SELECT_PORTAFOLIO_APV, params, PortafolioAPV.class);
    }

    @Override
    public void insertarReinversion(List<ReinversionSerieFondoDestino> filas) {
        EscrituraConfirmada.escrituraLocalPendiente();   // M3: the lock DDL commits it on HANA
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_REINVERSION_SOLICITUD, filas));
    }

    @Override
    public void insertarSolicitudes(List<SolicitudAporteRescate> solicitudes) {
        EscrituraConfirmada.escrituraLocalPendiente();
        conexion.usar(con -> SqlLote.ejecutar(con, SQL_INSERT_SOLICITUD, solicitudes));
    }
}
