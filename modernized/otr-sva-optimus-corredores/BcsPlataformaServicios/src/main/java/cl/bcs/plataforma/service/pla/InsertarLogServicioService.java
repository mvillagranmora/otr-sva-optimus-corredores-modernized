package cl.bcs.plataforma.service.pla;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Migrated from legacy cl.bcs.plataforma.pla.InsertarLogServicio (JavaCall).
 *
 * Batch-inserts service log records into PLA.LogServicio.
 */
@Service
public class InsertarLogServicioService {

    private static final Logger log = LoggerFactory.getLogger(InsertarLogServicioService.class);

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_INSERT_LOG;
    private final Gson gson = new GsonBuilder().serializeNulls().create();

    public InsertarLogServicioService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_INSERT_LOG = loadSql(
                "cl/bcs/plataforma/pla/sql/InsertarLogServicio_InsertRegistroLog.sql");
    }

    /**
     * Executes the service.
     *
     * @param jin JSON with: RegistroLog (array of { IdModulo, IdUsuario, TipoMensaje, Glosa, Valor1, Valor2 })
     * @return JSON with: P_OUT_ERRORESCONTADOR (0 = success)
     */
    @Transactional
    public JsonObject execute(JsonObject jin) {
        log.debug("INICIO::InsertarLogServicio");

        JsonObject out = new JsonObject();

        try {
            Type listType = new TypeToken<List<RegistroLog>>() {}.getType();
            List<RegistroLog> registros = null;
            if (jin.has("RegistroLog")) {
                registros = gson.fromJson(jin.get("RegistroLog"), listType);
            }

            if (registros == null || registros.isEmpty()) {
                out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
                return out;
            }

            SqlParameterSource[] sources = registros.stream()
                    .map(r -> new MapSqlParameterSource()
                            .addValue("IdModulo", r.IdModulo)
                            .addValue("IdUsuario", r.IdUsuario)
                            .addValue("TipoMensaje", r.TipoMensaje)
                            .addValue("Glosa", r.Glosa)
                            .addValue("Valor1", r.Valor1)
                            .addValue("Valor2", r.Valor2))
                    .toArray(SqlParameterSource[]::new);

            jdbc.batchUpdate(SQL_INSERT_LOG, sources);

            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            log.debug("FIN::InsertarLogServicio ({} registros)", registros.size());

        } catch (Exception e) {
            log.error("Error en InsertarLogServicio: {}", e.getMessage(), e);
            out.addProperty("P_OUT_ERRORESCONTADOR", 1L);
        }

        return out;
    }

    /** Legacy DTO — kept flat for backwards compatibility with callers. */
    public static class RegistroLog {
        public String IdModulo;
        public Long IdUsuario;
        public String TipoMensaje;
        public String Glosa;
        public String Valor1;
        public String Valor2;
    }

    /** Legacy entry wrapper — callers may send { "RegistroLog": [...] }. */
    public static class EntradaLogServicio {
        public List<RegistroLog> RegistroLog;
    }

    private static String loadSql(String path) {
        try (InputStream is = InsertarLogServicioService.class.getClassLoader()
                .getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
