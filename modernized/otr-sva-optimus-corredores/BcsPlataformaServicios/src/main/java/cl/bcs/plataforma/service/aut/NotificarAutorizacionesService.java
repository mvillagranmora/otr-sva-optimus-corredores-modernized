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
public class NotificarAutorizacionesService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public NotificarAutorizacionesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.aut.NotificarAutorizaciones"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sqlEventos = loadSql("cl/bcs/plataforma/aut/sql/NotificarEventoAutorizacion_SelectEventoAutorizacion.sql");
            String sqlAutorizadores = loadSql("cl/bcs/plataforma/aut/sql/NotificarEventoAutorizacion_SelectAutorizadoresEventoAutorizacion.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String fechaSistema = params.has("p_FechaSistema") && !params.get("p_FechaSistema").isJsonNull() ? params.get("p_FechaSistema").getAsString() : null;

            MapSqlParameterSource ps = new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("FechaSistema", fechaSistema);

            List<Map<String, Object>> eventos = jdbc.queryForList(sqlEventos, ps);
            List<Map<String, Object>> autorizadores = jdbc.queryForList(sqlAutorizadores, ps);

            JsonArray evtArr = new JsonArray();
            eventos.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); evtArr.add(o); });
            JsonArray autArr = new JsonArray();
            autorizadores.forEach(row -> { JsonObject o = new JsonObject(); row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null)); autArr.add(o); });

            result.add("P_OUT_EVENTOS", evtArr);
            result.add("P_OUT_AUTORIZADORES", autArr);
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
