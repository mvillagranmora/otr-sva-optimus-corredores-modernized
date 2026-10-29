package cl.bcs.plataforma.service.val;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Migrated from cl.bcs.plataforma.val.ValorizarSubyacentePacto (JavaCall).
 * Valuates subyacente pacto instruments using plazo-based formula.
 * Currency parity for non-CLP instruments requires mer.CalcularParidad (pla.* migration).
 */
@Service
public class ValorizarSubyacentePactoService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private static final BigDecimal BASE_REAJUSTABLE = new BigDecimal(360);
    private static final BigDecimal BASE_NO_REAJUSTABLE = new BigDecimal(30);

    private final NamedParameterJdbcTemplate jdbc;

    public ValorizarSubyacentePactoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.val.ValorizarSubyacentePacto";
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            String calculaFactor = params.has("p_CalculaFactor") ? params.get("p_CalculaFactor").getAsString() : "N";
            JsonArray instrumentosWS = params.has("p_InstrumentosWS")
                    ? params.get("p_InstrumentosWS").getAsJsonArray() : new JsonArray();

            JsonObject resultados = new JsonObject();
            JsonArray mensajes = new JsonArray();

            for (int i = 0; i < instrumentosWS.size(); i++) {
                JsonObject inst = instrumentosWS.get(i).getAsJsonObject();

                String idReajusteCabecera = inst.has("IdReajusteCabecera") && !inst.get("IdReajusteCabecera").isJsonNull()
                        ? inst.get("IdReajusteCabecera").getAsString() : "CLP";
                BigDecimal base = "CLP".equals(idReajusteCabecera) ? BASE_NO_REAJUSTABLE : BASE_REAJUSTABLE;

                String fechaEmision = inst.has("FechaEmision") ? inst.get("FechaEmision").getAsString() : null;
                String fechaValorizacion = inst.has("FechaValorizacion") ? inst.get("FechaValorizacion").getAsString() : null;
                BigDecimal monto = inst.has("Monto") && !inst.get("Monto").isJsonNull()
                        ? inst.get("Monto").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal tasa = inst.has("Tasa") && !inst.get("Tasa").isJsonNull()
                        ? inst.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO;
                Long idValorizacion = inst.has("IdValorizacion") ? inst.get("IdValorizacion").getAsLong() : null;

                BigDecimal plazo = BigDecimal.ZERO;
                if (fechaEmision != null && fechaValorizacion != null && fechaEmision.compareTo(fechaValorizacion) < 0) {
                    plazo = calcularPlazo(fechaEmision, fechaValorizacion);
                }

                BigDecimal montoSistema;
                if (plazo.compareTo(BigDecimal.ZERO) == 0) {
                    montoSistema = monto;
                } else {
                    BigDecimal plazoBase = plazo.divide(base, MathContext.DECIMAL128);
                    BigDecimal tasaDecimal = tasa.divide(new BigDecimal(100), MathContext.DECIMAL128);
                    montoSistema = monto.multiply(BigDecimal.ONE.add(tasaDecimal.multiply(plazoBase)))
                            .setScale(4, RoundingMode.HALF_UP);
                }

                JsonObject res = new JsonObject();
                res.add("IdValorizacion", inst.get("IdValorizacion"));
                res.addProperty("MontoMonedaSistema", montoSistema.toPlainString());
                res.addProperty("MontoTotalMonedaSistema", montoSistema.toPlainString());
                res.addProperty("ResultadoProceso", "Registro procesado");

                // TODO: requires pla.* utility — complete after pla subpackage migration
                // CalcularParidad needed for non-CLP instruments to convert MontoMonedaSistema
                // to MontoMonedaInstrumento and MontoTotalMonedaInstrumento.

                if (idValorizacion != null) {
                    resultados.add(idValorizacion.toString(), res);
                }
            }

            result.add("P_RESULTADOS_VALORIZACION", resultados);
            result.add("P_OUT_MENSAJES", mensajes);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }

    private BigDecimal calcularPlazo(String fechaInicio, String fechaFinal) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateInicio = sdf.parse(fechaInicio);
            Date dateFin = sdf.parse(fechaFinal);
            long diffMs = dateFin.getTime() - dateInicio.getTime();
            long dias = (TimeUnit.MINUTES.convert(diffMs, TimeUnit.MILLISECONDS) / 60) / 24;
            return new BigDecimal(dias);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}
