package cl.bcs.plataforma.service.pla.utils;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
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
public class BarraProgresoServicioService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(BarraProgresoServicioService.class);
    private static final Gson GSON = new Gson();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_PASO;
    private final String SQL_DELETE_PASO;
    private final String SQL_FINALIZAR;
    private final String SQL_SELECT_PROGRESOS;

    public BarraProgresoServicioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_PASO    = loadSql("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_InsertTablaPaso.sql");
        this.SQL_DELETE_PASO    = loadSql("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_DeleteTablaPaso.sql");
        this.SQL_FINALIZAR      = loadSql("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_UpdateProgresoFinalizado.sql");
        this.SQL_SELECT_PROGRESOS = loadSql("cl/bcs/plataforma/pla/sql/BarraProgresoServicio_SelectProgresos.sql");
    }

    @Override public String fqn() { return "cl.bcs.plataforma.pla.utils.BarraProgresoServicio"; }

    @Override
    public JsonObject execute(JsonObject params) {
        String accion = params.has("p_Accion") ? params.get("p_Accion").getAsString() : "CONSULTAR";
        Long idProceso = params.has("p_IdProceso") ? params.get("p_IdProceso").getAsLong() : null;

        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0);

        switch (accion) {
            case "INSERTAR" -> {
                jdbc.update(SQL_INSERT_PASO, new MapSqlParameterSource()
                    .addValue("IdProceso", idProceso)
                    .addValue("Descripcion", params.has("p_Descripcion") ? params.get("p_Descripcion").getAsString() : ""));
            }
            case "ELIMINAR" -> {
                jdbc.update(SQL_DELETE_PASO, new MapSqlParameterSource()
                    .addValue("IdProceso", idProceso));
            }
            case "FINALIZAR" -> {
                jdbc.update(SQL_FINALIZAR, new MapSqlParameterSource()
                    .addValue("IdProceso", idProceso));
            }
            default -> {
                // CONSULTAR
                List<Map<String, Object>> rows = jdbc.queryForList(SQL_SELECT_PROGRESOS,
                    new MapSqlParameterSource().addValue("IdProceso", idProceso));
                JsonArray arr = new JsonArray();
                rows.forEach(r -> {
                    JsonObject o = new JsonObject();
                    r.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                    arr.add(o);
                });
                result.add("P_OUT_PROGRESOS", arr);
            }
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
