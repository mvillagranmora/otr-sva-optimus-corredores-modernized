package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CargarMarketMakerDesdeBursatilService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CargarMarketMakerDesdeBursatilService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.CargarMarketMakerDesdeBursatil"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String fecha = params.has("p_FechaSistema") && !params.get("p_FechaSistema").isJsonNull()
                    ? params.get("p_FechaSistema").getAsString() : null;

            String sqlSelect = loadSql("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_SelectMarketMarketBursatil.sql");
            String sqlInsertPaso = loadSql("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_InsertMarketMarketTablaPaso.sql");
            String sqlDelete = loadSql("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_DeleteMarketMarket.sql");
            String sqlInsert = loadSql("cl/bcs/plataforma/ins/sql/CargarMarketMakerDesdeBursatil_InsertMarketMarket.sql");

            List<Map<String, Object>> rows = jdbc.queryForList(sqlSelect,
                    new MapSqlParameterSource().addValue("Fecha", fecha));

            for (Map<String, Object> row : rows) {
                jdbc.update(sqlInsertPaso, new MapSqlParameterSource()
                        .addValue("MERCADO", row.get("MERCADO") != null ? row.get("MERCADO").toString() : null)
                        .addValue("NEMO", row.get("NEMO") != null ? row.get("NEMO").toString() : null)
                        .addValue("COD_CORR", row.get("COD_CORR"))
                        .addValue("ESTADO", row.get("ESTADO") != null ? row.get("ESTADO").toString() : null)
                        .addValue("FEC_INI_MKT", row.get("FEC_INI_MKT") != null ? row.get("FEC_INI_MKT").toString() : null)
                        .addValue("FEC_TERM_MKT", row.get("FEC_TERM_MKT") != null ? row.get("FEC_TERM_MKT").toString() : null));
            }

            jdbc.update(sqlDelete, new MapSqlParameterSource());
            jdbc.update(sqlInsert, new MapSqlParameterSource());

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "CARGA_MARKET_MAKER_EXITOSA");
            mensajes.add(msg);
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
