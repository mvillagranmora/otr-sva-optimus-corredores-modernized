package cl.bcs.fondomutuo.service.javacall;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migration of legacy JavaCall cl.bcs.fondomutuo.CambiarEstadoSolicitudAgf.
 */
@Service
public class CambiarEstadoSolicitudAgfService {

    private static final Logger log = LoggerFactory.getLogger(CambiarEstadoSolicitudAgfService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String sqlSelect;
    private final String sqlUpdate;
    private final String sqlUpdateRelacionados;

    public CambiarEstadoSolicitudAgfService(NamedParameterJdbcTemplate jdbc) throws IOException {
        this.jdbc = jdbc;
        this.sqlSelect = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_SelectSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdate = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_UpdateSolicitudes.sql").getInputStream(), StandardCharsets.UTF_8);
        this.sqlUpdateRelacionados = StreamUtils.copyToString(
            new ClassPathResource("cl/bcs/fondomutuo/sql/CambiarEstadoSolicitudAgf_UpdateSolicitudesRelacionados.sql").getInputStream(), StandardCharsets.UTF_8);
    }

    public JsonObject execute(JsonObject params) {
        log.info("CambiarEstadoSolicitudAgf");
        long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 0L;
        JsonArray solicitudes = params.has("p_SolicitudesAGF") ? params.getAsJsonArray("p_SolicitudesAGF") : new JsonArray();

        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0);

        if (solicitudes.size() == 0) {
            out.addProperty("P_OUT_ERRORESCONTADOR", 1);
            return out;
        }

        List<Long> ids = new ArrayList<>();
        for (JsonElement elem : solicitudes) {
            ids.add(elem.getAsJsonObject().get("IdInterfazAgf").getAsLong());
        }

        List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect,
            new MapSqlParameterSource().addValue("IdInterfazAgf", ids).addValue("IdEmpresa", idEmpresa));

        List<SqlParameterSource> updateParams = new ArrayList<>();
        List<SqlParameterSource> errorParams = new ArrayList<>();

        for (JsonElement elem : solicitudes) {
            JsonObject sol = elem.getAsJsonObject();
            long id = sol.get("IdInterfazAgf").getAsLong();
            String idEstadoNuevo = sol.has("IdEstado") ? sol.get("IdEstado").getAsString() : "RES";
            String observacion = sol.has("Observacion") ? sol.get("Observacion").getAsString() : "";

            Map<String, Object> row = rows.stream()
                .filter(r -> id == ((Number) r.get("IdInterfazAgf")).longValue())
                .findFirst().orElse(null);

            if (row == null) continue;

            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("IdInterfazAgf", id)
                .addValue("IdEstadoNuevo", idEstadoNuevo)
                .addValue("Observacion", observacion)
                .addValue("IdEmpresa", idEmpresa);
            updateParams.add(ps);

            if ("ERR".equals(idEstadoNuevo)) {
                errorParams.add(ps);
            }
        }

        if (!updateParams.isEmpty()) jdbc.batchUpdate(sqlUpdate, updateParams.toArray(new SqlParameterSource[0]));
        if (!errorParams.isEmpty()) jdbc.batchUpdate(sqlUpdateRelacionados, errorParams.toArray(new SqlParameterSource[0]));

        return out;
    }
}
