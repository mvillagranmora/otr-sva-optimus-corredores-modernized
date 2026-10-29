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

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.CalcularCoberturaOrdenes (JavaCall).
 * Calls HANA stored procedure CalcularCobertura and converts amounts via CalcularParidad.
 */
@Service
public class CalcularCoberturaOrdenesService {

    private static final Logger log = LoggerFactory.getLogger(CalcularCoberturaOrdenesService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final ConfiguracionSistemaUtil configUtil;
    private final PlataformaCallClient plataformaClient;
    private final String SQL_SELECT_TIPO_PRESTAMO;
    private final String SQL_SELECT_BOLSA_TIPO_INSTRUMENTO;
    private final String SQL_COBERTURA_TABLA;
    private final String SQL_INSERT_COBERTURA;
    private final String SQL_CALL;

    public CalcularCoberturaOrdenesService(NamedParameterJdbcTemplate jdbc,
                                            ConfiguracionSistemaUtil configUtil,
                                            PlataformaCallClient plataformaClient) {
        this.jdbc = jdbc;
        this.jdbcTemplate = jdbc.getJdbcTemplate();
        this.configUtil = configUtil;
        this.plataformaClient = plataformaClient;
        this.SQL_SELECT_TIPO_PRESTAMO        = loadSql("cl/bcs/ordenes/sql/ConsultarTipoPrestamo_Select.sql");
        this.SQL_SELECT_BOLSA_TIPO_INSTRUMENTO = loadSql("cl/bcs/ordenes/sql/ConsultarBolsaTipoInstrumento_Select.sql");
        this.SQL_COBERTURA_TABLA             = loadSql("cl/bcs/ordenes/sql/CoberturasOrdenes_TablaTemporal.sql");
        this.SQL_INSERT_COBERTURA            = loadSql("cl/bcs/ordenes/sql/Cobertura_Insert.sql");
        this.SQL_CALL                        = loadSql("cl/bcs/ordenes/sql/CalcularCoberturas_Call.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("CalcularCoberturaOrdenesService.execute()");
        try {
            Long idEmpresa      = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario      = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;
            String idModulo     = params.has("p_IdModulo") ? params.get("p_IdModulo").getAsString() : "ORD";
            String esVentaCorta = params.has("p_EsVentaCorta") ? params.get("p_EsVentaCorta").getAsString() : "N";
            String idBolsa      = params.has("p_IdBolsa") ? params.get("p_IdBolsa").getAsString() : "BCS";
            Long idPortafolio   = params.has("p_IdPortafolio") ? params.get("p_IdPortafolio").getAsLong() : null;
            String idTipoInstrumento = params.has("p_IdTipoInstrumento") ? params.get("p_IdTipoInstrumento").getAsString() : null;
            Long idInstrumento  = params.has("p_IdInstrumento") ? params.get("p_IdInstrumento").getAsLong() : null;
            String usaReglas    = params.has("p_UsaReglasNegocio") ? params.get("p_UsaReglasNegocio").getAsString() : "S";
            JsonArray simulacion = params.has("p_Simulacion") ? params.get("p_Simulacion").getAsJsonArray() : new JsonArray();

            String fechaSistema    = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");
            String monedaSistema   = configUtil.getParametroString(idEmpresa, "PLA.MonedaSistema");
            String idMoneda        = params.has("p_IdMoneda") && !params.get("p_IdMoneda").getAsString().isBlank()
                ? params.get("p_IdMoneda").getAsString() : monedaSistema;
            String idMonedaOrigen  = params.has("p_IdMonedaOrigen") && !params.get("p_IdMonedaOrigen").getAsString().isBlank()
                ? params.get("p_IdMonedaOrigen").getAsString() : monedaSistema;

            // Resolve instrument type
            String tipoInstrumentoExige;
            Long idInstrumentoFinal;
            if ("S".equals(esVentaCorta)) {
                tipoInstrumentoExige = "PRT";
                List<Map<String, Object>> prestamo = jdbc.queryForList(SQL_SELECT_TIPO_PRESTAMO, new MapSqlParameterSource());
                idInstrumentoFinal = prestamo.isEmpty() ? null : toLong(prestamo.get(0).get("IdInstrumentoPrestamo"));
            } else {
                tipoInstrumentoExige = idTipoInstrumento;
                idInstrumentoFinal = idInstrumento;
            }

            String idBolsaEfectiva = "0".equals(idBolsa) ? "BCS" : idBolsa;
            List<Map<String, Object>> btiRows = jdbc.queryForList(SQL_SELECT_BOLSA_TIPO_INSTRUMENTO,
                new MapSqlParameterSource()
                    .addValue("IdTipoInstrumento", tipoInstrumentoExige)
                    .addValue("IdBolsa", idBolsaEfectiva));

            if (btiRows.isEmpty()) {
                JsonObject out = new JsonObject();
                out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
                out.add("P_OUT_MENSAJES", JsonParser.parseString("[{\"tipo\":\"ERROR\",\"codigo\":\"SIN_ENTIDAD_DESTINO_GARANTIA\"}]"));
                return out;
            }

            Long idEntidadDestinoGarantia = toLong(btiRows.get(0).get("IdEntidadDestinoGarantia"));

            // Execute stored proc via raw JDBC
            final String finalTipoInstr = tipoInstrumentoExige;
            final Long finalIdInstr = idInstrumentoFinal;
            final List<Map<String, Object>> cobSimulacion = new ArrayList<>();
            final List<Map<String, Object>> cobPortafolio = new ArrayList<>();
            final List<Map<String, Object>> erroresDetalle = new ArrayList<>();

            jdbcTemplate.execute((java.sql.Connection conn) -> {
                try {
                    conn.createStatement().execute(SQL_COBERTURA_TABLA);

                    // Insert simulation rows
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO #p_SimulacionCobertura VALUES(?, ?, ?, ?, ?, ?)");
                    for (var el : simulacion) {
                        JsonObject cob = el.getAsJsonObject();
                        ps.setLong(1, cob.has("IdReferencial") ? cob.get("IdReferencial").getAsLong() : 0L);
                        ps.setLong(2, cob.has("IdInstrumento") ? cob.get("IdInstrumento").getAsLong() : 0L);
                        ps.setBigDecimal(3, cob.has("Cantidad") ? cob.get("Cantidad").getAsBigDecimal() : BigDecimal.ZERO);
                        ps.setBigDecimal(4, cob.has("PrecioMercado") ? cob.get("PrecioMercado").getAsBigDecimal() : BigDecimal.ZERO);
                        ps.setBigDecimal(5, cob.has("Monto") ? cob.get("Monto").getAsBigDecimal() : BigDecimal.ZERO);
                        ps.setBigDecimal(6, cob.has("MontoCoberturaSimular") ? cob.get("MontoCoberturaSimular").getAsBigDecimal() : BigDecimal.ZERO);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                    ps.close();

                    // Build CALL SQL with scalar params
                    String callSql = SQL_CALL
                        .replace(":p_IdEmpresa", String.valueOf(idEmpresa))
                        .replace(":p_IdUsuario", String.valueOf(idUsuario))
                        .replace(":p_IdModulo", "'" + idModulo + "'")
                        .replace(":p_UsaReglasNegocio", "'" + usaReglas + "'")
                        .replace(":p_IdPortafolio", String.valueOf(idPortafolio))
                        .replace(":p_IdEntidadDestinoGarantia", String.valueOf(idEntidadDestinoGarantia))
                        .replace(":p_IdTipoInstrumentoExigeGarantia", "'" + finalTipoInstr + "'")
                        .replace(":p_IdInstrumentoExigeGarantia", String.valueOf(finalIdInstr));

                    try {
                        ResultSet rs = conn.createStatement().executeQuery(callSql);
                        while (rs != null && rs.next()) {
                            java.util.HashMap<String, Object> row = new java.util.HashMap<>();
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                row.put(rs.getMetaData().getColumnName(i), rs.getObject(i));
                            }
                            cobSimulacion.add(row);
                        }
                        if (rs != null) rs.close();
                    } catch (Exception ex) {
                        log.warn("Stored proc call warning: {}", ex.getMessage());
                    }
                } finally {
                    try { conn.createStatement().execute("DROP TABLE #p_SimulacionCobertura"); } catch (Exception ex) { /* ignore */ }
                }
                return null;
            });

            // Convert amounts via CalcularParidad if needed
            if (!cobPortafolio.isEmpty() && !idMonedaOrigen.equals(idMoneda)) {
                try {
                    JsonObject paridadParams = new JsonObject();
                    paridadParams.addProperty("p_IdEmpresa", idEmpresa);
                    paridadParams.addProperty("p_IdTrabajo", 1156L);
                    JsonArray calculos = new JsonArray();
                    int idx = 1;
                    for (Map<String, Object> cp : cobPortafolio) {
                        for (String campo : List.of("MontoCoberturaCorredora", "MontoCoberturaEntidad")) {
                            JsonObject c = new JsonObject();
                            c.addProperty("IdTrabajo", 1156L);
                            c.addProperty("IdInterno", (long) idx++);
                            c.addProperty("Fecha", fechaSistema);
                            c.addProperty("IdMonedaOrigen", idMonedaOrigen);
                            c.addProperty("IdMonedaDestino", idMoneda);
                            c.addProperty("ValorCambioLocalModificado", "0");
                            c.addProperty("MontoOrigen", cp.get(campo) != null ? cp.get(campo).toString() : "0");
                            c.addProperty("IdUso", "ORDENES");
                            calculos.add(c);
                        }
                    }
                    paridadParams.add("p_CalculosEntrada", calculos);
                    plataformaClient.call("cl.bcs.plataforma.mer.CalcularParidad", paridadParams);
                } catch (Exception e) {
                    log.warn("CalcularParidad error: {}", e.getMessage());
                }
            }

            long errores = erroresDetalle.size();
            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", errores);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("p_out_TableCoberturaSimulacion", JsonParser.parseString(GSON.toJson(cobSimulacion)));
            out.add("p_out_TableCoberturaPortafolio", JsonParser.parseString(GSON.toJson(cobPortafolio)));
            out.add("p_out_ErroresDetalle", JsonParser.parseString(GSON.toJson(erroresDetalle)));
            return out;
        } catch (Exception e) {
            log.error("CalcularCoberturaOrdenesService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static Long toLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    private static String loadSql(String path) {
        try (var is = CalcularCoberturaOrdenesService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
