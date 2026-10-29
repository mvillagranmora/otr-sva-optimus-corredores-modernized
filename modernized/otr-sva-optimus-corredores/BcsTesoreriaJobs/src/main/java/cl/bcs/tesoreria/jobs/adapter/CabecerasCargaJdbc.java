package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.tesoreria.javacall.dap.ArchivoFtp.Cabecera;
import cl.bcs.tesoreria.javacall.kit.SqlNombrado;
import cl.bcs.tesoreria.jobs.job.port.CabecerasCarga;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Production {@link CabecerasCarga}: legacy {@code CargaMasivaIngresoEgresoDAPFFMM.getCabeceras} (BcsTesoreriaServicios
 * CargaMasivaIngresoEgresoDAPFFMM.java:814-830). The Servicios service keeps that query private, so the same SQL file
 * ({@code CargaMasivaIngresoEgresoDAPFFMM_SelectCabeceras.sql}, in the tesoreria-service jar) runs here with the same parameters.
 */
@Component
public class CabecerasCargaJdbc implements CabecerasCarga {

    private static final String BASE_CODIGO_TIPO = "CARGA_MASIVA_INGRESO_EGRESO_";
    private static final String SQL_SELECT_CABECERA = SqlNombrado.recurso("CargaMasivaIngresoEgresoDAPFFMM_SelectCabeceras");

    private final SqlNombrado sql;

    public CabecerasCargaJdbc(NamedParameterJdbcTemplate jdbc) {
        this.sql = new SqlNombrado(jdbc);
    }

    @Override
    public int cantidad(String tipoArchivoCarga, String fechaEjecucion, String idEstado) throws Exception {
        Map<String, Object> params = new HashMap<>();
        params.put("IdCodigoTipo", BASE_CODIGO_TIPO + tipoArchivoCarga);
        params.put("TipoArchivo", tipoArchivoCarga);
        params.put("FechaEjecucion", fechaEjecucion);
        params.put("IdEstado", idEstado);
        return sql.consultar(SQL_SELECT_CABECERA, params, Cabecera.class).size();
    }
}
