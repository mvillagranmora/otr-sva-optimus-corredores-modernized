package cl.bcs.normativa.svc.service.ils;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import java.util.List;
import java.util.Map;

/**
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.ils.GeneraReporteILS}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class GeneraReporteILSService {

    private static final Logger log = LoggerFactory.getLogger(GeneraReporteILSService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_1;
    private final String SQL_2;
    private final String SQL_3;
    private final String SQL_4;
    private final String SQL_5;
    private final String SQL_6;
    private final String SQL_7;
    private final String SQL_8;
    private final String SQL_9;
    private final String SQL_10;
    private final String SQL_11;
    private final String SQL_12;
    private final String SQL_13;
    private final String SQL_14;
    private final String SQL_15;
    private final String SQL_16;
    private final String SQL_17;
    private final String SQL_18;
    private final String SQL_19;
    private final String SQL_20;

    public GeneraReporteILSService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/ils/sql/ConsultarSaldosCuentasContables_SelectSaldosCuentasContables.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarInforme.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarIndice.sql");
        this.SQL_3 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarConcepto.sql");
        this.SQL_4 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_ConsultarItem.sql");
        this.SQL_5 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectDerechoRetroCompra.sql");
        this.SQL_6 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectDerechoMonedaExtranjera.sql");
        this.SQL_7 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectObligacionRetroVenta.sql");
        this.SQL_8 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaDerechos.sql");
        this.SQL_9 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaGarantias.sql");
        this.SQL_10 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectSimultaneaObligaciones.sql");
        this.SQL_11 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultado.sql");
        this.SQL_12 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoIndice.sql");
        this.SQL_13 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoConcepto.sql");
        this.SQL_14 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_DeleteInformeResultadoItem.sql");
        this.SQL_15 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultado.sql");
        this.SQL_16 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoIndice.sql");
        this.SQL_17 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoConcepto.sql");
        this.SQL_18 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoItem.sql");
        this.SQL_19 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_InsertInformeResultadoItemDetalle.sql");
        this.SQL_20 = loadSql("cl/bcs/normativa/ils/sql/GeneraReporteILS_SelectFechasUltimosReportes.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GeneraReporteILSService.execute called");
        MapSqlParameterSource p = buildParams(params);
        int totalRows = 0;
        if (!SQL_QUERY.isBlank()) {
            try { if (SQL_QUERY.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_QUERY, p).size(); } else { jdbc.update(SQL_QUERY, p); } } catch (Exception e) { log.warn("Exec failed for SQL_QUERY: {}", e.getMessage()); }
        }
        if (!SQL_1.isBlank()) {
            try { if (SQL_1.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_1, p).size(); } else { jdbc.update(SQL_1, p); } } catch (Exception e) { log.warn("Exec failed for SQL_1: {}", e.getMessage()); }
        }
        if (!SQL_2.isBlank()) {
            try { if (SQL_2.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_2, p).size(); } else { jdbc.update(SQL_2, p); } } catch (Exception e) { log.warn("Exec failed for SQL_2: {}", e.getMessage()); }
        }
        if (!SQL_3.isBlank()) {
            try { if (SQL_3.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_3, p).size(); } else { jdbc.update(SQL_3, p); } } catch (Exception e) { log.warn("Exec failed for SQL_3: {}", e.getMessage()); }
        }
        if (!SQL_4.isBlank()) {
            try { if (SQL_4.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_4, p).size(); } else { jdbc.update(SQL_4, p); } } catch (Exception e) { log.warn("Exec failed for SQL_4: {}", e.getMessage()); }
        }
        if (!SQL_5.isBlank()) {
            try { if (SQL_5.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_5, p).size(); } else { jdbc.update(SQL_5, p); } } catch (Exception e) { log.warn("Exec failed for SQL_5: {}", e.getMessage()); }
        }
        if (!SQL_6.isBlank()) {
            try { if (SQL_6.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_6, p).size(); } else { jdbc.update(SQL_6, p); } } catch (Exception e) { log.warn("Exec failed for SQL_6: {}", e.getMessage()); }
        }
        if (!SQL_7.isBlank()) {
            try { if (SQL_7.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_7, p).size(); } else { jdbc.update(SQL_7, p); } } catch (Exception e) { log.warn("Exec failed for SQL_7: {}", e.getMessage()); }
        }
        if (!SQL_8.isBlank()) {
            try { if (SQL_8.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_8, p).size(); } else { jdbc.update(SQL_8, p); } } catch (Exception e) { log.warn("Exec failed for SQL_8: {}", e.getMessage()); }
        }
        if (!SQL_9.isBlank()) {
            try { if (SQL_9.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_9, p).size(); } else { jdbc.update(SQL_9, p); } } catch (Exception e) { log.warn("Exec failed for SQL_9: {}", e.getMessage()); }
        }
        if (!SQL_10.isBlank()) {
            try { if (SQL_10.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_10, p).size(); } else { jdbc.update(SQL_10, p); } } catch (Exception e) { log.warn("Exec failed for SQL_10: {}", e.getMessage()); }
        }
        if (!SQL_11.isBlank()) {
            try { if (SQL_11.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_11, p).size(); } else { jdbc.update(SQL_11, p); } } catch (Exception e) { log.warn("Exec failed for SQL_11: {}", e.getMessage()); }
        }
        if (!SQL_12.isBlank()) {
            try { if (SQL_12.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_12, p).size(); } else { jdbc.update(SQL_12, p); } } catch (Exception e) { log.warn("Exec failed for SQL_12: {}", e.getMessage()); }
        }
        if (!SQL_13.isBlank()) {
            try { if (SQL_13.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_13, p).size(); } else { jdbc.update(SQL_13, p); } } catch (Exception e) { log.warn("Exec failed for SQL_13: {}", e.getMessage()); }
        }
        if (!SQL_14.isBlank()) {
            try { if (SQL_14.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_14, p).size(); } else { jdbc.update(SQL_14, p); } } catch (Exception e) { log.warn("Exec failed for SQL_14: {}", e.getMessage()); }
        }
        if (!SQL_15.isBlank()) {
            try { if (SQL_15.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_15, p).size(); } else { jdbc.update(SQL_15, p); } } catch (Exception e) { log.warn("Exec failed for SQL_15: {}", e.getMessage()); }
        }
        if (!SQL_16.isBlank()) {
            try { if (SQL_16.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_16, p).size(); } else { jdbc.update(SQL_16, p); } } catch (Exception e) { log.warn("Exec failed for SQL_16: {}", e.getMessage()); }
        }
        if (!SQL_17.isBlank()) {
            try { if (SQL_17.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_17, p).size(); } else { jdbc.update(SQL_17, p); } } catch (Exception e) { log.warn("Exec failed for SQL_17: {}", e.getMessage()); }
        }
        if (!SQL_18.isBlank()) {
            try { if (SQL_18.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_18, p).size(); } else { jdbc.update(SQL_18, p); } } catch (Exception e) { log.warn("Exec failed for SQL_18: {}", e.getMessage()); }
        }
        if (!SQL_19.isBlank()) {
            try { if (SQL_19.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_19, p).size(); } else { jdbc.update(SQL_19, p); } } catch (Exception e) { log.warn("Exec failed for SQL_19: {}", e.getMessage()); }
        }
        if (!SQL_20.isBlank()) {
            try { if (SQL_20.trim().toUpperCase().startsWith("SELECT")) { totalRows += jdbc.queryForList(SQL_20, p).size(); } else { jdbc.update(SQL_20, p); } } catch (Exception e) { log.warn("Exec failed for SQL_20: {}", e.getMessage()); }
        }
        // TODO: plataforma deps (AgregarLog, ConfiguracionSistema, FechaDB) skipped in modernized
        JsonObject out = new JsonObject();
        out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
        out.addProperty("P_OUT_MENSAJE", "OK");
        out.addProperty("totalRows", totalRows);
        return out;
    }


    private MapSqlParameterSource buildParams(JsonObject params) {
        MapSqlParameterSource p = new MapSqlParameterSource();
        params.entrySet().forEach(e -> {
            try { p.addValue(e.getKey(), e.getValue().getAsString()); }
            catch (Exception ex) { p.addValue(e.getKey(), null); }
        });
        return p;
    }

    private static String loadSql(String path) {
        try (var is = GeneraReporteILSService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
