package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrearDeterioroEmisorService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CrearDeterioroEmisorService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.CrearDeterioroEmisor"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sql = loadSql("cl/bcs/plataforma/mer/sql/CrearDeterioroEmisor_InsertDeterioroEmisor.sql");

            jdbc.update(sql, new MapSqlParameterSource()
                    .addValue("p_IdEmisor", getS(params, "p_IdEmisor"))
                    .addValue("p_Fecha", getS(params, "p_Fecha"))
                    .addValue("p_ValorDeterioroEmisor", getS(params, "p_ValorDeterioroEmisor"))
                    .addValue("p_Observacion", getS(params, "p_Observacion"))
                    .addValue("p_IdEstado", getS(params, "p_IdEstado")));

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "DETERIORO_CREADO");
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
        }
        return result;
    }

    private String getS(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
