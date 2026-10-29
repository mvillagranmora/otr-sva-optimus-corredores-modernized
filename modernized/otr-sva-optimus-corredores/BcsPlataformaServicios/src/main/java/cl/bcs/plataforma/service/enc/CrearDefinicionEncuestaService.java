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
public class CrearDefinicionEncuestaService implements JavaCallService {

    private final NamedParameterJdbcTemplate jdbc;

    public CrearDefinicionEncuestaService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.enc.CrearDefinicionEncuesta"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        try {
            String sqlInsertEncuesta = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionEncuesta.sql");
            String sqlInsertPregunta = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionPregunta.sql");
            String sqlInsertRespuesta = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_InsertDefinicionRespuesta.sql");
            String sqlSelectEncuesta = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_SelectDefinicionEncuesta.sql");
            String sqlUpdateEncuesta = loadSql("cl/bcs/plataforma/enc/sql/CrearDefinicionEncuesta_UpdateDefinicionEncuesta.sql");

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // FechaDB.ObtenerFechaHoraSistemaTs needed for FechaIngreso
            // PlataformaUtils.MensajeSalida used for output messages

            Long idEmpresa = params.has("p_IdEmpresa") && !params.get("p_IdEmpresa").isJsonNull() ? params.get("p_IdEmpresa").getAsLong() : null;
            String nombre = params.has("p_Nombre") && !params.get("p_Nombre").isJsonNull() ? params.get("p_Nombre").getAsString() : null;
            String idEstado = params.has("p_IdEstado") && !params.get("p_IdEstado").isJsonNull() ? params.get("p_IdEstado").getAsString() : "ACT";

            List<Map<String, Object>> existentes = jdbc.queryForList(sqlSelectEncuesta,
                    new MapSqlParameterSource().addValue("IdEmpresa", idEmpresa));

            if (!existentes.isEmpty()) {
                jdbc.update(sqlUpdateEncuesta, new MapSqlParameterSource()
                        .addValue("IdEmpresa", idEmpresa).addValue("IdEstado", "INA"));
            }

            MapSqlParameterSource ps = new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("Nombre", nombre)
                    .addValue("IdEstado", idEstado)
                    .addValue("FechaIngreso", (String) null);
            jdbc.update(sqlInsertEncuesta, ps);

            if (params.has("p_Preguntas") && params.get("p_Preguntas").isJsonArray()) {
                for (var pel : params.getAsJsonArray("p_Preguntas")) {
                    JsonObject preg = pel.getAsJsonObject();
                    MapSqlParameterSource pps = new MapSqlParameterSource()
                            .addValue("IdEmpresa", idEmpresa)
                            .addValue("Pregunta", preg.has("Pregunta") && !preg.get("Pregunta").isJsonNull() ? preg.get("Pregunta").getAsString() : null)
                            .addValue("Orden", preg.has("Orden") && !preg.get("Orden").isJsonNull() ? preg.get("Orden").getAsInt() : null);
                    jdbc.update(sqlInsertPregunta, pps);
                    if (preg.has("Respuestas") && preg.get("Respuestas").isJsonArray()) {
                        for (var rel : preg.getAsJsonArray("Respuestas")) {
                            JsonObject resp = rel.getAsJsonObject();
                            jdbc.update(sqlInsertRespuesta, new MapSqlParameterSource()
                                    .addValue("IdEmpresa", idEmpresa)
                                    .addValue("IdPregunta", preg.has("IdPregunta") && !preg.get("IdPregunta").isJsonNull() ? preg.get("IdPregunta").getAsLong() : null)
                                    .addValue("Respuesta", resp.has("Respuesta") && !resp.get("Respuesta").isJsonNull() ? resp.get("Respuesta").getAsString() : null)
                                    .addValue("Puntaje", resp.has("Puntaje") && !resp.get("Puntaje").isJsonNull() ? resp.get("Puntaje").getAsLong() : null));
                        }
                    }
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
