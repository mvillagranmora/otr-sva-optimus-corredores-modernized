package cl.bcs.fondomutuo.adapter.out.hana;

import cl.bcs.fondomutuo.port.out.ParidadPort;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * REST adapter for parity calculations.
 * Delegates to cl.bcs.plataforma.mer.CalcularParidad in BcsPlataformaServicios
 * via the /internal/java-call protocol.
 */
@Component
public class RestParidadAdapter implements ParidadPort {

    private static final Logger log = LoggerFactory.getLogger(RestParidadAdapter.class);
    private static final String PARIDAD_FQN = "cl.bcs.plataforma.mer.CalcularParidad";

    private final RestClient restClient;
    private final String plataformaUrl;

    public RestParidadAdapter(
            @Value("${services.paridad.url:http://bcs-plataforma-servicios:8114}") String plataformaUrl) {
        this.plataformaUrl = plataformaUrl;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public BigDecimal calcularParidad(String idMonedaOrigen, String idMonedaDestino, LocalDate fecha, Long idEmpresa) {
        try {
            JsonObject params = new JsonObject();
            params.addProperty("p_IdEmpresa", idEmpresa);
            params.addProperty("p_IdTrabajo", 1);
            com.google.gson.JsonArray calculos = new com.google.gson.JsonArray();
            JsonObject calculo = new JsonObject();
            calculo.addProperty("IdTrabajo", 1);
            calculo.addProperty("IdInterno", 1);
            calculo.addProperty("Fecha", fecha != null ? fecha.toString() : LocalDate.now().toString());
            calculo.addProperty("IdMonedaOrigen", idMonedaOrigen);
            calculo.addProperty("IdMonedaDestino", idMonedaDestino);
            calculo.addProperty("ValorCambioLocalModificado", 0);
            calculo.addProperty("MontoOrigen", 1);
            calculo.addProperty("IdUso", "");
            calculos.add(calculo);
            params.add("p_CalculosEntrada", calculos);

            JsonObject body = new JsonObject();
            body.addProperty("class", PARIDAD_FQN);
            body.add("params", params);

            String response = restClient.post()
                    .uri(plataformaUrl + "/internal/java-call")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            if (result.has("p_out_CalculosSalida") && result.getAsJsonArray("p_out_CalculosSalida").size() > 0) {
                JsonObject calcs = result.getAsJsonArray("p_out_CalculosSalida").get(0).getAsJsonObject();
                if (calcs.has("FactorCambioLocal")) {
                    return calcs.get("FactorCambioLocal").getAsBigDecimal();
                }
            }
        } catch (Exception e) {
            log.warn("CalcularParidad call failed for {}->{}: {}", idMonedaOrigen, idMonedaDestino, e.getMessage());
        }
        return BigDecimal.ONE;
    }

    @Override
    public BigDecimal convertirMonto(BigDecimal monto, String idMonedaOrigen, String idMonedaDestino, LocalDate fecha, Long idEmpresa) {
        if (idMonedaOrigen.equalsIgnoreCase(idMonedaDestino)) return monto;
        BigDecimal paridad = calcularParidad(idMonedaOrigen, idMonedaDestino, fecha, idEmpresa);
        if (paridad.compareTo(BigDecimal.ZERO) == 0) return monto;
        // Assume origen→destino: if paridad > 1 (e.g. CLP/USD), divide; else multiply
        // Legacy uses MontoDestino from CalcularParidad directly, so we apply factor
        try {
            JsonObject params = new JsonObject();
            params.addProperty("p_IdEmpresa", idEmpresa);
            params.addProperty("p_IdTrabajo", 1);
            com.google.gson.JsonArray calculos = new com.google.gson.JsonArray();
            JsonObject calculo = new JsonObject();
            calculo.addProperty("IdTrabajo", 1);
            calculo.addProperty("IdInterno", 1);
            calculo.addProperty("Fecha", fecha != null ? fecha.toString() : LocalDate.now().toString());
            calculo.addProperty("IdMonedaOrigen", idMonedaOrigen);
            calculo.addProperty("IdMonedaDestino", idMonedaDestino);
            calculo.addProperty("ValorCambioLocalModificado", 0);
            calculo.addProperty("MontoOrigen", monto);
            calculo.addProperty("IdUso", "");
            calculos.add(calculo);
            params.add("p_CalculosEntrada", calculos);

            JsonObject body = new JsonObject();
            body.addProperty("class", PARIDAD_FQN);
            body.add("params", params);

            String response = restClient.post()
                    .uri(plataformaUrl + "/internal/java-call")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body.toString())
                    .retrieve()
                    .body(String.class);

            JsonObject result = JsonParser.parseString(response).getAsJsonObject();
            if (result.has("p_out_CalculosSalida") && result.getAsJsonArray("p_out_CalculosSalida").size() > 0) {
                JsonObject calcs = result.getAsJsonArray("p_out_CalculosSalida").get(0).getAsJsonObject();
                if (calcs.has("MontoDestino")) {
                    return calcs.get("MontoDestino").getAsBigDecimal();
                }
            }
        } catch (Exception e) {
            log.warn("convertirMonto call failed: {}", e.getMessage());
        }
        return monto;
    }
}
