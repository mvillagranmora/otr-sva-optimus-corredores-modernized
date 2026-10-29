package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.PlataformaCallClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
 * Migrated from legacy cl.bcs.ordenes.AnularOrdenMasiva (JavaCall).
 * Calls HANA stored procedure AnularOrdenMasiva then notifies via BcsPlataformaServicios.
 */
@Service
public class AnularOrdenMasivaService {

    private static final Logger log = LoggerFactory.getLogger(AnularOrdenMasivaService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd HH:mm:ss.SSS").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;
    private final String SQL_SELECT_ORDENES;
    private final String SQL_SELECT_ORDENES_HORARIOS;
    private final String SQL_SELECT_ORDENES_MODIFICADAS;
    private final String SQL_CALL;

    private static final String SQL_CREATE_TEMP =
        "CREATE LOCAL TEMPORARY TABLE #entrada_anular_ordenes " +
        "(\"IdOrden\" BIGINT, \"MotivoAnulacion\" NVARCHAR(5000), \"FechaHoraAnulacion\" TIMESTAMP)";
    private static final String SQL_INSERT_TEMP =
        "INSERT INTO #entrada_anular_ordenes VALUES(?, ?, ?)";
    private static final String SQL_DROP_TEMP =
        "DROP TABLE #entrada_anular_ordenes";

    public AnularOrdenMasivaService(NamedParameterJdbcTemplate jdbc,
                                     ConfiguracionSistemaUtil configUtil,
                                     PlataformaCallClient plataformaClient) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbc.getJdbcTemplate();
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
        this.SQL_SELECT_ORDENES           = loadSql("cl/bcs/ordenes/sql/AnularOrdenMasiva_SelectOrdenes.sql");
        this.SQL_SELECT_ORDENES_HORARIOS  = loadSql("cl/bcs/ordenes/sql/AnularOrdenMasiva_SelectOrdenesHorarios.sql");
        this.SQL_SELECT_ORDENES_MODIFICADAS = loadSql("cl/bcs/ordenes/sql/OrdenesModificadas_Select.sql");
        this.SQL_CALL                     = loadSql("cl/bcs/ordenes/sql/AnularOrdenMasiva_Call.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("AnularOrdenMasivaService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String requiereNotificacion = params.has("p_RequiereNotificacion")
                ? params.get("p_RequiereNotificacion").getAsString() : "S";
            JsonArray ordenesIn = params.has("p_ordenes") ? params.get("p_ordenes").getAsJsonArray()
                : (params.has("p_Ordenes") ? params.get("p_Ordenes").getAsJsonArray() : new JsonArray());

            Timestamp ahora = Timestamp.from(Instant.now());

            // Build order list with cancellation timestamp
            List<Map<String, Object>> ordenes = new ArrayList<>();
            for (var el : ordenesIn) {
                JsonObject ord = el.getAsJsonObject();
                ordenes.add(Map.of(
                    "IdOrden", ord.has("IdOrden") ? ord.get("IdOrden").getAsLong() : 0L,
                    "MotivoAnulacion", ord.has("MotivoAnulacion") ? ord.get("MotivoAnulacion").getAsString() : "",
                    "FechaHoraAnulacion", ahora
                ));
            }

            // Execute stored procedure via raw JDBC
            List<Map<String, Object>> erroresDetalle = new ArrayList<>();
            List<Map<String, Object>> infoDetalle = new ArrayList<>();
            List<Map<String, Object>> advertenciasDetalle = new ArrayList<>();

            jdbcTemplate.execute((java.sql.Connection conn) -> {
                try {
                    conn.createStatement().execute(SQL_CREATE_TEMP);
                    PreparedStatement ps = conn.prepareStatement(SQL_INSERT_TEMP);
                    for (Map<String, Object> ord : ordenes) {
                        ps.setLong(1, (Long) ord.get("IdOrden"));
                        ps.setString(2, (String) ord.get("MotivoAnulacion"));
                        ps.setTimestamp(3, (Timestamp) ord.get("FechaHoraAnulacion"));
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    ps.close();

                    // Build CALL SQL with scalar params substituted
                    String callSql = SQL_CALL
                        .replace(":p_IdEmpresa", String.valueOf(idEmpresa))
                        .replace(":p_IdUsuario", String.valueOf(idUsuario))
                        .replace(":p_IdModulo", "'" + idModulo + "'");

                    try {
                        ResultSet rs = conn.createStatement().executeQuery(callSql);
                        while (rs != null && rs.next()) {
                            Map<String, Object> row = new java.util.HashMap<>();
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                            }
                            infoDetalle.add(row);
                        }
                        if (rs != null) rs.close();
                    } catch (Exception ex) {
                        log.warn("Stored proc call warning: {}", ex.getMessage());
                    }
                } finally {
                    try { conn.createStatement().execute(SQL_DROP_TEMP); } catch (Exception ex) { /* ignore */ }
                }
                return null;
            });

            // Send OWP notifications if required and no errors
            if ("S".equalsIgnoreCase(requiereNotificacion) && erroresDetalle.isEmpty()) {
                try {
                    List<Long> idsOrden = new ArrayList<>();
                    for (Map<String, Object> ord : ordenes) {
                        idsOrden.add((Long) ord.get("IdOrden"));
                    }
                    if (!idsOrden.isEmpty()) {
                        List<Map<String, Object>> ordenesModificadas = jdbc.queryForList(
                            SQL_SELECT_ORDENES_MODIFICADAS,
                            new MapSqlParameterSource("p_IdOrdenes", idsOrden));

                        if (!ordenesModificadas.isEmpty()) {
                            JsonArray owpList = new JsonArray();
                            for (Map<String, Object> om : ordenesModificadas) {
                                JsonObject owpOrden = new JsonObject();
                                owpOrden.addProperty("IdEmpresa", idEmpresa);
                                owpOrden.addProperty("IdUsuario", idUsuario);
                                owpOrden.addProperty("Solsec", toLong(om.get("IdOrden")));
                                owpOrden.addProperty("NTitulos", getStr(om, "Cantidad"));
                                owpOrden.addProperty("Precio", getStr(om, "PrecioOrden"));
                                owpOrden.addProperty("Fecha", getStr(om, "FechaOperacion"));
                                String estadoFac = getStr(om, "IdEstadoFacturacion");
                                owpOrden.addProperty("Estado", "FAC".equalsIgnoreCase(estadoFac) ? "FAC" : getStr(om, "IdEstadoOrden"));
                                owpOrden.addProperty("Mercado", "A");
                                owpOrden.addProperty("Divisa", getStr(om, "IdMonedaOrden"));
                                owpOrden.addProperty("Nemo", getStr(om, "Nemotecnico"));
                                owpList.add(owpOrden);
                            }
                            JsonObject owpParams = new JsonObject();
                            owpParams.addProperty("p_IdEmpresa", idEmpresa);
                            owpParams.add("p_Ordenes", owpList);
                            plataformaClient.call("cl.bcs.plataforma.pla.NotificarEstadoOrdenesOWP", owpParams);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error in OWP notification: {}", e.getMessage());
                }
            }

            long numErrores = erroresDetalle.size();
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", numErrores);
            out.addProperty("P_OUT_INFORMACIONCONTADOR", (long) infoDetalle.size());
            out.addProperty("P_OUT_ADVERTENCIASCONTADOR", (long) advertenciasDetalle.size());
            out.add("P_OUT_ERRORESDETALLE", JsonParser.parseString(GSON.toJson(erroresDetalle)));
            out.add("P_OUT_INFORMACIONDETALLE", JsonParser.parseString(GSON.toJson(infoDetalle)));
            out.add("P_OUT_ADVERTENCIASDETALLE", JsonParser.parseString(GSON.toJson(advertenciasDetalle)));
            return out;
        } catch (Exception e) {
            log.error("AnularOrdenMasivaService error: {}", e.getMessage(), e);
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
        try (var is = AnularOrdenMasivaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
