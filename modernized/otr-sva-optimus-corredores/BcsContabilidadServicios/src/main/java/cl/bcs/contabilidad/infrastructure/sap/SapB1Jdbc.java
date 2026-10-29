package cl.bcs.contabilidad.infrastructure.sap;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;
import java.util.function.Supplier;

/**
 * The SAP Business One database connection, shared by the daily interface, the chart of accounts read used by
 * envío, and HomologarCuentasContables. Legacy: {@code PlataformaUtils.getDESAPEB1Datasource()}, a
 * platform-managed pool.
 *
 * <p>The pool is created on first use from {@link SapB1DatabaseSettings} ({@code SAP_B1_DB_*}), so the application
 * starts without them and only the SAP step fails when they are missing. This class is deliberately not a
 * {@code DataSource} bean, so Spring Boot keeps auto-configuring the HANA one. The pool is closed on shutdown.
 */
public final class SapB1Jdbc implements Supplier<NamedParameterJdbcTemplate>, DisposableBean {

    private final Supplier<Map<String, String>> entorno;
    private volatile HikariDataSource pool;
    private volatile NamedParameterJdbcTemplate jdbc;

    public SapB1Jdbc(Supplier<Map<String, String>> entorno) {
        this.entorno = entorno;
    }

    @Override
    public NamedParameterJdbcTemplate get() {
        NamedParameterJdbcTemplate actual = jdbc;
        if (actual == null) {
            synchronized (this) {
                if (jdbc == null) {
                    SapB1DatabaseSettings s = SapB1DatabaseSettings.fromEnvironment(entorno.get());
                    HikariConfig config = new HikariConfig();
                    config.setPoolName("sap-b1");
                    config.setJdbcUrl(s.url());
                    config.setUsername(s.username());
                    config.setPassword(s.password());
                    config.setMaximumPoolSize(4);
                    config.setConnectionTimeout(10_000);
                    config.setInitializationFailTimeout(-1);
                    pool = new HikariDataSource(config);
                    jdbc = new NamedParameterJdbcTemplate(pool);
                }
                actual = jdbc;
            }
        }
        return actual;
    }

    @Override
    public void destroy() {
        HikariDataSource p = pool;
        if (p != null) p.close();
    }
}
