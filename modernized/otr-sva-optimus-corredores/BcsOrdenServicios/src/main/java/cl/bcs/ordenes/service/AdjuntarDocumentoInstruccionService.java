package cl.bcs.ordenes.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Migrates cl.bcs.ordenes.AdjuntarDocumentoInstruccion.
 * Entidad is used only as a DTO — no cross-domain service call needed.
 */
@Service
public class AdjuntarDocumentoInstruccionService {

    private static final Logger log = LoggerFactory.getLogger(AdjuntarDocumentoInstruccionService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final DataSource dataSource;
    private final String sqlInsert;
    private final String sqlSelectInstruccion;
    private final String sqlFechaSistema;
    private final String sqlSecuencia;

    public AdjuntarDocumentoInstruccionService(NamedParameterJdbcTemplate jdbc, DataSource dataSource) {
        this.jdbc = jdbc;
        this.dataSource = dataSource;
        this.sqlInsert = loadSql("AdjuntarDocumentoInstruccion_InsertDocumentoInstruccion.sql");
        this.sqlSelectInstruccion = loadSql("AdjuntarDocumentoInstruccion_SelectInstruccion.sql");
        this.sqlFechaSistema = "SELECT TOP 1 CURRENT_TIMESTAMP FROM DUMMY";
        this.sqlSecuencia = "SELECT SERIES_GENERATE_BIGINT('BSCL.Plataforma.BD.Secuencias::DocumentoInstruccion', 1, 2) AS ID FROM DUMMY";
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::AdjuntarDocumentoInstruccion");
        JsonObject result = new JsonObject();
        JsonArray mensajes = new JsonArray();
        result.add("P_OUT_MENSAJES", mensajes);
        result.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        result.add("P_OUT_REFID", new JsonArray());

        try {
            JsonObject doc = params.has("p_DocumentoInstruccion")
                ? params.getAsJsonObject("p_DocumentoInstruccion") : new JsonObject();
            long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;
            long idInstruccion = doc.has("IdInstruccion") ? doc.get("IdInstruccion").getAsLong() : 0L;
            String adjunto = doc.has("Adjunto") ? doc.get("Adjunto").getAsString() : "";
            String nombre = doc.has("NombreDocumentoAdjunto") ? doc.get("NombreDocumentoAdjunto").getAsString() : "";
            String descripcion = doc.has("Descripcion") ? doc.get("Descripcion").getAsString() : "";

            // Validate instruccion exists
            List<Map<String, Object>> instruccionRows = jdbc.queryForList(
                sqlSelectInstruccion, new MapSqlParameterSource("IdInstruccion", idInstruccion));
            if (instruccionRows.isEmpty()) {
                JsonObject msg = new JsonObject();
                msg.addProperty("Tipo", "ERROR");
                msg.addProperty("Codigo", "UI_INSTRUCCION_NO_EXISTE");
                mensajes.add(msg);
                result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                return result;
            }

            // Get sequence ID
            List<Map<String, Object>> seqRows = jdbc.queryForList(sqlSecuencia, new MapSqlParameterSource());
            long idDocumentoInstruccion = seqRows.isEmpty() ? idInstruccion
                : ((Number) seqRows.get(0).get("ID")).longValue();

            // Decode base64 and insert BLOB via native JDBC
            byte[] decoded = Base64.getDecoder().decode(adjunto);
            Timestamp fechaSistema;
            try (Connection conn = dataSource.getConnection()) {
                fechaSistema = conn.createStatement()
                    .executeQuery(sqlFechaSistema)
                    .getTimestamp(1);
                Blob blob = conn.createBlob();
                blob.setBytes(1, decoded);
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
                    pstmt.setLong(1, idDocumentoInstruccion);
                    pstmt.setLong(2, idInstruccion);
                    pstmt.setString(3, nombre);
                    pstmt.setString(4, descripcion);
                    pstmt.setBlob(5, blob);
                    pstmt.setTimestamp(6, fechaSistema);
                    pstmt.executeUpdate();
                    conn.commit();
                }
            }

            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "OK");
            msg.addProperty("Codigo", "SE_ADJUNTA_DOCUMENTO_INSTRUCCION");
            mensajes.add(msg);

            JsonArray refId = new JsonArray();
            JsonObject entidad = new JsonObject();
            entidad.addProperty("IdInterno", idInstruccion);
            entidad.addProperty("IdEntidad", idDocumentoInstruccion);
            refId.add(entidad);
            result.add("P_OUT_REFID", refId);

        } catch (Exception e) {
            log.error("AdjuntarDocumentoInstruccion failed", e);
            JsonObject msg = new JsonObject();
            msg.addProperty("Tipo", "ERROR");
            msg.addProperty("Codigo", "NO_SE_LOGRO_ADJUNTAR_DOCUMENTO_INSTRUCCION");
            msg.addProperty("Descripcion", e.getMessage());
            mensajes.add(msg);
            result.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        }

        log.info("FIN::AdjuntarDocumentoInstruccion");
        return result;
    }

    private String loadSql(String fileName) {
        String path = "cl/bcs/ordenes/sql/" + fileName;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            if (is == null) return "-- SQL NOT FOUND: " + path;
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Could not load SQL: {}", path);
            return "-- SQL NOT FOUND: " + path;
        }
    }
}
