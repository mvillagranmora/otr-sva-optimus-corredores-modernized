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
public class VencerEventosAutorizacionService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public VencerEventosAutorizacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.VencerEventosAutorizacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlConsultaVencidos = loadSql("cl/bcs/plataforma/aut/sql/ConsultaEventoVencido.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // FechaDB needed for current date resolution against HANA system time
            Long idEmpresa = params.has("P_IDEMPRESA") && !params.get("P_IDEMPRESA").isJsonNull() ? params.get("P_IDEMPRESA").getAsLong() : null;
            String fechaSistema = params.has("P_FECHASISTEMA") && !params.get("P_FECHASISTEMA").isJsonNull() ? params.get("P_FECHASISTEMA").getAsString() : null;

            List<Map<String, Object>> eventosVencidos = jdbc.queryForList(sqlConsultaVencidos,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("FechaSistema", fechaSistema));

            JsonArray vencidos = new JsonArray();
            eventosVencidos.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); vencidos.add(o); });

            result.add("P_OUT_EVENTOSVENCIDOS", vencidos);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonArray msgs = new JsonArray(); JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); msgs.add(msg); result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
