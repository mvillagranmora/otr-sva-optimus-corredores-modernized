package cl.bcs.plataforma.service.aut;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RequiereEventoAutorizacionService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public RequiereEventoAutorizacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.RequiereEventoAutorizacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sql = loadSql("cl/bcs/plataforma/aut/sql/RequiereEventoAutorizacion_SelectRequiereAutorizacion.sql");
            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idConcepto = params.has("p_IdConcepto") && !params.get("p_IdConcepto").isJsonNull() ? params.get("p_IdConcepto").getAsString() : null;

            List<Map<String, Object>> rows = jdbc.queryForList(sql,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdConcepto", idConcepto));

            String requiere = rows.isEmpty() || rows.get(0).get("RequiereAutorizacion") == null ? "N"
                    : rows.get(0).get("RequiereAutorizacion").toString();
            result.addProperty("P_OUT_AUTORIZACION", requiere);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_AUTORIZACION", "N");
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
