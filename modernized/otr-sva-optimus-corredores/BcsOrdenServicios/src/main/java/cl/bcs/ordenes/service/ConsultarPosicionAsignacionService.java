package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.*;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarPosicionAsignacion (JavaCall).
 * Aggregates orders and transactions into buy/sell position by instrument for assignment view.
 */
@Service
public class ConsultarPosicionAsignacionService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarPosicionAsignacionService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_INSTRUMENTOS;
    private final String SQL_SELECT_TIPOS_INSTRUMENTO;

    public ConsultarPosicionAsignacionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.SQL_SELECT_ORDENES           = loadSql("cl/bcs/ordenes/sql/ConsultarPosicionAsignacion_SelectOrdenes.sql");
        this.SQL_SELECT_TRANSACCIONES     = loadSql("cl/bcs/ordenes/sql/ConsultarPosicionAsignacion_SelectTransacciones.sql");
        this.SQL_SELECT_INSTRUMENTOS      = loadSql("cl/bcs/ordenes/sql/ConsultarPosicion_SelectInstrumentos.sql");
        this.SQL_SELECT_TIPOS_INSTRUMENTO = loadSql("cl/bcs/ordenes/sql/ConsultarPosicion_SelectTipoInstrumento.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarPosicionAsignacionService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            String idClasificacion = params.has("p_IdClasificacionTipoInstrumento")
                ? params.get("p_IdClasificacionTipoInstrumento").getAsString() : null;
            String fechaConsulta = params.has("p_FechaConsulta")
                ? params.get("p_FechaConsulta").getAsString() : null;

            String idMonedaSistema = configUtil.getParametroString(idEmpresa, "PLA.MonedaSistema");

            // Load instrument and tipo-instrumento lookups
            MapSqlParameterSource instParams = new MapSqlParameterSource()
                .addValue("IdClasificacionTipoInstrumento", idClasificacion);
            List<Map<String, Object>> instrumentosList = jdbc.queryForList(SQL_SELECT_INSTRUMENTOS, instParams);
            Map<Long, Map<String, Object>> instrumentos = new HashMap<>();
            for (Map<String, Object> inst : instrumentosList) {
                Long id = toLong(inst.get("IdInstrumento"));
                if (id != null) instrumentos.put(id, inst);
            }

            List<Map<String, Object>> tiposList = jdbc.queryForList(SQL_SELECT_TIPOS_INSTRUMENTO, new MapSqlParameterSource());
            Map<String, Map<String, Object>> tipos = new HashMap<>();
            for (Map<String, Object> tipo : tiposList) {
                Object id = tipo.get("IdTipoInstrumento");
                if (id != null) tipos.put(id.toString(), tipo);
            }

            MapSqlParameterSource dateParams = new MapSqlParameterSource()
                .addValue("FechaConsulta", toDate(fechaConsulta));
            List<Map<String, Object>> ordenes = jdbc.queryForList(SQL_SELECT_ORDENES, dateParams);
            List<Map<String, Object>> transacciones = jdbc.queryForList(SQL_SELECT_TRANSACCIONES, dateParams);

            // Keyed by esVentaCorta + idInstrumento + idTipoInstrumento
            Map<String, Map<String, Object>> posicionMap = new LinkedHashMap<>();

            for (Map<String, Object> ord : ordenes) {
                Long idInstrumento = resolveInstrumentoOrden(ord);
                if (idInstrumento == null || !instrumentos.containsKey(idInstrumento)) continue;

                String esVentaCorta = getStr(ord, "EsVentaCorta");
                String idTipoInstrumento = getStr(ord, "IdTipoInstrumento");
                String key = esVentaCorta + "_" + idInstrumento + "_" + idTipoInstrumento;

                Map<String, Object> pos = posicionMap.computeIfAbsent(key, k -> {
                    Map<String, Object> p = new HashMap<>();
                    Map<String, Object> inst = instrumentos.get(idInstrumento);
                    Map<String, Object> tipo = tipos.get(idTipoInstrumento);
                    p.put("IdInstrumento", idInstrumento);
                    p.put("IdTipoInstrumento", idTipoInstrumento);
                    p.put("IdClasificacionTipoInstrumento", inst != null ? inst.get("IdClasificacionTipoInstrumento") : null);
                    p.put("Nemotecnico", inst != null ? inst.get("Nemotecnico") : null);
                    p.put("NombreTipoInstrumento", tipo != null ? tipo.get("NombreTipoInstrumento") : null);
                    p.put("EsVentaCorta", esVentaCorta);
                    p.put("EsVentaCortaUI", "S".equalsIgnoreCase(esVentaCorta) ? "SI" : "NO");
                    p.put("IdMoneda", idMonedaSistema);
                    p.put("OrdenadoCompra", BigDecimal.ZERO);
                    p.put("OrdenadoVenta", BigDecimal.ZERO);
                    p.put("TransadoCompra", BigDecimal.ZERO);
                    p.put("TransadoVenta", BigDecimal.ZERO);
                    return p;
                });

                BigDecimal ordenadoCompra = toBD(ord.get("OrdenadoCompra"));
                BigDecimal ordenadoVenta  = toBD(ord.get("OrdenadoVenta"));
                pos.put("OrdenadoCompra", ((BigDecimal) pos.get("OrdenadoCompra")).add(ordenadoCompra));
                pos.put("OrdenadoVenta",  ((BigDecimal) pos.get("OrdenadoVenta")).add(ordenadoVenta));
            }

            for (Map<String, Object> trx : transacciones) {
                Long idInstrumento = resolveInstrumentoTransaccion(trx);
                if (idInstrumento == null || !instrumentos.containsKey(idInstrumento)) continue;

                String esVentaCorta = getStr(trx, "EsVentaCorta");
                String idTipoInstrumento = getStr(trx, "IdTipoInstrumento");
                String key = esVentaCorta + "_" + idInstrumento + "_" + idTipoInstrumento;

                Map<String, Object> pos = posicionMap.get(key);
                if (pos == null) continue;

                BigDecimal transadoCompra = toBD(trx.get("TransadoCompra"));
                BigDecimal transadoVenta  = toBD(trx.get("TransadoVenta"));
                pos.put("TransadoCompra", ((BigDecimal) pos.get("TransadoCompra")).add(transadoCompra));
                pos.put("TransadoVenta",  ((BigDecimal) pos.get("TransadoVenta")).add(transadoVenta));
            }

            // Compute derived PorComprar / PorVender
            for (Map<String, Object> pos : posicionMap.values()) {
                BigDecimal ordC = (BigDecimal) pos.get("OrdenadoCompra");
                BigDecimal ordV = (BigDecimal) pos.get("OrdenadoVenta");
                BigDecimal trxC = (BigDecimal) pos.get("TransadoCompra");
                BigDecimal trxV = (BigDecimal) pos.get("TransadoVenta");
                pos.put("PorComprar", ordC.subtract(trxC).abs());
                pos.put("PorVender",  ordV.subtract(trxV).abs());
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_POSICION", JsonParser.parseString(GSON.toJson(new ArrayList<>(posicionMap.values()))));
            return out;
        } catch (Exception e) {
            log.error("ConsultarPosicionAsignacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private Long resolveInstrumentoOrden(Map<String, Object> ord) {
        String tipoOp = getStr(ord, "IdTipoOperacion");
        String canal  = getStr(ord, "IdTipoCanalIngreso");
        Long idInst    = toLong(ord.get("IdInstrumento"));
        Long idInstSub = toLong(ord.get("IdInstrumentoSubyacente"));
        if ("COMSIM".equalsIgnoreCase(tipoOp) && "AGE".equalsIgnoreCase(canal)) return idInst;
        if ("COMSIM".equalsIgnoreCase(tipoOp)) return idInstSub;
        if ("VENSIM".equalsIgnoreCase(tipoOp)) return idInstSub;
        return idInst;
    }

    private Long resolveInstrumentoTransaccion(Map<String, Object> trx) {
        String tipoOp = getStr(trx, "IdTipoOperacion");
        Long idInst    = toLong(trx.get("IdInstrumento"));
        Long idInstSub = toLong(trx.get("IdInstrumentoSubyacente"));
        if ("COMSIM".equalsIgnoreCase(tipoOp)) return idInst;
        if ("VENSIM".equalsIgnoreCase(tipoOp)) return idInstSub;
        return idInst;
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v != null ? v.toString() : "";
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return BigDecimal.valueOf(((Number) v).doubleValue());
        try { return new BigDecimal(v.toString()); } catch (Exception e) { return BigDecimal.ZERO; }
    }

    private static Date toDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Date.valueOf(s.substring(0, 10)); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarPosicionAsignacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
