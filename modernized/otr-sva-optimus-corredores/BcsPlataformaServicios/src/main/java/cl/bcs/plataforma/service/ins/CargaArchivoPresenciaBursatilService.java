package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CargaArchivoPresenciaBursatilService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargaArchivoPresenciaBursatilService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.CargaArchivoPresenciaBursatil"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (FechaDB) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        long errores = 0L;
        JsonArray mensajes = new JsonArray();

        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/ins/sql/CargaArchivoPresenciaBursatil_SelectInstrumentos.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/ins/sql/CargaArchivoPresenciaBursatil_InsertCargaArchivoPresenciaBursatil.sql");

            JsonArray lista = params.has("p_ListaCarga") && params.get("p_ListaCarga").isJsonArray()
                    ? params.getAsJsonArray("p_ListaCarga") : new JsonArray();

            for (JsonElement el : lista) {
                JsonObject item = el.getAsJsonObject();
                String nemotecnico = item.has("Nemotecnico") && !item.get("Nemotecnico").isJsonNull()
                        ? item.get("Nemotecnico").getAsString() : null;
                String fechaVigencia = item.has("FechaVigencia") && !item.get("FechaVigencia").isJsonNull()
                        ? item.get("FechaVigencia").getAsString() : null;
                BigDecimal factor = item.has("Factor") && !item.get("Factor").isJsonNull()
                        ? item.get("Factor").getAsBigDecimal() : BigDecimal.ZERO;
                Long idInstrumento = item.has("IdInstrumento") && !item.get("IdInstrumento").isJsonNull()
                        ? item.get("IdInstrumento").getAsLong() : null;

                try {
                    jdbc.update(sqlInsert, new MapSqlParameterSource()
                            .addValue("IdInstrumento", idInstrumento)
                            .addValue("Nemotecnico", nemotecnico)
                            .addValue("FechaVigencia", fechaVigencia)
                            .addValue("Factor", factor));
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
