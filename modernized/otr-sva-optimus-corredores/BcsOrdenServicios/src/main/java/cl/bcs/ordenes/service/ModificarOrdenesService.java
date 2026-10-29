package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
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
 * Migrated from legacy cl.bcs.ordenes.ModificarOrdenes (JavaCall).
 * Modifies existing orders: validates state, recalculates amounts/cobros via
 * CalcularMontoYCobrosOrdenService, then batch-updates orders and inserts new cobros.
 * Gracefully degrades when CalcularMontoYCobrosOrdenService is still a stub.
 */
@Service
public class ModificarOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(ModificarOrdenesService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final CalcularMontoYCobrosOrdenService calcularService;

    private final String SQL_SELECT_ESTADO_MODULO;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_COBROS;
    private final String SQL_SELECT_COBROS_RANGO;
    private final String SQL_UPDATE_ORDENES;
    private final String SQL_INSERT_COBROS;
    private final String SQL_SELECT_ORDEN_ASOCIADA;

    public ModificarOrdenesService(NamedParameterJdbcTemplate jdbc,
                                    RequisitosBasicosUtil requisitosUtil,
                                    CalcularMontoYCobrosOrdenService calcularService) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.calcularService = calcularService;
        this.SQL_SELECT_ESTADO_MODULO  = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_SELECT_ORDENES        = loadSql("cl/bcs/ordenes/sql/ModificarOrdenes_SelectOrdenes.sql");
        this.SQL_SELECT_COBROS         = loadSql("cl/bcs/ordenes/sql/ModificarOrdenes_SelectCobros.sql");
        this.SQL_SELECT_COBROS_RANGO   = loadSql("cl/bcs/ordenes/sql/ModificarOrdenes_SelectCobrosRango.sql");
        this.SQL_UPDATE_ORDENES        = loadSql("cl/bcs/ordenes/sql/ModificarOrdenes_UpdateOrdenes.sql");
        this.SQL_INSERT_COBROS         = loadSql("cl/bcs/ordenes/sql/Globales_InsertCobros.sql");
        this.SQL_SELECT_ORDEN_ASOCIADA = loadSql("cl/bcs/ordenes/sql/ModificarOrdenes_SelectOrdenAsociada.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ModificarOrdenesService.execute()");
        try {
            Long idEmpresa  = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario  = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            JsonArray ordenesIn  = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();
            JsonArray cobrosModIn = params.has("p_CobrosModificados") ? params.get("p_CobrosModificados").getAsJsonArray() : new JsonArray();

            JsonArray mensajes = new JsonArray();

            if (ordenesIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_ORDENES\"}]"));
                return out;
            }

            // Validate basic requirements
            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            // Validate module state
            List<Map<String, Object>> modulo = jdbc.queryForList(SQL_SELECT_ESTADO_MODULO,
                new MapSqlParameterSource()
                    .addValue("IdModulo", "ORD")
                    .addValue("IdEmpresa", idEmpresa));
            if (modulo.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"MODULO_NO_EXISTE\"}]"));
                return out;
            }
            if ("CERRADO".equalsIgnoreCase(getStr(modulo.get(0), "IdEstadoModulo"))) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"MODULO_CERRADO\"}]"));
                return out;
            }

            // Collect order IDs
            List<Long> idsOrden = new ArrayList<>();
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                Long id = ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : null;
                if (id != null) idsOrden.add(id);
            }

            // Load order info
            List<Map<String, Object>> infoOrdenesList = jdbc.queryForList(SQL_SELECT_ORDENES,
                new MapSqlParameterSource("idsOrden", idsOrden));
            Map<Long, Map<String, Object>> infoOrdenes = new HashMap<>();
            List<Long> idsOrdenAsociada = new ArrayList<>();
            for (Map<String, Object> ord : infoOrdenesList) {
                Long id = toLong(ord.get("IdOrden"));
                infoOrdenes.put(id, ord);
                String tipoOp = getStr(ord, "IdTipoOperacion");
                if ("COMRRF".equalsIgnoreCase(tipoOp) || "COMRRV".equalsIgnoreCase(tipoOp)) {
                    idsOrdenAsociada.add(id);
                }
            }

            // Load cobros
            List<Map<String, Object>> infoCobros = jdbc.queryForList(SQL_SELECT_COBROS,
                new MapSqlParameterSource("idsOrden", idsOrden));

            // Load tasa promedio for reportantes
            Map<Long, BigDecimal> hashTasaPromedioReportante = new HashMap<>();
            if (!idsOrdenAsociada.isEmpty()) {
                List<Map<String, Object>> asocList = jdbc.queryForList(SQL_SELECT_ORDEN_ASOCIADA,
                    new MapSqlParameterSource("idsOrdenAsociada", idsOrdenAsociada));
                for (Map<String, Object> asoc : asocList) {
                    Long idOrd = toLong(asoc.get("IdOrdenReportante"));
                    BigDecimal tasa = getBD(asoc, "TasaPromedioReporte");
                    if (idOrd != null) hashTasaPromedioReportante.put(idOrd, tasa);
                }
            }

            // Build CalcularMontoYCobros input
            JsonObject calcParams = new JsonObject();
            calcParams.addProperty("p_IdEmpresa", idEmpresa);
            calcParams.addProperty("p_IdModulo", idModulo);
            calcParams.addProperty("p_IdUsuario", idUsuario);
            JsonArray calcOrdenes = new JsonArray();
            JsonArray calcCobros = new JsonArray();

            // Build maps for cobros modificados
            Map<String, BigDecimal> hashCobrosModificados = new HashMap<>();
            for (var el : cobrosModIn) {
                JsonObject c = el.getAsJsonObject();
                Long idOrd = c.has("IdOrden") ? c.get("IdOrden").getAsLong() : null;
                String idCobro = c.has("IdCobro") ? c.get("IdCobro").getAsString() : "";
                BigDecimal valor = c.has("ValorCobroAplicado") ? c.get("ValorCobroAplicado").getAsBigDecimal() : BigDecimal.ZERO;
                if (idOrd != null) hashCobrosModificados.put(idOrd + "|" + idCobro, valor);
            }

            List<JsonObject> ordenesParaActualizar = new ArrayList<>();
            boolean needsCalculo = false;

            for (var el : ordenesIn) {
                JsonObject ordIn = el.getAsJsonObject();
                Long idOrden = ordIn.has("IdOrden") ? ordIn.get("IdOrden").getAsLong() : null;
                if (idOrden == null) continue;

                Map<String, Object> info = infoOrdenes.get(idOrden);
                String modCantidad  = ordIn.has("ModificaCantidad") ? ordIn.get("ModificaCantidad").getAsString() : "N";
                String modCobros    = ordIn.has("ModificaCobros") ? ordIn.get("ModificaCobros").getAsString() : "N";
                String modTasaProm  = ordIn.has("ModificaTasaPromedioReporte") ? ordIn.get("ModificaTasaPromedioReporte").getAsString() : "N";
                String modMonto     = ordIn.has("ModificaMonto") ? ordIn.get("ModificaMonto").getAsString() : "N";
                String modSistemaBursatil = ordIn.has("ModificaSistemaBursatil") ? ordIn.get("ModificaSistemaBursatil").getAsString() : "N";

                // Validate
                if (info == null) {
                    JsonObject m = new JsonObject();
                    m.addProperty("tipo", "ERROR");
                    m.addProperty("codigo", "ORDEN_NO_EXISTE");
                    m.addProperty("id", idOrden);
                    mensajes.add(m);
                    continue;
                }
                if (!"INGRESADA".equalsIgnoreCase(getStr(info, "IdEstadoOrden"))
                    && ("S".equalsIgnoreCase(modCantidad) || "S".equalsIgnoreCase(modCobros)
                        || "S".equalsIgnoreCase(modTasaProm) || "S".equalsIgnoreCase(modMonto))) {
                    JsonObject m = new JsonObject();
                    m.addProperty("tipo", "ERROR");
                    m.addProperty("codigo", "ESTADO_ORDEN_NO_PERMITE_MODIFICAR_CANTIDAD");
                    m.addProperty("id", idOrden);
                    mensajes.add(m);
                    continue;
                }

                // Build CalcularMontoYCobros order entry
                if ("S".equalsIgnoreCase(modCantidad) || "S".equalsIgnoreCase(modCobros)
                    || "S".equalsIgnoreCase(modTasaProm) || "S".equalsIgnoreCase(modMonto)) {

                    JsonObject calcOrd = new JsonObject();
                    calcOrd.addProperty("IdInterno", idOrden);
                    calcOrd.addProperty("IdInstrumento", toLong(info.get("IdInstrumento")));
                    calcOrd.addProperty("IdPortafolio", toLong(info.get("IdPortafolioCliente")));
                    calcOrd.addProperty("IdTipoOperacion", getStr(info, "IdTipoOperacion"));
                    calcOrd.addProperty("IdBolsa", getStr(info, "IdBolsa"));
                    calcOrd.addProperty("FechaOperacion", getStr(info, "FechaOperacion"));
                    calcOrd.addProperty("IdTipoCanalIngreso", getStr(info, "IdTipoCanalIngreso"));
                    calcOrd.addProperty("IdMonedaLiquidacionOrden",
                        ordIn.has("IdMonedaLiquidacionOrden") ? ordIn.get("IdMonedaLiquidacionOrden").getAsString() : getStr(info, "IdMonedaLiquidacionOrden"));
                    calcOrd.addProperty("IdEmisor",
                        ordIn.has("IdEmisor") ? ordIn.get("IdEmisor").getAsString() : getStr(info, "IdEmisor"));
                    calcOrd.addProperty("IdInstrumentoSubyacente",
                        ordIn.has("IdInstrumentoSubyacente") ? ordIn.get("IdInstrumentoSubyacente").getAsLong() : toLong(info.get("IdInstrumentoSubyacente")));
                    calcOrd.addProperty("EsFungible", getStr(info, "EsFungible"));
                    calcOrd.addProperty("PlazoIF", toLong(info.get("PlazoIF")));
                    calcOrd.addProperty("PlazoReporte", toLong(info.get("PlazoReporte")));
                    calcOrd.addProperty("PlazoSimultanea", toLong(info.get("PlazoSimultanea")));
                    calcOrd.addProperty("PlazoVentaCorta", toLong(info.get("PlazoVentaCorta")));
                    calcOrd.addProperty("TipoInteres", getStr(info, "TipoInteres"));
                    calcOrd.addProperty("TasaRFIF", getBD(info, "TasaRFIF"));
                    calcOrd.addProperty("TasaSimultanea", getBD(info, "TasaSimultanea"));
                    calcOrd.addProperty("TasaVentaCorta", getBD(info, "TasaVentaCorta"));
                    calcOrd.addProperty("TasaReportado", getBD(info, "TasaReporte"));
                    calcOrd.addProperty("TipoCambio", getBD(info, "TipoCambio"));
                    calcOrd.addProperty("ValorRescateOrden", getBD(info, "ValorRescate"));
                    calcOrd.addProperty("BaseTasa", toLong(info.get("BaseTasa")));
                    calcOrd.addProperty("IdCorredorCorresponsal", getStr(info, "IdCorredorCorresponsal"));
                    calcOrd.addProperty("IdTipoProducto", getStr(info, "IdProducto"));

                    if ("S".equalsIgnoreCase(modCantidad)) {
                        calcOrd.addProperty("CantidadOrden", ordIn.has("Cantidad") ? ordIn.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO);
                    } else {
                        calcOrd.addProperty("CantidadOrden", getBD(info, "Cantidad"));
                    }
                    if ("S".equalsIgnoreCase(modMonto)) {
                        calcOrd.addProperty("MontoOrden", ordIn.has("Monto") ? ordIn.get("Monto").getAsBigDecimal() : BigDecimal.ZERO);
                        calcOrd.addProperty("MontoTotal", ordIn.has("Monto") ? ordIn.get("Monto").getAsBigDecimal() : BigDecimal.ZERO);
                        calcOrd.addProperty("PrecioOrden", ordIn.has("Precio") ? ordIn.get("Precio").getAsBigDecimal() : BigDecimal.ONE);
                        calcOrd.addProperty("EsPorMonto", "S");
                    } else {
                        calcOrd.addProperty("MontoOrden", getBD(info, "MontoOrden"));
                        calcOrd.addProperty("MontoTotal", BigDecimal.ZERO);
                        calcOrd.addProperty("PrecioOrden", getBD(info, "PrecioOrden"));
                        calcOrd.addProperty("EsPorMonto", getStr(info, "EsPorMonto"));
                    }
                    if ("S".equalsIgnoreCase(modTasaProm)) {
                        calcOrd.addProperty("TasaReporte", ordIn.has("TasaPromedioReporte") ? ordIn.get("TasaPromedioReporte").getAsBigDecimal() : BigDecimal.ZERO);
                    } else {
                        String tipoOp = getStr(info, "IdTipoOperacion");
                        if ("COMRRV".equalsIgnoreCase(tipoOp) || "COMRRF".equalsIgnoreCase(tipoOp)) {
                            calcOrd.addProperty("TasaReporte", hashTasaPromedioReportante.getOrDefault(idOrden, BigDecimal.ZERO));
                        } else {
                            calcOrd.addProperty("TasaReporte", getBD(info, "TasaPromedioReporte"));
                        }
                    }
                    calcOrdenes.add(calcOrd);

                    // Add existing cobros
                    for (Map<String, Object> cob : infoCobros) {
                        if (idOrden.equals(toLong(cob.get("IdOrden")))) {
                            JsonObject calcCob = new JsonObject();
                            calcCob.addProperty("IdInterno", idOrden);
                            calcCob.addProperty("IdCobro", getStr(cob, "IdCobro"));
                            String cobroKey = idOrden + "|" + getStr(cob, "IdCobro");
                            if ("S".equalsIgnoreCase(modCobros) && hashCobrosModificados.containsKey(cobroKey)) {
                                calcCob.addProperty("ValorCobroAplicado", hashCobrosModificados.get(cobroKey));
                            } else {
                                calcCob.addProperty("ValorCobroAplicado", getBD(cob, "ValorCobroAplicado"));
                            }
                            calcCobros.add(calcCob);
                        }
                    }
                    needsCalculo = true;
                }

                ordenesParaActualizar.add(ordIn);
            }

            // Call CalcularMontoYCobrosOrdenService
            Map<Long, JsonObject> hashCalculo = new HashMap<>();
            List<JsonObject> cobrosNuevos = new ArrayList<>();
            if (needsCalculo && calcOrdenes.size() > 0) {
                calcParams.add("p_Ordenes", calcOrdenes);
                calcParams.add("p_CobrosModificados", calcCobros);
                try {
                    JsonObject calcResult = calcularService.execute(calcParams);
                    if (calcResult.has("p_out_Ordenes")) {
                        for (var o : calcResult.get("p_out_Ordenes").getAsJsonArray()) {
                            JsonObject calcOrd = o.getAsJsonObject();
                            Long id = calcOrd.has("IdInterno") ? calcOrd.get("IdInterno").getAsLong() : null;
                            if (id != null) hashCalculo.put(id, calcOrd);
                        }
                    }
                    if (calcResult.has("p_out_Cobros")) {
                        for (var c : calcResult.get("p_out_Cobros").getAsJsonArray()) {
                            cobrosNuevos.add(c.getAsJsonObject());
                        }
                    }
                } catch (UnsupportedOperationException e) {
                    log.warn("CalcularMontoYCobrosOrdenService deferred; updating orders without cobro recalculation");
                } catch (Exception e) {
                    log.warn("CalcularMontoYCobrosOrden error: {}", e.getMessage());
                }
            }

            // Update orders
            for (var el : ordenesIn) {
                JsonObject ordIn = el.getAsJsonObject();
                Long idOrden = ordIn.has("IdOrden") ? ordIn.get("IdOrden").getAsLong() : null;
                if (idOrden == null) continue;
                Map<String, Object> info = infoOrdenes.get(idOrden);
                if (info == null) continue;

                // Build update param source
                MapSqlParameterSource upd = new MapSqlParameterSource();
                upd.addValue("IdOrden", idOrden);

                // Apply calculo result if available
                JsonObject calc = hashCalculo.get(idOrden);
                if (calc != null) {
                    upd.addValue("Cantidad", bd(calc, "CantidadOperacion", getBD(info, "Cantidad")));
                    upd.addValue("MontoOrden", bd(calc, "MontoOperacion", getBD(info, "MontoOrden")));
                    upd.addValue("MontoOrdenPlazo", bd(calc, "MontoOperacionPlazo", BigDecimal.ZERO));
                    upd.addValue("MontoLiquidacion", bd(calc, "MontoOperacionLiquidacion", BigDecimal.ZERO));
                    upd.addValue("MontoLiquidacionPlazo", bd(calc, "MontoOperacionLiquidacionPlazo", BigDecimal.ZERO));
                    upd.addValue("MontoLocal", bd(calc, "MontoOperacionLocal", BigDecimal.ZERO));
                    upd.addValue("MontoLocalPlazo", bd(calc, "MontoOperacionLocalPlazo", BigDecimal.ZERO));
                    upd.addValue("PrecioPlazo", bd(calc, "PrecioPlazo", BigDecimal.ZERO));
                    String tipoOp = getStr(info, "IdTipoOperacion");
                    if ("VENRRF".equalsIgnoreCase(tipoOp) || "VENRRV".equalsIgnoreCase(tipoOp)) {
                        upd.addValue("TasaPromedioReporte", bd(calc, "TasaReporte", BigDecimal.ZERO));
                    } else {
                        upd.addValue("TasaPromedioReporte", getBD(info, "TasaPromedioReporte"));
                    }
                } else {
                    // Minimal update: apply what was in params
                    BigDecimal cantidad = ordIn.has("Cantidad") ? ordIn.get("Cantidad").getAsBigDecimal() : getBD(info, "Cantidad");
                    BigDecimal monto = ordIn.has("Monto") ? ordIn.get("Monto").getAsBigDecimal() : getBD(info, "MontoOrden");
                    BigDecimal tasaProm = ordIn.has("TasaPromedioReporte") ? ordIn.get("TasaPromedioReporte").getAsBigDecimal() : getBD(info, "TasaPromedioReporte");
                    upd.addValue("Cantidad", cantidad);
                    upd.addValue("MontoOrden", monto);
                    upd.addValue("MontoOrdenPlazo", BigDecimal.ZERO);
                    upd.addValue("MontoLiquidacion", BigDecimal.ZERO);
                    upd.addValue("MontoLiquidacionPlazo", BigDecimal.ZERO);
                    upd.addValue("MontoLocal", BigDecimal.ZERO);
                    upd.addValue("MontoLocalPlazo", BigDecimal.ZERO);
                    upd.addValue("PrecioPlazo", BigDecimal.ZERO);
                    upd.addValue("TasaPromedioReporte", tasaProm);
                }

                upd.addValue("IdSistemaBursatil", ordIn.has("IdSistemaBursatil") ? ordIn.get("IdSistemaBursatil").getAsString() : getStr(info, "IdSistemaBursatil"));
                upd.addValue("IdInstrumentoSubyacente", ordIn.has("IdInstrumentoSubyacente") ? ordIn.get("IdInstrumentoSubyacente").getAsLong() : toLong(info.get("IdInstrumentoSubyacente")));
                upd.addValue("IdEmisor", ordIn.has("IdEmisor") ? ordIn.get("IdEmisor").getAsString() : getStr(info, "IdEmisor"));
                upd.addValue("IdMonedaLiquidacionOrden", ordIn.has("IdMonedaLiquidacionOrden") ? ordIn.get("IdMonedaLiquidacionOrden").getAsString() : getStr(info, "IdMonedaLiquidacionOrden"));
                upd.addValue("IdCondicionDeLiquidacion", ordIn.has("IdCondicionDeLiquidacion") ? ordIn.get("IdCondicionDeLiquidacion").getAsString() : getStr(info, "IdCondicionDeLiquidacion"));
                upd.addValue("MontoInteresContado", ordIn.has("MontoInteresContado") ? ordIn.get("MontoInteresContado").getAsBigDecimal() : BigDecimal.ZERO);
                upd.addValue("MontoInteresPlazo", ordIn.has("MontoInteresPlazo") ? ordIn.get("MontoInteresPlazo").getAsBigDecimal() : BigDecimal.ZERO);

                try {
                    jdbc.update(SQL_UPDATE_ORDENES, upd);
                } catch (Exception e) {
                    log.warn("Error updating orden {}: {}", idOrden, e.getMessage());
                }

                JsonObject m = new JsonObject();
                m.addProperty("tipo", "INFORMACION");
                m.addProperty("codigo", "ORDEN_MODIFICADA");
                m.addProperty("id", idOrden);
                mensajes.add(m);
            }

            // Insert new cobros from calculo
            for (JsonObject cob : cobrosNuevos) {
                try {
                    jdbc.update(SQL_INSERT_COBROS,
                        new MapSqlParameterSource()
                            .addValue("IdOrden", cob.has("IdInterno") ? cob.get("IdInterno").getAsLong() : null)
                            .addValue("IdCobro", cob.has("IdCobro") ? cob.get("IdCobro").getAsString() : "")
                            .addValue("IdGrupoCobro", cob.has("IdGrupoCobro") ? cob.get("IdGrupoCobro").getAsLong() : null)
                            .addValue("IdMoneda", cob.has("IdMonedaCobro") ? cob.get("IdMonedaCobro").getAsString() : "CLP")
                            .addValue("ValorCobroAplicado", cob.has("ValorCobroAplicado") ? cob.get("ValorCobroAplicado").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("MontoCobro", cob.has("MontoCobro") ? cob.get("MontoCobro").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("MontoCobroLocal", cob.has("MontoCobroLocal") ? cob.get("MontoCobroLocal").getAsBigDecimal() : BigDecimal.ZERO)
                            .addValue("EsComprobante", "N")
                            .addValue("IdAsociacion", cob.has("IdAsociacion") ? cob.get("IdAsociacion").getAsLong() : 0L));
                } catch (Exception e) {
                    log.warn("Error inserting cobro for orden {}: {}", cob.has("IdInterno") ? cob.get("IdInterno") : "?", e.getMessage());
                }
            }

            long errCount = 0;
            for (var m : mensajes) {
                if ("ERROR".equalsIgnoreCase(m.getAsJsonObject().get("tipo").getAsString())) errCount++;
            }
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errCount);
            out.add("P_OUT_MENSAJES", mensajes);
            return out;
        } catch (Exception e) {
            log.error("ModificarOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static BigDecimal bd(JsonObject o, String key, BigDecimal def) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) return def;
        try { return o.get(key).getAsBigDecimal(); } catch (Exception e) { return def; }
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

    private static BigDecimal getBD(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String loadSql(String path) {
        try (var is = ModificarOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
