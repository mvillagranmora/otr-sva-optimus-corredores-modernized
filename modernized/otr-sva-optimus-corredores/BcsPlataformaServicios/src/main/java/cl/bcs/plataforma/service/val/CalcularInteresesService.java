package cl.bcs.plataforma.service.val;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from cl.bcs.plataforma.val.CalcularIntereses (JavaCall).
 * Performs simple and compound interest calculations.
 * Currency parity calculations require pla.* utilities.
 */
@Service
public class CalcularInteresesService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public CalcularInteresesService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.CalcularIntereses";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("IdEmpresa") ? params.get("IdEmpresa").getAsLong() : null;
            JsonArray datosEntrada = params.has("DatosEntrada") ? params.get("DatosEntrada").getAsJsonArray() : new JsonArray();

            JsonArray resultados = new JsonArray();

            for (int i = 0; i < datosEntrada.size(); i++) {
                JsonObject info = datosEntrada.get(i).getAsJsonObject();

                Long plazo = info.has("Plazo") && !info.get("Plazo").isJsonNull() ? info.get("Plazo").getAsLong() : null;
                Long plazoResidual = info.has("PlazoResidual") && !info.get("PlazoResidual").isJsonNull() ? info.get("PlazoResidual").getAsLong() : 0L;
                BigDecimal montoConsulta = info.has("MontoMonedaConsulta") && !info.get("MontoMonedaConsulta").isJsonNull()
                        ? info.get("MontoMonedaConsulta").getAsBigDecimal() : null;
                BigDecimal tasa = info.has("Tasa") && !info.get("Tasa").isJsonNull()
                        ? info.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO;
                Long base = info.has("Base") && !info.get("Base").isJsonNull() ? info.get("Base").getAsLong() : 0L;
                String tipoInteres = info.has("TipoInteres") && !info.get("TipoInteres").isJsonNull()
                        ? info.get("TipoInteres").getAsString() : "SIMPLE";
                BigDecimal cantidad = info.has("Cantidad") && !info.get("Cantidad").isJsonNull()
                        ? info.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal precioCompra = info.has("PrecioCompra") && !info.get("PrecioCompra").isJsonNull()
                        ? info.get("PrecioCompra").getAsBigDecimal() : BigDecimal.ZERO;

                JsonObject resultItem = new JsonObject();

                if (plazo == null || montoConsulta == null) {
                    resultItem.addProperty("ResultadoProceso", "ERROR");
                    resultados.add(resultItem);
                    continue;
                }

                long diasTranscurridos = plazo - plazoResidual;
                resultItem.addProperty("DiasTranscurridos", diasTranscurridos);

                if (tipoInteres.equalsIgnoreCase("SIMPLE")) {
                    BigDecimal tasaPorPlazo = base.equals(0L) ? BigDecimal.ZERO
                            : tasa.divide(new BigDecimal(100))
                                    .multiply(new BigDecimal(plazo).divide(new BigDecimal(base), 20, RoundingMode.HALF_UP));
                    BigDecimal tasaPorDias = base.equals(0L) ? BigDecimal.ZERO
                            : tasa.divide(new BigDecimal(100))
                                    .multiply(new BigDecimal(diasTranscurridos).divide(new BigDecimal(base), 20, RoundingMode.HALF_UP));

                    BigDecimal interesAcumulado = (cantidad.multiply(precioCompra)
                            .multiply(tasa).divide(new BigDecimal(100)))
                            .divide(new BigDecimal(base == 0L ? 1L : base), 20, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(diasTranscurridos));
                    BigDecimal interesDiario = (cantidad.multiply(precioCompra)
                            .multiply(tasa).divide(new BigDecimal(100)))
                            .divide(new BigDecimal(base == 0L ? 1L : base), 20, RoundingMode.HALF_UP);
                    BigDecimal montoFinal = cantidad.multiply(precioCompra)
                            .multiply(BigDecimal.ONE.add(tasaPorPlazo));
                    BigDecimal valorPresente = cantidad.multiply(precioCompra)
                            .multiply(BigDecimal.ONE.add(tasaPorDias));

                    resultItem.addProperty("TasaPorPlazo", tasaPorPlazo.toPlainString());
                    resultItem.addProperty("TasaPorDiasTranscurridos", tasaPorDias.toPlainString());
                    resultItem.addProperty("InteresAcumuladoMonedaConsulta", interesAcumulado.toPlainString());
                    resultItem.addProperty("InteresDiarioMonedaConsulta", interesDiario.toPlainString());
                    resultItem.addProperty("MontoMonedaConsulta", montoFinal.toPlainString());
                    resultItem.addProperty("ValorPresenteMonedaConsulta", valorPresente.toPlainString());

                } else if (tipoInteres.equalsIgnoreCase("COMPUESTO")) {
                    BigDecimal tasaDecimal = base.equals(0L) ? BigDecimal.ZERO : tasa.divide(new BigDecimal(100));
                    BigDecimal periodo = new BigDecimal(plazo).divide(new BigDecimal(base == 0L ? 1L : base), 20, RoundingMode.HALF_UP);
                    BigDecimal periodoTranscurrido = new BigDecimal(diasTranscurridos).divide(new BigDecimal(base == 0L ? 1L : base), 20, RoundingMode.HALF_UP);

                    double factor = Math.pow(BigDecimal.ONE.add(tasaDecimal).doubleValue(), periodo.doubleValue());
                    double factorTranscurrido = Math.pow(BigDecimal.ONE.add(tasaDecimal).doubleValue(), periodoTranscurrido.doubleValue());

                    BigDecimal interesDiario = montoConsulta.multiply(new BigDecimal(factor))
                            .subtract(montoConsulta.multiply(new BigDecimal(factorTranscurrido)));
                    BigDecimal interesAcumulado = montoConsulta.multiply(new BigDecimal(factorTranscurrido))
                            .subtract(montoConsulta);
                    BigDecimal valorPresente = montoConsulta.multiply(new BigDecimal(factorTranscurrido));
                    BigDecimal montoFinal = montoConsulta.multiply(new BigDecimal(factor));

                    resultItem.addProperty("InteresDiarioMonedaConsulta", interesDiario.toPlainString());
                    resultItem.addProperty("InteresAcumuladoMonedaConsulta", interesAcumulado.toPlainString());
                    resultItem.addProperty("ValorPresenteMonedaConsulta", valorPresente.toPlainString());
                    resultItem.addProperty("MontoMonedaConsulta", montoFinal.toPlainString());
                }

                resultItem.addProperty("ResultadoProceso", "OK");
                // TODO: requires pla.* utility — complete after pla subpackage migration
                // (MonedaSistema, MonedaAdicional lookup and CalcularParidad for multi-currency output)
                resultados.add(resultItem);
            }

            result.add("ResultadoCalculoInteres", resultados);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
