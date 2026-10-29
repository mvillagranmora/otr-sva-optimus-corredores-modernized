package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaSiguienteUtil;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarOrdenesConSobregiroCustodia.
 *
 * Validates overdraft (sobregiro) custody conditions for a batch of postreras ordenes:
 * - Loads transacciones and indicadores portafolio from HANA
 * - Computes fechas liquidacion via ObtenerDiaSiguienteUtil
 * - Calls ConsultarCustodiaIngresoOrden to get custody saldos
 * - Emits warnings for portfolios that would exceed custody limits
 */
@Service
public class ConsultarOrdenesConSobregiroCustodiaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarOrdenesConSobregiroCustodiaService.class);

    private static final String CLASIFICACION_IF = "IF";
    private static final String CLASIFICACION_RF = "RF";
    private static final String CLASIFICACION_RV = "RV";
    private static final String ACCION_VENTA     = "VENTA";
    private static final String FORMA_CUSTODIA   = "CUS";

    private final NamedParameterJdbcTemplate jdbc;
    private final ObtenerDiaSiguienteUtil diaSiguienteUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final ConsultarCustodiaIngresoOrdenService custodiaService;

    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_LIBROS_ORDENES;
    private final String SQL_UPDATE_LIBRO_ORDEN;
    private final String SQL_SELECT_INDICADORES_PORTAFOLIO;

    public ConsultarOrdenesConSobregiroCustodiaService(
            NamedParameterJdbcTemplate jdbc,
            ObtenerDiaSiguienteUtil diaSiguienteUtil,
            ConfiguracionSistemaUtil configUtil,
            ConsultarCustodiaIngresoOrdenService custodiaService) {
        this.jdbc             = jdbc;
        this.diaSiguienteUtil = diaSiguienteUtil;
        this.configUtil       = configUtil;
        this.custodiaService  = custodiaService;
        this.SQL_SELECT_TRANSACCIONES            = loadSql("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectTransacciones.sql");
        this.SQL_SELECT_LIBROS_ORDENES           = loadSql("cl/bcs/ordenes/sql/CrearOrdenesPostreras_SelectLibroOrdenesGrupo.sql");
        this.SQL_UPDATE_LIBRO_ORDEN              = loadSql("cl/bcs/ordenes/sql/CrearOrdenesPostreras_UpdateLibroOrdenes.sql");
        this.SQL_SELECT_INDICADORES_PORTAFOLIO   = loadSql("cl/bcs/ordenes/sql/Globales_SelectIndicadoresPortafolio.sql");
    }

    public JsonObject execute(JsonObject params) {
        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject out = new JsonObject();
        JsonArray mensajes = new JsonArray();
        out.add("P_OUT_MENSAJES", mensajes);
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);

        try {
            long idEmpresa  = params.has("p_IdEmpresa")  ? params.get("p_IdEmpresa").getAsLong()   : 0L;
            long idUsuario  = params.has("p_IdUsuario")  ? params.get("p_IdUsuario").getAsLong()   : 0L;
            String idModulo = params.has("p_IdModulo")   ? params.get("p_IdModulo").getAsString()  : "";
            JsonArray ordenesIn = params.has("p_Ordenes") ? params.getAsJsonArray("p_Ordenes") : new JsonArray();

            if (ordenesIn.size() == 0) {
                return out;
            }

            // ── 1. Load transacciones ──────────────────────────────────────────
            List<Long> idsTransaccion = new ArrayList<>();
            for (JsonElement el : ordenesIn) {
                JsonObject o = el.getAsJsonObject();
                if (o.has("IdTransaccion")) idsTransaccion.add(o.get("IdTransaccion").getAsLong());
            }

            Map<Long, Map<String, Object>> transacciones = new HashMap<>();
            if (!idsTransaccion.isEmpty()) {
                jdbc.queryForList(SQL_SELECT_TRANSACCIONES,
                        new MapSqlParameterSource("idsTransaccion", idsTransaccion))
                    .forEach(row -> transacciones.put(toLong(row.get("IdTransaccion")), row));
            }

            // ── 2. Handle grupo/libro normalization ────────────────────────────
            JsonObject firstOrden = ordenesIn.get(0).getAsJsonObject();
            if (firstOrden.has("IdGrupo") && !firstOrden.get("IdGrupo").isJsonNull()) {
                long idGrupo = firstOrden.get("IdGrupo").getAsLong();
                List<Map<String, Object>> libros = jdbc.queryForList(SQL_SELECT_LIBROS_ORDENES,
                        new MapSqlParameterSource("IdGrupo", idGrupo));
                if (!libros.isEmpty()) {
                    Long idLibroPrincipal = toLong(libros.get(0).get("IdLibroOrdenes"));
                    List<Map<String, Object>> updates = new ArrayList<>();
                    for (Long idTran : idsTransaccion) {
                        Map<String, Object> tran = transacciones.get(idTran);
                        if (tran != null) {
                            Long idLibroTran = toLong(tran.get("IdLibroOrdenes"));
                            if (!idLibroPrincipal.equals(idLibroTran)) {
                                Map<String, Object> upd = new HashMap<>();
                                upd.put("IdLibroOrdenes", idLibroPrincipal);
                                upd.put("IdTransaccion", idTran);
                                updates.add(upd);
                                tran.put("IdLibroOrdenes", idLibroPrincipal);
                            }
                        }
                    }
                    if (!updates.isEmpty()) {
                        jdbc.batchUpdate(SQL_UPDATE_LIBRO_ORDEN,
                                updates.stream()
                                       .map(u -> new MapSqlParameterSource(u))
                                       .toArray(MapSqlParameterSource[]::new));
                    }
                }
            }

            // ── 3. Build ordenes list annotated with transaccion data ──────────
            String operacionesParam = configUtil.getParametroString(idEmpresa, "CUS.OperacionesNegativas");
            List<String> operacionesCustodia = operacionesParam.isEmpty()
                    ? Collections.emptyList()
                    : Arrays.asList(operacionesParam.split(","));

            // Config for empresa pais
            String paisStr = configUtil.getParametroString(idEmpresa, "PLA.IdPais");
            long idPais = paisStr.isEmpty() ? 1L : toLong(paisStr);

            List<Map<String, Object>> ordenes = new ArrayList<>();
            Set<Long> idsPortafolio = new LinkedHashSet<>();

            for (JsonElement el : ordenesIn) {
                JsonObject oin = el.getAsJsonObject();
                Map<String, Object> ord = new HashMap<>();

                Long idTransaccion = oin.has("IdTransaccion") ? oin.get("IdTransaccion").getAsLong() : null;
                Map<String, Object> tran = idTransaccion != null ? transacciones.get(idTransaccion) : null;

                if (tran == null) {
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "E");
                    msg.addProperty("Codigo", "TRANSACCION_NO_EXISTE");
                    msg.addProperty("Descripcion", "Transaccion " + idTransaccion + " no encontrada");
                    mensajes.add(msg);
                    out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                    return out;
                }

                // Core fields from transaccion
                ord.put("IdTransaccion",              idTransaccion);
                ord.put("IdPortafolioCliente",        oin.has("IdPortafolio") ? oin.get("IdPortafolio").getAsLong() : 0L);
                ord.put("IdBoveda",                   strOrEmpty(oin, "IdBoveda"));
                ord.put("IdFormaCustodia",            strOrEmpty(oin, "IdFormaCustodia"));
                ord.put("Cantidad",                   oin.has("Cantidad") ? oin.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO);
                ord.put("ValorRescate",               oin.has("ValorRescate") ? oin.get("ValorRescate").getAsBigDecimal() : BigDecimal.ZERO);
                ord.put("EsVentaCorta",               strFromMap(tran, "EsVentaCorta", "N"));
                ord.put("IdTipoOperacion",            strFromMap(tran, "IdTipoOperacion", ""));
                ord.put("IdInstrumento",              toLong(tran.get("IdInstrumento")));
                ord.put("IdInstrumentoSubyacente",    toLong(tran.get("IdInstrumentoSubyacente")));
                ord.put("IdClasificacionTipoInstrumento", strFromMap(tran, "IdClasificacionTipoInstrumento", ""));
                ord.put("EsFungible",                 strFromMap(tran, "EsFungible", "N"));
                ord.put("NemotecnicoSVS",             strFromMap(tran, "NemotecnicoSVS", ""));
                ord.put("Nemotecnico",                strFromMap(tran, "Nemotecnico", ""));
                ord.put("DiasLiquidacionContado",     toLong(tran.getOrDefault("DiasLiquidacionContado", 0L)));
                ord.put("AccionOrden",                strFromMap(tran, "AccionOrden", ""));
                ord.put("FechaLiquidacion",           (String) null);  // filled in step 5
                ord.put("CumpleParaVentaDescubierta", "N");

                // Determine if sobregiro validation applies
                boolean validaSaldo = FORMA_CUSTODIA.equalsIgnoreCase(strOrEmpty(oin, "IdFormaCustodia"))
                        && ACCION_VENTA.equalsIgnoreCase((String) ord.get("AccionOrden"))
                        && "N".equalsIgnoreCase((String) ord.get("EsVentaCorta"))
                        && !CLASIFICACION_IF.equalsIgnoreCase((String) ord.get("IdClasificacionTipoInstrumento"))
                                || CLASIFICACION_IF.equalsIgnoreCase((String) ord.get("IdClasificacionTipoInstrumento"));
                ord.put("ValidaSaldoCustodia", validaSaldo ? "S" : "N");

                // Adjust cantidad for IF
                String clasTipo = (String) ord.get("IdClasificacionTipoInstrumento");
                if (CLASIFICACION_IF.equalsIgnoreCase(clasTipo) && "S".equalsIgnoreCase(strFromMap(tran, "EsFungible", "N"))) {
                    ord.put("Cantidad", BigDecimal.ONE);
                } else if (CLASIFICACION_IF.equalsIgnoreCase(clasTipo) && "N".equalsIgnoreCase(strFromMap(tran, "EsFungible", "N"))) {
                    Object corteMin = tran.get("CorteMinimo");
                    if (corteMin != null) {
                        BigDecimal corte = new BigDecimal(corteMin.toString());
                        if (corte.compareTo(BigDecimal.ZERO) != 0) {
                            BigDecimal vr = (BigDecimal) ord.get("ValorRescate");
                            ord.put("Cantidad", vr.divide(corte, 0, java.math.RoundingMode.FLOOR));
                        }
                    }
                }

                idsPortafolio.add((Long) ord.get("IdPortafolioCliente"));
                ordenes.add(ord);
            }

            // ── 4. Indicadores portafolio ──────────────────────────────────────
            Map<Long, Map<String, Object>> indicadores = new HashMap<>();
            if (!idsPortafolio.isEmpty()) {
                jdbc.queryForList(SQL_SELECT_INDICADORES_PORTAFOLIO,
                        new MapSqlParameterSource("idsPortafolio", idsPortafolio))
                    .forEach(row -> indicadores.put(toLong(row.get("IdPortafolio")), row));
            }

            // Flag ValidaSaldoCustodia using operacionesCustodia list
            for (Map<String, Object> ord : ordenes) {
                String idTipoOp = (String) ord.get("IdTipoOperacion");
                String clasTipo = (String) ord.get("IdClasificacionTipoInstrumento");
                Map<String, Object> ind = indicadores.getOrDefault(ord.get("IdPortafolioCliente"), Collections.emptyMap());
                boolean opEnLista = operacionesCustodia.stream().anyMatch(op -> op.equalsIgnoreCase(idTipoOp));
                if (!opEnLista) {
                    ord.put("ValidaSaldoCustodia", "N");
                } else if (FORMA_CUSTODIA.equalsIgnoreCase((String) ord.get("IdFormaCustodia"))
                        && ACCION_VENTA.equalsIgnoreCase((String) ord.get("AccionOrden"))
                        && "N".equalsIgnoreCase((String) ord.get("EsVentaCorta"))) {
                    ord.put("ValidaSaldoCustodia", "S");
                } else {
                    ord.put("ValidaSaldoCustodia", "N");
                }
            }

            // ── 5. Fechas liquidacion via ObtenerDiaSiguienteUtil ──────────────
            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
            if (fechaSistema.isEmpty()) fechaSistema = LocalDate.now().toString();

            List<ObtenerDiaSiguienteUtil.FechaIn> fechasIn = new ArrayList<>();
            for (Map<String, Object> ord : ordenes) {
                Long dias = (Long) ord.get("DiasLiquidacionContado");
                if (dias == null) { dias = 0L; ord.put("DiasLiquidacionContado", 0L); }
                fechasIn.add(new ObtenerDiaSiguienteUtil.FechaIn(fechaSistema, dias, idPais));
            }
            ObtenerDiaSiguienteUtil.Salida salFechas = diaSiguienteUtil.calcular(idPais, "S", "N", fechasIn);
            for (int i = 0; i < ordenes.size() && i < salFechas.p_out_fechas.size(); i++) {
                ordenes.get(i).put("FechaLiquidacion", salFechas.p_out_fechas.get(i).fechaCalculada);
            }

            // ── 6. ConsultarCustodiaIngresoOrden ──────────────────────────────
            // Accumulate custody data per (idPortafolio, idInstrumento, fecha, boveda, tipoOp)
            Map<Long, Map<String, Object>> saldosCustodia = new HashMap<>();
            boolean tieneCustodiaOrdenes = ordenes.stream().anyMatch(o -> "S".equals(o.get("ValidaSaldoCustodia")));

            if (tieneCustodiaOrdenes) {
                try {
                    JsonObject custIn = new JsonObject();
                    custIn.addProperty("P_IDEMPRESA", idEmpresa);
                    custIn.addProperty("P_IDUSUARIO", idUsuario);
                    custIn.addProperty("P_IDMODULO",  idModulo);
                    custIn.addProperty("P_VALORIZARFLUJO", "N");

                    // Compute max dias proyeccion and min fecha saldo
                    Long maxDias = 0L;
                    String fechaSaldoMin = fechaSistema;
                    for (Map<String, Object> ord : ordenes) {
                        if (!"S".equals(ord.get("ValidaSaldoCustodia"))) continue;
                        Long d = (Long) ord.get("DiasLiquidacionContado");
                        if (d != null && d > maxDias) maxDias = d;
                        String fl = (String) ord.get("FechaLiquidacion");
                        if (fl != null && fl.compareTo(fechaSaldoMin) < 0) fechaSaldoMin = fl;
                    }
                    custIn.addProperty("P_DIASPROYECCION", maxDias);
                    custIn.addProperty("P_FECHASALDO",     fechaSaldoMin);

                    JsonArray ordenesArr = new JsonArray();
                    Set<String> seen = new HashSet<>();
                    for (Map<String, Object> ord : ordenes) {
                        if (!"S".equals(ord.get("ValidaSaldoCustodia"))) continue;
                        String key = ord.get("IdBoveda") + "|" + ord.get("IdPortafolioCliente")
                                   + "|" + ord.get("IdTipoOperacion") + "|" + ord.get("IdInstrumento");
                        if (!seen.add(key)) continue;
                        JsonObject oItem = new JsonObject();
                        oItem.addProperty("IdBoveda",        (String) ord.get("IdBoveda"));
                        oItem.addProperty("IdPortafolio",    (Long)   ord.get("IdPortafolioCliente"));
                        oItem.addProperty("IdTipoOperacion", (String) ord.get("IdTipoOperacion"));
                        oItem.addProperty("IdInstrumento",   (Long)   ord.get("IdInstrumento"));
                        oItem.addProperty("IdClasificacionTipoInstrumento", (String) ord.get("IdClasificacionTipoInstrumento"));
                        oItem.addProperty("CumpleParaVentaDescubierta", (String) ord.get("CumpleParaVentaDescubierta"));
                        ordenesArr.add(oItem);
                    }
                    custIn.add("P_ORDENES", ordenesArr);

                    JsonObject custOut = custodiaService.execute(custIn);

                    // Index custody saldos by portafolio for validation
                    if (custOut.has("P_OUT_SALIDA")) {
                        for (JsonElement sldEl : custOut.getAsJsonArray("P_OUT_SALIDA")) {
                            JsonObject sld = sldEl.getAsJsonObject();
                            Long idPort = sld.has("IdPortafolio") ? sld.get("IdPortafolio").getAsLong() : 0L;
                            saldosCustodia.put(idPort, new Gson().fromJson(sld, Map.class));
                        }
                    }

                    // Forward custody errors as warnings
                    if (custOut.has("P_OUT_ERRORESDETALLE")) {
                        for (JsonElement errEl : custOut.getAsJsonArray("P_OUT_ERRORESDETALLE")) {
                            JsonObject err = errEl.getAsJsonObject();
                            JsonObject msg = new JsonObject();
                            msg.addProperty("Tipo", "A");
                            msg.addProperty("Codigo", err.has("Codigo") ? err.get("Codigo").getAsString() : "SOBREGIRO_CUSTODIA");
                            msg.addProperty("Descripcion", err.has("Descripcion") ? err.get("Descripcion").getAsString() : "Sobregiro en custodia detectado");
                            mensajes.add(msg);
                        }
                    }
                } catch (Exception e) {
                    log.warn("ConsultarCustodiaIngresoOrden degraded (stub or error): {}", e.getMessage());
                }
            }

            // ── 7. Sobregiro validation ────────────────────────────────────────
            // Compute total cantidad por grupo (portafolio+instrumento+fecha+boveda)
            Map<String, BigDecimal> totalCantidadGrupo = new HashMap<>();
            for (Map<String, Object> ord : ordenes) {
                if (!"S".equals(ord.get("ValidaSaldoCustodia"))) continue;
                String grupKey = ord.get("IdBoveda") + "|" + ord.get("IdPortafolioCliente")
                               + "|" + ord.get("IdTipoOperacion") + "|" + ord.get("IdInstrumento")
                               + "|" + ord.get("FechaLiquidacion");
                BigDecimal cant = (BigDecimal) ord.get("Cantidad");
                totalCantidadGrupo.merge(grupKey, cant != null ? cant : BigDecimal.ZERO, BigDecimal::add);
            }

            for (Map<String, Object> ord : ordenes) {
                if (!"S".equals(ord.get("ValidaSaldoCustodia"))) continue;

                Long idPort = (Long) ord.get("IdPortafolioCliente");
                Map<String, Object> ind  = indicadores.getOrDefault(idPort, Collections.emptyMap());
                Map<String, Object> sld  = saldosCustodia.getOrDefault(idPort, Collections.emptyMap());
                String clasTipo = (String) ord.get("IdClasificacionTipoInstrumento");

                String idTipoOp = (String) ord.get("IdTipoOperacion");
                boolean opEnLista = operacionesCustodia.stream().anyMatch(op -> op.equalsIgnoreCase(idTipoOp));
                if (!opEnLista) continue;

                boolean sobregiro = false;
                if (CLASIFICACION_RF.equalsIgnoreCase(clasTipo))
                    sobregiro = "S".equalsIgnoreCase(strFromMap(ind, "SobregiroRF", "N"));
                else if (CLASIFICACION_RV.equalsIgnoreCase(clasTipo))
                    sobregiro = "S".equalsIgnoreCase(strFromMap(ind, "SobregiroRV", "N"));
                else if (CLASIFICACION_IF.equalsIgnoreCase(clasTipo))
                    sobregiro = "S".equalsIgnoreCase(strFromMap(ind, "SobregiroIF", "N"));

                if (!sobregiro) continue;

                String grupKey = ord.get("IdBoveda") + "|" + idPort + "|" + idTipoOp
                               + "|" + ord.get("IdInstrumento") + "|" + ord.get("FechaLiquidacion");
                BigDecimal totalOrdenes = totalCantidadGrupo.getOrDefault(grupKey, BigDecimal.ZERO);
                BigDecimal saldoDisp    = toBigDecimal(sld.get("Cantidad"));

                boolean excede = totalOrdenes.compareTo(saldoDisp) > 0;
                if (excede) {
                    BigDecimal diferencia = totalOrdenes.subtract(saldoDisp);
                    JsonObject msg = new JsonObject();
                    msg.addProperty("Tipo", "A");
                    msg.addProperty("Codigo", "UI_ASIGNACION_SOBREGIRO");
                    msg.addProperty("Descripcion", String.format(
                        "Portafolio %s instrumento %s: sobregiro custodia %s",
                        strFromMap(ind, "CodigoPortafolio", idPort.toString()),
                        ord.get("Nemotecnico"),
                        diferencia.toPlainString()));
                    mensajes.add(msg);
                }
            }

            // Count errors in mensajes
            long errCount = 0;
            for (JsonElement m : mensajes) {
                if ("E".equals(m.getAsJsonObject().get("Tipo").getAsString())) errCount++;
            }
            out.addProperty("P_OUT_ERRORESCONTADOR", errCount);
            out.addProperty("P_OUT_RESULTADO", errCount == 0 ? "OK" : "ERROR");

        } catch (Exception e) {
            log.error("ConsultarOrdenesConSobregiroCustodiaService error: {}", e.getMessage(), e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "E");
            msg.addProperty("Codigo", "ERROR_SOBREGIRO_CUSTODIA");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
        }

        return out;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Long)    return (Long) o;
        if (o instanceof Number)  return ((Number) o).longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return 0L; }
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        try { return new BigDecimal(o.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static String strFromMap(Map<String, Object> m, String key, String def) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : def;
    }

    private static String strOrEmpty(JsonObject o, String key) {
        return o.has(key) && !o.get(key).isJsonNull() ? o.get(key).getAsString() : "";
    }

    private static String loadSql(String path) {
        try (var is = ConsultarOrdenesConSobregiroCustodiaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load SQL: " + path, e);
        }
    }
}
