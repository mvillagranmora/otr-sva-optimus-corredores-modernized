package cl.bcs.plataforma.service.ins;

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
public class CargaArchivoValorRiesgoRVService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargaArchivoValorRiesgoRVService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.CargaArchivoValorRiesgoRV"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        long errores = 0L;
        JsonArray mensajes = new JsonArray();

        try {
            String sqlUpdate = loadSql("cl/bcs/plataforma/ins/sql/CargaArchivoValorRiesgoRV_UpdateValorRiesgo.sql");

            JsonArray lista = params.has("p_ListaCarga") && params.get("p_ListaCarga").isJsonArray()
                    ? params.getAsJsonArray("p_ListaCarga") : new JsonArray();

            for (JsonElement el : lista) {
                JsonObject item = el.getAsJsonObject();
                String nemotecnico = item.has("Nemotecnico") && !item.get("Nemotecnico").isJsonNull()
                        ? item.get("Nemotecnico").getAsString() : null;
                String entidad = item.has("Entidad") && !item.get("Entidad").isJsonNull()
                        ? item.get("Entidad").getAsString() : null;
                String valor = item.has("Valor") && !item.get("Valor").isJsonNull()
                        ? item.get("Valor").getAsString() : null;

                try {
                    jdbc.update(sqlUpdate, new MapSqlParameterSource()
                            .addValue("Nemotecnico", nemotecnico)
                            .addValue("Entidad", entidad)
                            .addValue("Valor", valor));
                } catch (Exception e) {
                    errores++;
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "ERROR");
                    msg.addProperty("Codigo", nemotecnico);
                    msg.addProperty("Descripcion", e.getMessage());
                    mensajes.add(msg);
                }
            }
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("p_out_ErroresContador", errores);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
