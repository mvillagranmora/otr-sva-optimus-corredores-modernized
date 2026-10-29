package cl.bcs.plataforma.service.enc;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActualizarDefinicionPuntajesService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ActualizarDefinicionPuntajesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.ActualizarDefinicionPuntajes"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelectCat = loadSql("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_SelectCategoriaInversionista.sql");
            String sqlDelete = loadSql("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_DeleteDefinicionPuntajes.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/enc/sql/ActualizarDefinicionPuntajes_InsertDefinicionPuntaje.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;

            List<Map<String, Object>> categorias = jdbc.queryForList(sqlSelectCat,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            jdbc.update(sqlDelete, new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            if (params.has("p_DefinicionPuntajes") && params.get("p_DefinicionPuntajes").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_DefinicionPuntajes")) {
                    JsonObject def = el.getAsJsonObject();
                    jdbc.update(sqlInsert, new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa)
                            .addValue("IdCategoriaInversionista", def.has("IdCategoriaInversionista") && !def.get("IdCategoriaInversionista").isJsonNull() ? def.get("IdCategoriaInversionista").getAsString() : null)
                            .addValue("PuntajeMinimo", def.has("PuntajeMinimo") && !def.get("PuntajeMinimo").isJsonNull() ? def.get("PuntajeMinimo").getAsLong() : null)
                            .addValue("PuntajeMaximo", def.has("PuntajeMaximo") && !def.get("PuntajeMaximo").isJsonNull() ? def.get("PuntajeMaximo").getAsLong() : null));
                }
            }

            result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            result.add("P_OUT_MENSAJES", mensajes);
        } catch (Exception e) {
            JsonObject msg = new JsonObject(); msg.addProperty("Tipo", "ERROR"); msg.addProperty("Descripcion", e.getMessage()); mensajes.add(msg);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L); result.add("P_OUT_MENSAJES", mensajes);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) { return new String(is.readAllBytes()); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
