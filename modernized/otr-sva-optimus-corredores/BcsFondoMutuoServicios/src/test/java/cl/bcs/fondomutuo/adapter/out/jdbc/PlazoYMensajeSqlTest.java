package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.transaction.support.TransactionTemplate;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.config.TransaccionConfig;
import cl.bcs.fondomutuo.kit.AccesoDatosException;

/**
 * Architecture review F3 W1: H2 (the statements of a unit of work carry the REMAINING time of the transaction as query timeout; the release of the lock, which runs even
 * after the deadline, carries none) and M3 (the message of the {@code AccesoDatosException} has SQLState and ErrorCode, never the text of the database exception).
 */
class PlazoYMensajeSqlTest {

    final List<Integer> plazos = new ArrayList<>();
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
            Object r = invocar(real, m, a);
            if (m.getName().equals("prepareStatement") && r instanceof PreparedStatement st) {
                return Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {PreparedStatement.class}, (p2, m2, a2) -> {
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

    private static final String SQL = "SELECT 1 AS X";

    @Test
    void dentroDeLaTransaccionConPlazo_laConsultaLlevaElTiempoRestante() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        t.executeWithoutResult(s -> conexion.usar(con -> SqlNombrado.filas(con, SQL, Map.of())));
        assertThat(plazos).hasSize(1);
        assertThat(plazos.get(0)).isBetween(1, 30);
    }

    @Test
    void tambienLosUpdates_yConsultarPorPojo() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        t.executeWithoutResult(s -> conexion.usar(con -> SqlNombrado.actualizar(con, "UPDATE \"BSCL.Plataforma.BD::REG.Ciudad\" SET \"IdCiudad\" = \"IdCiudad\" WHERE 1 = 0", Map.of())));
        assertThat(plazos).hasSize(1);
    }

    @Test
    void usarAunVencido_noAplicaPlazo_yFuncionaConElPlazoVencido() throws Exception {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(1);
        // the timed-out check marks the transaction rollback-only (Spring), so the commit at the end reports it: expected
        assertThatThrownBy(() -> t.executeWithoutResult(s -> {
            try {
                Thread.sleep(1300);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            assertThatThrownBy(() -> conexion.usar(con -> SqlNombrado.filas(con, SQL, Map.of()))).isInstanceOf(TransactionTimedOutException.class);
            assertThat(plazos).as("an expired deadline fails before the statement").isEmpty();
            conexion.usarAunVencido(con -> SqlNombrado.filas(con, SQL, Map.of()));
            assertThat(plazos).as("the release of the lock must not be cut by the deadline").isEmpty();
        })).isInstanceOf(org.springframework.transaction.UnexpectedRollbackException.class);
    }

    @Test
    void sinTransaccionConPlazo_nadaCambia() {
        conexion.usar(con -> SqlNombrado.filas(con, SQL, Map.of()));
        new TransactionTemplate(tm).executeWithoutResult(s -> conexion.usar(con -> SqlNombrado.filas(con, SQL, Map.of())));   // default timeout: none
        assertThat(plazos).isEmpty();
    }

    @Test
    void elPlazoNoSeFiltraAlSalirDeUsar() {
        TransactionTemplate t = new TransactionTemplate(tm);
        t.setTimeout(30);
        t.executeWithoutResult(s -> {
            conexion.usar(con -> SqlNombrado.filas(con, SQL, Map.of()));
            plazos.clear();
            conexion.usarAunVencido(con -> conexion.usar(con2 -> SqlNombrado.filas(con2, SQL, Map.of())));   // nested: inner applies, outer restores "none"
            assertThat(plazos).hasSize(1);
            plazos.clear();
            conexion.usarAunVencido(con -> SqlNombrado.filas(con, SQL, Map.of()));
            assertThat(plazos).isEmpty();
        });
    }

    @Test
    void m3_elMensajeLlevaSqlStateYErrorCode_noElTextoDeLaBd() {
        SQLException origen = new SQLException("unique constraint violated: value 'SECRETO-12345'", "23505", 301);
        assertThatThrownBy(() -> conexion.usar(con -> {
            throw origen;
        })).isInstanceOf(AccesoDatosException.class).hasMessage("SQL failure: SQLState=23505 ErrorCode=301").hasCause(origen);
    }
}
