package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgregarUsoParidadParametroFinancieroService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AgregarUsoParidadParametroFinancieroService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.AgregarUsoParidadParametroFinanciero"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/mer/sql/AgregarUsoParidadParametroFinanciero_SelectUsoParidadParametroFinanciero.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/mer/sql/AgregarUsoParidadParametroFinanciero_InsertUsoParidadParametroFinanciero.sql");

            JsonArray usos = params.has("p_UsoParidadParametrosFinancieros") && params.get("p_UsoParidadParametrosFinancieros").isJsonArray()
                    ? params.getAsJsonArray("p_UsoParidadParametrosFinancieros") : new JsonArray();

            for (JsonElement el : usos) {
                JsonObject uso = el.getAsJsonObject();
                String idParametro = uso.has("IdParametroFinanciero") && !uso.get("IdParametroFinanciero").isJsonNull()
                        ? uso.get("IdParametroFinanciero").getAsString() : null;
                String idUso = uso.has("IdUso") && !uso.get("IdUso").isJsonNull()
                        ? uso.get("IdUso").getAsString() : null;
                Integer prioridad = uso.has("Prioridad") && !uso.get("Prioridad").isJsonNull()
                        ? uso.get("Prioridad").getAsInt() : 0;

                List<Map<String, Object>> existing = jdbc.queryForList(sqlSelect,
                        new MapSqlParameterSource()
                                .addValue("IdParametroFinanciero", idParametro)
                                .addValue("IdUso", idUso));

                if (existing.isEmpty()) {
                    jdbc.update(sqlInsert, new MapSqlParameterSource()
                            .addValue("IdParametroFinanciero", idParametro)
                            .addValue("IdUso", idUso)
                            .addValue("Prioridad", prioridad));
                } else {
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "ERROR");
                    msg.addProperty("Codigo", "USO_YA_EXISTE");
                    mensajes.add(msg);
                    errores++;
                }
            }

            if (errores == 0) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "OK");
                msg.addProperty("Codigo", "USO_AGREGADO");
                mensajes.add(msg);
            }
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
