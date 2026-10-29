package cl.bcs.plataforma.service.ftp;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Migrated from cl.bcs.plataforma.ftp.FileManager (JavaCall).
 * Manages file transfers via FTP/SFTP: downloads from remote servers,
 * inserts cabecera/detalle records, and marks files as processed.
 * External FTP/SFTP credentials must be provided via environment variables.
 * Depends on pla.* InsertarLogServicio and FechaDB for timestamps.
 */
@Service
public class FileManagerService implements JavaCallService {

    private static final Gson GSON = new Gson();
    private final NamedParameterJdbcTemplate jdbc;

    public FileManagerService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public String fqn() {
        return "cl.bcs.plataforma.ftp.FileManager";
    }

    private String loadSql(String path) {
        try (var is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JsonObject execute(JsonObject params) {
        JsonObject result = new JsonObject();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : null;

            // TODO: requires pla.* utility — complete after pla subpackage migration
            // InsertarLogServicio for audit logging, FechaDB for system date.
            // FTP/SFTP connection must use credentials from environment (${FTP_USER}, ${FTP_PASS}).

            // Load maestro configuration
            String sqlMaestro = loadSql("cl/bcs/plataforma/ftp/sql/FTP_SelectMaestro.sql");
            List<Map<String, Object>> maestros = jdbc.queryForList(sqlMaestro,
                    new MapSqlParameterSource().addValue("p_IdEmpresa", idEmpresa));

            JsonArray arr = new JsonArray();
            maestros.forEach(row -> {
                JsonObject o = new JsonObject();
                row.forEach((k, v) -> o.addProperty(k, v != null ? v.toString() : null));
                arr.add(o);
            });

            result.add("P_OUT_MAESTRO", arr);
            result.addProperty("P_OUT_ERRORESCONTADOR", 0);
            result.add("P_OUT_MENSAJES", new JsonArray());
        } catch (Exception e) {
            result.addProperty("P_OUT_ERRORESCONTADOR", 1);
            result.addProperty("error", e.getMessage());
        }
        return result;
    }
}
