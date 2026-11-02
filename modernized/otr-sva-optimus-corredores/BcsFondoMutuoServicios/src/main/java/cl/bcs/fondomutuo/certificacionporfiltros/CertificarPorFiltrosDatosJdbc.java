package cl.bcs.fondomutuo.certificacionporfiltros;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlNombrado;
import cl.bcs.fondomutuo.adapter.out.jdbc.SqlRecursos;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacionporfiltros.CertificarPorFiltrosContrato.InFiltros;

/** {@link CertificarPorFiltrosDatos} over the legacy SQL, verbatim (no ORDER BY added: the row order is the engine's). A null filter field stays null: the COALESCE of the SQL decides. */
public class CertificarPorFiltrosDatosJdbc implements CertificarPorFiltrosDatos {

    private static final String SQL_SELECT_SOLICITUDES = SqlRecursos.modulo("CertificarPorFiltrosSolicitudAporteRescate_SelectSolicitudes.sql");

    private final ConexionUnidadDeTrabajo conexion;

    public CertificarPorFiltrosDatosJdbc(ConexionUnidadDeTrabajo conexion) {
        this.conexion = conexion;
    }

    @Override
    public List<InSolicitudes> solicitudes(InFiltros filtro, String fechaCertificacion, Long idEmpresa) {
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmisor", filtro.IdEmisor);
        params.put("IdMoneda", filtro.IdMoneda);
        params.put("IdSerieFondo", filtro.IdSerieFondo);
        params.put("Nacional", filtro.Nacional);
        params.put("TipoSolicitud", filtro.TipoSolicitud);
        params.put("IdTipoMovimientoFondoMutuo", filtro.IdTipoMovimientoFondoMutuo);
        params.put("MoneyMarket", filtro.MoneyMarket);
        params.put("IdCarga", filtro.IdCarga);
        params.put("FechaCertificacion", fechaCertificacion);
        params.put("IdEmpresa", idEmpresa);
        return conexion.usar(con -> SqlNombrado.consultar(con, SQL_SELECT_SOLICITUDES, params, InSolicitudes.class));
    }
}
