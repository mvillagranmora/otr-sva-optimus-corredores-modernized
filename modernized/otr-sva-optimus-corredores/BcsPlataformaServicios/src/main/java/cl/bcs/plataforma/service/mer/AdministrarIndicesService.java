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
import java.util.List;
import java.util.Map;

@Service
public class AdministrarIndicesService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public AdministrarIndicesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.AdministrarIndices"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlConsultar = loadSql("cl/bcs/plataforma/mer/sql/AdministrarIndices_ConsultarIndiceFinanciero.sql");
            String sqlConsultarValor = loadSql("cl/bcs/plataforma/mer/sql/AdministrarIndices_ConsultarValorIndiceFinanciero.sql");
            String sqlCrear = loadSql("cl/bcs/plataforma/mer/sql/AdministrarIndices_CrearIndiceFinanciero.sql");
            String sqlUpdate = loadSql("cl/bcs/plataforma/mer/sql/AdministrarIndices_UpdateIndiceFinanciero.sql");

            JsonArray parametros = params.has("p_ParametrosFinancieros") && params.get("p_ParametrosFinancieros").isJsonArray()
                    ? params.getAsJsonArray("p_ParametrosFinancieros") : new JsonArray();

            for (JsonElement el : parametros) {
                JsonObject p = el.getAsJsonObject();
                String idIndice = p.has("IdIndiceFinanciero") && !p.get("IdIndiceFinanciero").isJsonNull()
                        ? p.get("IdIndiceFinanciero").getAsString() : null;
                String fecha = p.has("Fecha") && !p.get("Fecha").isJsonNull()
                        ? p.get("Fecha").getAsString() : null;
                BigDecimal valor = p.has("Valor") && !p.get("Valor").isJsonNull()
                        ? p.get("Valor").getAsBigDecimal() : BigDecimal.ZERO;

                List<Map<String, Object>> existing = jdbc.queryForList(sqlConsultarValor,
                        new MapSqlParameterSource()
                                .addValue("IdIndiceFinanciero", idIndice)
                                .addValue("Fecha", fecha));

                if (existing.isEmpty()) {
                    jdbc.update(sqlCrear, new MapSqlParameterSource()
                            .addValue("IdIndiceFinanciero", idIndice)
                            .addValue("Fecha", fecha)
                            .addValue("Valor", valor));
                } else {
                    jdbc.update(sqlUpdate, new MapSqlParameterSource()
                            .addValue("IdIndiceFinanciero", idIndice)
                            .addValue("Fecha", fecha)
                            .addValue("Valor", valor));
                }
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "INDICES_ADMINISTRADOS");
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
