package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.PlataformaCallClient;
import cl.bcs.ordenes.util.RequisitosBasicosUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.CantarOrdenes (JavaCall).
 * Calls HANA stored procedure CambiarEstadoOrdenMasivo to offer orders,
 * then sends WebSocket notifications via BcsPlataformaServicios.
 */
@Service
public class CantarOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(CantarOrdenesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final RequisitosBasicosUtil requisitosUtil;
    private final PlataformaCallClient plataformaClient;
    private final NotificarEstadoOrdenService notificarService;
    private final String SQL_CREAR_TEMPORAL;
    private final String SQL_ELIMINAR_TEMPORAL;
    private final String SQL_CALL;
    private final String SQL_UPDATE_ORDENES;

    public CantarOrdenesService(NamedParameterJdbcTemplate jdbc,
                                 RequisitosBasicosUtil requisitosUtil,
                                 PlataformaCallClient plataformaClient,
                                 NotificarEstadoOrdenService notificarService) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbc.getJdbcTemplate();
        this.requisitosUtil = requisitosUtil;
        this.plataformaClient = plataformaClient;
        this.notificarService = notificarService;
        this.SQL_CREAR_TEMPORAL  = loadSql("cl/bcs/ordenes/sql/CantarOrdenes_CrearEntrada.sql");
        this.SQL_ELIMINAR_TEMPORAL = loadSql("cl/bcs/ordenes/sql/CantarOrdenes_EliminarEntrada.sql");
        this.SQL_CALL             = loadSql("cl/bcs/ordenes/sql/CambiarEstadoOrdenMasivo_Call.sql");
        this.SQL_UPDATE_ORDENES   = loadSql("cl/bcs/ordenes/sql/CantarOrdenes_UpdateOrden.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CantarOrdenesService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            JsonArray ordenesIn = params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray();

            RequisitosBasicosUtil.Salida rb = requisitosUtil.validar(idEmpresa, idUsuario, idModulo);
            if (rb.contarErrores() > 0) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", rb.contarErrores());
                out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(rb.p_out_Mensajes)));
                return out;
            }

            List<Map<String, Object>> mensajes = new ArrayList<>();
            List<Long> ordenesNotificar = new ArrayList<>();
            List<Map<String, Object>> ordenesActualizar = new ArrayList<>();

            // Execute HANA stored procedure via raw JDBC (temp table + CALL)
            List<SalidaDetalle> salidaDetalle = jdbcTemplate.execute((java.sql.Connection conn) -> {
                List<SalidaDetalle> resultados = new ArrayList<>();
                try {
                    // Create temp table
                    conn.createStatement().execute(SQL_CREAR_TEMPORAL);

                    // Insert orders into temp table
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO #entrada_cantar_ordenes VALUES(?, ?)");
                    for (var el : ordenesIn) {
                        JsonObject ord = el.getAsJsonObject();
                        long idOrden   = ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : 0L;
                        long idInterno = ord.has("IdInterno") ? ord.get("IdInterno").getAsLong() : 0L;
                        ps.setLong(1, idOrden);
                        ps.setLong(2, idInterno);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    ps.close();

                    // Call stored procedure
                    String callSql = SQL_CALL.replace(":p_NuevoEstado", "'OFE'");
                    ResultSet rs = conn.createStatement().executeQuery(callSql);
                    if (rs != null) {
                        while (rs.next()) {
                            SalidaDetalle sd = new SalidaDetalle();
                            try { sd.IdOperacion = rs.getLong("IdOperacion"); } catch (Exception ex) { sd.IdOperacion = 0L; }
                            try { sd.Resultado = rs.getString("Resultado"); } catch (Exception ex) { sd.Resultado = ""; }
                            resultados.add(sd);
                        }
                        rs.close();
                    }
                } finally {
                    try { conn.createStatement().execute(SQL_ELIMINAR_TEMPORAL); } catch (Exception ex) {
                        log.debug("Temp table drop: {}", ex.getMessage());
                    }
                }
                return resultados;
            });

            Timestamp ahora = Timestamp.from(Instant.now());

            for (SalidaDetalle sd : salidaDetalle) {
                if ("OK".equalsIgnoreCase(sd.Resultado)) {
                    ordenesActualizar.add(Map.of("IdOrden", sd.IdOperacion, "FechaOferta", ahora, "IdBolsa", ""));
                    ordenesNotificar.add(sd.IdOperacion);
                    mensajes.add(Map.of("IdInterno", sd.IdOperacion, "tipo", "OK", "codigo", "ORDEN_OFERTADA"));
                }
            }

            // Update orders
            for (Map<String, Object> ord : ordenesActualizar) {
                try {
                    jdbc.update(SQL_UPDATE_ORDENES,
                        new org.springframework.jdbc.core.namedparam.MapSqlParameterSource()
                            .addValue("IdOrden", ord.get("IdOrden"))
                            .addValue("FechaOferta", ord.get("FechaOferta"))
                            .addValue("IdBolsa", ord.get("IdBolsa")));
                } catch (Exception e) {
                    log.warn("Error updating orden {}: {}", ord.get("IdOrden"), e.getMessage());
                }
            }

            // Notify local service about state changes
            if (!ordenesNotificar.isEmpty()) {
                try {
                    JsonObject notifParams = new JsonObject();
                    notifParams.addProperty("p_IdEmpresa", idEmpresa);
                    notifParams.addProperty("p_IdUsuario", idUsuario);
                    notifParams.addProperty("p_IdModulo", idModulo);
                    JsonArray ordArray = new JsonArray();
                    for (Long idOrd : ordenesNotificar) {
                        JsonObject o = new JsonObject();
                        o.addProperty("IdOrden", idOrd);
                        ordArray.add(o);
                    }
                    notifParams.add("p_Ordenes", ordArray);
                    notificarService.execute(notifParams);
                } catch (Exception e) {
                    log.warn("Error in notificar: {}", e.getMessage());
                }
            }

            // Send WebSocket notification via BcsPlataformaServicios
            if (!ordenesNotificar.isEmpty()) {
                try {
                    JsonArray wsMsgs = new JsonArray();
                    JsonObject wsMsg = new JsonObject();
                    wsMsg.addProperty("IdEmpresa", idEmpresa);
                    wsMsg.addProperty("Evento", "ORDEN_OFERTADA");
                    wsMsg.addProperty("DATA", "");
                    wsMsgs.add(wsMsg);
                    JsonObject wsParams = new JsonObject();
                    wsParams.addProperty("p_IdEmpresa", idEmpresa);
                    wsParams.addProperty("p_IdUsuario", idUsuario);
                    wsParams.addProperty("p_IdModulo", idModulo);
                    wsParams.add("p_Mensajes", wsMsgs);
                    plataformaClient.call("cl.bcs.plataforma.pla.AgregarMensajeWebsocket", wsParams);
                } catch (Exception e) {
                    log.warn("Error in WebSocket notification: {}", e.getMessage());
                }
            }

            long errores = mensajes.stream().filter(m -> "ERROR".equals(m.get("tipo"))).count();
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", JsonParser.parseString(GSON.toJson(mensajes)));
            return out;
        } catch (Exception e) {
            log.error("CantarOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static class SalidaDetalle {
        Long IdOperacion;
        String Resultado;
    }

    private static String loadSql(String path) {
        try (var is = CantarOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
