package cl.bcs.plataforma.service.ins;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ConsultarNemoNegociacionService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public ConsultarNemoNegociacionService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.ins.ConsultarNemoNegociacion"; }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            String sql = loadSql("cl/bcs/plataforma/ins/sql/BuscarNemoNegociacion_Select.sql");

            JsonArray datosIn = params.has("p_DatosInstrumentoConsulta") && params.get("p_DatosInstrumentoConsulta").isJsonArray()
                    ? params.getAsJsonArray("p_DatosInstrumentoConsulta") : new JsonArray();

            List<Long> listaIds = new ArrayList<>();
            for (JsonElement el : datosIn) {
                JsonObject d = el.getAsJsonObject();
                if (d.has("IdInstrumentoRaiz") && !d.get("IdInstrumentoRaiz").isJsonNull()) {
                    listaIds.add(d.get("IdInstrumentoRaiz").getAsLong());
                }
            }

            if (listaIds.isEmpty()) listaIds.add(-1L);

            List<Map<String, Object>> rows = jdbc.queryForList(sql, new MapSqlParameterSource()
                    .addValue("IdInstrumento", listaIds));

            Map<String, Map<String, Object>> byRaizCondMoneda = rows.stream()
                    .filter(r -> r.get("IdInstrumentoRaiz") != null)
                    .collect(Collectors.toMap(
                            r -> r.get("IdInstrumentoRaiz").toString() + "_"
                                    + (r.get("IdCondicionLiquidacion") != null ? r.get("IdCondicionLiquidacion").toString() : "") + "_"
                                    + (r.get("IdMoneda") != null ? r.get("IdMoneda").toString() : ""),
                            r -> r,
                            (a, b) -> a));

            Map<String, Map<String, Object>> byIdMoneda = rows.stream()
                    .filter(r -> r.get("IdInstrumento") != null)
                    .collect(Collectors.toMap(
                            r -> r.get("IdInstrumento").toString() + "_"
                                    + (r.get("IdMoneda") != null ? r.get("IdMoneda").toString() : ""),
                            r -> r,
                            (a, b) -> a));

            JsonArray salida = new JsonArray();
            long errores = 0L;
            JsonArray mensajes = new JsonArray();

            for (JsonElement el : datosIn) {
                JsonObject d = el.getAsJsonObject();
                long idConsulta = d.has("IdConsulta") ? d.get("IdConsulta").getAsLong() : 0L;
                Long idRaiz = d.has("IdInstrumentoRaiz") && !d.get("IdInstrumentoRaiz").isJsonNull()
                        ? d.get("IdInstrumentoRaiz").getAsLong() : null;
                String moneda = d.has("IdMoneda") && !d.get("IdMoneda").isJsonNull()
                        ? d.get("IdMoneda").getAsString() : "";
                String condLiq = d.has("IdCondLiquidacion") && !d.get("IdCondLiquidacion").isJsonNull()
                        ? d.get("IdCondLiquidacion").getAsString() : "";

                JsonObject item = new JsonObject();
                item.addProperty("IdConsulta", idConsulta);
                item.addProperty("IdInstrumentoConsultado", idRaiz);
                item.addProperty("IdMoneda", moneda);
                item.addProperty("IdCondLiquidacion", condLiq);

                String keyRaiz = idRaiz + "_" + condLiq + "_" + moneda;
                String keyId = idRaiz + "_" + moneda;

                Map<String, Object> found = byRaizCondMoneda.containsKey(keyRaiz)
                        ? byRaizCondMoneda.get(keyRaiz)
                        : byIdMoneda.get(keyId);

                if (found != null) {
                    item.addProperty("IdInstrumento", found.get("IdInstrumento") != null ? found.get("IdInstrumento").toString() : "0");
                    item.addProperty("Nemotecnico", found.get("Nemotecnico") != null ? found.get("Nemotecnico").toString() : "");
                } else {
                    item.addProperty("IdInstrumento", 0);
                    item.addProperty("Nemotecnico", "");
                    errores = 1L;
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "ERROR");
                    msg.addProperty("Codigo", "INSTRUMENTO_NEGOCIACION_NO_EXISTE");
                    mensajes.add(msg);
                }
                salida.add(item);
            }

            result.add("P_InstrumentoSalida", salida);
            result.addProperty("P_OUT_ERRORESCONTADOR", errores);
            result.add("P_OUT_MENSAJES", mensajes);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            JsonArray msgs = new JsonArray();
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Descripcion", e.getMessage());
            msgs.add(msg);
            result.add("P_OUT_MENSAJES", msgs);
        }
        return result;
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
