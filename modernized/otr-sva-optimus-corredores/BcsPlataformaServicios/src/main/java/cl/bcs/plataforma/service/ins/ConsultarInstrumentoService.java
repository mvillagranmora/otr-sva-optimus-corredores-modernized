package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ConsultarInstrumentoService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarInstrumentoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ConsultarInstrumento"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ConsultarEmpresa, ConsultarFlujoCustodia) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        try {
            String tipoOperacion = params.has("p_TipoOperacion") && !params.get("p_TipoOperacion").isJsonNull()
                    ? params.get("p_TipoOperacion").getAsString() : "C";

            List<String> clasificaciones = new ArrayList<>();
            List<String> nemos = new ArrayList<>();
            List<String> tiposInstrumento = new ArrayList<>();
            int filtroClasif = 0, filtroNemo = 0, filtroTipo = 0;

            JsonArray datos = params.has("p_Datos") && params.get("p_Datos").isJsonArray()
                    ? params.getAsJsonArray("p_Datos") : new JsonArray();
            for (JsonElement el : datos) {
                JsonObject d = el.getAsJsonObject();
                if (d.has("IdClasificacionTipoInstrumento") && !d.get("IdClasificacionTipoInstrumento").isJsonNull())
                    clasificaciones.add(d.get("IdClasificacionTipoInstrumento").getAsString());
                if (d.has("Nemotecnico") && !d.get("Nemotecnico").isJsonNull())
                    nemos.add(d.get("Nemotecnico").getAsString());
                if (d.has("IdTipoInstrumento") && !d.get("IdTipoInstrumento").isJsonNull())
                    tiposInstrumento.add(d.get("IdTipoInstrumento").getAsString());
            }

            if (clasificaciones.isEmpty()) filtroClasif = 1;
            if (nemos.isEmpty()) filtroNemo = 1;
            if (tiposInstrumento.isEmpty()) filtroTipo = 1;

            String sql = loadSql("cl/bcs/plataforma/ins/sql/ConsultarInstrumento_ConsultarInstrumento.sql");
            List<Map<String, Object>> rows = jdbc.queryForList(sql, new MapSqlParameterSource()
                    .addValue("IdClasificacionTipoInstrumento", clasificaciones.isEmpty() ? List.of("") : clasificaciones)
                    .addValue("Nemotecnico", nemos.isEmpty() ? List.of("") : nemos)
                    .addValue("IdTipoInstrumento", tiposInstrumento.isEmpty() ? List.of("") : tiposInstrumento)
                    .addValue("IdEstado", "ACT")
                    .addValue("FiltroIdClasificacionTipoInstrumento", filtroClasif)
                    .addValue("FiltroNemotecnico", filtroNemo)
                    .addValue("FiltroIdTipoInstrumento", filtroTipo));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });
            result.add("P_OUT_INSTRUMENTO", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            msgs.add(msg);
            result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
