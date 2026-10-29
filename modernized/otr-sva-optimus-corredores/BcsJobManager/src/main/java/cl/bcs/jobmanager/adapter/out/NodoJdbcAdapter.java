package cl.bcs.jobmanager.adapter.out;

import cl.bcs.jobmanager.port.out.NodoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JDBC adapter for node registration and heartbeat operations.
 */
@Repository
public class NodoJdbcAdapter implements NodoPort {

    private static final Logger log = LoggerFactory.getLogger(NodoJdbcAdapter.class);

    private static final String SQL_DELETE_NODO =
            "DELETE FROM \"BSCL.Plataforma.BD::BAT.Nodos\""
          + " WHERE \"Nodo\" = :nodo AND \"Job\" = :job";

    private static final String SQL_INSERT_NODO =
            "INSERT INTO \"BSCL.Plataforma.BD::BAT.Nodos\""
          + " (\"Nodo\", \"Job\", \"Pid\", \"FechaUltimoCiclo\", \"Reiniciar\")"
          + " VALUES (:nodo, :job, :pid, CURRENT_UTCTIMESTAMP, 'N')";

    private static final String SQL_DELETE_HEARTBEAT =
            "DELETE FROM \"BSCL.Plataforma.BD::PLA.Heartbeat\" WHERE \"Id\" = :id";

    private static final String SQL_INSERT_HEARTBEAT =
            "INSERT INTO \"BSCL.Plataforma.BD::PLA.Heartbeat\""
          + " (\"Id\", \"LastTimestamp\") VALUES (:id, CURRENT_TIMESTAMP)";

    private static final String SQL_UPDATE_CYCLE_NODO =
            "UPDATE \"BSCL.Plataforma.BD::BAT.Nodos\""
          + " SET \"FechaUltimoCiclo\" = CURRENT_UTCTIMESTAMP"
          + " WHERE \"Pid\" = :pid";

    private static final String SQL_UPDATE_CYCLE_HEARTBEAT =
            "UPDATE \"BSCL.Plataforma.BD::PLA.Heartbeat\""
          + " SET \"LastTimestamp\" = CURRENT_TIMESTAMP"
          + " WHERE \"Id\" = :id";

    private static final String SQL_IS_REBOOT =
            "SELECT TOP 1 \"Reiniciar\" FROM \"BSCL.Plataforma.BD::BAT.Nodos\""
          + " WHERE \"Pid\" = :pid"
          + " ORDER BY \"FechaUltimoCiclo\" DESC";

    private static final String SQL_GET_ZOMBIES =
            "SELECT \"Pid\" FROM \"BSCL.Plataforma.BD::BAT.Nodos\""
          + " WHERE \"FechaUltimoCiclo\" < ADD_SECONDS(CURRENT_UTCTIMESTAMP, :neg)";

    private static final String SQL_DELETE_ZOMBIE =
            "DELETE FROM \"BSCL.Plataforma.BD::BAT.Nodos\" WHERE \"Pid\" = :pid";

    private static final String SQL_PING = "SELECT 1 FROM dummy";

    private final NamedParameterJdbcTemplate jdbc;

    public NodoJdbcAdapter(
            @Qualifier("jobManagerJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void addNodo(String nodo, String job, int pid, String processId) {
        Objects.requireNonNull(nodo, "nodo must not be null");
        Objects.requireNonNull(job, "job must not be null");

        MapSqlParameterSource deleteParams = new MapSqlParameterSource()
                .addValue("nodo", nodo)
                .addValue("job", job);
        jdbc.update(SQL_DELETE_NODO, deleteParams);

        MapSqlParameterSource insertParams = new MapSqlParameterSource()
                .addValue("nodo", nodo)
                .addValue("job", job)
                .addValue("pid", pid);
        jdbc.update(SQL_INSERT_NODO, insertParams);

        if (processId != null && !processId.isEmpty()) {
            MapSqlParameterSource hbParams = new MapSqlParameterSource("id", processId);
            jdbc.update(SQL_DELETE_HEARTBEAT, hbParams);
            jdbc.update(SQL_INSERT_HEARTBEAT, hbParams);
        }
    }

    @Override
    @Transactional
    public void updateCycleNodo(int pid, String processId) {
        MapSqlParameterSource params = new MapSqlParameterSource("pid", pid);
        jdbc.update(SQL_UPDATE_CYCLE_NODO, params);

        if (processId != null && !processId.isEmpty()) {
            MapSqlParameterSource hbParams = new MapSqlParameterSource("id", processId);
            jdbc.update(SQL_UPDATE_CYCLE_HEARTBEAT, hbParams);
        }
    }

    @Override
    public boolean isRebootRequested(int pid) {
        MapSqlParameterSource params = new MapSqlParameterSource("pid", pid);
        List<String> rows = jdbc.query(SQL_IS_REBOOT, params,
                (rs, rowNum) -> rs.getString("Reiniciar"));
        return !rows.isEmpty() && "S".equals(rows.get(0));
    }

    @Override
    @Transactional
    public List<Integer> getZombieNodos(int timeoutSeconds, boolean clean) {
        MapSqlParameterSource params = new MapSqlParameterSource("neg", -1 * timeoutSeconds);
        List<Integer> pids = jdbc.query(SQL_GET_ZOMBIES, params,
                (rs, rowNum) -> rs.getInt("Pid"));

        if (clean) {
            for (int pid : pids) {
                MapSqlParameterSource delParams = new MapSqlParameterSource("pid", pid);
                jdbc.update(SQL_DELETE_ZOMBIE, delParams);
            }
        }
        return pids;
    }

    @Override
    public boolean pingDb() {
        try {
            jdbc.getJdbcOperations().queryForObject(SQL_PING, Integer.class);
            return true;
        } catch (Exception e) {
            log.error("DB ping failed: {}", e.getMessage());
            return false;
        }
    }
}
