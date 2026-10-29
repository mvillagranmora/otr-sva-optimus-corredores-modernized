package cl.bcs.ordenes.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.utils.ActualizarOrderIdHT (JavaCall).
 * Reads BVL/SEBRA folio-to-order mappings from an external DB and updates
 * ORD.Transaccion.OrderIdNegociacion on the main HANA DB.
 * Requires SEBRA_JDBC_URL, SEBRA_USERNAME, SEBRA_PASSWORD env vars.
 */
@Service
public class ActualizarOrderIdHTService {

    private static final Logger log = LoggerFactory.getLogger(ActualizarOrderIdHTService.class);

    private static final String SQL_SELECT_EMPRESA_CODIGO =
        "SELECT \"Codigo\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" WHERE \"IdEmpresa\" = :IdEmpresa";

    private static final String SQL_UPDATE_TRANSACCION =
        "UPDATE \"BSCL.Orden.BD::ORD.Transaccion\" " +
        "SET \"OrderIdNegociacion\" = CASE " +
        "  WHEN \"IdTipoOperacion\" LIKE 'COM%' THEN :ID_ORDEN_COM " +
        "  WHEN \"IdTipoOperacion\" LIKE 'VEN%' THEN :ID_ORDEN_VEN " +
        "  ELSE '' END " +
        "WHERE \"FechaSistemaCreacion\" = :FECHA AND \"Folio\" = :FOLIO";

    private final NamedParameterJdbcTemplate jdbc;
    private final String sebraUrl;
    private final String sebraUsername;
    private final String sebraPassword;

    public ActualizarOrderIdHTService(
            NamedParameterJdbcTemplate jdbc,
            @Value("${bcs.sebra.datasource.url:}") String sebraUrl,
            @Value("${bcs.sebra.datasource.username:}") String sebraUsername,
            @Value("${bcs.sebra.datasource.password:}") String sebraPassword) {
        this.jdbc = jdbc;
        this.sebraUrl = sebraUrl;
        this.sebraUsername = sebraUsername;
        this.sebraPassword = sebraPassword;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("ActualizarOrderIdHTService.execute()");
        try {
            Long idEmpresa  = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            String fechaDesde = params.has("p_FechaDesde") ? params.get("p_FechaDesde").getAsString() : "";
            String fechaHasta = params.has("p_FechaHasta") ? params.get("p_FechaHasta").getAsString() : "";

            if (sebraUrl == null || sebraUrl.isBlank()) {
                log.warn("SEBRA datasource not configured (bcs.sebra.datasource.url is empty); skipping ActualizarOrderIdHT");
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString(
                    "[{\"tipo\":\"ADVERTENCIA\",\"codigo\":\"SEBRA_NO_CONFIGURADO\"}]"));
                return out;
            }

            // Get corredor code from empresa
            int codigoCorredora = 0;
            try {
                List<Map<String, Object>> empresaRows = jdbc.queryForList(SQL_SELECT_EMPRESA_CODIGO,
                    new MapSqlParameterSource("IdEmpresa", idEmpresa));
                if (!empresaRows.isEmpty()) {
                    Object codigo = empresaRows.get(0).get("Codigo");
                    if (codigo != null) codigoCorredora = Integer.parseInt(codigo.toString().trim());
                }
            } catch (Exception e) {
                log.warn("Could not get empresa Codigo: {}", e.getMessage());
            }

            // Connect to SEBRA external DB and query TBRVTR/TBRVTRBO
            List<Map<String, Object>> transaccionesSebra = querySebra(fechaDesde, fechaHasta, codigoCorredora);

            // Batch update ORD.Transaccion with OrderId from SEBRA
            int updated = 0;
            for (Map<String, Object> tx : transaccionesSebra) {
                try {
                    jdbc.update(SQL_UPDATE_TRANSACCION,
                        new MapSqlParameterSource()
                            .addValue("FECHA", getStr(tx, "FECHA"))
                            .addValue("FOLIO", getStr(tx, "FOLIO").trim())
                            .addValue("ID_ORDEN_COM", getStr(tx, "ID_ORDEN_COM"))
                            .addValue("ID_ORDEN_VEN", getStr(tx, "ID_ORDEN_VEN")));
                    updated++;
                } catch (Exception e) {
                    log.warn("Error updating transaccion folio={}: {}", tx.get("FOLIO"), e.getMessage());
                }
            }

            log.info("ActualizarOrderIdHT: updated {} transactions from SEBRA", updated);
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            return out;
        } catch (Exception e) {
            log.error("ActualizarOrderIdHTService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private List<Map<String, Object>> querySebra(String fechaDesde, String fechaHasta, int corredor) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // Create a JDBC connection to SEBRA using configured properties
            java.sql.Connection sebraConn = java.sql.DriverManager.getConnection(sebraUrl, sebraUsername, sebraPassword);
            String sql = "SELECT TR.FECHA, RTRIM(LTRIM(TR.FOLIO)) AS FOLIO, BO.ID_ORDEN_COM, BO.ID_ORDEN_VEN " +
                "FROM TBRVTR TR LEFT JOIN TBRVTRBO BO ON TR.FECHA = BO.FECHA AND TR.FOLIO = BO.FOLIO " +
                "WHERE TR.FECHA BETWEEN ? AND ? AND (TR.COD_CORR_COM = ? OR TR.COD_CORR_VEN = ?)";
            try (java.sql.PreparedStatement ps = sebraConn.prepareStatement(sql)) {
                ps.setString(1, fechaDesde);
                ps.setString(2, fechaHasta);
                ps.setInt(3, corredor);
                ps.setInt(4, corredor);
                java.sql.ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Map<String, Object> row = new java.util.HashMap<>();
                    row.put("FECHA", rs.getString("FECHA"));
                    row.put("FOLIO", rs.getString("FOLIO"));
                    row.put("ID_ORDEN_COM", rs.getString("ID_ORDEN_COM") != null ? rs.getString("ID_ORDEN_COM") : "");
                    row.put("ID_ORDEN_VEN", rs.getString("ID_ORDEN_VEN") != null ? rs.getString("ID_ORDEN_VEN") : "");
                    result.add(row);
                }
                rs.close();
            }
            sebraConn.close();
        } catch (Exception e) {
            log.error("Error querying SEBRA DB: {}", e.getMessage());
        }
        return result;
    }

    private static String getStr(Map<String, Object> m, String key) {
        Object v = m == null ? null : m.get(key);
        return v != null ? v.toString() : "";
    }
}
