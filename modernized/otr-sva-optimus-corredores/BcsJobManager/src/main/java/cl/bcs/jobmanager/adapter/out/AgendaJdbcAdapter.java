package cl.bcs.jobmanager.adapter.out;

import cl.bcs.jobmanager.domain.Agenda;
import cl.bcs.jobmanager.port.out.AgendaPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JDBC adapter for Agenda (scheduler diary) persistence.
 *
 * Methods that call HANA stored procedures (updateNextExecutionDate, addPrograma,
 * firstExecution, getDateForPrograma) are not testable with H2 and must be
 * marked with @Disabled("requires HANA") in tests.
 */
@Repository
public class AgendaJdbcAdapter implements AgendaPort {

    private static final Logger log = LoggerFactory.getLogger(AgendaJdbcAdapter.class);

    private static final String SQL_READ_AGENDAS =
            "SELECT DISTINCT"
          + "  \"UltimaFechaDeEjecucion\","
          + "  \"SiguienteFechaDeEjecucion\","
          + "  \"IdAgenda\","
          + "  \"habilitado\","
          + "  AGENDA.\"IdTipoPrograma\","
          + "  \"TipoClasePrograma\","
          + "  \"NombreClasePrograma\","
          + "  \"EsDemonio\""
          + " FROM \"BSCL.Plataforma.BD::BAT.Agenda\" AGENDA"
          + " LEFT JOIN bscl.\"BSCL.Plataforma.BD::BAT.TipoPrograma\" TIPO"
          + "   ON TIPO.\"IdTipoPrograma\" = AGENDA.\"IdTipoPrograma\""
          + " WHERE \"habilitado\" = 'S'";

    private static final String SQL_READ_PARAMS =
            "SELECT \"NombreParametro\", \"Valor\""
          + " FROM \"BSCL.Plataforma.BD.Vistas::BAT.ConsultarAgenda\""
          + " WHERE \"IdAgenda\" = :id";

    // HANA stored procedure calls — disabled in H2 tests
    private static final String SQL_UPDATE_NEXT_DATE =
            "{CALL bscl.\"BSCL.Plataforma.BD.Servicios::BAT.ActualizarSiguienteFechaEjecucionAgenda\""
          + "(?, ?, ?, ?, ?, ?)}";

    private static final String SQL_ADD_PROGRAMA =
            "call bscl.\"BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma\" (?,?,?,?,?,?,#PARAM_TABLE#,?,?,?,?,?,?)";

    private static final String SQL_FIRST_EXECUTION =
            "call \"BSCL.Plataforma.BD.Servicios::BAT.CorregirPrimeraEjecucion\"(?, ?, ?, ?)";

    private static final String SQL_GET_DATE =
            "call \"BSCL.Plataforma.BD.Servicios::BAT.ObtenerSiguienteFechaAgendada\"(?, ?, ?, ?, ?, ?)";

    private final NamedParameterJdbcTemplate jdbc;

    public AgendaJdbcAdapter(
            @Qualifier("jobManagerJdbcTemplate") NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public List<Agenda> readEnabledAgendas() {
        return jdbc.query(SQL_READ_AGENDAS, new MapSqlParameterSource(), (rs, rowNum) -> {
            Agenda a = new Agenda();
            a.setIdAgenda(rs.getInt("IdAgenda"));
            a.setHabilitado(rs.getString("habilitado"));
            a.setIdTipoPrograma(rs.getString("IdTipoPrograma"));
            a.setTipoClasePrograma(rs.getString("TipoClasePrograma"));
            a.setNombreClasePrograma(rs.getString("NombreClasePrograma"));
            a.setUltimaFechaEjecucion(rs.getString("UltimaFechaDeEjecucion"));
            a.setSiguienteFechaEjecucion(rs.getString("SiguienteFechaDeEjecucion"));
            a.setEsDemonio(rs.getString("EsDemonio"));
            return a;
        });
    }

    @Override
    public Map<String, String> readParams(long idAgenda) {
        Objects.requireNonNull(idAgenda, "idAgenda must not be null");
        MapSqlParameterSource params = new MapSqlParameterSource("id", idAgenda);
        Map<String, String> result = new HashMap<>();
        jdbc.query(SQL_READ_PARAMS, params, rs -> {
            result.put(rs.getString("NombreParametro"), rs.getString("Valor"));
        });
        return result;
    }

    @Override
    @Transactional
    public void updateNextExecutionDate(long idAgenda, int segundos) {
        // HANA stored procedure — not testable with H2
        jdbc.getJdbcOperations().execute(
            SQL_UPDATE_NEXT_DATE,
            (java.sql.CallableStatement cs) -> {
                cs.setInt(1, 1);
                cs.setInt(2, 3);
                cs.setString(3, "PLA");
                cs.setLong(4, idAgenda);
                cs.setInt(5, segundos);
                cs.registerOutParameter(6, java.sql.Types.NVARCHAR);
                cs.execute();
                return null;
            });
    }

    @Override
    @Transactional
    public int addPrograma(int idAgenda, String idTipoTarea, int date, Map<String, String> parameters) {
        // HANA stored procedure — not testable with H2
        String tempTable = "#Parametros_" + idAgenda;
        String createSql = "CREATE LOCAL TEMPORARY TABLE " + tempTable
                + " (\"NombreParametro\" NVARCHAR(100), \"Valor\" NVARCHAR(100))";
        String insertSql = "INSERT INTO " + tempTable + " VALUES (?, ?)";
        String callSql = "call bscl.\"BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma\""
                + " (?,?,?,?,?,?," + tempTable + ",?,?,?,?,?,?)";

        try {
            jdbc.getJdbcOperations().execute(createSql);
        } catch (Exception e) {
            log.warn("Temp table already exists (idAgenda={}): {}", idAgenda, e.getMessage());
        }

        if (!parameters.isEmpty()) {
            jdbc.getJdbcOperations().batchUpdate(insertSql,
                    parameters.entrySet().stream()
                            .map(e -> new Object[]{
                                    e.getKey() == null ? "" : e.getKey(),
                                    e.getValue() == null ? "" : e.getValue()
                            })
                            .toList());
        }

        int[] result = {-1};
        jdbc.getJdbcOperations().execute(callSql,
                (java.sql.CallableStatement cs) -> {
                    cs.setLong(1, 1);
                    cs.setLong(2, 0);
                    cs.setString(3, "ORD");
                    cs.setLong(4, idAgenda);
                    cs.setString(5, idTipoTarea);
                    cs.setLong(6, date);
                    cs.setString(7, "S");
                    cs.registerOutParameter(8, java.sql.Types.NVARCHAR);
                    cs.registerOutParameter(9, java.sql.Types.NVARCHAR);
                    if (cs.execute()) {
                        try (java.sql.ResultSet rs = cs.getResultSet()) {
                            if (rs.next()) {
                                result[0] = Integer.parseInt(rs.getString(1));
                            }
                        }
                    }
                    return null;
                });

        try {
            jdbc.getJdbcOperations().execute("DROP TABLE " + tempTable);
        } catch (Exception e) {
            log.warn("Could not drop temp table {}: {}", tempTable, e.getMessage());
        }

        return result[0];
    }

    @Override
    @Transactional
    public boolean firstExecution() {
        // HANA stored procedure — not testable with H2
        try {
            jdbc.getJdbcOperations().execute(SQL_FIRST_EXECUTION,
                    (java.sql.CallableStatement cs) -> {
                        cs.setLong(1, 1);
                        cs.setLong(2, 1);
                        cs.setString(3, "BAT");
                        cs.registerOutParameter(4, java.sql.Types.NVARCHAR);
                        cs.execute();
                        return null;
                    });
            return true;
        } catch (Exception e) {
            log.error("firstExecution failed: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public int getDateForPrograma(long idAgenda) {
        // HANA stored procedure — not testable with H2
        int[] date = {-1};
        try {
            jdbc.getJdbcOperations().execute(SQL_GET_DATE,
                    (java.sql.CallableStatement cs) -> {
                        cs.setLong(1, 1);
                        cs.setLong(2, 1);
                        cs.setString(3, "ORD");
                        cs.setLong(4, idAgenda);
                        cs.registerOutParameter(5, java.sql.Types.BIGINT);
                        cs.registerOutParameter(6, java.sql.Types.NVARCHAR);
                        if (cs.execute()) {
                            try (java.sql.ResultSet rs = cs.getResultSet()) {
                                if (rs.next()) {
                                    date[0] = Integer.parseInt(rs.getString(1));
                                }
                            }
                        }
                        return null;
                    });
        } catch (Exception e) {
            log.error("getDateForPrograma failed (idAgenda={}): {}", idAgenda, e.getMessage());
            date[0] = 0;
        }
        return date[0];
    }

    /**
     * Pure-logic date comparison used by DiaryService.
     * Returns true if fecha1 (next execution) is before or equal to fechaActual (last execution),
     * meaning the schedule is due.
     */
    @Override
    public boolean compareDates(String fecha1, String fechaActual) {
        Objects.requireNonNull(fecha1, "fecha1 must not be null");
        Objects.requireNonNull(fechaActual, "fechaActual must not be null");
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date d1 = fmt.parse(fecha1);
            Date d2 = fmt.parse(fechaActual);
            return !d1.after(d2);  // d1 <= d2 means schedule is due
        } catch (Exception e) {
            log.error("compareDates failed: {}", e.getMessage());
            return false;
        }
    }
}
