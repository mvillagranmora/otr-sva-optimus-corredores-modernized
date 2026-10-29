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
public class BorradorResponderEncuestaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public BorradorResponderEncuestaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.BorradorResponderEncuesta"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_SelectDatosInversionista.sql");
            String sqlUpdateCategoria = loadSql("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_UpdateCategoriaInversionista.sql");
            String sqlUpdateRespondio = loadSql("cl/bcs/plataforma/enc/sql/BorradorResponderEncuesta_UpdateRespondioEncuesta.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idPortafolio = params.has("p_IdPortafolio") && !params.get("p_IdPortafolio").isJsonNull() ? params.get("p_IdPortafolio").getAsLong() : null;
            String idCategoriaInversionista = params.has("p_IdCategoriaInversionista") && !params.get("p_IdCategoriaInversionista").isJsonNull() ? params.get("p_IdCategoriaInversionista").getAsString() : null;

            List<Map<String, Object>> datos = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdPortafolio", idPortafolio));

            jdbc.update(sqlUpdateCategoria, new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdPortafolio", idPortafolio)
                    .addValue("IdCategoriaInversionista", idCategoriaInversionista));

            jdbc.update(sqlUpdateRespondio, new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdPortafolio", idPortafolio)
                    .addValue("RespondioEncuesta", "S"));

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
