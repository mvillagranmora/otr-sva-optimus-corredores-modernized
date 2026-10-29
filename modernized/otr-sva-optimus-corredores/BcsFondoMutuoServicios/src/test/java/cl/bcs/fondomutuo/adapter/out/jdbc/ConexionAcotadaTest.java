package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionTemplate;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.config.TransaccionConfig;

/**
 * Review of 2026-10-28 (FFMM and custody in process), H2: the connection that a library receives inside the unit of work ({@link ConexionAcotada}) (a) refuses what only the
 * host may do (commit, rollback, close, session settings) and (b) gives every statement it creates the remaining time of the host transaction; the cleanup DELETE of the
 * library is never cut by an expired deadline.
 */
class ConexionAcotadaTest {

    static final String BORRADO = "DELETE FROM \"BSCL.Plataforma.BD::REG.Ciudad\" WHERE 1 = 0";

    final List<Integer> plazos = new ArrayList<>();
    final List<String> invocadas = new ArrayList<>();
    DataSource ds;
    PlatformTransactionManager tm;
    ConexionUnidadDeTrabajo conexion;

    @BeforeEach
    void preparar() {
        DataSource h2 = TestBd.nueva();
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
        return (Connection) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Connection.class}, (p, m, a) -> {
            invocadas.add(m.getName());
            Object r = invocar(real, m, a);
            if (r instanceof PreparedStatement st) {
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {PreparedStatement.class}, (p2, m2, a2) -> {
                    if (m2.getName().equals("setQueryTimeout")) plazos.add((Integer) a2[0]);
                    return invocar(st, m2, a2);
                });
            }
            if (r instanceof Statement st) {
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {Statement.class}, (p2, m2, a2) -> {
                    if (m2.getName().equals("setQueryTimeout")) plazos.add((Integer) a2[0]);
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

    @FunctionalInterface
    interface Llamada {
        void con(Connection c) throws Exception;
    }

    record Caso(String nombre, Llamada llamada) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    static List<Caso> prohibidos() {
        return List.of(new Caso("commit", Connection::commit), new Caso("rollback", Connection::rollback), new Caso("rollback(Savepoint)", c -> c.rollback(null)),
                new Caso("setAutoCommit", c -> c.setAutoCommit(false)), new Caso("close", Connection::close), new Caso("setReadOnly", c -> c.setReadOnly(true)),
                new Caso("setTransactionIsolation", c -> c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE)), new Caso("abort", c -> c.abort(Runnable::run)),
                new Caso("releaseSavepoint", c -> c.releaseSavepoint(null)), new Caso("setSavepoint", Connection::setSavepoint),
                new Caso("setSavepoint(name)", c -> c.setSavepoint("x")), new Caso("setHoldability", c -> c.setHoldability(1)),
                new Caso("setCatalog", c -> c.setCatalog("x")), new Caso("setSchema", c -> c.setSchema("x")), new Caso("unwrap", c -> c.unwrap(Connection.class)));
    }

    @ParameterizedTest
    @MethodSource("prohibidos")
    void rechazaLoQueSoloElAnfitrionPuedeHacer(Caso caso) {
        new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> {
            Connection acotada = ConexionAcotada.de(con);
            invocadas.clear();
            assertThatThrownBy(() -> caso.llamada().con(acotada)).isInstanceOf(IllegalStateException.class).hasMessageContaining("host owns the transaction");
            assertThat(invocadas).as("the call never reaches the real connection").isEmpty();
            return null;
        }));
    }

    @Test
    void loDemasSeDelega() {
        new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> {
            Connection acotada = ConexionAcotada.de(con);
            assertThat(acotada.isClosed()).isFalse();
            assertThat(acotada.getAutoCommit()).isFalse();   // the transaction of the host
            List<Map<String, Object>> filas = SqlNombrado.filas(acotada, "SELECT 1 AS X", Map.of());
            assertThat(filas).hasSize(1);
            return null;
        }));
    }

    @Test
    void aplicaElPlazoRestanteDeLaTransaccionACadaSentencia() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        t.executeWithoutResult(s -> conexion.usar(con -> {
            Connection acotada = ConexionAcotada.de(con);
            acotada.prepareStatement("SELECT 1").close();
            acotada.prepareStatement("SELECT 1", Statement.NO_GENERATED_KEYS).close();
            acotada.createStatement().close();
            return null;
        }));
        assertThat(plazos).hasSize(3).allSatisfy(p -> assertThat(p).isBetween(1, 30));
    }

    @Test
    void sinPlazoNoAplicaNada() {
        new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> {   // default timeout: none
            ConexionAcotada.de(con).prepareStatement("SELECT 1").close();
            return null;
        }));
        conexion.usar(con -> {   // no transaction
            ConexionAcotada.de(con).prepareStatement("SELECT 1").close();
            return null;
        });
        conexion.usarAunVencido(con -> {
            ConexionAcotada.de(con).prepareStatement("SELECT 1").close();
            return null;
        });
        assertThat(plazos).isEmpty();
    }

    @Test
    void conElPlazoAgotado_unaConsultaFalla_peroLaLimpiezaDeLaLibreriaSobrevive() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(1);
        AtomicReference<Throwable> consulta = new AtomicReference<>();
        // the timed-out check marks the transaction rollback-only (Spring): the commit at the end reports it, expected
        assertThatThrownBy(() -> t.executeWithoutResult(s -> conexion.usar(con -> {
            Connection acotada = ConexionAcotada.de(con);
            try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            try {
                acotada.prepareStatement("SELECT 1");
            } catch (Throwable e) {
                consulta.set(e);
            }
            assertThat(plazos).as("a statement created after the deadline never got a timeout nor ran").isEmpty();
            // the cleanup of the work rows of the library (CUS-FLU-14): not cut by the expired deadline, minimum 1 second
            try (var st = acotada.prepareStatement("  delete" + BORRADO.substring(6))) {
                assertThat(st.executeUpdate()).isZero();
            }
            assertThat(plazos).containsExactly(1);
            return null;
        }))).isInstanceOf(org.springframework.transaction.UnexpectedRollbackException.class);
        assertThat(consulta.get()).isInstanceOf(TransactionTimedOutException.class);
    }

    @Test
    void laLimpiezaConTiempoRestanteLlevaElTiempoRestante() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        t.executeWithoutResult(s -> conexion.usar(con -> {
            try (var st = ConexionAcotada.de(con).prepareStatement(BORRADO)) {
                st.executeUpdate();
            }
            return null;
        }));
        assertThat(plazos).hasSize(1);
        assertThat(plazos.get(0)).isBetween(2, 30);
    }
}
