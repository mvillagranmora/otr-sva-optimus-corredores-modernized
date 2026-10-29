package cl.bcs.jobmanager.adapter.out;

import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.port.out.ProgramaPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Read-only JDBC adapter of the program queue (BAT.Programa).
 *
 * Selection = the legacy TransactionManager.readProgramm predicate (TM:41-47) over existing columns, plus the
 * explicit type filter of this dispatcher. It is a plain SELECT: no FOR UPDATE and no SKIP LOCKED (neither the
 * legacy nor this code lock anything here; the job module claims the program atomically). The FIFO order
 * (fechaCreacion) is harmless and keeps old programs from starving. The date window is bound from Java (UTC), so
 * the same SQL runs on HANA and H2 and the clock can be injected.
 *
 * Deliberately absent: any UPDATE of BAT.Programa, parameter loading (the job module reads the BAT view) and the
 * RULE-059 automatic retry (the legacy never retried: updateTryProgramm is commented out, BcsJobManager.java:104-105).
 */
@Repository
public class ProgramaJdbcAdapter implements ProgramaPort {

    static final String SQL_READ_ELIGIBLE =
            "SELECT P.\"IdPrograma\", P.\"idEstado\", P.\"IdTipoPrograma\", P.\"Intento\","
          + " TP.\"TipoClasePrograma\", TP.\"NombreClasePrograma\", TP.\"MaximoIntentos\","
          + " P.\"IdAgenda\", COALESCE(AGENDA.\"EsDemonio\", 'N') AS \"EsDemonio\""
          + " FROM \"BSCL.Plataforma.BD::BAT.Programa\" P"
          + " INNER JOIN \"BSCL.Plataforma.BD::BAT.TipoPrograma\" TP"
          + "   ON P.\"IdTipoPrograma\" = TP.\"IdTipoPrograma\""
          + " LEFT JOIN \"BSCL.Plataforma.BD::BAT.Agenda\" AGENDA"
          + "   ON AGENDA.\"IdAgenda\" = P.\"IdAgenda\""
          + " WHERE P.\"Bloqueo\" = 'N'"
          + "   AND P.\"Nodo\" = :nodo"
          + "   AND P.\"idEstado\" = 'PEN'"
          + "   AND P.\"IdTipoPrograma\" IN (:tipos)"
          + "   AND P.\"Intento\" < TP.\"MaximoIntentos\""
          + "   AND P.\"fechaEjecucion\" BETWEEN :desde AND :hasta"
          + " ORDER BY P.\"fechaCreacion\" ASC, P.\"IdPrograma\" ASC";

    private final NamedParameterJdbcTemplate jdbc;

    public ProgramaJdbcAdapter(@Qualifier("jobManagerJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Programa> readEligiblePrograms(String node, Collection<String> tipos, Instant now) {
        Objects.requireNonNull(node, "node must not be null");
        Objects.requireNonNull(tipos, "tipos must not be null");
        Objects.requireNonNull(now, "now must not be null");
        if (tipos.isEmpty()) {
            return List.of(); // inert: no query at all
        }
        LocalDateTime hasta = LocalDateTime.ofInstant(now, ZoneOffset.UTC);
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("nodo", node)
                .addValue("tipos", List.copyOf(tipos))
                .addValue("desde", Timestamp.valueOf(hasta.toLocalDate().atStartOfDay()))
                .addValue("hasta", Timestamp.valueOf(hasta));
        return jdbc.query(SQL_READ_ELIGIBLE, params, (rs, rowNum) -> {
            Programa p = new Programa();
            p.setIdPrograma(rs.getLong("IdPrograma"));
            p.setIdEstado(rs.getString("idEstado"));
            p.setIdTipoPrograma(rs.getString("IdTipoPrograma"));
            p.setIntento(rs.getInt("Intento"));
            p.setMaximoIntentos(rs.getInt("MaximoIntentos"));
            p.setNombreClasePrograma(rs.getString("NombreClasePrograma"));
            p.setTipoClasePrograma(rs.getString("TipoClasePrograma"));
            p.setIdAgenda(rs.getInt("IdAgenda"));
            p.setDemonio("S".equals(rs.getString("EsDemonio")));
            return p;
        });
    }
}
