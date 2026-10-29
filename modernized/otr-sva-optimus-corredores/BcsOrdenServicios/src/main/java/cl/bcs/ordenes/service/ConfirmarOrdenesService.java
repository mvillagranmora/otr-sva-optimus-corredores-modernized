package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
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
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConfirmarOrdenes (JavaCall).
 */
@Service
public class ConfirmarOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarOrdenesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final ConfiguracionSistemaUtil configUtil;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_USUARIO;
    private final String SQL_UPDATE_ORDEN;
    private final String SQL_SELECT_MODULO;

    public ConfirmarOrdenesService(
            NamedParameterJdbcTemplate jdbc,
            RequisitosBasicosUtil requisitosUtil,
            ConfiguracionSistemaUtil configUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.configUtil = configUtil;
        this.SQL_SELECT_ORDENES  = loadSql("cl/bcs/ordenes/sql/ConfirmarOrdenes_ConsultarOrdenes.sql");
        this.SQL_SELECT_USUARIO  = loadSql("cl/bcs/ordenes/sql/ConfirmarOrdenes_ConsultarUsuarioCliente.sql");
        this.SQL_UPDATE_ORDEN    = loadSql("cl/bcs/ordenes/sql/ConfirmarOrdenes_UpdateOrden.sql");
        this.SQL_SELECT_MODULO   = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ConfirmarOrdenesService.execute()");
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

            JsonArray ordenesIn = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();
            if (ordenesIn.size() == 0) {
                mensajes.add(Map.of("tipo", "ERROR", "codigo", "NO_SE_INGRESARON_ORDENES", "IdInterno", 0L));
                return buildOut(mensajes);
            }

            // Check module state
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

            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");

            // Collect order IDs
            List<Long> idOrden = new ArrayList<>();
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                if (ord.has("IdOrden")) idOrden.add(ord.get("IdOrden").getAsLong());
            }

            List<Map<String, Object>> ordenes = jdbc.queryForList(SQL_SELECT_ORDENES,
                new MapSqlParameterSource("IdEmpresa", idEmpresa).addValue("IdOrden", idOrden));
            Map<Long, Map<String, Object>> ordenesMap = new HashMap<>();
            ordenes.forEach(o -> ordenesMap.put(toLong(o.get("IdOrden")), o));

            List<Map<String, Object>> usuarios = jdbc.queryForList(SQL_SELECT_USUARIO,
                new MapSqlParameterSource("IdEmpresa", idEmpresa).addValue("IdUsuario", List.of(idUsuario)));

            // Confirm each order
            String fechaConfirmacion = fechaSistema != null && !fechaSistema.isBlank() ? fechaSistema : LocalDateTime.now().toString().substring(0, 10);
            for (var el : ordenesIn) {
                JsonObject ordIn = el.getAsJsonObject();
                Long id = ordIn.has("IdOrden") ? ordIn.get("IdOrden").getAsLong() : null;
                Map<String, Object> ord = id != null ? ordenesMap.get(id) : null;

                if (ord == null) {
                    mensajes.add(Map.of("tipo", "ERROR", "codigo", "ORDEN_NO_EXISTE", "IdInterno", id != null ? id : 0L));
                    continue;
                }

                String estadoOrden = ord.get("IdEstadoOrden") != null ? ord.get("IdEstadoOrden").toString() : "";
                if ("ANU".equals(estadoOrden) || "REC".equals(estadoOrden) || "VEN".equals(estadoOrden)) {
                    mensajes.add(Map.of("tipo", "ERROR", "codigo", "ORDEN_ESTADO_ANULADA", "IdInterno", id));
                    continue;
                }

                String estadoConf = ord.get("IdEstadoConfirmacion") != null ? ord.get("IdEstadoConfirmacion").toString() : "";
                if ("ENPLAZO".equals(estadoConf) || "FUERA".equals(estadoConf)) {
                    mensajes.add(Map.of("tipo", "ERROR", "codigo", "ORDEN_ESTADO_CONFIRMADA", "IdInterno", id));
                    continue;
                }

                jdbc.update(SQL_UPDATE_ORDEN,
                    new MapSqlParameterSource("IdOrden", id)
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdUsuario", idUsuario)
                        .addValue("FechaConfirmacion", fechaConfirmacion));

                mensajes.add(Map.of("tipo", "INFO", "codigo", "ORDEN_CONFIRMADA", "IdInterno", id));
            }

            return buildOut(mensajes);
        } catch (Exception e) {
            log.error("ConfirmarOrdenesService error: {}", e.getMessage(), e);
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
        try (var is = ConfirmarOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
