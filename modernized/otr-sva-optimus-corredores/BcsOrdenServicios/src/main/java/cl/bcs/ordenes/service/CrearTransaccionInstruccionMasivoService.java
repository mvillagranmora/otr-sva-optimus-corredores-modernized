package cl.bcs.ordenes.service;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.CrearTransaccionInstruccionMasivo.
 * Gets IdEntidad via SocioNegocio, then delegates to CrearTransaccionesService locally.
 */
@Service
public class CrearTransaccionInstruccionMasivoService {

    private static final Logger log = LoggerFactory.getLogger(CrearTransaccionInstruccionMasivoService.class);
    private static final String FQN_RESUMEN_USUARIO = "cl.bcs.socionegocio.soc.ObtenerResumenUsuario";

    private final NamedParameterJdbcTemplate jdbc;
    private final CrearTransaccionesService crearTransaccionesService;
    private final RestClient socionegocioClient;
    private final String sqlSelectTransacciones;

    public CrearTransaccionInstruccionMasivoService(
            NamedParameterJdbcTemplate jdbc,
            CrearTransaccionesService crearTransaccionesService,
            @Value("${bcs.socionegocio.url:http://bcs-socio-negocio-servicios:8098}") String socionegocioUrl) {
        this.jdbc = jdbc;
        this.crearTransaccionesService = crearTransaccionesService;
        this.socionegocioClient = RestClient.builder().baseUrl(socionegocioUrl).build();
        this.sqlSelectTransacciones = loadSql("CrearTransaccionInstruccionMasivo_SelectTransacciones.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearTransaccionInstruccionMasivo");
        JsonObject result = new JsonObject();
        result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        result.add("P_OUT_MENSAJES", new JsonArray());
        result.add("p_out_Transacciones", new JsonArray());

        try {
            long idEmpresa = longParam(params, "p_IdEmpresa", 1L);
            long idUsuario = longParam(params, "p_IdUsuario", 0L);
            String idModulo = strParam(params, "p_IdModulo", "");
            String usaReglasNegocio = strParam(params, "p_UsaReglasNegocio", "N");
            String idTipoCanalIngreso = strParam(params, "p_IdTipoCanalIngreso", "");
            JsonArray instrucciones = params.has("p_Instrucciones") && params.get("p_Instrucciones").isJsonArray()
                ? params.getAsJsonArray("p_Instrucciones") : new JsonArray();

            // Get IdEntidad from socionegocio ResumenUsuario
            long idEntidad = obtenerIdEntidad(idEmpresa, idUsuario);

            // Current timestamp from DB
            String fechaHoraSistema = getCurrentTimestamp(idEmpresa);

            // Build CrearTransacciones input
            JsonObject ctParams = new JsonObject();
            ctParams.addProperty("p_IdEmpresa", idEmpresa);
            ctParams.addProperty("p_IdUsuario", idUsuario);
            ctParams.addProperty("p_IdModulo", idModulo);
            ctParams.addProperty("p_UsaReglasNegocio", usaReglasNegocio);
            ctParams.addProperty("p_simularOperacion", "N");
            ctParams.add("p_Reproceso", JsonNull.INSTANCE);

            JsonArray transaccionesInput = new JsonArray();
            for (JsonElement el : instrucciones) {
                if (!el.isJsonObject()) continue;
                JsonObject instr = el.getAsJsonObject();
                transaccionesInput.add(mapInstruccionToTransaccion(instr, idEntidad, fechaHoraSistema));
            }
            ctParams.add("p_Transacciones", transaccionesInput);

            // Delegate to local CrearTransaccionesService
            JsonObject ctResult = crearTransaccionesService.execute(ctParams);
            long errCount = ctResult.has("P_OUT_ERRORESCONTADOR") ? ctResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
            result.addProperty("P_OUT_ERRORESCONTADOR", errCount);
            if (ctResult.has("P_OUT_MENSAJES")) result.add("P_OUT_MENSAJES", ctResult.get("P_OUT_MENSAJES"));
            if (errCount > 0) return result;

            // Build IdTransaccion → IdInterno map from resultado
            Map<Long, Long> idInternoPorIdTransaccion = new HashMap<>();
            List<Long> idsTransacciones = new ArrayList<>();
            if (ctResult.has("P_OUT_RESULTADO") && ctResult.get("P_OUT_RESULTADO").isJsonArray()) {
                for (JsonElement el : ctResult.getAsJsonArray("P_OUT_RESULTADO")) {
                    JsonObject r = el.getAsJsonObject();
                    long idTx = r.has("IdTransaccion") ? r.get("IdTransaccion").getAsLong() : 0L;
                    long idInt = r.has("IdInterno") ? r.get("IdInterno").getAsLong() : 0L;
                    idInternoPorIdTransaccion.put(idTx, idInt);
                    idsTransacciones.add(idTx);
                }
            }

            // Query final transacciones
            if (!idsTransacciones.isEmpty()) {
                MapSqlParameterSource qp = new MapSqlParameterSource("IdsTransacciones", idsTransacciones);
                List<Map<String, Object>> rows = jdbc.queryForList(sqlSelectTransacciones, qp);
                JsonArray transaccionesSalida = new JsonArray();
                for (Map<String, Object> row : rows) {
                    JsonObject t = new JsonObject();
                    row.forEach((k, v) -> {
                        if (v == null) t.add(k, JsonNull.INSTANCE);
                        else t.addProperty(k, v.toString());
                    });
                    long idTx = row.containsKey("IdTransaccion") ? ((Number) row.get("IdTransaccion")).longValue() : 0L;
                    t.addProperty("IdInterno", idInternoPorIdTransaccion.getOrDefault(idTx, 0L));
                    transaccionesSalida.add(t);
                }
                result.add("p_out_Transacciones", transaccionesSalida);
            }

        } catch (Exception e) {
            log.error("CrearTransaccionInstruccionMasivo failed", e);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        }

        log.info("FIN::CrearTransaccionInstruccionMasivo");
        return result;
    }

    private long obtenerIdEntidad(long idEmpresa, long idUsuario) {
        try {
            JsonObject snParams = new JsonObject();
            snParams.addProperty("p_IdEmpresa", idEmpresa);
            snParams.addProperty("p_IdUsuario", idUsuario);
            JsonObject snResponse = callSocioNegocio(FQN_RESUMEN_USUARIO, snParams);
            if (snResponse.has("IdEntidad") && !snResponse.get("IdEntidad").isJsonNull())
                return snResponse.get("IdEntidad").getAsLong();
        } catch (Exception e) {
            log.warn("obtenerIdEntidad failed (graceful): {}", e.getMessage());
        }
        return 0L;
    }

    private String getCurrentTimestamp(long idEmpresa) {
        try {
            return new Timestamp(System.currentTimeMillis()).toString();
        } catch (Exception e) {
            return new Timestamp(System.currentTimeMillis()).toString();
        }
    }

    private JsonObject mapInstruccionToTransaccion(JsonObject i, long idEntidad, String fechaHoraSistema) {
        JsonObject t = new JsonObject();
        String tipoInstr = strParam(i, "IdClasificacionTipoInstrumento", "RV");
        BigDecimal cantidad;
        if ("IF".equals(tipoInstr)) {
            cantidad = BigDecimal.ONE;
        } else if ("RF".equals(tipoInstr)) {
            cantidad = i.has("ValorNominal") && !i.get("ValorNominal").isJsonNull()
                ? i.get("ValorNominal").getAsBigDecimal() : BigDecimal.ZERO;
        } else {
            cantidad = i.has("Cantidad") && !i.get("Cantidad").isJsonNull()
                ? i.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO;
        }
        t.addProperty("Cantidad", cantidad);
        t.addProperty("CodigoOperadorCorredor", "");
        t.addProperty("CodigoOperadorContraparte", "");
        t.addProperty("FolioTransaccion", strParam(i, "IdInstruccion", "0"));
        t.addProperty("EsBilateral", "S");
        t.addProperty("EsCuentaPropia", "N");
        t.addProperty("EsFueraRueda", "S");
        t.addProperty("EsFungible", strParam(i, "EsFungible", ""));
        t.addProperty("EsRuteo", "N");
        t.addProperty("EsVentaCorta", "N");
        t.addProperty("FechaCreacion", strParam(i, "FechaTrade", ""));
        t.addProperty("FechaEmision", strParam(i, "FechaEmisionInstrumento", ""));
        t.addProperty("FechaLiquidacionContado", strParam(i, "FechaLiquidacion", ""));
        t.addProperty("FechaVencimiento", strParam(i, "FechaVencimientoInstruccion", ""));
        t.addProperty("FolioTransaccionPlazo", "");
        t.addProperty("GastosCorresponsal", BigDecimal.ZERO);
        // Extract time from system timestamp
        t.addProperty("HoraCreacion", fechaHoraSistema.length() >= 19 ? fechaHoraSistema.substring(11, 19) : "00:00:00");
        t.addProperty("IdBoveda", strParam(i, "IdBoveda", ""));
        t.addProperty("IdBolsa", strParam(i, "IdBolsa", ""));
        t.addProperty("IdCondicionDeLiquidacionContado", strParam(i, "IdCondicionDeLiquidacion", ""));
        t.addProperty("IdCorredoraContraparte", longParam(i, "IdIntermediario", 0L));
        t.addProperty("IdCorredoraCorresponsal", 0L);
        t.addProperty("IdEmisor", longParam(i, "IdEmisor", 0L));
        t.addProperty("IdEmpleado", idEntidad);
        t.addProperty("IdInstrumento", longParam(i, "IdInstrumento", 0L));
        t.addProperty("IdInstrumentoSubyacente", 0L);
        t.addProperty("IdInterno", longParam(i, "IdInstruccion", 0L));
        t.addProperty("IdLibroOrdenes", longParam(i, "IdLibroOrdenes", 0L));
        t.addProperty("IdMoneda", strParam(i, "IdMonedaInstrumento", ""));
        t.addProperty("IdMonedaEmision", strParam(i, "IdMonedaEmisionInstrumento", ""));
        t.addProperty("IdMonedaLiquidacionCorresponsal", "");
        t.addProperty("IdSistemaBursatil", "MANUAL");
        t.addProperty("IdTipoOperacion", strParam(i, "IdTipoOperacion", ""));
        t.addProperty("IdTipoReajuste", strParam(i, "IdTipoReajusteInstrumento", ""));
        t.addProperty("IndicadorDCV", "");
        t.addProperty("IndicadorOrdenDirecta", "");
        t.addProperty("Intereses", BigDecimal.ZERO);
        t.add("LiqFisicaOpciones", JsonNull.INSTANCE);
        t.addProperty("MarcaNegociacion", "");
        t.addProperty("MejorPrecioCompra", BigDecimal.ZERO);
        t.addProperty("MejorPrecioVenta", BigDecimal.ZERO);
        t.addProperty("Monto", i.has("MontoLiquidacion") && !i.get("MontoLiquidacion").isJsonNull()
            ? i.get("MontoLiquidacion").getAsBigDecimal() : BigDecimal.ZERO);
        t.addProperty("MontoLiquidacionCorresponsal", BigDecimal.ZERO);
        t.addProperty("MontoPlazo", BigDecimal.ZERO);
        t.addProperty("NemotecnicoEspecificoBolson", "");
        t.addProperty("NemotecnicoSVS", strParam(i, "NemotecnicoSVS", ""));
        t.addProperty("Plazo", "RV".equals(tipoInstr) ? 0L : longParam(i, "PlazoVencimiento", 0L));
        t.addProperty("Precio", i.has("PrecioInstruccion") && !i.get("PrecioInstruccion").isJsonNull()
            ? i.get("PrecioInstruccion").getAsBigDecimal() : BigDecimal.ZERO);
        t.addProperty("PrecioPlazo", BigDecimal.ZERO);
        t.addProperty("Reajustes", BigDecimal.ZERO);
        t.addProperty("Tasa", i.has("TirTasa") && !i.get("TirTasa").isJsonNull()
            ? i.get("TirTasa").getAsBigDecimal() : BigDecimal.ZERO);
        t.addProperty("TipoCambioCorresponsal", BigDecimal.ZERO);
        t.addProperty("ValorRescate", i.has("ValorRescate") && !i.get("ValorRescate").isJsonNull()
            ? i.get("ValorRescate").getAsBigDecimal() : BigDecimal.ZERO);
        t.addProperty("FlujoOrigen", "INGRESOINSTRUCCION");
        return t;
    }

    private JsonObject callSocioNegocio(String fqn, JsonObject snParams) {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("class", fqn);
            body.add("params", snParams);
            String resp = socionegocioClient.post()
                .uri("/internal/java-call")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body.toString())
                .retrieve()
                .body(String.class);
            if (resp == null || resp.isBlank()) return new JsonObject();
            return JsonParser.parseString(resp).getAsJsonObject();
        } catch (Exception e) {
            log.warn("SocioNegocio call {} failed (graceful): {}", fqn, e.getMessage());
            return new JsonObject();
        }
    }

    private String strParam(JsonObject p, String key, String def) {
        return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsString() : def;
    }

    private long longParam(JsonObject p, String key, long def) {
        try { return p.has(key) && !p.get(key).isJsonNull() ? p.get(key).getAsLong() : def; }
        catch (Exception e) { return def; }
    }

    private String loadSql(String fileName) {
        String path = "cl/bcs/ordenes/sql/" + fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return "-- SQL NOT FOUND: " + path;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "-- SQL NOT FOUND: " + path;
        }
    }
}
