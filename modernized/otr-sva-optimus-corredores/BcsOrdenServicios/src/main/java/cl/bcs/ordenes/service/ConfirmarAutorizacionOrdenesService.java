package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.PlataformaCallClient;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConfirmarAutorizacionOrdenes (JavaCall).
 */
@Service
public class ConfirmarAutorizacionOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmarAutorizacionOrdenesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private static final String ESTADO_PDA = "PDA";
    private static final String ESTADO_ING = "ING";
    private static final String TIPO_CANAL_MANUAL = "MAN";

    private final NamedParameterJdbcTemplate jdbc;
    private final PlataformaCallClient plataformaClient;
    private final CambiarEstadoOrdenesService cambiarEstadoService;
    private final RutearOrdenesService rutearOrdenesService;
    private final String SQL_INSERT_TABLA_PASO;
    private final String SQL_UPDATE_ESTADO_ORDEN;
    private final String SQL_SELECT_ORDEN_AUTORIZADAS;
    private final String SQL_UPDATE_FECHA_AUTORIZACION;

    public ConfirmarAutorizacionOrdenesService(NamedParameterJdbcTemplate jdbc,
                                               PlataformaCallClient plataformaClient,
                                               CambiarEstadoOrdenesService cambiarEstadoService,
                                               RutearOrdenesService rutearOrdenesService) {
        this.jdbc = jdbc;
        this.plataformaClient = plataformaClient;
        this.cambiarEstadoService = cambiarEstadoService;
        this.rutearOrdenesService = rutearOrdenesService;
        this.SQL_INSERT_TABLA_PASO         = loadSql("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_InsertTablaPaso.sql");
        this.SQL_UPDATE_ESTADO_ORDEN       = loadSql("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_UpdateEstadoOrden.sql");
        this.SQL_SELECT_ORDEN_AUTORIZADAS  = loadSql("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_SelectOrdenAutorizadas.sql");
        this.SQL_UPDATE_FECHA_AUTORIZACION = loadSql("cl/bcs/ordenes/sql/ConfirmarAutorizacionOrdenes_UpdateFechaAutorizacion.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ConfirmarAutorizacionOrdenesService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            JsonArray ordenesIn = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();

            List<Map<String, Object>> mensajes = new ArrayList<>();

            // Insert orders into staging table
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                Long idOrden = ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : null;
                if (idOrden == null) continue;
                jdbc.update(SQL_INSERT_TABLA_PASO, new MapSqlParameterSource("IdOrden", idOrden));
            }

            // Update internal state to AUT for PDA orders
            jdbc.update(SQL_UPDATE_ESTADO_ORDEN, new MapSqlParameterSource());

            // Fetch orders that were actually updated to AUT (have IdEstadoLimite = AUT)
            List<Map<String, Object>> ordenesAutorizadas = jdbc.queryForList(SQL_SELECT_ORDEN_AUTORIZADAS, new MapSqlParameterSource());

            // Validate: all input orders must have passed authorization
            java.util.Set<Long> idsAutorizados = new java.util.HashSet<>();
            for (Map<String, Object> ord : ordenesAutorizadas) {
                Long id = toLong(ord.get("IdOrden"));
                if (id != null) idsAutorizados.add(id);
            }

            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                Long idOrden = ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : null;
                if (idOrden != null && !idsAutorizados.contains(idOrden)) {
                    mensajes.add(Map.of("IdInterno", (long) mensajes.size() + 1L,
                        "tipo", "ERROR", "codigo", "UI_ESTADO_LIMITE_INCORRECTO"));
                }
            }

            if (!mensajes.isEmpty()) {
                long errores = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo"))).count();
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", errores);
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(mensajes)));
                return out;
            }

            // Change state: PDA → ING via CambiarEstadoOrdenesService
            JsonObject cambiarParams = new JsonObject();
            cambiarParams.addProperty("p_IdEmpresa", idEmpresa);
            cambiarParams.addProperty("p_IdUsuario", idUsuario);
            JsonArray cambiarOrdenes = new JsonArray();
            for (Map<String, Object> ord : ordenesAutorizadas) {
                JsonObject o = new JsonObject();
                o.addProperty("IdOrden", toLong(ord.get("IdOrden")));
                o.addProperty("IdNuevoEstado", ESTADO_ING);
                cambiarOrdenes.add(o);
            }
            cambiarParams.add("p_Ordenes", cambiarOrdenes);
            JsonObject cambiarResult = cambiarEstadoService.execute(cambiarParams);

            // Check for errors from state change
            if (cambiarResult.has("P_OUT_ERRORESCONTADOR") && cambiarResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return cambiarResult;
            }

            // Send WebSocket notification for authorized orders
            try {
                JsonArray wsMessages = new JsonArray();
                for (Map<String, Object> ord : ordenesAutorizadas) {
                    JsonObject msgAviso = new JsonObject();
                    msgAviso.addProperty("IdEmpresa", idEmpresa);
                    msgAviso.addProperty("Evento", "AVISO");
                    msgAviso.addProperty("DATA", String.format("INGRESO ORDEN\r\nNemotecnico: %s\r\nId Orden: %d\r\n",
                        getStr(ord, "Nemotecnico"), toLong(ord.get("IdOrden"))));
                    wsMessages.add(msgAviso);

                    JsonObject msgCrear = new JsonObject();
                    msgCrear.addProperty("IdEmpresa", idEmpresa);
                    msgCrear.addProperty("Evento", "CREAR_ORDEN");
                    msgCrear.addProperty("DATA", "");
                    wsMessages.add(msgCrear);
                }
                if (wsMessages.size() > 0) {
                    JsonObject wsParams = new JsonObject();
                    wsParams.addProperty("p_IdEmpresa", idEmpresa);
                    wsParams.addProperty("p_IdUsuario", idUsuario);
                    wsParams.addProperty("p_IdModulo", idModulo);
                    wsParams.add("p_Mensajes", wsMessages);
                    plataformaClient.call("cl.bcs.plataforma.pla.AgregarMensajeWebsocket", wsParams);
                }
            } catch (Exception e) {
                log.warn("Error sending WebSocket notification: {}", e.getMessage());
            }

            // Update FechaAceptacion and FechaAutorizacion
            Timestamp ahora = Timestamp.from(Instant.now());
            for (Map<String, Object> ord : ordenesAutorizadas) {
                jdbc.update(SQL_UPDATE_FECHA_AUTORIZACION,
                    new MapSqlParameterSource()
                        .addValue("IdOrden", toLong(ord.get("IdOrden")))
                        .addValue("FechaAceptacion", ahora)
                        .addValue("FechaAutorizacion", ahora));
            }

            // Route non-MANUAL orders
            try {
                JsonArray rutearOrdenes = new JsonArray();
                for (Map<String, Object> ord : ordenesAutorizadas) {
                    String canalDistrib = getStr(ord, "IdTipoCanalDistribucion");
                    if (!TIPO_CANAL_MANUAL.equalsIgnoreCase(canalDistrib)) {
                        JsonObject o = new JsonObject();
                        o.addProperty("IdOrden", toLong(ord.get("IdOrden")));
                        o.addProperty("IdBolsaDistribucion", getStr(ord, "IdBolsaDistribucion"));
                        rutearOrdenes.add(o);
                    }
                }
                if (rutearOrdenes.size() > 0) {
                    JsonObject rutearParams = new JsonObject();
                    rutearParams.addProperty("p_IdEmpresa", idEmpresa);
                    rutearParams.add("p_Ordenes", rutearOrdenes);
                    rutearOrdenesService.execute(rutearParams);
                }
            } catch (Exception e) {
                log.warn("Error routing orders: {}", e.getMessage());
            }

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            return out;
        } catch (Exception e) {
            log.error("ConfirmarAutorizacionOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
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

    private static String loadSql(String path) {
        try (var is = ConfirmarAutorizacionOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
