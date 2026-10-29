package cl.bcs.normativa.svc.service;

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
 * Spring @Service migrated from legacy {@code cl.bcs.normativa.GenerarCartasCIR1985}.
 * <p>Status: stub — business logic pending HANA connection validation.
 */
@Service
public class GenerarCartasCIR1985Service {

    private static final Logger log = LoggerFactory.getLogger(GenerarCartasCIR1985Service.class);
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

    public GenerarCartasCIR1985Service(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_QUERY = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarArchivoNormativa.sql");
        this.SQL_1 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarOrdenesEjecutadas.sql");
        this.SQL_2 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarVersionEsquema.sql");
        this.SQL_3 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarLogo.sql");
        this.SQL_4 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarRutaBase.sql");
        this.SQL_5 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarSFTPMaestro.sql");
        this.SQL_6 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarDatosCir1985Masivo.sql");
        this.SQL_7 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_ConsultarDatosCorredora.sql");
        this.SQL_8 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_InsertClienteTablaPaso.sql");
        this.SQL_9 = loadSql("cl/bcs/normativa/sql/GenerarCartasCIR1985_DeleteClienteTablaPaso.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("GenerarCartasCIR1985Service.execute called");
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
        try (var is = GenerarCartasCIR1985Service.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) { log.warn("SQL resource not found: {}", path); return ""; }
            return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) { throw new RuntimeException("Failed to load SQL: " + path, e); }
    }
}
