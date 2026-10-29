package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ObtenerDetalleRefacturacion (JavaCall).
 */
@Service
public class ObtenerDetalleRefacturacionService {

    private static final Logger log = LoggerFactory.getLogger(ObtenerDetalleRefacturacionService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_SELECT_COMPROBANTES_BASE;
    private final String SQL_SELECT_TRANSACCIONES;
    private final String SQL_SELECT_MOVIMIENTOS;
    private final String SQL_SELECT_COMPROBANTES_NOTA;
    private final String SQL_SELECT_ASIGNACIONES;

    public ObtenerDetalleRefacturacionService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_SELECT_COMPROBANTES_BASE = loadSql("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectComprobantesBase.sql");
        this.SQL_SELECT_TRANSACCIONES     = loadSql("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectTransacciones.sql");
        this.SQL_SELECT_MOVIMIENTOS       = loadSql("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectMovimientosXComprobante.sql");
        this.SQL_SELECT_COMPROBANTES_NOTA = loadSql("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectComprobantesNotaDeCredito.sql");
        this.SQL_SELECT_ASIGNACIONES      = loadSql("cl/bcs/ordenes/sql/ObtenerDetalleRefacturacion_SelectAsignaciones.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ObtenerDetalleRefacturacionService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : null;

            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            MapSqlParameterSource sqlParams = new MapSqlParameterSource()
                .addValue("IdEmpresa", idEmpresa)
                .addValue("IdAsignacion", params.has("p_IdAsignacion") ? params.get("p_IdAsignacion").getAsLong() : null)
                .addValue("FechaDesde", toDate(params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : null))
                .addValue("FechaHasta", toDate(params.has("p_FechaHasta") ? params.get("p_FechaHasta").getAsString() : null));

            List<Map<String, Object>> comprobantesBase = jdbc.queryForList(SQL_SELECT_COMPROBANTES_BASE, sqlParams);
            List<Map<String, Object>> transacciones    = jdbc.queryForList(SQL_SELECT_TRANSACCIONES, sqlParams);
            List<Map<String, Object>> movimientos      = jdbc.queryForList(SQL_SELECT_MOVIMIENTOS, sqlParams);
            List<Map<String, Object>> comprobantesNota = jdbc.queryForList(SQL_SELECT_COMPROBANTES_NOTA, sqlParams);
            List<Map<String, Object>> asignaciones     = jdbc.queryForList(SQL_SELECT_ASIGNACIONES, sqlParams);

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_COMPROBANTES_BASE", JsonParser.parseString(GSON.toJson(comprobantesBase)));
            out.add("P_OUT_TRANSACCIONES", JsonParser.parseString(GSON.toJson(transacciones)));
            out.add("P_OUT_MOVIMIENTOS", JsonParser.parseString(GSON.toJson(movimientos)));
            out.add("P_OUT_COMPROBANTES_NOTA_CREDITO", JsonParser.parseString(GSON.toJson(comprobantesNota)));
            out.add("P_OUT_ASIGNACIONES", JsonParser.parseString(GSON.toJson(asignaciones)));
            return out;
        } catch (Exception e) {
            log.error("ObtenerDetalleRefacturacionService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static Date toDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return Date.valueOf(s.substring(0, 10)); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = ObtenerDetalleRefacturacionService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
