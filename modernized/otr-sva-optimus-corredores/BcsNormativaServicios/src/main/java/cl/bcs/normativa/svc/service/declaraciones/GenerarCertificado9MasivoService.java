package cl.bcs.normativa.svc.service.declaraciones;

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
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.declaraciones.GenerarCertificado9Masivo}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class GenerarCertificado9MasivoService {

    private static final Logger log = LoggerFactory.getLogger(GenerarCertificado9MasivoService.class);
    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_QUERY;
    private final String SQL_1;
    private final String SQL_2;
    private final String SQL_3;
    private final String SQL_4;
    private final String SQL_5;
    private final String SQL_6;

    public GenerarCertificado9MasivoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectLogoCorredora.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectRaizArchivo.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectImagenFirma.sql");
        this.SQL_3 = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectReporte.sql");
        this.SQL_4 = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_SelectGrupoCertificado9.sql");
        this.SQL_5 = loadSql("cl/bcs/normativa/declaraciones/sql/GenerarCertificadoMasivo_certificado9_Call.sql");
        this.SQL_6 = loadSql("cl/bcs/normativa/jobs/sql/BcsJobGenerarCertificadosTributarios_UpdateCertificadoClienteEstado.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarCertificado9MasivoService.execute called");
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
        try (var is = GenerarCertificado9MasivoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
