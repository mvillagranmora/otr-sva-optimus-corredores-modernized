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
public class CrearDefinicionPuntajeService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearDefinicionPuntajeService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.CrearDefinicionPuntaje"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlInsert = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionPuntaje_InsertDefinicionPuntaje.sql");
            String sqlSelectCat = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionPuntaje_SelectCategoriaInversionista.sql");
            String sqlSelectDef = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionPuntaje_SelectDefinicionPuntaje.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String idCategoria = params.has("p_IdCategoriaInversionista") && !params.get("p_IdCategoriaInversionista").isJsonNull() ? params.get("p_IdCategoriaInversionista").getAsString() : null;

            List<Map<String, Object>> categorias = jdbc.queryForList(sqlSelectCat,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));
            List<Map<String, Object>> existentes = jdbc.queryForList(sqlSelectDef,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            jdbc.update(sqlInsert, new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdCategoriaInversionista", idCategoria)
                    .addValue("PuntajeMinimo", params.has("p_PuntajeMinimo") && !params.get("p_PuntajeMinimo").isJsonNull() ? params.get("p_PuntajeMinimo").getAsLong() : null)
                    .addValue("PuntajeMaximo", params.has("p_PuntajeMaximo") && !params.get("p_PuntajeMaximo").isJsonNull() ? params.get("p_PuntajeMaximo").getAsLong() : null));

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
