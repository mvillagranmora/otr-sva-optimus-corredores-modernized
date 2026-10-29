package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class GenerarArchivoMovimientosService {

    private static final Logger log = LoggerFactory.getLogger(GenerarArchivoMovimientosService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelectMovimientos;
    private final String sqlSelectHomologacion;

    public GenerarArchivoMovimientosService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelectMovimientos = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultaMovimientosGeneracionArchivoMov_SelectMovimientos.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlSelectHomologacion = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/ConsultaGeneracionArchivos_SelectHomologacion.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("GenerarArchivoMovimientos");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        String fechaDesde = params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : "";
        String fechaHasta = params.has("p_FechaHasta") ? params.get("p_FechaHasta").getAsString() : "";

        MapSqlParameterSource ps = new MapSqlParameterSource()
            .addValue("IdEmpresa", idEmpresa)
            .addValue("FechaDesde", fechaDesde)
            .addValue("FechaHasta", fechaHasta);

        List<Map<String, Object>> movimientos = jdbc.queryForList(sqlSelectMovimientos, ps);
        List<Map<String, Object>> homologacion = jdbc.queryForList(sqlSelectHomologacion, ps);

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);
        JsonArray arrMov = new JsonArray();
        for (Map<String, Object> row : movimientos) arrMov.add(GSON.toJsonTree(row));
        out.add("P_OUT_MOVIMIENTOS", arrMov);
        JsonArray arrHom = new JsonArray();
        for (Map<String, Object> row : homologacion) arrHom.add(GSON.toJsonTree(row));
        out.add("P_OUT_HOMOLOGACION", arrHom);
        return out;
    }
}
