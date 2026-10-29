package cl.bcs.fondomutuo.custodia;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.custodia.flujo.h2.FlujoCaso;
import cl.bcs.custodia.flujo.h2.HanaH2;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.cierre3b.SecuenciaH2;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * H2 database of one custody case with the REAL host plumbing of FondoMutuo: a {@link DataSource} that hands out real, distinct connections (seen
 * through the H2 dialect bridge of the custody harness) and records every {@code getConnection}, a {@link DataSourceTransactionManager} (as
 * {@code config.TransaccionConfig}: {@code globalRollbackOnParticipationFailure = false}), the module's {@link ConexionUnidadDeTrabajo}, the sequences read
 * from the H2 sequence of the same name ({@link SecuenciaH2}, on the connection of the unit of work) and the in-process adapter
 * {@link FlujoCustodiaEnProceso} over the library. {@link #eventos} records what the connection of the transaction received ({@code commit},
 * {@code rollback}, {@code setAutoCommit}, {@code close}...), so a test can prove what the library did NOT do.
 */
final class EntornoCustodia implements AutoCloseable {

    static final String RECURSOS = "/fondomutuo-custodia-en-proceso/";
    private static final AtomicInteger DB = new AtomicInteger();

    final String url = "jdbc:h2:mem:custodiaenproceso" + DB.incrementAndGet() + ";MODE=LEGACY;DB_CLOSE_DELAY=-1";
    /** keeper of the database and seed connection; also the "other session" that sees only committed data */
    final Connection otraSesion;
    final AtomicInteger conexionesPedidas = new AtomicInteger();
    final List<String> eventos = Collections.synchronizedList(new ArrayList<>());
    final JsonArray secuenciasSolicitadas = new JsonArray();
    final DataSource dataSource;
    final TransactionTemplate tx;
    final ConexionUnidadDeTrabajo conexion;
    final SecuenciaPort secuencias;

    EntornoCustodia(Path caso) throws Exception {
        otraSesion = DriverManager.getConnection(url, "sa", "");
        Path base = recursos();
        for (String f : new String[] { "schema-flujo.sql", "schema-session.sql", "seed/base.sql" }) FlujoCaso.runScript(otraSesion, base.resolve(f));
        if (caso != null && java.nio.file.Files.exists(caso.resolve("seed.sql"))) FlujoCaso.runScript(otraSesion, caso.resolve("seed.sql"));
        otraSesion.commit();

        JdbcDataSource real = new JdbcDataSource();
        real.setURL(url);
        real.setUser("sa");
        real.setPassword("");
        dataSource = new AbstractDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                conexionesPedidas.incrementAndGet();
                return espiada(HanaH2.wrap(real.getConnection()));
            }

            @Override
            public Connection getConnection(String u, String p) throws SQLException {
                return getConnection();
            }
        };
        DataSourceTransactionManager tm = new DataSourceTransactionManager(dataSource);
        tm.setGlobalRollbackOnParticipationFailure(false);
        tx = new TransactionTemplate(tm);
        conexion = new ConexionUnidadDeTrabajo(dataSource);
        secuencias = (nombre, cantidad) -> conexion.usar(con -> new SecuenciaH2(con, secuenciasSolicitadas).siguientes(nombre, cantidad));
    }

    static Path recursos() {
        URL u = EntornoCustodia.class.getResource(RECURSOS + "schema-flujo.sql");
        if (u == null) throw new AssertionError("test resources fondomutuo-custodia-en-proceso/ missing");
        try {
            return Path.of(u.toURI()).getParent();
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private Connection espiada(Connection c) {
        return (Connection) Proxy.newProxyInstance(EntornoCustodia.class.getClassLoader(), new Class<?>[] { Connection.class }, (p, m, a) -> {
            String n = m.getName();
            if (n.equals("commit") || n.equals("rollback") || n.equals("setAutoCommit") || n.equals("close") || n.equals("setSavepoint") || n.equals("releaseSavepoint")) {
                eventos.add(n);
            }
            try {
                return m.invoke(c, a);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    ConsultarFlujoCustodiaFlujo biblioteca() {
        return new ConsultarFlujoCustodiaFlujo(FlujoCustodiaEnProceso.valorizadorCerrado(), secuencias::siguientes);
    }

    FlujoCustodiaEnProceso adaptador() {
        return new FlujoCustodiaEnProceso(conexion, biblioteca());
    }

    /** rows of the audit table of the work table (every insert and delete of CUS.FlujoCustodia_MatrizUso, ever committed), seen from ANOTHER session */
    int auditoriaVisibleDesdeOtraSesion() {
        return contar("ORACLE.MatrizUsoAuditoria");
    }

    int matrizUsoVisibleDesdeOtraSesion() {
        return contar(FlujoCaso.MATRIZ);
    }

    private int contar(String tabla) {
        try (Statement st = otraSesion.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM \"" + tabla + "\"")) {
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception {
        cl.bcs.fondomutuo.BdEfimeraH2.cerrar(otraSesion);
    }

    static JsonObject entrada(Path caso) {
        try {
            return FlujoCaso.leer(caso.resolve("case.json")).getAsJsonObject("entrada").deepCopy();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
