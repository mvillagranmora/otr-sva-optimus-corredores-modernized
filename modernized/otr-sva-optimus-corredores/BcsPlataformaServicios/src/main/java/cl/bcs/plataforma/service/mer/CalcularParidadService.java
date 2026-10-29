package cl.bcs.plataforma.service.mer;

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

@Service
public class CalcularParidadService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CalcularParidadService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.mer.CalcularParidad"; }

    @Override
    public JsonObject execute(JsonObject params) {
        // TODO: requires pla.* utility (ConfiguracionSistema) — complete after pla subpackage migration
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        long errores = 0L;

        try {
            String sqlUsos = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectUsos.sql");
            String sqlParamFecha = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametroFinancieroFechaSolicitada.sql");
            String sqlPivoteFecha = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectPivoteFechaSolicitada.sql");
            String sqlPivoteDefault = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectPivoteDefault.sql");
            String sqlParamsFinancieros = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametrosFinancieros.sql");
            String sqlParamsFinancierosClp = loadSql("cl/bcs/plataforma/mer/sql/CalcularParidad_SelectParametrosFinancierosValorCLP.sql");

            JsonArray calculosEntrada = params.has("p_CalculosEntrada") && params.get("p_CalculosEntrada").isJsonArray()
                    ? params.getAsJsonArray("p_CalculosEntrada") : new JsonArray();

            JsonArray salida = new JsonArray();
            for (JsonElement el : calculosEntrada) {
                JsonObject calc = el.getAsJsonObject();
                String fecha = calc.has("Fecha") && !calc.get("Fecha").isJsonNull() ? calc.get("Fecha").getAsString() : null;
                String monedaOrigen = calc.has("IdMonedaOrigen") && !calc.get("IdMonedaOrigen").isJsonNull() ? calc.get("IdMonedaOrigen").getAsString() : null;
                String monedaDestino = calc.has("IdMonedaDestino") && !calc.get("IdMonedaDestino").isJsonNull() ? calc.get("IdMonedaDestino").getAsString() : null;
                String idUso = calc.has("IdUso") && !calc.get("IdUso").isJsonNull() ? calc.get("IdUso").getAsString() : "";

                List<Map<String, Object>> params1 = jdbc.queryForList(sqlParamFecha,
                        new MapSqlParameterSource()
                                .addValue("IdMonedaOrigen", monedaOrigen)
                                .addValue("IdMonedaDestino", monedaDestino)
                                .addValue("Fecha", fecha)
                                .addValue("IdUso", idUso));

                JsonObject calcSalida = new JsonObject();
                calcSalida.addProperty("IdInterno", calc.has("IdInterno") ? calc.get("IdInterno").getAsLong() : 0L);
                calcSalida.addProperty("Fecha", fecha);
                calcSalida.addProperty("IdMonedaOrigen", monedaOrigen);
                calcSalida.addProperty("IdMonedaDestino", monedaDestino);
                if (!params1.isEmpty()) {
                    Map<String, Object> row = params1.get(0);
                    calcSalida.addProperty("FactorCambio", row.get("Valor") != null ? row.get("Valor").toString() : "0");
                } else {
                    calcSalida.addProperty("FactorCambio", "0");
                }
                salida.add(calcSalida);
            }

            result.add("P_OUT_CALCULOS_SALIDA", salida);
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
