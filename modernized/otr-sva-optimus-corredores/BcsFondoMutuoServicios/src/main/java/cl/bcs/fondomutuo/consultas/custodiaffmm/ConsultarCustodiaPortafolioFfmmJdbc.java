package cl.bcs.fondomutuo.consultas.custodiaffmm;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/**
 * The five read SQL of {@code ConsultarCustodiaPortafolioFFMM} ({@code ConsultarCustodiaPortafolioFFMM_*.sql}), verbatim, on the connection of the unit of work, with the named
 * parameters the legacy builds (columns mapped by label, as the legacy {@code SQL.query}). The guarantee SQL are called WITHOUT {@code TipoFechaConsulta} (the kit raises
 * {@code Parameter not found} for a parameter that is in the map and not in the SQL), the custody SQL with it.
 */
@Component
public class ConsultarCustodiaPortafolioFfmmJdbc implements ConsultarCustodiaPortafolioFfmmDatos {

    private static final String SQL_SELECT_PERSONA_DESTINO = SqlRecursos.modulo("ConsultarCustodiaPortafolioFFMM_SelectPersonaDestino.sql");
    private static final String SQL_SELECT_CUSTODIA = SqlRecursos.modulo("ConsultarCustodiaPortafolioFFMM_SelectConsultarCustodia.sql");
    private static final String SQL_SELECT_GARANTIA = SqlRecursos.modulo("ConsultarCustodiaPortafolioFFMM_SelectConsultarGarantia.sql");
    private static final String SQL_SELECT_CUSTODIA_HISTORICO = SqlRecursos.modulo("ConsultarCustodiaPortafolioFFMM_SelectConsultarCustodiaHistorico.sql");
    private static final String SQL_SELECT_GARANTIA_HISTORICO = SqlRecursos.modulo("ConsultarCustodiaPortafolioFFMM_SelectConsultarGarantiaHistorico.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarCustodiaPortafolioFfmmJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaVigente(Parametros parametros) {
        Map<String, Object> params = mapa(parametros, false);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_GARANTIA, params, ConsultarCustodiaPortafolioFfmmContrato.Garantia.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioFfmmContrato.Garantia> garantiaHistorico(Parametros parametros) {
        Map<String, Object> params = mapa(parametros, false);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_GARANTIA_HISTORICO, params, ConsultarCustodiaPortafolioFfmmContrato.Garantia.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaVigente(Parametros parametros) {
        Map<String, Object> params = mapa(parametros, true);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_CUSTODIA, params, ConsultarCustodiaPortafolioFfmmContrato.Custodia.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioFfmmContrato.Custodia> custodiaHistorico(Parametros parametros) {
        Map<String, Object> params = mapa(parametros, true);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_CUSTODIA_HISTORICO, params, ConsultarCustodiaPortafolioFfmmContrato.Custodia.class));
    }

    @Override
    public List<ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino> personasDestino(Long idPersonaCuentaPropia) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdPersonaCuentaPropia", idPersonaCuentaPropia);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_PERSONA_DESTINO, params, ConsultarCustodiaPortafolioFfmmContrato.PersonaDestino.class));
    }

    /** The legacy {@code HashMap}: a null stays a null (it binds as SQL NULL). */
    private static Map<String, Object> mapa(Parametros p, boolean conTipoFecha) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", p.IdEmpresa);
        params.put("IdCliente", p.IdCliente);
        params.put("IdPortafolio", p.IdPortafolio);
        params.put("IdInstrumento", p.IdInstrumento);
        params.put("FechaConsulta", p.FechaConsulta);
        params.put("IdProductoAgrupador", p.IdProductoAgrupador);
        params.put("IdEmisor", p.IdEmisor);
        params.put("IdFamiliaFondo", p.IdFamiliaFondo);
        if (conTipoFecha) params.put("TipoFechaConsulta", p.TipoFechaConsulta);
        return params;
    }
}
