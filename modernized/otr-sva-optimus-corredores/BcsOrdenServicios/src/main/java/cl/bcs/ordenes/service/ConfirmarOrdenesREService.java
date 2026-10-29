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
 * Migrated from legacy cl.bcs.ordenes.ConfirmarOrdenesRE (JavaCall).
 * RE = Renta Fija / confirmación de operaciones RF.
 */
@Service
public class ConfirmarOrdenesREService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarOrdenesREService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final RequisitosBasicosUtil requisitosUtil;
    private final String SQL_SELECT_MODULO;
    private final String SQL_UPDATE_ORDEN;

    public ConfirmarOrdenesREService(NamedParameterJdbcTemplate jdbc, RequisitosBasicosUtil requisitosUtil) {
        this.jdbc = jdbc;
        this.requisitosUtil = requisitosUtil;
        this.SQL_SELECT_MODULO = loadSql("cl/bcs/ordenes/sql/Globales_SelectEstadoModulo.sql");
        this.SQL_UPDATE_ORDEN  = loadSql("cl/bcs/ordenes/sql/ConfirmarOrden_UpdateOrden.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ConfirmarOrdenesREService.execute()");
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

            // Validate no duplicate correlativos
            List<Long> correlativos = new ArrayList<>();
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                Long idInterno = ord.has("IdInterno") ? ord.get("IdInterno").getAsLong() : null;
                if (idInterno != null && correlativos.contains(idInterno)) {
                    mensajes.add(Map.of("tipo", "ERROR", "codigo", "CORRELATIVO_INCORRECTO", "IdInterno", 0L));
                    return buildOut(mensajes);
                }
                if (idInterno != null) correlativos.add(idInterno);
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

            // Confirm each order
            for (var el : ordenesIn) {
                JsonObject ordIn = el.getAsJsonObject();
                Long idOrden = ordIn.has("IdOrden") ? ordIn.get("IdOrden").getAsLong() : null;
                if (idOrden == null) continue;

                jdbc.update(SQL_UPDATE_ORDEN,
                    new MapSqlParameterSource("IdOrden", idOrden)
                        .addValue("IdEmpresa", idEmpresa)
                        .addValue("IdUsuario", idUsuario)
                        .addValue("FechaConfirmacion", ""));

                mensajes.add(Map.of("tipo", "INFO", "codigo", "ORDEN_CONFIRMADA", "IdInterno", idOrden));
            }

            return buildOut(mensajes);
        } catch (Exception e) {
            log.error("ConfirmarOrdenesREService error: {}", e.getMessage(), e);
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

    private static String loadSql(String path) {
        try (var is = ConfirmarOrdenesREService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
