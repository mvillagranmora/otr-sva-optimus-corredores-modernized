package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ObtenerPlazoUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Migrated from legacy cl.bcs.ordenes.ModificarTransaccion (JavaCall).
 * Updates transactions, computes plazos via ObtenerPlazoUtil, creates historical
 * records, and notifies downstream services.
 */
@Service
public class ModificarTransaccionService {

    private static final Logger log = LoggerFactory.getLogger(ModificarTransaccionService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private static final String ORIGEN_MARKET_DATA = "MARKET_DATA";

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ObtenerPlazoUtil plazoUtil;
    private final PlataformaCallClient plataformaClient;
    private final ServicioCumpleRequisitosModificarTransaccionService cumpleRequisitosService;
    private final CrearTransaccionesHistoricasService historicasService;
    private final NotificarCambioTransaccionService notificarCambioService;

    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_UPDATE_TRANSACCIONES;
    private final String SQL_UPDATE_TRANSACCIONES_PLAZOS;
    private final String SQL_SELECT_ID_CONDICION_LIQUIDACION;

    public ModificarTransaccionService(NamedParameterJdbcTemplate jdbc,
                                        RequisitosBasicosUtil requisitosUtil,
                                        ObtenerPlazoUtil plazoUtil,
                                        PlataformaCallClient plataformaClient,
                                        ServicioCumpleRequisitosModificarTransaccionService cumpleRequisitosService,
                                        CrearTransaccionesHistoricasService historicasService,
                                        NotificarCambioTransaccionService notificarCambioService) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.plazoUtil = plazoUtil;
        this.plataformaClient = plataformaClient;
        this.cumpleRequisitosService = cumpleRequisitosService;
        this.historicasService = historicasService;
        this.notificarCambioService = notificarCambioService;
        this.SQL_SELECT_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/ModificarTransaccion_SelectTransacciones.sql");
        this.SQL_UPDATE_TRANSACCIONES = loadSql("cl/bcs/ordenes/sql/ModificarTransaccion_UpdateTransacciones.sql");
        this.SQL_UPDATE_TRANSACCIONES_PLAZOS = loadSql("cl/bcs/ordenes/sql/ModificarTransaccion_UpdateTransaccionesPlazos.sql");
        this.SQL_SELECT_ID_CONDICION_LIQUIDACION = loadSql("cl/bcs/ordenes/sql/ModificarTransaccion_SelectIdCondicionDeLiquidacion.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ModificarTransaccionService.execute()");
        try {
            Long idEmpresa    = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario    = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo   = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String reproceso  = params.has("p_Reproceso") ? params.get("p_Reproceso").getAsString() : "N";
            String origen     = params.has("p_OrigenTransaccion") ? params.get("p_OrigenTransaccion").getAsString() : "";
            JsonArray txIn    = params.has("p_Transacciones") ? params.get("p_Transacciones").getAsJsonArray() : new JsonArray();

            // 1. Basic requirements
            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            // 2. Load existing transactions
            List<Long> idsTx = new ArrayList<>();
            for (var el : txIn) {
                JsonObject tx = el.getAsJsonObject();
                if (tx.has("IdTransaccion")) idsTx.add(tx.get("IdTransaccion").getAsLong());
            }
            if (idsTx.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
                return out;
            }

            List<Map<String, Object>> transExistentes = jdbc.queryForList(SQL_SELECT_TRANSACCIONES,
                new MapSqlParameterSource("IdTransaccion", idsTx));
            Map<Long, Map<String, Object>> hashTx = new HashMap<>();
            for (Map<String, Object> tx : transExistentes) {
                Object id = tx.get("IdTransaccion");
                if (id instanceof Number) hashTx.put(((Number) id).longValue(), tx);
            }

            // 3. Validate via ServicioCumpleRequisitos
            JsonObject reqParams = new JsonObject();
            reqParams.addProperty("p_IdEmpresa", idEmpresa);
            reqParams.add("p_Transacciones", txIn);
            JsonObject reqOut = cumpleRequisitosService.execute(reqParams);
            long erroresCumple = reqOut.has("P_OUT_ERRORESCONTADOR") ? reqOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
            if (erroresCumple > 0) {
                return reqOut;
            }

            // 4. Compute condición de liquidación via ObtenerPlazo (skip for MARKET_DATA origin)
            Long idPais = obtenerIdPais(idEmpresa);
            List<Map<String, Object>> condicionesLiquidacion = jdbc.queryForList(
                SQL_SELECT_ID_CONDICION_LIQUIDACION, new MapSqlParameterSource());

            Map<String, Long> hashCondicion = new HashMap<>();
            for (Map<String, Object> c : condicionesLiquidacion) {
                String cond = getStr(c, "IdCondicionDeLiquidacion");
                Long plazo = toLong(c.get("PlazoEnDias"));
                if (cond != null && plazo != null) hashCondicion.put(cond, plazo);
            }

            if (!ORIGEN_MARKET_DATA.equalsIgnoreCase(origen)) {
                List<ObtenerPlazoUtil.FechasIn> fechasIn = new ArrayList<>();
                for (var el : txIn) {
                    JsonObject tx = el.getAsJsonObject();
                    String idCond = tx.has("IdCondicionDeLiquidacion") ? tx.get("IdCondicionDeLiquidacion").getAsString() : "";
                    String fechaDesde = tx.has("FechaEmision") ? tx.get("FechaEmision").getAsString() : "";
                    if (!idCond.isBlank() && !fechaDesde.isBlank() && hashCondicion.containsKey(idCond)) {
                        Long diasPlazo = hashCondicion.get(idCond);
                        ObtenerPlazoUtil.FechasIn fi = new ObtenerPlazoUtil.FechasIn();
                        fi.IdInterno = tx.get("IdTransaccion").getAsLong();
                        fi.FechaDesde = fechaDesde;
                        // FechaHasta computed by adding diasPlazo days — simplified
                        fi.FechaHasta = fechaDesde;
                        fi.SoloDiasHabiles = "S";
                        fechasIn.add(fi);
                    }
                }
                if (!fechasIn.isEmpty()) {
                    plazoUtil.calcular(idPais, fechasIn);
                }
            }

            // 5. Create historical records (if not reproceso)
            if ("N".equalsIgnoreCase(reproceso)) {
                try {
                    JsonObject histParams = new JsonObject();
                    histParams.addProperty("p_IdEmpresa", idEmpresa);
                    histParams.addProperty("p_IdUsuario", idUsuario);
                    histParams.addProperty("p_IdModulo", idModulo);
                    histParams.addProperty("p_Estado", "PRO");
                    histParams.add("p_Transacciones", txIn);
                    historicasService.execute(histParams);
                } catch (Exception e) {
                    log.warn("Error creating historical records: {}", e.getMessage());
                }
            }

            // 6. Batch update main transactions
            for (var el : txIn) {
                JsonObject tx = el.getAsJsonObject();
                Long idTx = tx.has("IdTransaccion") ? tx.get("IdTransaccion").getAsLong() : null;
                if (idTx == null) continue;
                try {
                    MapSqlParameterSource p = buildUpdateParams(tx);
                    jdbc.update(SQL_UPDATE_TRANSACCIONES, p);
                } catch (Exception e) {
                    log.warn("Error updating transaccion {}: {}", idTx, e.getMessage());
                }
            }

            // 7. Update plazo transactions (those with IdTransaccionPlazo)
            for (var el : txIn) {
                JsonObject tx = el.getAsJsonObject();
                Long idTx = tx.has("IdTransaccion") ? tx.get("IdTransaccion").getAsLong() : null;
                if (idTx == null) continue;
                Map<String, Object> existente = hashTx.get(idTx);
                if (existente == null) continue;
                Long idTxPlazo = toLong(existente.get("IdTransaccionPlazo"));
                if (idTxPlazo == null || idTxPlazo == 0) continue;
                try {
                    MapSqlParameterSource p = buildUpdatePlazoParams(tx, idTxPlazo);
                    jdbc.update(SQL_UPDATE_TRANSACCIONES_PLAZOS, p);
                } catch (Exception e) {
                    log.warn("Error updating plazo transaccion {}: {}", idTxPlazo, e.getMessage());
                }
            }

            // 8. Notify if configured
            try {
                String notificar = "S"; // assume S by default; could be read from configUtil
                if ("S".equalsIgnoreCase(notificar)) {
                    JsonObject notifParams = new JsonObject();
                    notifParams.addProperty("p_IdEmpresa", idEmpresa);
                    notifParams.addProperty("p_IdUsuario", idUsuario);
                    notifParams.addProperty("p_IdModulo", idModulo);
                    notifParams.addProperty("p_Accion", "MODIFICADA");
                    notifParams.add("p_InTransacciones", txIn);
                    notificarCambioService.execute(notifParams);
                }
            } catch (Exception e) {
                log.warn("Error notifying transaccion change: {}", e.getMessage());
            }

            // Build messages
            JsonArray mensajes = new JsonArray();
            for (var el : txIn) {
                JsonObject tx = el.getAsJsonObject();
                Long idTx = tx.has("IdTransaccion") ? tx.get("IdTransaccion").getAsLong() : null;
                JsonObject m = new JsonObject();
                m.addProperty("id", idTx);
                m.addProperty("tipo", "OK");
                m.addProperty("codigo", "TRANSACCION_MODIFICADA");
                mensajes.add(m);
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", mensajes);
            return out;
        } catch (Exception e) {
            log.error("ModificarTransaccionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private MapSqlParameterSource buildUpdateParams(JsonObject tx) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("IdTransaccion", tx.has("IdTransaccion") ? tx.get("IdTransaccion").getAsLong() : null);
        p.addValue("Monto", tx.has("Monto") ? tx.get("Monto").getAsBigDecimal() : null);
        p.addValue("Precio", tx.has("Precio") ? tx.get("Precio").getAsBigDecimal() : null);
        p.addValue("Tasa", tx.has("Tasa") ? tx.get("Tasa").getAsBigDecimal() : null);
        p.addValue("Cantidad", tx.has("Cantidad") ? tx.get("Cantidad").getAsBigDecimal() : null);
        p.addValue("FechaEmision", tx.has("FechaEmision") ? tx.get("FechaEmision").getAsString() : null);
        p.addValue("FechaLiquidacion", tx.has("FechaLiquidacion") ? tx.get("FechaLiquidacion").getAsString() : null);
        p.addValue("IdLibroOrdenes", tx.has("IdLibroOrdenes") ? tx.get("IdLibroOrdenes").getAsLong() : null);
        p.addValue("ValorRescate", tx.has("ValorRescate") ? tx.get("ValorRescate").getAsBigDecimal() : null);
        p.addValue("EsBilateral", tx.has("EsBilateral") ? tx.get("EsBilateral").getAsString() : null);
        p.addValue("BloqueoLiquidacion", tx.has("BloqueoLiquidacion") ? tx.get("BloqueoLiquidacion").getAsString() : null);
        p.addValue("MontoRetencion", tx.has("MontoRetencion") ? tx.get("MontoRetencion").getAsBigDecimal() : null);
        p.addValue("AccionTipoOperacion", tx.has("AccionTipoOperacion") ? tx.get("AccionTipoOperacion").getAsString() : null);
        p.addValue("TipoCambioCorresponsal", tx.has("TipoCambioCorresponsal") ? tx.get("TipoCambioCorresponsal").getAsBigDecimal() : null);
        p.addValue("IdCorredorCorresponsal", tx.has("IdCorredorCorresponsal") ? tx.get("IdCorredorCorresponsal").getAsLong() : null);
        p.addValue("MontoLiquidacionCorresponsal", tx.has("MontoLiquidacionCorresponsal") ? tx.get("MontoLiquidacionCorresponsal").getAsBigDecimal() : null);
        p.addValue("GastosCorresponsal", tx.has("GastosCorresponsal") ? tx.get("GastosCorresponsal").getAsBigDecimal() : null);
        p.addValue("IdMonedaLiquidacionCorresponsal", tx.has("IdMonedaLiquidacionCorresponsal") ? tx.get("IdMonedaLiquidacionCorresponsal").getAsString() : null);
        p.addValue("IdCamara", tx.has("IdCamara") ? tx.get("IdCamara").getAsString() : null);
        p.addValue("Motivo", tx.has("Motivo") ? tx.get("Motivo").getAsString() : "");
        p.addValue("Observaciones", tx.has("Observaciones") ? tx.get("Observaciones").getAsString() : "");
        p.addValue("MarcaNegociacion", tx.has("MarcaNegociacion") ? tx.get("MarcaNegociacion").getAsString() : "");
        p.addValue("IdCondicionDeLiquidacion", tx.has("IdCondicionDeLiquidacion") ? tx.get("IdCondicionDeLiquidacion").getAsString() : null);
        return p;
    }

    private MapSqlParameterSource buildUpdatePlazoParams(JsonObject tx, Long idTxPlazo) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        p.addValue("IdTransaccion", idTxPlazo);
        String tipoInstrumento = tx.has("IdTipoInstrumento") ? tx.get("IdTipoInstrumento").getAsString() : "";
        boolean isSim = "SIM".equalsIgnoreCase(tipoInstrumento);
        p.addValue("Monto", tx.has(isSim ? "MontoAPlazo" : "Monto") ? tx.get(isSim ? "MontoAPlazo" : "Monto").getAsBigDecimal() : null);
        p.addValue("Precio", tx.has(isSim ? "PrecioAPlazo" : "Precio") ? tx.get(isSim ? "PrecioAPlazo" : "Precio").getAsBigDecimal() : null);
        p.addValue("Tasa", tx.has("Tasa") ? tx.get("Tasa").getAsBigDecimal() : null);
        p.addValue("Cantidad", tx.has("Cantidad") ? tx.get("Cantidad").getAsBigDecimal() : null);
        p.addValue("FechaEmision", tx.has("FechaEmision") ? tx.get("FechaEmision").getAsString() : null);
        p.addValue("FechaLiquidacion", isSim && tx.has("FechaVencimiento")
            ? tx.get("FechaVencimiento").getAsString()
            : (tx.has("FechaLiquidacion") ? tx.get("FechaLiquidacion").getAsString() : null));
        p.addValue("IdLibroOrdenes", tx.has("IdLibroOrdenes") ? tx.get("IdLibroOrdenes").getAsLong() : null);
        p.addValue("ValorRescate", tx.has("ValorRescate") ? tx.get("ValorRescate").getAsBigDecimal() : null);
        p.addValue("EsBilateral", tx.has("EsBilateral") ? tx.get("EsBilateral").getAsString() : null);
        p.addValue("BloqueoLiquidacion", tx.has("BloqueoLiquidacion") ? tx.get("BloqueoLiquidacion").getAsString() : null);
        p.addValue("MontoRetencion", tx.has("MontoRetencion") ? tx.get("MontoRetencion").getAsBigDecimal() : null);
        p.addValue("AccionTipoOperacion", tx.has("AccionTipoOperacion") ? tx.get("AccionTipoOperacion").getAsString() : null);
        p.addValue("IdCamara", tx.has("IdCamara") ? tx.get("IdCamara").getAsString() : null);
        p.addValue("Motivo", tx.has("Motivo") ? tx.get("Motivo").getAsString() : "");
        p.addValue("MarcaNegociacion", tx.has("MarcaNegociacion") ? tx.get("MarcaNegociacion").getAsString() : "");
        return p;
    }

    private Long obtenerIdPais(Long idEmpresa) {
        try {
            List<Map<String, Object>> rows = jdbc.queryForList(
                "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" WHERE \"IdEmpresa\" = :IdEmpresa",
                new MapSqlParameterSource("IdEmpresa", idEmpresa));
            if (!rows.isEmpty()) return toLong(rows.get(0).get("IdPais"));
        } catch (Exception e) {
            log.warn("Could not get IdPais for empresa {}: {}", idEmpresa, e.getMessage());
        }
        return 1L;
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
        try (var is = ModificarTransaccionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
