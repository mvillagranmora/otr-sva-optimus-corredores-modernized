package cl.bcs.cuentainversion.jobs.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;

/**
 * In-memory H2 (MODE=LEGACY) with the BAT objects of job pattern H4: {@code schema-jobs.sql} (aliases and the tables the goldens use)
 * plus {@code schema-programa.sql} (the extra BAT columns and tables). SQL goes through {@link H2Hana} like the golden harness, so
 * {@code CURRENT_UTCTIMESTAMP} is the fixed {@link H2Hana#reloj}. The schema is the real HANA DDL (BAT_DDL_REAL.md), see schema-programa.sql.
 */
public final class ProgramaH2 implements AutoCloseable {

    public static final Timestamp RELOJ = Timestamp.valueOf("2026-03-15 08:30:00");
    private static final String PROG = "\"BSCL.Plataforma.BD::BAT.Programa\"";

    private final String url = "jdbc:h2:mem:progh4_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
    private final Connection setup;
    private final DataSource ds;

    public ProgramaH2() {
        try {
            setup = DriverManager.getConnection(url, "sa", "");
            script("/cuenta-inversion-jobs/schema-jobs.sql");
            script("/cuenta-inversion-jobs/schema-programa.sql");
        } catch (SQLException e) {
            throw new AssertionError("cannot build the H2 fixture: " + e, e);
        }
        H2Hana.reloj = RELOJ;
        ds = new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return H2Hana.wrap(DriverManager.getConnection(url, "sa", ""));
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return getConnection();
            }
        };
    }

    private void script(String recurso) throws SQLException {
        String sql;
        try (InputStream in = ProgramaH2.class.getResourceAsStream(recurso)) {
            sql = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        StringBuilder limpio = new StringBuilder();
        for (String linea : sql.split("\n")) {
            if (!linea.strip().startsWith("--")) {
                limpio.append(linea).append('\n');
            }
        }
        try (Statement st = setup.createStatement()) {
            for (String sentencia : limpio.toString().split(";\\s*\n")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }

    public DataSource dataSource() {
        return ds;
    }

    public JdbcTemplate jdbc() {
        return new JdbcTemplate(ds);
    }

    public DataSourceTransactionManager transacciones() {
        return new DataSourceTransactionManager(ds);
    }

    public ProgramaH2 tipo(String idTipo, String clase, int maximoIntentos) {
        jdbc().update("INSERT INTO \"BSCL.Plataforma.BD::BAT.TipoPrograma\" (\"IdTipoPrograma\", \"MaximoIntentos\", \"NombreClasePrograma\") VALUES (?, ?, ?)",
                idTipo, maximoIntentos, clase);
        return this;
    }

    public ProgramaH2 agenda(long idAgenda) {
        jdbc().update("INSERT INTO \"BSCL.Plataforma.BD::BAT.Agenda\" (\"IdAgenda\", \"EsDemonio\", \"Minuto\") VALUES (?, 'N', '00')", idAgenda);
        return this;
    }

    /** A freshly created program as BAT.AgregarPrograma leaves it: PEN, Intento 0, Bloqueo 'N' (column default). */
    public ProgramaH2 programa(long idPrograma, String idTipo, long idAgenda) {
        jdbc().update("INSERT INTO " + PROG + " (\"IdPrograma\", \"IdTipoPrograma\", \"IdAgenda\", \"idEstado\", \"Intento\", \"Bloqueo\", \"Resultado\") "
                + "VALUES (?, ?, ?, 'PEN', 0, 'N', 'Esperando ejecucion')", idPrograma, idTipo, idAgenda);
        return this;
    }

    public ProgramaH2 estado(long idPrograma, String idEstado, int intento, String bloqueo) {
        jdbc().update("UPDATE " + PROG + " SET \"idEstado\" = ?, \"Intento\" = ?, \"Bloqueo\" = ? WHERE \"IdPrograma\" = ?", idEstado, intento, bloqueo, idPrograma);
        return this;
    }

    public ProgramaH2 parametro(long idPrograma, long secuencia, String nombre, String valor, byte[] blob) {
        jdbc().update("INSERT INTO \"BSCL.Plataforma.BD::BAT.ParametrosPrograma\" (\"IdSecuencia\", \"IdGrupoParametro\", \"IdPrograma\", \"Orden\", "
                + "\"NombreParametro\", \"Valor\", \"ValorBlob\") VALUES (?, 1, ?, ?, ?, ?, ?)", secuencia, idPrograma, secuencia, nombre, valor, blob);
        return this;
    }

    /** The BAT.Programa row as a map (column name to value). */
    public Map<String, Object> fila(long idPrograma) {
        return jdbc().queryForMap("SELECT * FROM " + PROG + " WHERE \"IdPrograma\" = ?", idPrograma);
    }

    public Object agendaUltima(long idAgenda) {
        return jdbc().queryForMap("SELECT * FROM \"BSCL.Plataforma.BD::BAT.Agenda\" WHERE \"IdAgenda\" = ?", idAgenda).get("UltimaFechaDeEjecucion");
    }

    public int marcas() {
        return jdbc().queryForObject("SELECT COUNT(*) FROM \"TEST.Marca\"", Integer.class);
    }

    @Override
    public void close() {
        H2Hana.reloj = Timestamp.valueOf("2025-04-01 10:15:30"); // the default the harness starts from
    }
}
