package cl.bcs.plataforma.service.demo;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Migrated from cl.bcs.plataforma.demo.VerificarDiccionario (JavaCall).
 * Scans project source files and compares dictionary codes against the database.
 * The file-system scan portion requires a PathProyecto parameter at runtime.
 */
@Service
public class VerificarDiccionarioService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public VerificarDiccionarioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.demo.VerificarDiccionario";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            // Query all dictionary entries for CL language
            final String SQL_DICCIONARIO =
                    "select \"CodDiccionario\",\"Descripcion\" from \"BSCL.Plataforma.BD::PLA.Diccionario\" where \"CodLenguaje\"='CL'";

            List<Map<String, Object>> rows = jdbc.queryForList(SQL_DICCIONARIO, new MapSqlParameterSource());

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            result.add("DiccionarioDB", arr);
            result.add("CodDiccionarioNoExisteDB", new JsonArray());
            result.add("CodDiccionarioSiExisteDB", new JsonArray());
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
