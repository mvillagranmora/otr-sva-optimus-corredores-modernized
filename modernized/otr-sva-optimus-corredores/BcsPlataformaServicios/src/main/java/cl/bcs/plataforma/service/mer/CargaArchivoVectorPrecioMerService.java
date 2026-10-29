package cl.bcs.plataforma.service.mer;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CargaArchivoVectorPrecioMerService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargaArchivoVectorPrecioMerService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.CargaArchivoVectorPrecio_mer"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlUpsert = loadSql("cl/bcs/plataforma/mer/sql/CargaArchivoVectorPrecio_UPSERT_MER.sql");

            JsonArray filas = params.has("p_Filas") && params.get("p_Filas").isJsonArray()
                    ? params.getAsJsonArray("p_Filas") : new JsonArray();

            for (JsonElement el : filas) {
                JsonObject fila = el.getAsJsonObject();
                try {
                    String fecha = fila.has("FECHA") && !fila.get("FECHA").isJsonNull() ? fila.get("FECHA").getAsString() : null;
                    String nemo = fila.has("NEMOTECNICO") && !fila.get("NEMOTECNICO").isJsonNull() ? fila.get("NEMOTECNICO").getAsString() : null;
                    String entidad = fila.has("ENTIDAD") && !fila.get("ENTIDAD").isJsonNull() ? fila.get("ENTIDAD").getAsString() : null;
                    String tipoRegistro = fila.has("TIPOREGISTRO") && !fila.get("TIPOREGISTRO").isJsonNull() ? fila.get("TIPOREGISTRO").getAsString() : null;
                    BigDecimal valorRegistro = fila.has("VALORREGISTRO") && !fila.get("VALORREGISTRO").isJsonNull()
                            ? fila.get("VALORREGISTRO").getAsBigDecimal() : BigDecimal.ZERO;

                    jdbc.update(sqlUpsert, new MapSqlParameterSource()
                            .addValue("FECHA", fecha)
                            .addValue("NEMOTECNICO", nemo)
                            .addValue("ENTIDAD", entidad)
                            .addValue("TIPOREGISTRO", tipoRegistro)
                            .addValue("VALORREGISTRO", valorRegistro));
                } catch (Exception e) {
                    errores++;
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "ERROR");
                    msg.addProperty("Descripcion", e.getMessage());
                    mensajes.add(msg);
                }
            }
        } catch (Exception e) {
            errores++;
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("P_OUT_ERRORESCONTADOR", errores);
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
