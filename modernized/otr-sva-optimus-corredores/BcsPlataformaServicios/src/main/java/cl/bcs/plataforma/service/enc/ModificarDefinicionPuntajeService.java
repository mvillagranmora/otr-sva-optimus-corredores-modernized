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
public class ModificarDefinicionPuntajeService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarDefinicionPuntajeService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.ModificarDefinicionPuntaje"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionPuntaje_SelectDefinicionPuntaje.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionPuntaje_UpdateDefinicionPuntaje.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idCategoria = params.has("p_IdCategoriaInversionista") && !params.get("p_IdCategoriaInversionista").isJsonNull() ? params.get("p_IdCategoriaInversionista").getAsString() : null;

            List<Map<String, Object>> existentes = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdCategoriaInversionista", idCategoria));

            if (!existentes.isEmpty()) {
                jdbc.update(sqlUpdate, new MapSqlParameterSource()
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdCategoriaInversionista", idCategoria)
                        .addValue("PuntajeMinimo", params.has("p_PuntajeMinimo") && !params.get("p_PuntajeMinimo").isJsonNull() ? params.get("p_PuntajeMinimo").getAsLong() : null)
                        .addValue("PuntajeMaximo", params.has("p_PuntajeMaximo") && !params.get("p_PuntajeMaximo").isJsonNull() ? params.get("p_PuntajeMaximo").getAsLong() : null));
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
