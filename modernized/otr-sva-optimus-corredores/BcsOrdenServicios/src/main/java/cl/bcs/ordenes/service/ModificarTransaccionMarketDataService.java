package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import cl.bcs.ordenes.util.ObtenerPlazoUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ModificarTransaccionMarketData (JavaCall).
 * Processes SIM market-data modification messages: resolves dates via ObtenerDiaSiguiente,
 * matches transactions by hash key, delegates to ModificarTransaccionService.
 */
@Service
public class ModificarTransaccionMarketDataService {

    private static final Logger log = LoggerFactory.getLogger(ModificarTransaccionMarketDataService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ObtenerPlazoUtil plazoUtil;
    private final ModificarTransaccionService modificarTransaccionService;

    private final String SQL_SELECT_CORREDORAS;
    private final String SQL_SELECT_ID_CONDICION_LIQUIDACION;
    private final String SQL_SELECT_TRANSACCIONES_MODIFICAR_SIM;

    public ModificarTransaccionMarketDataService(NamedParameterJdbcTemplate jdbc,
                                                  ObtenerDiaSiguienteUtil diaSiguienteUtil,
                                                  ObtenerPlazoUtil plazoUtil,
                                                  ModificarTransaccionService modificarTransaccionService) {
        this.jdbc = jdbc;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.plazoUtil = plazoUtil;
        this.modificarTransaccionService = modificarTransaccionService;
        this.SQL_SELECT_CORREDORAS = loadSql("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectCorredoras.sql");
        this.SQL_SELECT_ID_CONDICION_LIQUIDACION = loadSql("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectIdCondicionDeLiquidacion.sql");
        this.SQL_SELECT_TRANSACCIONES_MODIFICAR_SIM = loadSql("cl/bcs/ordenes/sql/ModificarTransaccionMarketData_SelectTransaccionesModificarSIM.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ModificarTransaccionMarketDataService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            JsonArray transaccionesSIM = params.has("transaccionesSIM") ? params.get("transaccionesSIM").getAsJsonArray() : new JsonArray();

            JsonArray mensajes = new JsonArray();
            if (transaccionesSIM.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", mensajes);
                return out;
            }

            // Load corredoras and conditions
            List<Map<String, Object>> corredoras = jdbc.queryForList(SQL_SELECT_CORREDORAS, new MapSqlParameterSource());
            Map<String, Long> hashCorredoras = new HashMap<>();
            Long idCorredoraEmpresa = null;
            for (Map<String, Object> c : corredoras) {
                String codigo = getStr(c, "CodigoCorredora");
                Long id = toLong(c.get("IdCorredora"));
                if (codigo != null && id != null) hashCorredoras.put(codigo, id);
            }

            List<Map<String, Object>> condiciones = jdbc.queryForList(SQL_SELECT_ID_CONDICION_LIQUIDACION, new MapSqlParameterSource());
            Map<String, Long> hashCondicion = new HashMap<>();
            for (Map<String, Object> c : condiciones) {
                String cond = getStr(c, "IdCondicionDeLiquidacion");
                Long plazo = toLong(c.get("PlazoEnDias"));
                if (!cond.isBlank() && plazo != null) hashCondicion.put(cond, plazo);
            }

            SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyyMMdd");
            SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy-MM-dd");

            // Build hash keys for SIM transactions
            List<String> llaves = new ArrayList<>();
            for (var el : transaccionesSIM) {
                JsonObject tx = el.getAsJsonObject();
                try {
                    String fecha   = sdfOut.format(sdfIn.parse(tx.has("transactionDay") ? tx.get("transactionDay").getAsString() : "19700101"));
                    String folio   = tx.has("folio") ? tx.get("folio").getAsString() : "";
                    String symbol  = tx.has("symbol") ? tx.get("symbol").getAsString() : "";
                    String buyFirm = tx.has("buyExecutingFirm") ? tx.get("buyExecutingFirm").getAsString() : "";
                    String selFirm = tx.has("sellExecutingFirm") ? tx.get("sellExecutingFirm").getAsString() : "";

                    Long idCompra  = hashCorredoras.get(buyFirm);
                    Long idVenta   = hashCorredoras.get(selFirm);

                    String accion = "";
                    if (idCompra != null && idCompra.equals(idCorredoraEmpresa)) accion = "COM";
                    else if (idVenta != null && idVenta.equals(idCorredoraEmpresa)) accion = "VEN";

                    if (!accion.isEmpty()) {
                        String hash = folio + "_" + fecha + "_" + symbol + "_" + accion;
                        llaves.add(hash);
                    }

                    // Compute FechaVencimiento (next business day after FechaTransaccion + plazo)
                    String expirationDate = tx.has("expirationDate") ? tx.get("expirationDate").getAsString() : "";
                    if (!expirationDate.isBlank() && expirationDate.length() == 8) {
                        String condId = tx.has("IdCondicionDeLiquidacion") ? tx.get("IdCondicionDeLiquidacion").getAsString() : "";
                        Long plazo = hashCondicion.getOrDefault(condId, 0L);
                        if (plazo > 0) {
                            ObtenerDiaSiguienteUtil.FechaIn fi = new ObtenerDiaSiguienteUtil.FechaIn(
                                sdfOut.format(sdfIn.parse(expirationDate)), plazo, 1L);
                            ObtenerDiaSiguienteUtil.Salida sal = diaSiguienteUtil.calcular(1L, "S", "N", List.of(fi));
                            if (!sal.p_out_fechas.isEmpty()) {
                                // FechaVencimiento set but not stored here (TX row already identified by hash)
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error processing SIM tx: {}", e.getMessage());
                }
            }

            if (llaves.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", mensajes);
                return out;
            }

            // Find matching transactions
            List<Map<String, Object>> txParaModificar = jdbc.queryForList(
                SQL_SELECT_TRANSACCIONES_MODIFICAR_SIM,
                new MapSqlParameterSource("Llaves", llaves));

            if (txParaModificar.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", mensajes);
                return out;
            }

            // Build input for ModificarTransaccionService
            JsonArray txModificar = new JsonArray();
            for (Map<String, Object> txRow : txParaModificar) {
                Long idTx = toLong(txRow.get("IdTransaccion"));
                if (idTx == null) continue;
                // Find matching SIM entry
                for (var el : transaccionesSIM) {
                    JsonObject simTx = el.getAsJsonObject();
                    JsonObject modTx = new JsonObject();
                    modTx.addProperty("IdTransaccion", idTx);
                    modTx.addProperty("p_OrigenTransaccion", "MARKET_DATA");
                    if (simTx.has("quantity")) modTx.addProperty("Cantidad", simTx.get("quantity").getAsBigDecimal());
                    if (simTx.has("price")) modTx.addProperty("Precio", simTx.get("price").getAsBigDecimal());
                    if (simTx.has("rate")) modTx.addProperty("Tasa", simTx.get("rate").getAsBigDecimal());
                    txModificar.add(modTx);
                    break;
                }
            }

            JsonObject modParams = new JsonObject();
            modParams.addProperty("p_IdEmpresa", idEmpresa);
            modParams.addProperty("p_IdUsuario", idUsuario);
            modParams.addProperty("p_IdModulo", idModulo);
            modParams.addProperty("p_OrigenTransaccion", "MARKET_DATA");
            modParams.add("p_Transacciones", txModificar);
            JsonObject modResult = modificarTransaccionService.execute(modParams);

            return modResult;
        } catch (Exception e) {
            log.error("ModificarTransaccionMarketDataService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ModificarTransaccionMarketDataService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
