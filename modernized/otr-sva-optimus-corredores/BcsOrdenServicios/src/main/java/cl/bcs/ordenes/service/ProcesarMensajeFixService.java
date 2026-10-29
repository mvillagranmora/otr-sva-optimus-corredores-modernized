package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerPlazoUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ProcesarMensajeFix (JavaCall).
 * Processes FIX protocol messages (EN_NEGOCIACION, ORDEN_RECHAZADA, ROUTED_XBCL,
 * ORDEN_ANULADA, CALCE_COMPLETO, CALCE_PARCIAL, NEWS) by delegating to:
 *   - CambiarEstadoOrdenesService for order state transitions
 *   - CrearTransaccionesMarketDataBECService for CALCE transactions (graceful if stub)
 *   - CrearAsignacionesService for CALCE assignments (graceful if stub)
 *   - EnviarAsignacionesFacturarService for invoicing calce assignments
 */
@Service
public class ProcesarMensajeFixService {

    private static final Logger log = LoggerFactory.getLogger(ProcesarMensajeFixService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    // FIX state constants (from ConstantesOrdenes)
    private static final String ESTADO_FIX_EN_NEGOCIACION   = "0";
    private static final String ESTADO_FIX_ORDEN_RECHAZADA  = "8";
    private static final String ESTADO_FIX_ROUTED_XBCL      = "A";
    private static final String ESTADO_FIX_ORDEN_ANULADA    = "4";
    private static final String ESTADO_FIX_CALCE_COMPLETO   = "2";
    private static final String ESTADO_FIX_CALCE_PARCIAL    = "1";
    private static final String ESTADO_FIX_NEWS             = "B";

    private static final String ESTADO_ORDEN_OFERTADA       = "OFERTADA";
    private static final String ESTADO_ORDEN_RECHAZADA      = "RECHAZADA";
    private static final String ESTADO_ORDEN_ANULADA        = "ANULADA";
    private static final String ESTADO_ORDEN_ANULADA_PARCIAL = "ANULADA_PARCIAL";

    private static final String TIPO_EVENTO_BITACORA_EN_NEGOCIACION         = "EN_NEGOCIACION";
    private static final String TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA        = "ORDEN_RECHAZADA";
    private static final String TIPO_EVENTO_BITACORA_ORDEN_ANULADA          = "ORDEN_ANULADA";
    private static final String TIPO_EVENTO_BITACORA_ORDEN_ANULADA_PARCIAL  = "ORDEN_ANULADA_PARCIALMENTE";
    private static final String TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL         = "ROUTED_TO_XBCL";

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final ObtenerPlazoUtil plazoUtil;
    private final CambiarEstadoOrdenesService cambiarEstadoService;
    private final CrearTransaccionesMarketDataBECService crearTxBECService;
    private final CrearAsignacionesService crearAsignacionesService;
    private final EnviarAsignacionesFacturarService enviarAsigService;

    private final String SQL_SELECT_CORREDORAS;
    private final String SQL_INSERT_BITACORA;
    private final String SQL_SELECT_COUNT_ASIGNACIONES;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_CONFIGURACION_CANAL;
    private final String SQL_SELECT_MENSAJES_PENDIENTES;

    public ProcesarMensajeFixService(NamedParameterJdbcTemplate jdbc,
                                      ConfiguracionSistemaUtil configUtil,
                                      ObtenerPlazoUtil plazoUtil,
                                      CambiarEstadoOrdenesService cambiarEstadoService,
                                      CrearTransaccionesMarketDataBECService crearTxBECService,
                                      CrearAsignacionesService crearAsignacionesService,
                                      EnviarAsignacionesFacturarService enviarAsigService) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.plazoUtil = plazoUtil;
        this.cambiarEstadoService = cambiarEstadoService;
        this.crearTxBECService = crearTxBECService;
        this.crearAsignacionesService = crearAsignacionesService;
        this.enviarAsigService = enviarAsigService;
        this.SQL_SELECT_CORREDORAS      = loadSql("cl/bcs/ordenes/sql/ProcesarMensajeFix_SelectCorredoras.sql");
        this.SQL_INSERT_BITACORA        = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_InsertBitacora.sql");
        this.SQL_SELECT_COUNT_ASIGNACIONES = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectCountAsignaciones.sql");
        this.SQL_SELECT_TRANSACCIONES   = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectTransacciones.sql");
        this.SQL_SELECT_ORDENES         = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectOrdenes.sql");
        this.SQL_SELECT_CONFIGURACION_CANAL = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectConfiguracionCanalDistribucion.sql");
        this.SQL_SELECT_MENSAJES_PENDIENTES = loadSql("cl/bcs/ordenes/sql/ProcesarMensajesFix_SelectMensajesPendientes.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ProcesarMensajeFixService.execute()");
        try {
            Long idEmpresa  = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario  = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String tabla    = params.has("p_Tabla") ? params.get("p_Tabla").getAsString() : "BCS_FIX";
            JsonArray mensajesFix = params.has("p_MensajesFix")
                ? params.get("p_MensajesFix").getAsJsonArray() : new JsonArray();

            JsonArray outMensajes = new JsonArray();
            JsonArray outResultado = new JsonArray();

            // Categorize FIX messages
            List<JsonObject> ordenesEnNegociacion = new ArrayList<>();
            List<JsonObject> ordenesEnviadasBEC = new ArrayList<>();
            List<JsonObject> ordenesPorRechazar = new ArrayList<>();
            List<JsonObject> ordenesPorAnular = new ArrayList<>();
            List<JsonObject> calces = new ArrayList<>();
            List<JsonObject> mensajesNews = new ArrayList<>();

            for (var el : mensajesFix) {
                JsonObject fix = el.getAsJsonObject();
                BigDecimal secOrden = fix.has("SEC_ORDEN") && !fix.get("SEC_ORDEN").isJsonNull()
                    ? fix.get("SEC_ORDEN").getAsBigDecimal() : BigDecimal.ZERO;

                // Check for pending messages
                if (secOrden.compareTo(BigDecimal.ZERO) >= 1) {
                    try {
                        String queryPendientes = SQL_SELECT_MENSAJES_PENDIENTES.replace("<NOMBRE_TABLA>", tabla);
                        List<Map<String, Object>> pendientes = jdbc.queryForList(queryPendientes,
                            new MapSqlParameterSource()
                                .addValue("SEC_ORDEN", secOrden)
                                .addValue("FECHA", gs(fix, "FECHA"))
                                .addValue("NRO_ENTRADA", gs(fix, "NRO_ENTRADA")));
                        if (!pendientes.isEmpty()) {
                            log.debug("Mensajes pendientes para orden: {}", secOrden);
                            Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                            JsonObject r = new JsonObject();
                            r.addProperty("IdInterno", idInterno);
                            r.addProperty("Tipo", "ADVERTENCIA");
                            r.addProperty("IdReferencia", idInterno);
                            r.addProperty("MensajeReferencia", "Mensajes pendientes para orden " + secOrden);
                            outResultado.add(r);
                            continue;
                        }
                    } catch (Exception e) {
                        log.warn("Error checking pending messages: {}", e.getMessage());
                    }
                }

                String estado = gs(fix, "ESTADO");
                if (ESTADO_FIX_EN_NEGOCIACION.equals(estado)) ordenesEnNegociacion.add(fix);
                else if (ESTADO_FIX_ORDEN_RECHAZADA.equals(estado)) ordenesPorRechazar.add(fix);
                else if (ESTADO_FIX_ROUTED_XBCL.equals(estado)) ordenesEnviadasBEC.add(fix);
                else if (ESTADO_FIX_ORDEN_ANULADA.equals(estado)) ordenesPorAnular.add(fix);
                else if (ESTADO_FIX_CALCE_COMPLETO.equals(estado) || ESTADO_FIX_CALCE_PARCIAL.equals(estado)) calces.add(fix);
                else if (ESTADO_FIX_NEWS.equals(estado)) mensajesNews.add(fix);
            }

            // ROUTED TO XBCL — log bitacora only
            for (JsonObject fix : ordenesEnviadasBEC) {
                Long secOrden = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                insertBitacora(idEmpresa, 0L, secOrden, 0L, TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL, gs(fix, "MENSAJE"), "ORDEN");
                JsonObject r = new JsonObject();
                r.addProperty("IdInterno", idInterno);
                r.addProperty("Tipo", "OK");
                r.addProperty("IdReferencia", secOrden);
                r.addProperty("MensajeReferencia", TIPO_EVENTO_BITACORA_ROUTED_TO_XBCL);
                outResultado.add(r);
            }

            // EN NEGOCIACION — change order state to OFERTADA
            if (!ordenesEnNegociacion.isEmpty()) {
                procesarCambioEstado(idEmpresa, idUsuario, ordenesEnNegociacion,
                    ESTADO_ORDEN_OFERTADA, TIPO_EVENTO_BITACORA_EN_NEGOCIACION,
                    outMensajes, outResultado);
            }

            // ORDEN RECHAZADA
            if (!ordenesPorRechazar.isEmpty()) {
                procesarCambioEstado(idEmpresa, idUsuario, ordenesPorRechazar,
                    ESTADO_ORDEN_RECHAZADA, TIPO_EVENTO_BITACORA_ORDEN_RECHAZADA,
                    outMensajes, outResultado);
            }

            // ORDEN ANULADA — check assignment count
            if (!ordenesPorAnular.isEmpty()) {
                List<Long> idsOrden = new ArrayList<>();
                for (JsonObject fix : ordenesPorAnular) {
                    Long secOrden = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                    if (secOrden != null && !idsOrden.contains(secOrden)) idsOrden.add(secOrden);
                }
                Map<Long, Long> hashCountAsig = new HashMap<>();
                try {
                    List<Map<String, Object>> countList = jdbc.queryForList(SQL_SELECT_COUNT_ASIGNACIONES,
                        new MapSqlParameterSource("idsOrden", idsOrden));
                    for (Map<String, Object> row : countList) {
                        Long idOrd = toLong(row.get("IdOrden"));
                        Long cnt = toLong(row.get("NumeroAsignaciones"));
                        if (idOrd != null) hashCountAsig.put(idOrd, cnt != null ? cnt : 0L);
                    }
                } catch (Exception e) {
                    log.warn("Error counting assignments: {}", e.getMessage());
                }

                JsonObject cambiarParams = new JsonObject();
                cambiarParams.addProperty("p_IdEmpresa", idEmpresa);
                cambiarParams.addProperty("p_IdUsuario", idUsuario);
                JsonArray ordenesArr = new JsonArray();
                Set<Long> procesadas = new HashSet<>();
                for (JsonObject fix : ordenesPorAnular) {
                    Long secOrden = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                    Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                    if (secOrden == null || !hashCountAsig.containsKey(secOrden)) {
                        JsonObject r = new JsonObject();
                        r.addProperty("IdInterno", idInterno);
                        r.addProperty("Tipo", "ERROR");
                        r.addProperty("MensajeReferencia", "ORDEN_NO_EXISTE");
                        outResultado.add(r);
                        continue;
                    }
                    if (procesadas.contains(secOrden)) continue;
                    procesadas.add(secOrden);
                    long numAsig = hashCountAsig.getOrDefault(secOrden, 0L);
                    JsonObject ordEntry = new JsonObject();
                    ordEntry.addProperty("IdOrden", secOrden);
                    ordEntry.addProperty("IdNuevoEstado", numAsig > 0 ? ESTADO_ORDEN_ANULADA_PARCIAL : ESTADO_ORDEN_ANULADA);
                    ordenesArr.add(ordEntry);
                }
                if (ordenesArr.size() > 0) {
                    cambiarParams.add("p_Ordenes", ordenesArr);
                    JsonObject camResult = cambiarEstadoService.execute(cambiarParams);
                    if (camResult.has("P_OUT_MENSAJES")) {
                        for (var m : camResult.get("P_OUT_MENSAJES").getAsJsonArray()) {
                            JsonObject msg = m.getAsJsonObject();
                            String tipo = gs(msg, "tipo");
                            if ("OK".equalsIgnoreCase(tipo) || "INFORMACION".equalsIgnoreCase(tipo)) {
                                Long idOrd = msg.has("id") ? msg.get("id").getAsLong() : null;
                                long numAsig = hashCountAsig.getOrDefault(idOrd, 0L);
                                String evTipo = numAsig > 0 ? TIPO_EVENTO_BITACORA_ORDEN_ANULADA_PARCIAL : TIPO_EVENTO_BITACORA_ORDEN_ANULADA;
                                insertBitacora(idEmpresa, 0L, idOrd, 0L, evTipo, "", "ORDEN");
                                for (JsonObject fix : ordenesPorAnular) {
                                    Long secOrden2 = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                                    Long idInterno2 = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                                    if (Objects.equals(secOrden2, idOrd)) {
                                        JsonObject r = new JsonObject();
                                        r.addProperty("IdInterno", idInterno2);
                                        r.addProperty("Tipo", "OK");
                                        r.addProperty("IdReferencia", idOrd);
                                        r.addProperty("MensajeReferencia", evTipo);
                                        outResultado.add(r);
                                    }
                                }
                            }
                            outMensajes.add(msg);
                        }
                    }
                }
            }

            // CALCE — create transactions and assignments (depends on stubs)
            if (!calces.isEmpty()) {
                List<String> transaccionKeys = new ArrayList<>();
                List<Long> idsOrdenCalce = new ArrayList<>();
                for (JsonObject fix : calces) {
                    String folioTrans = gs(fix, "FOLIO_TRANS");
                    if (!folioTrans.isBlank()) {
                        String bolsa = gs(fix, "BOLSA");
                        String suffix = "XBCL".equalsIgnoreCase(bolsa) ? "BEC" : "BCS";
                        transaccionKeys.add(folioTrans.trim() + gs(fix, "FECHA") + suffix);
                        Long sec = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                        if (sec != null && !idsOrdenCalce.contains(sec)) idsOrdenCalce.add(sec);
                    }
                }

                Map<String, Map<String, Object>> hashTx = new HashMap<>();
                Map<Long, Map<String, Object>> hashOrdenes = new HashMap<>();
                try {
                    if (!transaccionKeys.isEmpty()) {
                        List<Map<String, Object>> txList = jdbc.queryForList(SQL_SELECT_TRANSACCIONES,
                            new MapSqlParameterSource("transaccionKeys", transaccionKeys));
                        for (Map<String, Object> tx : txList) {
                            String key = str(tx, "FolioTransKey");
                            hashTx.put(key, tx);
                        }
                    }
                    if (!idsOrdenCalce.isEmpty()) {
                        List<Map<String, Object>> ordList = jdbc.queryForList(SQL_SELECT_ORDENES,
                            new MapSqlParameterSource("idsOrden", idsOrdenCalce));
                        for (Map<String, Object> ord : ordList) {
                            Long id = toLong(ord.get("IdOrden"));
                            if (id != null) hashOrdenes.put(id, ord);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error loading calce data: {}", e.getMessage());
                }

                // Group calces into matched pairs (calce 1 + calce 2 by same FOLIO_TRANS)
                Map<String, List<JsonObject>> gruposCalce = new LinkedHashMap<>();
                for (JsonObject fix : calces) {
                    String key = gs(fix, "FOLIO_TRANS").trim() + "|" + gs(fix, "FECHA");
                    gruposCalce.computeIfAbsent(key, k -> new ArrayList<>()).add(fix);
                }

                for (Map.Entry<String, List<JsonObject>> entry : gruposCalce.entrySet()) {
                    List<JsonObject> grupo = entry.getValue();
                    try {
                        // Try to create transactions via CrearTransaccionesMarketDataBECService
                        JsonArray txEntrada = new JsonArray();
                        for (JsonObject fix : grupo) {
                            JsonObject txEntry = new JsonObject();
                            txEntry.addProperty("FOLIO_TRANS", gs(fix, "FOLIO_TRANS"));
                            txEntry.addProperty("FECHA", gs(fix, "FECHA"));
                            txEntry.addProperty("BOLSA", gs(fix, "BOLSA"));
                            txEntry.addProperty("SEC_ORDEN", fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : 0L);
                            txEntry.addProperty("CANTIDAD", fix.has("CANTIDAD") ? fix.get("CANTIDAD").getAsBigDecimal() : BigDecimal.ZERO);
                            txEntry.addProperty("PRECIO", fix.has("PRECIO") ? fix.get("PRECIO").getAsBigDecimal() : BigDecimal.ZERO);
                            txEntry.addProperty("MONTO", fix.has("MONTO") ? fix.get("MONTO").getAsBigDecimal() : BigDecimal.ZERO);
                            txEntrada.add(txEntry);
                        }
                        JsonObject crearTxParams = new JsonObject();
                        crearTxParams.addProperty("p_IdEmpresa", idEmpresa);
                        crearTxParams.addProperty("p_IdUsuario", idUsuario);
                        crearTxParams.addProperty("p_IdModulo", idModulo);
                        crearTxParams.add("p_Transacciones", txEntrada);
                        JsonObject crearTxResult = crearTxBECService.execute(crearTxParams);

                        // Build assignments from created transactions
                        JsonArray asigEntrada = new JsonArray();
                        if (crearTxResult.has("P_OUT_RESULTADO")) {
                            for (var r : crearTxResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                                JsonObject txRes = r.getAsJsonObject();
                                JsonObject asig = new JsonObject();
                                asig.addProperty("IdTransaccion", txRes.has("IdTransaccion") ? txRes.get("IdTransaccion").getAsLong() : 0L);
                                asig.addProperty("IdOrden", txRes.has("IdOrden") ? txRes.get("IdOrden").getAsLong() : 0L);
                                asig.addProperty("Monto", txRes.has("Monto") ? txRes.get("Monto").getAsBigDecimal() : BigDecimal.ZERO);
                                asig.addProperty("Cantidad", txRes.has("Cantidad") ? txRes.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO);
                                asig.addProperty("Precio", txRes.has("Precio") ? txRes.get("Precio").getAsBigDecimal() : BigDecimal.ZERO);
                                asig.addProperty("EsFacturaInmediata", "S");
                                asigEntrada.add(asig);
                            }
                        }

                        // Create assignments
                        if (asigEntrada.size() > 0) {
                            JsonObject crearAsigParams = new JsonObject();
                            crearAsigParams.addProperty("p_IdEmpresa", idEmpresa);
                            crearAsigParams.addProperty("p_IdUsuario", idUsuario);
                            crearAsigParams.addProperty("p_IdModulo", idModulo);
                            crearAsigParams.add("p_Asignaciones", asigEntrada);
                            crearAsigParams.addProperty("p_simularOperacion", "N");
                            crearAsigParams.addProperty("p_UsaReglasNegocio", "S");
                            JsonObject crearAsigResult = crearAsignacionesService.execute(crearAsigParams);

                            // Enqueue for invoicing
                            if (crearAsigResult.has("P_OUT_RESULTADO")) {
                                try {
                                    JsonArray enviarAsigArr = new JsonArray();
                                    for (var r : crearAsigResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                                        JsonObject asig = r.getAsJsonObject();
                                        if (asig.has("IdAsignacion") && "S".equalsIgnoreCase(gs(asig, "EsFacturaInmediata"))) {
                                            JsonObject ea = new JsonObject();
                                            ea.addProperty("IdAsignacion", asig.get("IdAsignacion").getAsLong());
                                            ea.addProperty("GeneraComprobante", "S");
                                            ea.addProperty("IdTipoComprobante", "FAE");
                                            enviarAsigArr.add(ea);
                                        }
                                    }
                                    if (enviarAsigArr.size() > 0) {
                                        JsonObject envParams = new JsonObject();
                                        envParams.addProperty("p_IdEmpresa", idEmpresa);
                                        envParams.addProperty("p_IdUsuario", idUsuario);
                                        envParams.addProperty("p_IdModulo", idModulo);
                                        envParams.add("p_Asignaciones", enviarAsigArr);
                                        enviarAsigService.execute(envParams);
                                    }
                                } catch (Exception e) {
                                    log.warn("Error sending assignments to invoice: {}", e.getMessage());
                                }
                            }
                        }

                        for (JsonObject fix : grupo) {
                            Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                            Long sec = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                            insertBitacora(idEmpresa, 0L, sec, 0L, "CALCE", gs(fix, "MENSAJE"), "ORDEN");
                            JsonObject r = new JsonObject();
                            r.addProperty("IdInterno", idInterno);
                            r.addProperty("Tipo", "OK");
                            r.addProperty("IdReferencia", sec);
                            r.addProperty("MensajeReferencia", "CALCE_PROCESADO");
                            outResultado.add(r);
                        }
                    } catch (UnsupportedOperationException e) {
                        log.warn("CrearTransaccionesMarketDataBECService or CrearAsignacionesService deferred; CALCE deferred to TomEE: {}", e.getMessage());
                        for (JsonObject fix : grupo) {
                            Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                            JsonObject r = new JsonObject();
                            r.addProperty("IdInterno", idInterno);
                            r.addProperty("Tipo", "ADVERTENCIA");
                            r.addProperty("MensajeReferencia", "CALCE_DEFERRED");
                            outResultado.add(r);
                        }
                    } catch (Exception e) {
                        log.warn("Error processing calce group {}: {}", entry.getKey(), e.getMessage());
                        for (JsonObject fix : grupo) {
                            Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                            JsonObject r = new JsonObject();
                            r.addProperty("IdInterno", idInterno);
                            r.addProperty("Tipo", "ERROR");
                            r.addProperty("MensajeReferencia", e.getMessage());
                            outResultado.add(r);
                        }
                    }
                }
            }

            // NEWS — log only
            for (JsonObject fix : mensajesNews) {
                Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                insertBitacora(idEmpresa, 0L, 0L, 0L, "NEWS", gs(fix, "MENSAJE"), "ORDEN");
                JsonObject r = new JsonObject();
                r.addProperty("IdInterno", idInterno);
                r.addProperty("Tipo", "OK");
                r.addProperty("MensajeReferencia", "NEWS");
                outResultado.add(r);
            }

            long errCount = 0;
            for (var m : outMensajes) {
                if ("ERROR".equalsIgnoreCase(gs(m.getAsJsonObject(), "tipo"))) errCount++;
            }
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errCount);
            out.add("P_OUT_MENSAJES", outMensajes);
            out.add("P_OUT_RESULTADO", outResultado);
            return out;
        } catch (Exception e) {
            log.error("ProcesarMensajeFixService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private void procesarCambioEstado(Long idEmpresa, Long idUsuario,
                                       List<JsonObject> mensajes, String nuevoEstado, String tipoBitacora,
                                       JsonArray outMensajes, JsonArray outResultado) {
        JsonObject cambiarParams = new JsonObject();
        cambiarParams.addProperty("p_IdEmpresa", idEmpresa);
        cambiarParams.addProperty("p_IdUsuario", idUsuario);
        JsonArray ordenesArr = new JsonArray();
        Set<Long> procesadas = new HashSet<>();
        for (JsonObject fix : mensajes) {
            Long secOrden = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
            if (secOrden == null || !procesadas.add(secOrden)) continue;
            JsonObject entry = new JsonObject();
            entry.addProperty("IdOrden", secOrden);
            entry.addProperty("IdNuevoEstado", nuevoEstado);
            ordenesArr.add(entry);
        }
        if (ordenesArr.size() == 0) return;
        cambiarParams.add("p_Ordenes", ordenesArr);
        try {
            JsonObject result = cambiarEstadoService.execute(cambiarParams);
            if (result.has("P_OUT_MENSAJES")) {
                for (var m : result.get("P_OUT_MENSAJES").getAsJsonArray()) {
                    JsonObject msg = m.getAsJsonObject();
                    String tipo = gs(msg, "tipo");
                    Long idOrd = msg.has("id") ? msg.get("id").getAsLong() : null;
                    if (("OK".equalsIgnoreCase(tipo) || "INFORMACION".equalsIgnoreCase(tipo)) && idOrd != null) {
                        insertBitacora(idEmpresa, 0L, idOrd, 0L, tipoBitacora, "", "ORDEN");
                        for (JsonObject fix : mensajes) {
                            Long sec = fix.has("SEC_ORDEN") ? fix.get("SEC_ORDEN").getAsLong() : null;
                            Long idInterno = fix.has("IdInterno") ? fix.get("IdInterno").getAsLong() : null;
                            if (Objects.equals(sec, idOrd)) {
                                JsonObject r = new JsonObject();
                                r.addProperty("IdInterno", idInterno);
                                r.addProperty("Tipo", "OK");
                                r.addProperty("IdReferencia", idOrd);
                                r.addProperty("MensajeReferencia", gs(msg, "descripcion") + " " + tipoBitacora);
                                outResultado.add(r);
                            }
                        }
                    }
                    outMensajes.add(msg);
                }
            }
        } catch (Exception e) {
            log.warn("Error changing order state to {}: {}", nuevoEstado, e.getMessage());
        }
    }

    private void insertBitacora(Long idEmpresa, Long idAsignacion, Long idOrden, Long idTransaccion,
                                  String tipoEvento, String mensaje, String nombreEntidad) {
        try {
            jdbc.update(SQL_INSERT_BITACORA,
                new MapSqlParameterSource()
                    .addValue("IdEmpresa", idEmpresa)
                    .addValue("IdAsignacion", idAsignacion)
                    .addValue("IdOrden", idOrden)
                    .addValue("IdTransaccion", idTransaccion)
                    .addValue("IdTipoEventoBitacora", tipoEvento)
                    .addValue("MensajeEventoBitacora", mensaje)
                    .addValue("NombreEntidad", nombreEntidad));
        } catch (Exception e) {
            log.warn("Error inserting bitacora: {}", e.getMessage());
        }
    }

    private static String gs(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ProcesarMensajeFixService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
