package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

/**
 * Multi-portafolio cash flow view: delegates to ConsultarFlujoCaja, then (when
 * multiple portafolios are present) converts results to CLP via CalcularParidad.
 *
 * CalcularParidad is in BcsPlataformaServicios (mer package) and can be injected
 * directly. If not yet migrated it is called as a JavaCall via the internal registry.
 */
@Service
public class ConsultarFlujoCajaClienteService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarFlujoCajaClienteService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Random RAND = new Random();

    private final ConsultarFlujoCajaService consultarFlujoCaja;
    private final JavaCallService calcularParidad;

    public ConsultarFlujoCajaClienteService(ConsultarFlujoCajaService consultarFlujoCaja,
                                             java.util.List<JavaCallService> allServices) {
        this.consultarFlujoCaja = consultarFlujoCaja;
        this.calcularParidad = allServices.stream()
            .filter(s -> "cl.bcs.plataforma.mer.CalcularParidad".equals(s.fqn()))
            .findFirst().orElse(null);
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.ConsultarFlujoCajaCliente"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ConsultarFlujoCajaCliente");
        try {
            JsonObject flujoCajaOut = consultarFlujoCaja.execute(params);

            long errores = flujoCajaOut.has("P_OUT_ERRORESCONTADOR") ? flujoCajaOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
            if (errores > 0) return flujoCajaOut;

            JsonArray portafoliosJson = params.has("p_PortafolioAConsultar") ? params.getAsJsonArray("p_PortafolioAConsultar") : new JsonArray();
            if (portafoliosJson.size() <= 1) return flujoCajaOut;
            if (calcularParidad == null) {
                log.warn("CalcularParidad not wired — returning single-currency cash flow");
                return flujoCajaOut;
            }

            String fechaDesde = params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : "";
            Long idEmpresa    = params.has("p_IdEmpresa")  ? params.get("p_IdEmpresa").getAsLong() : 1L;

            // Build CalcularParidad input from flujoCaja results
            JsonArray flujoCaja = flujoCajaOut.has("P_OUT_FLUJOCAJA") ? flujoCajaOut.getAsJsonArray("P_OUT_FLUJOCAJA") : new JsonArray();
            JsonArray calculos = new JsonArray();
            for (JsonElement el : flujoCaja) {
                JsonObject fc = el.getAsJsonObject();
                JsonObject calculo = new JsonObject();
                calculo.addProperty("IdTrabajo",       Math.abs(RAND.nextLong() % 10000000000L));
                calculo.addProperty("IdInterno",       0L);
                calculo.addProperty("Fecha",           fc.has("FechaSaldo") ? fc.get("FechaSaldo").getAsString() : fechaDesde);
                calculo.addProperty("IdMonedaOrigen",  fc.has("IdMoneda") ? fc.get("IdMoneda").getAsString() : "CLP");
                calculo.addProperty("IdMonedaDestino", "CLP");
                calculo.addProperty("MontoOrigen",     fc.has("Saldo") ? fc.get("Saldo").getAsBigDecimal() : BigDecimal.ZERO);
                calculo.addProperty("IdUso",           fc.has("NombreUso") ? fc.get("NombreUso").getAsString() : "");
                calculos.add(calculo);
            }

            JsonObject paridadParams = new JsonObject();
            paridadParams.addProperty("p_IdEmpresa", idEmpresa);
            paridadParams.addProperty("p_IdTrabajo", Math.abs(RAND.nextLong() % 10000000000L));
            paridadParams.add("p_CalculosEntrada", calculos);

            JsonObject paridadOut = calcularParidad.execute(paridadParams);
            long paridadErr = paridadOut.has("P_OUT_ERRORESCONTADOR") ? paridadOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;

            if (paridadErr > 0) {
                flujoCajaOut.addProperty("P_OUT_ERRORESCONTADOR", errores + paridadErr);
                JsonArray msgs = flujoCajaOut.has("P_OUT_MENSAJES") ? flujoCajaOut.getAsJsonArray("P_OUT_MENSAJES") : new JsonArray();
                JsonObject errMsg = new JsonObject();
                errMsg.addProperty("tipo", "ERROR"); errMsg.addProperty("codigo", "ERROR_PARIDAD");
                msgs.add(errMsg);
                flujoCajaOut.add("P_OUT_MENSAJES", msgs);
                return flujoCajaOut;
            }

            // Aggregate paridad results by NombreUso (sum saldos in CLP)
            JsonArray calculosSalida = paridadOut.has("p_out_CalculosSalida") ? paridadOut.getAsJsonArray("p_out_CalculosSalida") : new JsonArray();
            Map<String, JsonObject> flujosAgrupados = new LinkedHashMap<>();

            for (JsonElement el : calculosSalida) {
                JsonObject cs = el.getAsJsonObject();
                String nombreUso = cs.has("IdUso") ? cs.get("IdUso").getAsString() : "";
                String monedaDest = cs.has("IdMonedaDestino") ? cs.get("IdMonedaDestino").getAsString() : "CLP";
                BigDecimal montoDestino = cs.has("MontoDestino") ? cs.get("MontoDestino").getAsBigDecimal() : BigDecimal.ZERO;

                // IdPortafolio from the matching portafolio in the original request (first that matches IdMonedaOrigen)
                String monedaOrigen = cs.has("IdMonedaOrigen") ? cs.get("IdMonedaOrigen").getAsString() : "";
                Long idPortafolio = 0L;
                for (JsonElement pe : portafoliosJson) {
                    JsonObject po = pe.getAsJsonObject();
                    String pMon = po.has("IdMoneda") && !po.get("IdMoneda").isJsonNull() ? po.get("IdMoneda").getAsString() : "";
                    if (monedaOrigen.equalsIgnoreCase(pMon)) {
                        idPortafolio = po.has("IdPortafolio") ? po.get("IdPortafolio").getAsLong() : 0L;
                        break;
                    }
                }

                JsonObject existing = flujosAgrupados.get(nombreUso);
                if (existing == null) {
                    JsonObject fc = new JsonObject();
                    fc.addProperty("FechaSaldo",   fechaDesde);
                    fc.addProperty("NombreUso",    nombreUso);
                    fc.addProperty("IdMoneda",     monedaDest);
                    fc.addProperty("IdPortafolio", idPortafolio);
                    fc.addProperty("Saldo",        montoDestino);
                    flujosAgrupados.put(nombreUso, fc);
                } else {
                    BigDecimal prev = existing.has("Saldo") ? existing.get("Saldo").getAsBigDecimal() : BigDecimal.ZERO;
                    existing.addProperty("Saldo", prev.add(montoDestino));
                }
            }

            JsonArray newFlujoCaja = new JsonArray();
            flujosAgrupados.values().forEach(newFlujoCaja::add);
            flujoCajaOut.add("P_OUT_FLUJOCAJA", newFlujoCaja);

            return flujoCajaOut;
        } catch (Exception e) {
            log.error("Error en ConsultarFlujoCajaCliente: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            return err;
        }
    }
}
