package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.AnularAsociaciones (JavaCall).
 */
@Service
public class AnularAsociacionesService {

    private static final Logger log = LoggerFactory.getLogger(AnularAsociacionesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_SELECT_MODULO;
    private final String SQL_SELECT_ORDEN_ASOCIADA;
    private final String SQL_ANULA_ORDEN_ASOCIADA;
    private final String SQL_SELECT_ORDENES;

    public AnularAsociacionesService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_SELECT_MODULO       = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_SELECT_ORDEN_ASOCIADA = loadSql("cl/bcs/ordenes/sql/AnularAsociaciones_SelectOrdenAsociada.sql");
        this.SQL_ANULA_ORDEN_ASOCIADA  = loadSql("cl/bcs/ordenes/sql/AnularAsociaciones_DeleteOrdenAsociada.sql");
        this.SQL_SELECT_ORDENES        = loadSql("cl/bcs/ordenes/sql/AnularAsociaciones_SelectOrdenes.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("AnularAsociacionesService.execute()");
        List<Map<String, Object>> mensajes = new ArrayList<>();
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

            JsonArray asociacionesIn = params.has("p_Asociaciones") ? params.get("p_Asociaciones").getAsJsonArray() : new JsonArray();
            if (asociacionesIn.size() == 0) {
                mensajes.add(Map.of("tipo", "ERROR", "codigo", "NO_SE_INGRESARON_ASOCIACIONES", "IdInterno", 0L));
                return buildOut(mensajes);
            }

            // Validate module state
            List<Map<String, Object>> modulo = jdbc.queryForList(SQL_SELECT_MODULO,
                new MapSqlParameterSource("IdModulo", "ORD").addValue("IdEmpresa", idEmpresa));
            if (modulo.isEmpty()) {
                mensajes.add(Map.of("tipo", "ERROR", "codigo", "MODULO_NO_EXISTE", "IdInterno", 0L));
                return buildOut(mensajes);
            }
            Object estado = modulo.get(0).get("IdEstadoModulo");
            if ("CERRADO".equalsIgnoreCase(estado != null ? estado.toString() : "")) {
                mensajes.add(Map.of("tipo", "ERROR", "codigo", "MODULO_CERRADO", "IdInterno", 0L));
                return buildOut(mensajes);
            }

            // Collect association IDs
            List<Long> idsAsociacion = new ArrayList<>();
            for (var el : asociacionesIn) {
                JsonObject asoc = el.getAsJsonObject();
                if (asoc.has("IdAsociacion")) idsAsociacion.add(asoc.get("IdAsociacion").getAsLong());
            }

            if (idsAsociacion.isEmpty()) {
                mensajes.add(Map.of("tipo", "ERROR", "codigo", "NO_SE_INGRESARON_ASOCIACIONES", "IdInterno", 0L));
                return buildOut(mensajes);
            }

            // Fetch current associations
            MapSqlParameterSource queryParams = new MapSqlParameterSource("idsAsociacion", idsAsociacion);
            List<Map<String, Object>> ordenAsoc = jdbc.queryForList(SQL_SELECT_ORDEN_ASOCIADA, queryParams);

            if (!ordenAsoc.isEmpty()) {
                // Delete associations in batch
                for (Map<String, Object> asoc : ordenAsoc) {
                    jdbc.update(SQL_ANULA_ORDEN_ASOCIADA,
                        new MapSqlParameterSource("IdAsociacion", asoc.get("IdAsociacion")));
                }

                // Return updated orders
                List<Long> idsOrdenes = new ArrayList<>();
                for (Map<String, Object> asoc : ordenAsoc) {
                    Object idOrd = asoc.get("IdOrden");
                    if (idOrd != null) idsOrdenes.add(toLong(idOrd));
                    Object idOrdAsoc = asoc.get("IdOrdenAsociada");
                    if (idOrdAsoc != null) idsOrdenes.add(toLong(idOrdAsoc));
                }

                List<Map<String, Object>> ordenes = jdbc.queryForList(SQL_SELECT_ORDENES,
                    new MapSqlParameterSource("idsOrdenes", idsOrdenes));

                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
                out.add("P_OUT_ORDENES", JsonParser.parseString(GSON.toJson(ordenes)));
                return out;
            }

            mensajes.add(Map.of("tipo", "ERROR", "codigo", "ASOCIACION_NO_EXISTE", "IdInterno", 0L));
            return buildOut(mensajes);
        } catch (Exception e) {
            log.error("AnularAsociacionesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private JsonObject buildOut(List<Map<String, Object>> mensajes) {
        long errores = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo"))).count();
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);
        out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(mensajes)));
        return out;
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = AnularAsociacionesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
