package cl.bcs.contabilidad.jobs.support;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * In-memory H2 (MODE=LEGACY) with the BAT objects of job pattern H4: the same {@code BAT.Programa} table that {@link H2Jobs} creates
 * (IdPrograma, RutaSalida) plus {@code schema-programa.sql} (the extra BAT columns and tables, real HANA DDL, BAT_DDL_REAL.md). It is a
 * separate fixture on purpose: {@link H2Jobs} and the existing schema stay as they are.
 *
 * <p>The repository SQL is written for HANA: this fixture's connections strip the {@code BSCL.} schema prefix and replace
 * {@code CURRENT_UTCTIMESTAMP} with the fixed {@link #RELOJ}, so the tests are deterministic.
 */
public final class ProgramaH2 implements AutoCloseable {

    public static final Timestamp RELOJ = Timestamp.valueOf("2026-03-15 08:30:00");
    private static final String PROG = "\"BSCL.Plataforma.BD::BAT.Programa\"";
    private static final Pattern PREFIJO = Pattern.compile("\\bBSCL\\.\"");
    private static final Pattern UTC_AHORA = Pattern.compile("\\bCURRENT_UTCTIMESTAMP\\b", Pattern.CASE_INSENSITIVE);

    private final String url = "jdbc:h2:mem:progh4_" + UUID.randomUUID() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
    private final Connection setup;
    private final DataSource ds;

    public ProgramaH2() {
        try {
            setup = DriverManager.getConnection(url, "sa", "");
            try (Statement st = setup.createStatement()) {
                st.execute("CREATE TABLE " + PROG + " (\"IdPrograma\" BIGINT PRIMARY KEY, \"RutaSalida\" VARCHAR(1000))");
                st.execute("CREATE TABLE \"TEST.Marca\" (\"Valor\" VARCHAR(200))");
            }
            script("/contabilidad-jobs/schema-programa.sql");
        } catch (SQLException e) {
            throw new AssertionError("cannot build the H2 fixture: " + e, e);
        }
        ds = new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return traducida(DriverManager.getConnection(url, "sa", ""));
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return getConnection();
            }
        };
    }

    static String traducir(String sql) {
        String s = PREFIJO.matcher(sql).replaceAll("\"");
        return UTC_AHORA.matcher(s).replaceAll("TIMESTAMP '" + RELOJ + "'");
    }

    private static Connection traducida(Connection real) {
        return (Connection) Proxy.newProxyInstance(ProgramaH2.class.getClassLoader(), new Class<?>[]{Connection.class}, (proxy, metodo, args) -> {
            try {
                if (metodo.getName().equals("prepareStatement") && args != null && args[0] instanceof String sql) {
                    Object[] nuevos = args.clone();
                    nuevos[0] = traducir(sql);
                    return metodo.invoke(real, nuevos);
                }
                return metodo.invoke(real, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
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
        // nothing to restore: the clock is a constant of this fixture
    }
}
