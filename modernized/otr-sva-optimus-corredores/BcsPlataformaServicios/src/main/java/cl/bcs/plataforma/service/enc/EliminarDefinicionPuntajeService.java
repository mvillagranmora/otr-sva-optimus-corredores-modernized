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
public class EliminarDefinicionPuntajeService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public EliminarDefinicionPuntajeService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.EliminarDefinicionPuntaje"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/enc/sql/EliminarDefinicionPuntaje_SelectDefinicionPuntaje.sql");
            String sqlDelete = loadSql("cl/bcs/plataforma/enc/sql/EliminarDefinicionPuntaje_DeleteDefinicionPuntaje.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idCategoria = params.has("p_IdCategoriaInversionista") && !params.get("p_IdCategoriaInversionista").isJsonNull() ? params.get("p_IdCategoriaInversionista").getAsString() : null;

            List<Map<String, Object>> existentes = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdCategoriaInversionista", idCategoria));

            if (!existentes.isEmpty()) {
                jdbc.update(sqlDelete, new MapSqlParameterSource()
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdCategoriaInversionista", idCategoria));
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
