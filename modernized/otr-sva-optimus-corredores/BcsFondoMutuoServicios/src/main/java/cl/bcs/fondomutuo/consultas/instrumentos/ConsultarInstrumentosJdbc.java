package cl.bcs.fondomutuo.consultas.instrumentos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;

/** The three reads of {@code ConsultarInstrumentosFondosMutuos}, SQL verbatim, on the connection of the unit of work. */
@Component
public class ConsultarInstrumentosJdbc implements ConsultarInstrumentosDatos {

    private static final String SQL_SELECT_FONDO = SqlRecursos.modulo("ConsultarInstrumentosFondosMutuos_SelectFondoMutuo.sql");
    private static final String SQL_SELECT_FONDO_REINVERSION = SqlRecursos.modulo("ConsultarInstrumentosFondosMutuos_SelectFondoMutuoReinversion.sql");
    private static final String SQL_SELECT_REGIMEN = SqlRecursos.modulo("ConsultarInstrumentosFondosMutuos_SelectRegimen.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public ConsultarInstrumentosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<ConsultarInstrumentosContrato.OutFondoMutuo> fondos(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen,
            String mostrarBloqueados) {
        Map<String, Object> params = parametros(idEmpresa, filtro, filtroRegimen);
        params.put("MostrarBloqueados", mostrarBloqueados);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_FONDO, params, ConsultarInstrumentosContrato.OutFondoMutuo.class));
    }

    @Override
    public List<ConsultarInstrumentosContrato.OutFondoMutuo> fondosReinversion(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen) {
        Map<String, Object> params = parametros(idEmpresa, filtro, filtroRegimen);
        params.put("IdSerieFondoOrigen", filtro.IdSerieFondoOrigen);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_FONDO_REINVERSION, params, ConsultarInstrumentosContrato.OutFondoMutuo.class));
    }

    @Override
    public List<ConsultarInstrumentosContrato.Regimen> regimenes(List<String> idsSerieFondo) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdSerieFondo", idsSerieFondo);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_REGIMEN, params, ConsultarInstrumentosContrato.Regimen.class));
    }

    private static Map<String, Object> parametros(Long idEmpresa, ConsultarInstrumentosContrato.InFondos filtro, long filtroRegimen) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        params.put("IdSerieFondo", filtro.IdSerieFondo);
        params.put("IdEmisor", filtro.IdEmisor);
        params.put("IdFamiliaFondo", filtro.IdFamiliaFondo);
        params.put("FiltroRegimen", filtroRegimen);
        params.put("IdRegimenFondoMutuo", filtro.IdRegimenFondoMutuo);
        params.put("IdCategoriaPerfilFondoMutuo", filtro.IdCategoriaPerfilInversionista);
        params.put("IdEstado", filtro.IdEstado);
        params.put("IdMoneda", filtro.IdMoneda);
        params.put("FondoExtranjero", filtro.FondoExtranjero);
        params.put("InstrumentoOWP", filtro.InstrumentoOWP);
        params.put("EsAPV", filtro.EsAPV);
        return params;
    }
}
