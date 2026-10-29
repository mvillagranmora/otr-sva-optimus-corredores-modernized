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
public class ModificarDefinicionEncuestaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public ModificarDefinicionEncuestaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.ModificarDefinicionEncuesta"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlSelect = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_SelectDefinicionEncuesta.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateDefinicionEncuesta.sql");
            String sqlUpdateOtras = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateOtrasEncuestas.sql");
            String sqlSelectRespElim = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_SelectRespuestasEliminadas.sql");
            String sqlUpdateRespElim = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateRespuestasEliminadas.sql");
            String sqlUpdateRespMod = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdateRespuestasModificadas.sql");
            String sqlUpdatePregElim = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdatePreguntasEliminadas.sql");
            String sqlUpdatePregMod = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_UpdatePreguntasModificadas.sql");
            String sqlInsertPreg = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_InsertDefinicionPregunta.sql");
            String sqlInsertResp = loadSql("cl/bcs/plataforma/enc/sql/ModificarDefinicionEncuesta_InsertDefinicionRespuesta.sql");

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idEncuesta = params.has("p_IdEncuesta") && !params.get("p_IdEncuesta").isJsonNull() ? params.get("p_IdEncuesta").getAsLong() : null;
            String nombre = params.has("p_Nombre") && !params.get("p_Nombre").isJsonNull() ? params.get("p_Nombre").getAsString() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : "ACT";

            List<Map<String, Object>> encuesta = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdEncuesta", idEncuesta));

            if ("ACT".equals(idEstado)) {
                jdbc.update(sqlUpdateOtras, new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa).addValue("IdEncuesta", idEncuesta));
            }

            jdbc.update(sqlUpdate, new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa).addValue("IdEncuesta", idEncuesta)
                    .addValue("Nombre", nombre).addValue("IdEstado", idEstado));

            if (params.has("p_PreguntasEliminadas") && params.get("p_PreguntasEliminadas").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_PreguntasEliminadas")) {
                    JsonObject p = el.getAsJsonObject();
                    Long idPregunta = p.has("IdPregunta") && !p.get("IdPregunta").isJsonNull() ? p.get("IdPregunta").getAsLong() : null;
                    jdbc.update(sqlUpdatePregElim, new MapSqlParameterSource().addValue("IdPregunta", idPregunta).addValue("IdEmpresa", idEmpresa));
                    jdbc.queryForList(sqlSelectRespElim, new MapSqlParameterSource().addValue("IdPregunta", idPregunta).addValue("IdEmpresa", idEmpresa))
                            .forEach(r -> jdbc.update(sqlUpdateRespElim, new MapSqlParameterSource().addValue("IdRespuesta", r.get("IdRespuesta")).addValue("IdEmpresa", idEmpresa)));
                }
            }

            if (params.has("p_PreguntasModificadas") && params.get("p_PreguntasModificadas").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_PreguntasModificadas")) {
                    JsonObject p = el.getAsJsonObject();
                    jdbc.update(sqlUpdatePregMod, new MapSqlParameterSource()
                            .addValue("IdPregunta", p.has("IdPregunta") && !p.get("IdPregunta").isJsonNull() ? p.get("IdPregunta").getAsLong() : null)
                            .addValue("Pregunta", p.has("Pregunta") && !p.get("Pregunta").isJsonNull() ? p.get("Pregunta").getAsString() : null)
                            .addValue("Orden", p.has("Orden") && !p.get("Orden").isJsonNull() ? p.get("Orden").getAsInt() : null)
                            .addValue("IdEmpresa", idEmpresa));
                }
            }

            if (params.has("p_PreguntasNuevas") && params.get("p_PreguntasNuevas").isJsonArray()) {
                for (var el : params.getAsJsonArray("p_PreguntasNuevas")) {
                    JsonObject p = el.getAsJsonObject();
                    jdbc.update(sqlInsertPreg, new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa).addValue("IdEncuesta", idEncuesta)
                            .addValue("Pregunta", p.has("Pregunta") && !p.get("Pregunta").isJsonNull() ? p.get("Pregunta").getAsString() : null)
                            .addValue("Orden", p.has("Orden") && !p.get("Orden").isJsonNull() ? p.get("Orden").getAsInt() : null));
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
