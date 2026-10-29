package cl.bcs.apv.jobs.job;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import cl.bcs.apv.infrastructure.jdbc.SqlNombrado;

/**
 * Legacy {@code AgregarLogPrograma.logica(con, IdPrograma, IdTipoPrograma, Mensaje, print)} (Plataforma BcsPlataformaServicios bat/AgregarLogPrograma.java:14-33).
 * The legacy called the HANA procedure {@code BAT.AgregarLogDebugPrograma}, whose whole body is one INSERT on the caller's connection (Plataforma/BD/Servicios/BAT/
 * AgregarLogDebugPrograma.sql). User decision: it stays a direct INSERT on the job's own transaction (the log row is rolled back with the job), not an HTTP call.
 */
final class LogPrograma {

    private static final Logger log = LoggerFactory.getLogger(LogPrograma.class);

    private static final String SQL_INSERT = "INSERT INTO \"BSCL.Plataforma.BD::BAT.LogPrograma\" (\"FechaLog\",\"IdTipoPrograma\",\"IdPrograma\",\"Mensaje\") "
            + "VALUES (CURRENT_TIMESTAMP, :IdTipoPrograma, :IdPrograma, :Mensaje)";

    private LogPrograma() {
    }

    static void agregar(SqlNombrado sql, Long idPrograma, String idTipoPrograma, String mensaje) throws SQLException {
        Map<String, Object> params = new HashMap<>();
        params.put("IdTipoPrograma", idTipoPrograma);
        params.put("IdPrograma", idPrograma);
        params.put("Mensaje", mensaje);
        sql.actualizar(SQL_INSERT, params);
        log.info("Programa {}-{} // {}", idPrograma, idTipoPrograma, mensaje); // legacy print = true: System.out
    }

    static SqlNombrado sql(NamedParameterJdbcTemplate jdbc) {
        return new SqlNombrado(jdbc);
    }
}
