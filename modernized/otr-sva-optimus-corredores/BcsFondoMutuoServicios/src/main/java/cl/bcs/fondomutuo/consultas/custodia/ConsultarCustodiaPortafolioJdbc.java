package cl.bcs.fondomutuo.consultas.custodia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * The four read SQL of the class ({@code ConsultarCustodiaPortafolioFondoMutuo_Select*.sql}), verbatim, on the connection of the unit of work, with the named parameters the
 * legacy builds (columns mapped by label, as the legacy {@code SQL.query}).
 */
@Component
public class ConsultarCustodiaPortafolioJdbc implements ConsultarCustodiaPortafolioDatos {

    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("ConsultarCustodiaPortafolioFondoMutuo_SelectSolicitudes.sql");
    private static final String SQL_SELECT_SOLICITUDES_TOTALES = SqlRecursos.modulo("ConsultarCustodiaPortafolioFondoMutuo_SelectSolicitudesTotales.sql");
    private static final String SQL_SELECT_SERIES = SqlRecursos.modulo("ConsultarCustodiaPortafolioFondoMutuo_SelectSeries.sql");
    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ConsultarCustodiaPortafolioFondoMutuo_SelectRegimen.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarCustodiaPortafolioJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarCustodiaPortafolioContrato.Solicitud> solicitudes(Parametros parametros) {
        Map<String, Object> params = mapa(parametros);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SOLICITUDES, params, ConsultarCustodiaPortafolioContrato.Solicitud.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioContrato.SolicitudTotal> solicitudesTotales(Parametros parametros) {
        Map<String, Object> params = mapa(parametros);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SOLICITUDES_TOTALES, params, ConsultarCustodiaPortafolioContrato.SolicitudTotal.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioContrato.SerieFondo> series(List<Long> idsInstrumento) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdInstrumento", idsInstrumento);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SERIES, params, ConsultarCustodiaPortafolioContrato.SerieFondo.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioContrato.Regimen> regimenes(List<String> idsSerieFondo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerieFondo);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_REGIMEN, params, ConsultarCustodiaPortafolioContrato.Regimen.class));
    }

    /** The legacy {@code HashMap}: a null stays a null (it binds as SQL NULL). */
    private static Map<String, Object> mapa(Parametros p) {
        Map<String, Object> params = new HashMap<>();
        params.put("IngresoAutomatico", p.IngresoAutomatico);
        params.put("FiltraIngresoAutomatico", p.FiltraIngresoAutomatico);
        params.put("IdEmpresa", p.IdEmpresa);
        params.put("IdCliente", p.IdCliente);
        params.put("IdPortafolio", p.IdPortafolio);
        params.put("IdInstrumento", p.IdInstrumento);
        params.put("IdProductoAgrupador", p.IdProductoAgrupador);
        params.put("IdEmisor", p.IdEmisor);
        params.put("IdFamiliaFondo", p.IdFamiliaFondo);
        return params;
    }
}
