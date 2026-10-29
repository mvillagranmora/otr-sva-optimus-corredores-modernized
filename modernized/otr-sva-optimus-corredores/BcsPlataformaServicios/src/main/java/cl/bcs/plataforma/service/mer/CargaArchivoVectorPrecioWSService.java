package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CargaArchivoVectorPrecioWSService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargaArchivoVectorPrecioWSService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.CargaArchivoVectorPrecioWS"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlFuente = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectFuente.sql");
            String sqlTablaRegistros = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_TablaRegistros.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_InsertTablaRegistros.sql");
            String sqlSelectRegistros = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectTablaRegistros.sql");
            String sqlDelete = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_DeleteTablaRegistros.sql");
            String sqlInstrumentos = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecioWS_SelectInstrumentos.sql");

            String idFuente = params.has("p_IdFuente") && !params.get("p_IdFuente").isJsonNull()
                    ? params.get("p_IdFuente").getAsString() : null;
            String fecha = params.has("p_Fecha") && !params.get("p_Fecha").isJsonNull()
                    ? params.get("p_Fecha").getAsString() : null;

            JsonArray registros = params.has("p_Registros") && params.get("p_Registros").isJsonArray()
                    ? params.getAsJsonArray("p_Registros") : new JsonArray();

            for (JsonElement el : registros) {
                JsonObject reg = el.getAsJsonObject();
                jdbc.update(sqlInsert, new MapSqlParameterSource()
                        .addValue("IdFuente", idFuente)
                        .addValue("Fecha", fecha)
                        .addValue("Nemotecnico", reg.has("Nemotecnico") && !reg.get("Nemotecnico").isJsonNull() ? reg.get("Nemotecnico").getAsString() : null)
                        .addValue("Valor", reg.has("Valor") && !reg.get("Valor").isJsonNull() ? reg.get("Valor").getAsString() : null));
            }

            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelectRegistros,
                    new MapSqlParameterSource().addValue("IdFuente", idFuente).addValue("Fecha", fecha));

            JsonArray arr = new JsonArray();
            rows.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });
            result.add("P_OUT_REGISTROS", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
