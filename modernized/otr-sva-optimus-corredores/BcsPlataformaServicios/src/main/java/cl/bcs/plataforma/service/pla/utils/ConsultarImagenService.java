package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ConsultarImagenService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarImagenService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT_IMAGENES;

    public ConsultarImagenService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT_IMAGENES = loadSql("cl/bcs/plataforma/pla/sql/ConsultarImagen_SelectImagenes.sql");
    }

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.ConsultarImagen"; }

    @Override
    public JsonObject execute(JsonObject params) {
        Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;

        List<Map<String, Object>> rows = jdbc.queryForList(SQL_SELECT_IMAGENES,
            new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

        JsonArray arr = new JsonArray();
        rows.forEach(r -> {
            JsonObject o = new JsonObject();
            r.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
            arr.add(o);
        });
        JsonObject result = new JsonObject();
        result.add("P_OUT_IMAGENES", arr);
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
