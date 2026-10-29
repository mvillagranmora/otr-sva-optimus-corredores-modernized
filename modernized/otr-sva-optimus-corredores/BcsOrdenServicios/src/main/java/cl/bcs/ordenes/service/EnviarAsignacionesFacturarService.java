package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.FacturacionCallClient;
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
 * Migrated from legacy cl.bcs.ordenes.EnviarAsignacionesFacturar (JavaCall).
 * Marks assignments ready to invoice, computes settlement days via ObtenerPlazoUtil,
 * then calls BcsFacturacionServicios to create comprobantes.
 */
@Service
public class EnviarAsignacionesFacturarService {

    private static final Logger log = LoggerFactory.getLogger(EnviarAsignacionesFacturarService.class);
    private static final Gson GSON = new GsonBuilder()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ObtenerPlazoUtil plazoUtil;
    private final FacturacionCallClient facturacionClient;

    private final String SQL_SELECT_ASIGNACIONES;
    private final String SQL_SELECT_ASIGNACIONES_GRUPO;
    private final String SQL_SELECT_COBROS;
    private final String SQL_SELECT_FORMAS_DE_PAGO;
    private final String SQL_SELECT_COND_LIQUIDACION;
    private final String SQL_UPDATE_ASIGNACIONES;
    private final String SQL_SELECT_PORCENTAJE_IMPUESTO;

    private static final String SQL_SELECT_ID_PAIS =
        "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" WHERE \"IdEmpresa\" = :IdEmpresa";

    public EnviarAsignacionesFacturarService(NamedParameterJdbcTemplate jdbc,
                                              ObtenerPlazoUtil plazoUtil,
                                              FacturacionCallClient facturacionClient) {
        this.jdbc = jdbc;
        this.plazoUtil = plazoUtil;
        this.facturacionClient = facturacionClient;
        this.SQL_SELECT_ASIGNACIONES         = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectAsignaciones.sql");
        this.SQL_SELECT_ASIGNACIONES_GRUPO   = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectAsignacionesGrupo.sql");
        this.SQL_SELECT_COBROS               = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectCobros.sql");
        this.SQL_SELECT_FORMAS_DE_PAGO       = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectFormasDePago.sql");
        this.SQL_SELECT_COND_LIQUIDACION     = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectCondLiquidacion.sql");
        this.SQL_UPDATE_ASIGNACIONES         = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_UpdateAsignaciones.sql");
        this.SQL_SELECT_PORCENTAJE_IMPUESTO  = loadSql("cl/bcs/ordenes/sql/EnviarAsignacionesFacturar_SelectPorcentajeImpuesto.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("EnviarAsignacionesFacturarService.execute()");
        try {
            Long idEmpresa  = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario  = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String simular  = params.has("p_simularOperacion") ? params.get("p_simularOperacion").getAsString() : "N";
            JsonArray asigIn = params.has("p_Asignaciones") ? params.get("p_Asignaciones").getAsJsonArray() : new JsonArray();

            if (asigIn.size() == 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
                out.add("P_OUT_RESULTADO", JsonParser.parseString("[]"));
                return out;
            }

            // Collect IDs
            List<Long> idsAsignacion = new ArrayList<>();
            Map<Long, JsonObject> hashAsigMeta = new HashMap<>();
            for (var el : asigIn) {
                JsonObject a = el.getAsJsonObject();
                Long id = a.has("IdAsignacion") ? a.get("IdAsignacion").getAsLong() : null;
                if (id != null) {
                    idsAsignacion.add(id);
                    hashAsigMeta.put(id, a);
                }
            }

            // Load assignment details
            List<Map<String, Object>> asignaciones = jdbc.queryForList(SQL_SELECT_ASIGNACIONES,
                new MapSqlParameterSource("IdAsignacion", idsAsignacion));

            // Load cobros
            List<Map<String, Object>> cobros = jdbc.queryForList(SQL_SELECT_COBROS,
                new MapSqlParameterSource("IdAsignacion", idsAsignacion));
            Map<Long, List<Map<String, Object>>> hashCobros = new HashMap<>();
            for (Map<String, Object> c : cobros) {
                Long idA = toLong(c.get("IdAsignacion"));
                hashCobros.computeIfAbsent(idA, k -> new ArrayList<>()).add(c);
            }

            // Load condiciones de liquidación
            List<Map<String, Object>> condLiquidacion = jdbc.queryForList(SQL_SELECT_COND_LIQUIDACION,
                new MapSqlParameterSource());
            Map<String, Integer> hashCondLiq = new HashMap<>();
            for (Map<String, Object> c : condLiquidacion) {
                String cond = getStr(c, "IdCondicionDeLiquidacion");
                Object diasObj = c.get("PlazoEnDias");
                if (!cond.isBlank() && diasObj instanceof Number)
                    hashCondLiq.put(cond, ((Number) diasObj).intValue());
            }

            // Get IdPais for empresa
            Long idPais = 1L;
            try {
                List<Map<String, Object>> empresaRows = jdbc.queryForList(SQL_SELECT_ID_PAIS,
                    new MapSqlParameterSource("IdEmpresa", idEmpresa));
                if (!empresaRows.isEmpty()) idPais = toLong(empresaRows.get(0).get("IdPais"));
            } catch (Exception e) {
                log.warn("Could not get IdPais: {}", e.getMessage());
            }

            // Compute plazos via ObtenerPlazoUtil
            List<ObtenerPlazoUtil.FechasIn> fechasIn = new ArrayList<>();
            for (Map<String, Object> asig : asignaciones) {
                String cond = getStr(asig, "IdCondicionDeLiquidacion");
                String fechaDesde = getStr(asig, "FechaEmision");
                String fechaHasta = getStr(asig, "FechaLiquidacion");
                Long idAsig = toLong(asig.get("IdAsignacion"));
                if (!cond.isBlank() && !fechaDesde.isBlank() && !fechaHasta.isBlank()) {
                    ObtenerPlazoUtil.FechasIn fi = new ObtenerPlazoUtil.FechasIn();
                    fi.IdInterno = idAsig;
                    fi.FechaDesde = fechaDesde;
                    fi.FechaHasta = fechaHasta;
                    fi.SoloDiasHabiles = "S";
                    fechasIn.add(fi);
                }
            }
            Map<Long, Long> hashPlazo = new HashMap<>();
            if (!fechasIn.isEmpty()) {
                ObtenerPlazoUtil.Salida plazoSalida = plazoUtil.calcular(idPais, fechasIn);
                for (ObtenerPlazoUtil.FechasOut fo : plazoSalida.p_out_Fechas) {
                    if (fo.IdInterno != null) hashPlazo.put(fo.IdInterno, fo.Plazo);
                }
            }

            // Update assignments to ENV_FAC state
            if (!"S".equalsIgnoreCase(simular) && !asignaciones.isEmpty()) {
                for (Map<String, Object> asig : asignaciones) {
                    Long idA = toLong(asig.get("IdAsignacion"));
                    Long plazo = hashPlazo.getOrDefault(idA, 0L);
                    try {
                        jdbc.update(SQL_UPDATE_ASIGNACIONES,
                            new MapSqlParameterSource()
                                .addValue("IdAsignacion", idA)
                                .addValue("DiasLiquidacion", plazo));
                    } catch (Exception e) {
                        log.warn("Error updating asignacion {}: {}", idA, e.getMessage());
                    }
                }
            }

            // Build result and call FacturacionServicios for each assignment
            JsonArray resultado = new JsonArray();
            long errores = 0;
            JsonArray mensajes = new JsonArray();

            for (Map<String, Object> asig : asignaciones) {
                Long idA = toLong(asig.get("IdAsignacion"));
                JsonObject meta = hashAsigMeta.get(idA);
                String generaComprobante = meta != null && meta.has("GeneraComprobante")
                    ? meta.get("GeneraComprobante").getAsString() : "S";
                String idTipoComprobante = meta != null && meta.has("IdTipoComprobante")
                    ? meta.get("IdTipoComprobante").getAsString() : "FAE";

                if ("S".equalsIgnoreCase(generaComprobante) && !"S".equalsIgnoreCase(simular)) {
                    try {
                        JsonObject crearParams = buildCrearComprobanteParams(idEmpresa, idUsuario, idModulo, asig,
                            hashCobros.getOrDefault(idA, Collections.emptyList()),
                            hashPlazo.getOrDefault(idA, 0L), idTipoComprobante);
                        JsonObject crearResult = facturacionClient.call("cl.bcs.facturacion.CrearComprobante", crearParams);
                        long errCrear = crearResult.has("P_OUT_ERRORESCONTADOR") ? crearResult.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                        if (errCrear > 0) {
                            errores++;
                            if (crearResult.has("P_OUT_MENSAJES")) {
                                for (var m : crearResult.get("P_OUT_MENSAJES").getAsJsonArray()) mensajes.add(m);
                            }
                        } else {
                            JsonObject r = new JsonObject();
                            r.addProperty("IdAsignacion", idA);
                            r.addProperty("IdTransaccion", toLong(asig.get("IdTransaccion")));
                            r.addProperty("EsAPlazo", getStr(asig, "EsAPlazo"));
                            r.addProperty("IdCliente", toLong(asig.get("IdCliente")));
                            r.addProperty("IdPortafolio", toLong(asig.get("IdPortafolio")));
                            if (crearResult.has("IdComprobante")) r.add("IdComprobante", crearResult.get("IdComprobante"));
                            if (crearResult.has("IdMovimientoComprobante")) r.add("IdMovimientoComprobante", crearResult.get("IdMovimientoComprobante"));
                            resultado.add(r);
                        }
                    } catch (Exception e) {
                        log.warn("Error calling CrearComprobante for asignacion {}: {}", idA, e.getMessage());
                    }
                } else {
                    JsonObject r = new JsonObject();
                    r.addProperty("IdAsignacion", idA);
                    r.addProperty("IdTransaccion", toLong(asig.get("IdTransaccion")));
                    r.addProperty("EsAPlazo", getStr(asig, "EsAPlazo"));
                    r.addProperty("IdCliente", toLong(asig.get("IdCliente")));
                    r.addProperty("IdPortafolio", toLong(asig.get("IdPortafolio")));
                    resultado.add(r);
                }
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", mensajes);
            out.add("P_OUT_RESULTADO", resultado);
            return out;
        } catch (Exception e) {
            log.error("EnviarAsignacionesFacturarService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private JsonObject buildCrearComprobanteParams(Long idEmpresa, Long idUsuario, String idModulo,
                                                    Map<String, Object> asig,
                                                    List<Map<String, Object>> cobros,
                                                    Long plazo, String idTipoComprobante) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", idEmpresa);
        p.addProperty("p_IdUsuario", idUsuario);
        p.addProperty("p_IdModulo", idModulo);
        p.addProperty("p_simularOperacion", "N");
        p.addProperty("p_CrearComprobanteAutomatico", "S");
        p.addProperty("p_EsIngresoAutomatico", "N");
        p.addProperty("p_UsaReglasNegocio", "S");

        JsonArray movimientos = new JsonArray();
        JsonObject mov = new JsonObject();
        mov.addProperty("IdAsignacion", toLong(asig.get("IdAsignacion")));
        mov.addProperty("IdTransaccion", toLong(asig.get("IdTransaccion")));
        mov.addProperty("IdCliente", toLong(asig.get("IdCliente")));
        mov.addProperty("IdPortafolio", toLong(asig.get("IdPortafolio")));
        mov.addProperty("IdInstrumento", toLong(asig.get("IdInstrumento")));
        mov.addProperty("IdTipoOperacion", getStr(asig, "IdTipoOperacion"));
        mov.addProperty("IdBolsa", getStr(asig, "IdBolsa"));
        mov.addProperty("IdMonedaTransaccion", getStr(asig, "IdMonedaTransaccion"));
        mov.addProperty("MontoAsignado", getBD(asig, "MontoAsignado"));
        mov.addProperty("CantidadAsignada", getBD(asig, "CantidadAsignada"));
        mov.addProperty("PrecioAsignacion", getBD(asig, "PrecioAsignacion"));
        mov.addProperty("FechaEmision", getStr(asig, "FechaEmision"));
        mov.addProperty("FechaLiquidacion", getStr(asig, "FechaLiquidacion"));
        mov.addProperty("DiasLiquidacion", plazo);
        mov.addProperty("EsAPlazo", getStr(asig, "EsAPlazo"));
        mov.addProperty("IdTipoComprobante", idTipoComprobante);
        mov.addProperty("CrearComprobanteAutomatico", "S");

        JsonArray cobrosArr = new JsonArray();
        for (Map<String, Object> c : cobros) {
            JsonObject cobro = new JsonObject();
            cobro.addProperty("IdCobro", getStr(c, "IdCobro"));
            cobro.addProperty("IdMonedaCobro", getStr(c, "IdMonedaCobro"));
            cobro.addProperty("MontoCobro", getBD(c, "MontoCobro"));
            cobrosArr.add(cobro);
        }
        mov.add("Cobros", cobrosArr);
        movimientos.add(mov);

        p.add("p_Movimientos", movimientos);
        return p;
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
        try (var is = EnviarAsignacionesFacturarService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
