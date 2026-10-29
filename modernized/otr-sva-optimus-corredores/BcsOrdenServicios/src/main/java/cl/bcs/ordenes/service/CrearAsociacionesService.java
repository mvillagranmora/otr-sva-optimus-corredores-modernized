package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.CrearAsociaciones (JavaCall).
 * Creates repo associations between reportante and reportado orders.
 * Splits orders, calculates amounts by ModalidadCalculo (M/C/R), persists via batch INSERT.
 */
@Service
public class CrearAsociacionesService {

    private static final Logger log = LoggerFactory.getLogger(CrearAsociacionesService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;
    private final CrearOrdenesService crearOrdenesService;
    private final ModificarOrdenesService modificarOrdenesService;

    private final String SQL_SELECT_ESTADO_MODULO;
    private final String SQL_INSERT_ORDEN_ASOCIADA;
    private final String SQL_SELECT_ORDENES;

    public CrearAsociacionesService(NamedParameterJdbcTemplate jdbc,
                                     RequisitosBasicosUtil requisitosUtil,
                                     ConfiguracionSistemaUtil configUtil,
                                     PlataformaCallClient plataformaClient,
                                     CrearOrdenesService crearOrdenesService,
                                     ModificarOrdenesService modificarOrdenesService) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
        this.crearOrdenesService = crearOrdenesService;
        this.modificarOrdenesService = modificarOrdenesService;
        this.SQL_SELECT_ESTADO_MODULO   = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_INSERT_ORDEN_ASOCIADA  = loadSql("cl/bcs/ordenes/sql/CrearAsociaciones_InsertOrdenAsociada.sql");
        this.SQL_SELECT_ORDENES         = loadSql("cl/bcs/ordenes/sql/CrearAsociaciones_SelectOrdenes.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CrearAsociacionesService.execute()");
        try {
            Long idEmpresa    = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario    = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo   = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String simular    = params.has("p_simularOperacion") ? params.get("p_simularOperacion").getAsString() : "N";
            String usaReglas  = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";
            JsonArray asocIn  = params.has("p_Asociaciones") ? params.get("p_Asociaciones").getAsJsonArray() : new JsonArray();

            JsonArray mensajes = new JsonArray();

            // Validate basic requirements
            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            if (asocIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_ASOCIACIONES\"}]"));
                return out;
            }

            // Validate unique correlativos
            Set<Long> correlativos = new HashSet<>();
            for (var el : asocIn) {
                JsonObject a = el.getAsJsonObject();
                Long interno = a.has("IdInterno") ? a.get("IdInterno").getAsLong() : null;
                if (interno != null && !correlativos.add(interno)) {
                    JsonObject out = new JsonObject();
                    out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                    out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"CORRELATIVO_INCORRECTO\"}]"));
                    return out;
                }
            }

            // Load all orders referenced in associations
            Set<Long> idOrdenes = new LinkedHashSet<>();
            for (var el : asocIn) {
                JsonObject a = el.getAsJsonObject();
                if (a.has("IdOrdenM")) idOrdenes.add(a.get("IdOrdenM").getAsLong());
                if (a.has("IdOrdenN")) idOrdenes.add(a.get("IdOrdenN").getAsLong());
            }

            List<Map<String, Object>> ordenesOut = jdbc.queryForList(SQL_SELECT_ORDENES,
                new MapSqlParameterSource("Ordenes", new ArrayList<>(idOrdenes)));
            if (ordenesOut.size() != idOrdenes.size()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"ORDENES_INVALIDAS\"}]"));
                return out;
            }

            Map<Long, Map<String, Object>> hashOrdenes = new HashMap<>();
            for (Map<String, Object> ord : ordenesOut) {
                hashOrdenes.put(toLong(ord.get("IdOrden")), ord);
            }

            // Validate modulo state
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

            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");

            // Build orders to create (partial reportante) and orders to update
            List<JsonObject> ordenesCrear = new ArrayList<>();
            List<JsonObject> ordenesActualizar = new ArrayList<>();

            for (var el : asocIn) {
                JsonObject asoc = el.getAsJsonObject();
                Long idOrdenM = asoc.has("IdOrdenM") ? asoc.get("IdOrdenM").getAsLong() : null;
                Long idOrdenN = asoc.has("IdOrdenN") ? asoc.get("IdOrdenN").getAsLong() : null;
                BigDecimal monto = asoc.has("Monto") ? asoc.get("Monto").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal cantidad = asoc.has("Cantidad") ? asoc.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal precio = asoc.has("Precio") ? asoc.get("Precio").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal tasa = asoc.has("Tasa") ? asoc.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO;

                Map<String, Object> ordReportante = hashOrdenes.get(idOrdenM);
                Map<String, Object> ordReportado = hashOrdenes.get(idOrdenN);

                if (ordReportante != null) {
                    BigDecimal montoDisponible = getBD(ordReportante, "MontoDisponibleAsociar");
                    if (montoDisponible.compareTo(BigDecimal.ZERO) == 0) montoDisponible = BigDecimal.ONE; // avoid div/0
                    BigDecimal cantidadPct = monto.multiply(new BigDecimal(100))
                        .divide(montoDisponible, 0, RoundingMode.HALF_DOWN);
                    if (cantidadPct.compareTo(BigDecimal.ZERO) == 0) cantidadPct = BigDecimal.ONE;

                    BigDecimal remMonto = montoDisponible.subtract(monto);

                    if (remMonto.compareTo(BigDecimal.ZERO) > 0) {
                        // Create new order for the matched portion
                        JsonObject newOrd = buildOrdenEntrada((long) ordenesCrear.size(), ordReportante, ordReportado,
                            monto, cantidadPct, precio, tasa, fechaSistema);
                        ordenesCrear.add(newOrd);
                        // Update existing order with remainder
                        JsonObject updOrd = buildOrdenUpdate((long) ordenesActualizar.size(), ordReportante, ordReportado,
                            remMonto, getBD(ordReportante, "CantidadDisponibleAsociar").subtract(cantidad), false);
                        ordenesActualizar.add(updOrd);
                    } else {
                        // Update existing order with full amount
                        JsonObject updOrd = buildOrdenUpdate((long) ordenesActualizar.size(), ordReportante, ordReportado,
                            monto, cantidadPct, true);
                        ordenesActualizar.add(updOrd);
                    }
                }
            }

            // Call CalcularParidad for currency conversion on new orders
            if (!ordenesCrear.isEmpty()) {
                try {
                    JsonArray calculos = new JsonArray();
                    for (JsonObject ord : ordenesCrear) {
                        JsonObject c = new JsonObject();
                        c.addProperty("IdTrabajo", 12L);
                        c.addProperty("IdInterno", ord.has("IdInterno") ? ord.get("IdInterno").getAsLong() : 0L);
                        c.addProperty("Fecha", fechaSistema);
                        c.addProperty("IdMonedaOrigen", ord.has("IdMonedaInstrumento") ? ord.get("IdMonedaInstrumento").getAsString() : "CLP");
                        c.addProperty("IdMonedaDestino", ord.has("IdMonedaLiquidacionOrden") ? ord.get("IdMonedaLiquidacionOrden").getAsString() : "CLP");
                        c.addProperty("ValorCambioLocalModificado", 0);
                        c.addProperty("MontoOrigen", ord.has("MontoNeto") ? ord.get("MontoNeto").getAsString() : "0");
                        c.addProperty("IdUso", "ORDENES");
                        calculos.add(c);
                    }
                    JsonObject parParams = new JsonObject();
                    parParams.addProperty("p_IdEmpresa", idEmpresa);
                    parParams.addProperty("p_IdTrabajo", 12L);
                    parParams.add("p_CalculosEntrada", calculos);
                    plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", parParams);
                } catch (Exception e) {
                    log.warn("CalcularParidad error: {}", e.getMessage());
                }
            }

            // Create new orders
            Map<Long, Long> mapInternoToNuevaOrden = new HashMap<>();
            if (!ordenesCrear.isEmpty() && !"S".equalsIgnoreCase(simular)) {
                try {
                    JsonArray ordenesArr = new JsonArray();
                    for (JsonObject o : ordenesCrear) ordenesArr.add(o);
                    JsonObject crearParams = new JsonObject();
                    crearParams.addProperty("p_IdEmpresa", idEmpresa);
                    crearParams.addProperty("p_IdUsuario", idUsuario);
                    crearParams.addProperty("p_IdModulo", idModulo);
                    crearParams.addProperty("p_simularOperacion", "N");
                    crearParams.addProperty("p_UsaReglasNegocio", usaReglas);
                    crearParams.add("p_Ordenes", ordenesArr);
                    crearParams.add("p_Garantias", new JsonArray());
                    crearParams.add("p_CobrosModificados", new JsonArray());
                    JsonObject crearResult = crearOrdenesService.execute(crearParams);
                    long errCrear = crearResult.has("P_OUT_ERRORESCONTADOR") ? crearResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                    if (errCrear > 0) {
                        if (crearResult.has("P_OUT_MENSAJES")) {
                            for (var m : crearResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                        }
                        JsonObject out = new JsonObject();
                        out.addProperty("P_OUT_ERRORESCONTADOR", errCrear);
                        out.add("P_OUT_MENSAJES", mensajes);
                        return out;
                    }
                    if (crearResult.has("P_OUT_RESULTADO")) {
                        for (var r : crearResult.get("P_OUT_RESULTADO").getAsJsonArray()) {
                            JsonObject res = r.getAsJsonObject();
                            Long interno = res.has("IdInterno") ? res.get("IdInterno").getAsLong() : null;
                            Long idOrden = res.has("IdOrden") ? res.get("IdOrden").getAsLong() : null;
                            if (interno != null && idOrden != null) mapInternoToNuevaOrden.put(interno, idOrden);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error creating split orders: {}", e.getMessage());
                }
            }

            // Modify existing orders (remainder)
            if (!ordenesActualizar.isEmpty() && !"S".equalsIgnoreCase(simular)) {
                try {
                    JsonArray ordenesArr = new JsonArray();
                    for (JsonObject o : ordenesActualizar) ordenesArr.add(o);
                    JsonObject modParams = new JsonObject();
                    modParams.addProperty("p_IdEmpresa", idEmpresa);
                    modParams.addProperty("p_IdUsuario", idUsuario);
                    modParams.addProperty("p_IdModulo", idModulo);
                    modParams.add("p_Ordenes", ordenesArr);
                    modParams.add("p_CobrosModificados", new JsonArray());
                    JsonObject modResult = modificarOrdenesService.execute(modParams);
                    long errMod = modResult.has("P_OUT_ERRORESCONTADOR") ? modResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                    if (errMod > 0) {
                        if (modResult.has("P_OUT_MENSAJES")) {
                            for (var m : modResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                        }
                        JsonObject out = new JsonObject();
                        out.addProperty("P_OUT_ERRORESCONTADOR", errMod);
                        out.add("P_OUT_MENSAJES", mensajes);
                        return out;
                    }
                } catch (UnsupportedOperationException e) {
                    log.warn("ModificarOrdenesService deferred; skipping order update for associations");
                } catch (Exception e) {
                    log.warn("Error modifying split orders: {}", e.getMessage());
                }
            }

            // Insert association records
            JsonArray resultado = new JsonArray();
            List<Map<String, Object>> asociacionesParaInsertar = new ArrayList<>();

            for (var el : asocIn) {
                JsonObject asoc = el.getAsJsonObject();
                Long interno   = asoc.has("IdInterno") ? asoc.get("IdInterno").getAsLong() : null;
                Long idOrdenM  = asoc.has("IdOrdenM") ? asoc.get("IdOrdenM").getAsLong() : null;
                Long idOrdenN  = asoc.has("IdOrdenN") ? asoc.get("IdOrdenN").getAsLong() : null;
                BigDecimal monto = asoc.has("Monto") ? asoc.get("Monto").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal cantidad = asoc.has("Cantidad") ? asoc.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO;
                BigDecimal precio = asoc.has("Precio") ? asoc.get("Precio").getAsBigDecimal() : BigDecimal.ZERO;
                String modalidad = asoc.has("ModalidadCalculo") ? asoc.get("ModalidadCalculo").getAsString() : "M";

                // Remap IdOrdenM if a new order was created for it
                Long idOrdenMFinal = idOrdenM;
                for (int i = 0; i < ordenesCrear.size(); i++) {
                    JsonObject ord = ordenesCrear.get(i);
                    if (idOrdenM.equals(ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : null)) {
                        Long interno2 = ord.has("IdInterno") ? ord.get("IdInterno").getAsLong() : null;
                        if (interno2 != null && mapInternoToNuevaOrden.containsKey(interno2)) {
                            idOrdenMFinal = mapInternoToNuevaOrden.get(interno2);
                        }
                        break;
                    }
                }

                // Calculate amount by modalidad
                BigDecimal cantFinal, montoFinal;
                if ("M".equals(modalidad)) {
                    BigDecimal p2 = precio.setScale(2, RoundingMode.HALF_UP);
                    cantFinal = p2.compareTo(BigDecimal.ZERO) > 0
                        ? monto.divide(p2, 0, RoundingMode.FLOOR) : BigDecimal.ZERO;
                    if (cantFinal.compareTo(BigDecimal.ZERO) < 0) cantFinal = BigDecimal.ZERO;
                    montoFinal = cantFinal.multiply(p2).setScale(2, RoundingMode.HALF_UP);
                } else if ("C".equals(modalidad)) {
                    cantFinal = cantidad;
                    montoFinal = cantFinal.multiply(precio);
                } else { // R
                    montoFinal = monto;
                    cantFinal = precio.compareTo(BigDecimal.ZERO) > 0
                        ? monto.multiply(new BigDecimal(100)).divide(precio, 0, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                }

                // Assign a sequence ID (simplified — use timestamp as surrogate)
                Long idOrdenAsociada = System.currentTimeMillis() + interno;

                Map<String, Object> newReg = new HashMap<>();
                newReg.put("IdOrdenAsociada", idOrdenAsociada);
                newReg.put("IdOrdenN", idOrdenN);
                newReg.put("IdOrdenM", idOrdenMFinal);
                newReg.put("IdOrdenMOrigen", idOrdenM);
                newReg.put("Precio", precio);
                newReg.put("ModalidadCalculo", modalidad);
                newReg.put("Monto", montoFinal);
                newReg.put("Cantidad", cantFinal);
                newReg.put("Tasa", asoc.has("Tasa") ? asoc.get("Tasa").getAsBigDecimal() : BigDecimal.ZERO);
                newReg.put("IdUsuario", idUsuario);
                asociacionesParaInsertar.add(newReg);

                JsonObject r = new JsonObject();
                r.addProperty("IdInterno", interno);
                r.addProperty("IdOrdenAsociada", idOrdenAsociada);
                r.addProperty("Monto", montoFinal);
                r.addProperty("Cantidad", cantFinal);
                r.addProperty("Msg", "OK");
                resultado.add(r);
            }

            // Persist associations
            if (!"S".equalsIgnoreCase(simular) && !asociacionesParaInsertar.isEmpty()) {
                for (Map<String, Object> reg : asociacionesParaInsertar) {
                    try {
                        jdbc.update(SQL_INSERT_ORDEN_ASOCIADA,
                            new MapSqlParameterSource()
                                .addValue("IdOrdenAsociada", reg.get("IdOrdenAsociada"))
                                .addValue("IdOrdenN", reg.get("IdOrdenN"))
                                .addValue("IdOrdenM", reg.get("IdOrdenM"))
                                .addValue("IdOrdenMOrigen", reg.get("IdOrdenMOrigen"))
                                .addValue("Precio", reg.get("Precio"))
                                .addValue("ModalidadCalculo", reg.get("ModalidadCalculo"))
                                .addValue("Monto", reg.get("Monto"))
                                .addValue("Cantidad", reg.get("Cantidad"))
                                .addValue("Tasa", reg.get("Tasa"))
                                .addValue("IdUsuario", reg.get("IdUsuario")));
                    } catch (Exception e) {
                        log.warn("Error inserting asociacion: {}", e.getMessage());
                    }
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", mensajes);
            out.add("p_out_Resultado", resultado);
            return out;
        } catch (Exception e) {
            log.error("CrearAsociacionesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private JsonObject buildOrdenEntrada(long idx, Map<String, Object> repte, Map<String, Object> repdo,
                                          BigDecimal monto, BigDecimal cantidad, BigDecimal precio,
                                          BigDecimal tasa, String fechaSistema) {
        JsonObject o = new JsonObject();
        o.addProperty("IdInterno", idx);
        o.addProperty("IdOrden", toLong(repte.get("IdOrden")));
        o.addProperty("FechaRecepcion", fechaSistema);
        o.addProperty("FechaVigencia", fechaSistema);
        o.addProperty("FechaEmision", fechaSistema);
        o.addProperty("FechaOperacion", fechaSistema);
        o.addProperty("IdTipoOperacion", getStr(repte, "IdTipoOperacion"));
        o.addProperty("IdCondicionDeLiquidacion", getStr(repdo, "IdCondicionDeLiquidacion"));
        o.addProperty("IdTipoVigencia", getStr(repte, "IdTipoVigenciaOrden"));
        o.addProperty("Glosa", getStr(repte, "GlosaOrden"));
        o.addProperty("Cantidad", cantidad);
        o.addProperty("MontoNeto", monto);
        o.addProperty("ValorRescate", 0);
        o.addProperty("IdBoveda", getStr(repdo, "IdBoveda"));
        o.addProperty("IdFormaCustodia", getStr(repte, "IdFormaCustodia"));
        o.addProperty("IdPorcentajeVisibilidad", 1L);
        o.addProperty("PrecioLimite", precio);
        o.addProperty("EsFacturaInmediata", "N");
        o.addProperty("IdTipoPrecio", "M");
        o.addProperty("IdPortafolio", toLong(repte.get("IdPortafolio")));
        o.addProperty("IdTipoLibroOrden", "NORMAL");
        o.addProperty("IdTipoCanalIngreso", getStr(repte, "IdTipoCanalIngreso"));
        o.addProperty("IdTipoReajuste", getStr(repte, "IdTipoReajuste"));
        o.addProperty("IdEmisor", getStr(repdo, "IdEmisor"));
        o.addProperty("IdBolsa", getStr(repte, "IdBolsa"));
        o.addProperty("EspecificaBolsa", getStr(repte, "EspecificaBolsa"));
        o.addProperty("IdMonedaInstrumento", getStr(repdo, "IdMonedaInstrumento"));
        o.addProperty("IdMonedaLiquidacionOrden", getStr(repdo, "IdMonedaLiquidacionOrden"));
        o.addProperty("TipoCambio", 0);
        o.addProperty("IdInstrumento", toLong(repte.get("IdInstrumento")));
        o.addProperty("IdInstrumentoSubyacente", toLong(repdo.get("IdInstrumentoSubyacente")));
        o.addProperty("IdCondicionDeLiquidacionPlazo", "TP");
        o.addProperty("TasaVentaCorta", 0);
        o.addProperty("TasaSimultanea", 0);
        o.addProperty("TasaRFIF", 0);
        o.addProperty("PlazoIF", 0L);
        o.addProperty("PlazoSimultanea", 0L);
        o.addProperty("PlazoVentaCorta", 0L);
        o.addProperty("EsVentaCorta", "N");
        o.addProperty("EsOD", "N");
        o.addProperty("EsFungible", "N");
        o.addProperty("IdTipoCanalDistribucion", "MAN");
        o.addProperty("TasaReporte", tasa);
        o.addProperty("TasaReportanteReferencia", getBD(repte, "TasaReportanteReferencia"));
        o.addProperty("TasaPromedioReporte", getBD(repte, "TasaPromedioReporte"));
        o.addProperty("PlazoReporte", toLong(repte.get("Plazo")));
        o.addProperty("BaseTasa", toLong(repte.get("BaseTasa")));
        o.addProperty("PagoCobroCuentaInversion", "S");
        o.addProperty("IdPreferenciaPagoPortafolio", 0L);
        o.addProperty("TipoInteres", "C");
        o.addProperty("IdCuentaCustodioPortafolio", toLong(repte.get("IdCuentaCustodioPortafolio")));
        return o;
    }

    private JsonObject buildOrdenUpdate(long idx, Map<String, Object> repte, Map<String, Object> repdo,
                                         BigDecimal monto, BigDecimal cantidad, boolean incluyeCondicion) {
        JsonObject o = new JsonObject();
        o.addProperty("IdOrden", toLong(repte.get("IdOrden")));
        o.addProperty("ModificaCantidad", "N");
        o.addProperty("Cantidad", cantidad);
        o.addProperty("ModificaCobros", "N");
        o.addProperty("IdAsociacion", 0L);
        o.addProperty("TasaPromedioReporte", getBD(repte, "TasaPromedioReporte"));
        o.addProperty("ModificaTasaPromedioReporte", "S");
        o.addProperty("ModificaSistemaBursatil", "N");
        o.addProperty("ModificaMonto", "S");
        o.addProperty("Monto", monto);
        o.addProperty("Precio", getBD(repdo, "PrecioOrden"));
        o.addProperty("IdInstrumentoSubyacente", toLong(repte.get("IdInstrumentoSubyacente")));
        o.addProperty("IdEmisor", getStr(repte, "IdEmisor"));
        o.addProperty("IdMonedaLiquidacionOrden", getStr(repte, "IdMonedaLiquidacionOrden"));
        if (incluyeCondicion) {
            o.addProperty("IdCondicionDeLiquidacion", getStr(repdo, "IdCondicionDeLiquidacion"));
        }
        return o;
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
        try (var is = CrearAsociacionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
