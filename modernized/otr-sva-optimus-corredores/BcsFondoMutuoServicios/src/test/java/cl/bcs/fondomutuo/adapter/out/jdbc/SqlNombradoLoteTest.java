package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.config.TransaccionConfig;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.requisitos.SqlLote;

/**
 * Architecture review F3 W2 (H-2): {@link SqlNombrado#actualizarLote} / {@link SqlLote#ejecutar} run the rows with ONE prepared statement and a JDBC batch (chunks of 1000), with
 * the remaining time of the transaction before every chunk; a failing row is the sanitised {@link AccesoDatosException}; on H2 the rows after the failing one still run (as the
 * legacy batch), and the host's rollback leaves the table as it was.
 */
class SqlNombradoLoteTest {

    public static class Fila {
        public Integer ID;
        public BigDecimal V;
        public String T;

        Fila(int id, BigDecimal v, String t) {
            this.ID = id;
            this.V = v;
            this.T = t;
        }
    }

    private static final String INSERT = "INSERT INTO T_LOTE (ID, V, T) VALUES (:ID, :V, :T)";

    final List<String> eventos = new ArrayList<>();
    DataSource ds;
    PlatformTransactionManager tm;
    ConexionUnidadDeTrabajo conexion;

    @BeforeEach
    void preparar() {
        DataSource h2 = TestBd.nueva();
        TestBd.ejecutar(h2, "CREATE TABLE T_LOTE (ID INT PRIMARY KEY, V DECIMAL(40,20), T VARCHAR(20) NOT NULL)");
        ds = new DelegatingDataSource(h2) {
            @Override
            public Connection getConnection() throws SQLException {
                return espiar(super.getConnection());
            }
        };
        tm = new TransaccionConfig().transactionManager(ds);
        conexion = new ConexionUnidadDeTrabajo(ds);
    }

    private Connection espiar(Connection real) {
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            Object r = invocar(real, m, a);
            if (m.getName().equals("prepareStatement") && r instanceof PreparedStatement st) {
                eventos.add("prepare");
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] { PreparedStatement.class }, (p2, m2, a2) -> {
                    String n = m2.getName();
                    if (n.equals("setQueryTimeout") || n.equals("addBatch") || n.equals("executeBatch") || n.equals("executeUpdate")) eventos.add(n);
                    return invocar(st, m2, a2);
                });
            }
            return r;
        });
    }

    private static Object invocar(Object o, java.lang.reflect.Method m, Object[] a) throws Throwable {
        try {
            return m.invoke(o, a);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private int cuenta() throws SQLException {
        try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM T_LOTE")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static List<Fila> filas(int n, int idRepetido) {
        List<Fila> l = new ArrayList<>();
        for (int i = 1; i <= n; i++) l.add(new Fila(i == idRepetido ? 1 : i, new BigDecimal("1.5"), "t"));
        return l;
    }

    private long veces(String evento) {
        return eventos.stream().filter(e -> e.equals(evento)).count();
    }

    @Test
    void unaSolaSentencia_unAddBatchPorFila_unExecuteBatch_yElPlazoAntesDeEjecutar() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        int[] total = new int[1];
        t.executeWithoutResult(s -> total[0] = conexion.usar(con -> SqlLote.ejecutar(con, INSERT, filas(12, 0))));
        assertThat(total[0]).isEqualTo(12);
        assertThat(veces("prepare")).isEqualTo(1);
        assertThat(veces("addBatch")).isEqualTo(12);
        assertThat(veces("executeBatch")).isEqualTo(1);
        assertThat(veces("executeUpdate")).isZero();
        assertThat(veces("setQueryTimeout")).isEqualTo(1);
        assertThat(eventos.indexOf("setQueryTimeout")).isLessThan(eventos.indexOf("executeBatch"));
    }

    @Test
    void tramosDe1000Filas_conSuPropioPlazo_yTodasLasFilasEnLaTabla() throws Exception {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(60);
        t.executeWithoutResult(s -> conexion.usar(con -> SqlLote.ejecutar(con, INSERT, filas(2500, 0))));
        assertThat(veces("prepare")).isEqualTo(1);
        assertThat(veces("executeBatch")).isEqualTo(3);
        assertThat(veces("setQueryTimeout")).isEqualTo(3);
        assertThat(cuenta()).isEqualTo(2500);
    }

    @Test
    void unaListaVacia_noPreparaNada() {
        int n = conexion.usar(con -> SqlLote.ejecutar(con, INSERT, new ArrayList<Fila>()));
        assertThat(n).isZero();
        assertThat(eventos).isEmpty();
    }

    @Test
    void elBigDecimalSeLigaConEscala16HalfDown() throws Exception {
        List<Fila> una = new ArrayList<>();
        una.add(new Fila(1, new BigDecimal("0.12345678901234565"), "t"));   // 17 decimals, the last one 5: HALF_DOWN keeps ...56
        una.add(new Fila(2, new BigDecimal("0.12345678901234567"), "t"));
        new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> SqlLote.ejecutar(con, INSERT, una)));
        try (Connection c = ds.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery("SELECT V FROM T_LOTE ORDER BY ID")) {
            rs.next();
            assertThat(rs.getBigDecimal(1).stripTrailingZeros().toPlainString()).isEqualTo("0.1234567890123456");
            rs.next();
            assertThat(rs.getBigDecimal(1).stripTrailingZeros().toPlainString()).isEqualTo("0.1234567890123457");
        }
    }

    @Test
    void unaFilaQueFallaAMitad_esLaExcepcionSaneada_laSesionVeLasDemasFilas_yElRollbackDejaLaTablaComoAntes() throws Exception {
        int[] enSesion = new int[1];
        TransactionTemplate t = new TransactionTemplate(tm);
        Throwable[] error = new Throwable[1];
        t.executeWithoutResult(s -> {
            try {
                conexion.usar(con -> SqlLote.ejecutar(con, INSERT, filas(12, 6)));   // row 6 repeats the primary key of row 1
            } catch (AccesoDatosException e) {
                error[0] = e;
            }
            enSesion[0] = conexion.usar(con -> {
                try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM T_LOTE")) {
                    rs.next();
                    return rs.getInt(1);
                }
            });
            s.setRollbackOnly();   // what the host does with a counter > 0 or an exception
        });
        assertThat(error[0]).isInstanceOf(AccesoDatosException.class);
        assertThat(error[0].getMessage()).startsWith("SQL failure: SQLState=").doesNotContain("T_LOTE").doesNotContain("PRIMARY");
        assertThat(error[0].getCause()).isInstanceOf(java.sql.BatchUpdateException.class);
        // H2 keeps running the rows after the failing one, as the legacy batch: 12 rows, 1 rejected
        assertThat(enSesion[0]).isEqualTo(11);
        assertThat(cuenta()).as("rolled back by the host").isZero();
    }

    @Test
    void ligaTodasLasFilasAntesDeEjecutar_unaFilaConUnParametroDeMas_fallaSinEjecutarNada() {
        List<Map<String, Object>> lote = new ArrayList<>();
        lote.add(Map.of("ID", 1, "V", BigDecimal.ONE, "T", "t"));
        lote.add(Map.of("ID", 2, "V", BigDecimal.ONE, "T", "t", "OTRO", "x"));
        assertThatThrownBy(() -> new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> SqlNombrado.actualizarLote(con, INSERT, lote))))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Parameter not found");
        assertThat(veces("executeBatch")).isZero();
    }
}
