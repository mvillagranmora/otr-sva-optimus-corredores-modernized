package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Migrated from legacy cl.bcs.ordenes.CalcularCantidadVisible (JavaCall).
 * Computes order visibility percentage based on monto padrón and exchange rate.
 */
@Service
public class CalcularCantidadVisibleService {

    private static final Logger log = LoggerFactory.getLogger(CalcularCantidadVisibleService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;

    public CalcularCantidadVisibleService(NamedParameterJdbcTemplate jdbc,
                                          ConfiguracionSistemaUtil configUtil,
                                          PlataformaCallClient plataformaClient) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
    }

    public JsonObject execute(JsonObject params) {
        log.debug("CalcularCantidadVisibleService.execute()");
        try {
            Long idEmpresa     = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            String monedaInstr = params.has("p_MonedaInstrumento") ? params.get("p_MonedaInstrumento").getAsString() : "CLP";
            String montoBrutoStr = params.has("p_MontoBruto") ? params.get("p_MontoBruto").getAsString() : "0";
            String fecha       = params.has("p_Fecha") ? params.get("p_Fecha").getAsString() : null;
            Long visibilidadParam = params.has("p_VisibilidadParam") && !params.get("p_VisibilidadParam").isJsonNull()
                ? params.get("p_VisibilidadParam").getAsLong() : null;

            BigDecimal montoPadron;
            try {
                String mpStr = configUtil.getParametroString(idEmpresa, "MontoPadron");
                montoPadron = (mpStr != null && !mpStr.isBlank()) ? new BigDecimal(mpStr) : BigDecimal.ONE;
                if (montoPadron.compareTo(BigDecimal.ZERO) == 0) montoPadron = BigDecimal.ONE;
            } catch (Exception e) {
                montoPadron = BigDecimal.ONE;
            }

            if (!"CLP".equals(monedaInstr)) {
                JsonObject paridadParams = new JsonObject();
                paridadParams.addProperty("p_IdEmpresa", idEmpresa);
                paridadParams.addProperty("p_IdTrabajo", 1156L);
                JsonArray calculos = new JsonArray();
                JsonObject calculo = new JsonObject();
                calculo.addProperty("IdTrabajo", 1156L);
                calculo.addProperty("IdInterno", 1L);
                calculo.addProperty("Fecha", fecha);
                calculo.addProperty("IdMonedaOrigen", "CLP");
                calculo.addProperty("IdMonedaDestino", monedaInstr);
                calculo.addProperty("ValorCambioLocalModificado", BigDecimal.ZERO.toPlainString());
                calculo.addProperty("MontoOrigen", montoPadron.toPlainString());
                calculo.addProperty("IdUso", "ORDENES");
                calculos.add(calculo);
                paridadParams.add("p_CalculosEntrada", calculos);

                JsonObject paridadResult = plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", paridadParams);
                if (paridadResult.has("p_out_CalculosSalida")) {
                    JsonArray salidas = paridadResult.getAsJsonArray("p_out_CalculosSalida");
                    if (salidas.size() > 0) {
                        JsonObject s = salidas.get(0).getAsJsonObject();
                        if (s.has("MontoDestino") && !s.get("MontoDestino").isJsonNull()) {
                            BigDecimal montoDestino = s.get("MontoDestino").getAsBigDecimal();
                            MathContext mc = new MathContext(8);
                            montoPadron = montoDestino.round(mc);
                        }
                    }
                }
            }

            BigDecimal montoBruto = new BigDecimal(montoBrutoStr.replace(".", ""));
            BigDecimal resultadoDivision = montoPadron.compareTo(BigDecimal.ZERO) != 0
                ? montoBruto.divide(montoPadron, 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

            String porcentaje = "";
            List<String> arregloPorcentaje = new ArrayList<>();

            if (visibilidadParam != null && visibilidadParam <= 5) {
                for (long i = 1; i <= visibilidadParam; i++) {
                    arregloPorcentaje.add(String.valueOf(i));
                }
                porcentaje = String.valueOf(visibilidadParam);
                if (visibilidadParam == 1) arregloPorcentaje.clear();
            } else {
                double d = resultadoDivision.doubleValue();
                if (d >= 0 && d < 2) {
                    porcentaje = "1";
                } else if (d >= 2 && d < 4) {
                    porcentaje = "2";
                    arregloPorcentaje.add("1"); arregloPorcentaje.add("2");
                } else if (d >= 4 && d < 5) {
                    porcentaje = "3";
                    arregloPorcentaje.add("1"); arregloPorcentaje.add("2"); arregloPorcentaje.add("3");
                } else if (d >= 5 && d < 10) {
                    porcentaje = "4";
                    arregloPorcentaje.add("1"); arregloPorcentaje.add("2"); arregloPorcentaje.add("3"); arregloPorcentaje.add("4");
                } else if (d >= 10) {
                    porcentaje = "5";
                    arregloPorcentaje.add("1"); arregloPorcentaje.add("2"); arregloPorcentaje.add("3"); arregloPorcentaje.add("4"); arregloPorcentaje.add("5");
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("p_out_Porcentaje", porcentaje);
            out.add("p_out_arregloPorcentaje", JsonParser.parseString(GSON.toJson(arregloPorcentaje)));
            return out;
        } catch (Exception e) {
            log.error("CalcularCantidadVisibleService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }
}
