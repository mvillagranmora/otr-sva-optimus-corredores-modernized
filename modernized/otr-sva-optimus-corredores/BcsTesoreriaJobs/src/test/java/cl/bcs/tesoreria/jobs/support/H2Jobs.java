package cl.bcs.tesoreria.jobs.support;

import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.JobStatusListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.support.DefaultTransactionStatus;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Local H2 stand-in for the HANA connection a legacy {@code BcsJob.run()} opened (BcsJob.java:128-149).
 *
 * <p>Holds the one table the job framework itself writes, {@code "BSCL.Plataforma.BD::BAT.Programa"}
 * ({@code BcsJob.java:171}, {@code UPDATE ... SET "RutaSalida" = :RutaSalida WHERE "IdPrograma" = :IdPrograma}), plus
 * a test-only {@code "TEST.Marca"} table that fakes write to, so that commit and rollback are observed in data, not
 * assumed. Everything is in memory; nothing connects to HANA, QA or desa02.
 */
public final class H2Jobs {

    public static final String TABLA_PROGRAMA = "BSCL.Plataforma.BD::BAT.Programa";

    private final DataSource dataSource;
    private final ContadorTransacciones tm;
    private final JdbcTemplate jdbc;
    private final Estados estados = new Estados();

    private H2Jobs(String modo) {
        this.dataSource = new SimpleDriverDataSource(new org.h2.Driver(),
                "jdbc:h2:mem:jobs_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1" + modo, "sa", "");
        this.tm = new ContadorTransacciones(dataSource);
        this.jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE \"" + TABLA_PROGRAMA + "\" (\"IdPrograma\" BIGINT PRIMARY KEY, \"RutaSalida\" VARCHAR(1000))");
        jdbc.execute("CREATE TABLE \"TEST.Marca\" (\"Orden\" BIGINT AUTO_INCREMENT PRIMARY KEY, \"Valor\" VARCHAR(200))");
    }

    public static H2Jobs nueva() {
        return new H2Jobs("");
    }

    /** H2 in MODE=LEGACY, as the BcsContabilidadServicios envio goldens use (HANA-like NEXTVAL and '' vs NULL). */
    public static H2Jobs nuevaLegacy() {
        return new H2Jobs(";MODE=LEGACY");
    }

    public DataSource dataSource() {
        return dataSource;
    }

    public JdbcTemplate jdbc() {
        return jdbc;
    }

    public ContadorTransacciones transactionManager() {
        return tm;
    }

    public Estados estados() {
        return estados;
    }

    public JobRunner runner(TesoreriaJob... jobs) {
        return new JobRunner(tm, jdbc, List.of(jobs), estados);
    }

    /** Inserts a BAT.Programa row with the given RutaSalida (may be null). */
    public H2Jobs programa(long idPrograma, String rutaSalida) {
        jdbc.update("INSERT INTO \"" + TABLA_PROGRAMA + "\" (\"IdPrograma\", \"RutaSalida\") VALUES (?, ?)", idPrograma, rutaSalida);
        return this;
    }

    public String rutaSalida(long idPrograma) {
        List<String> r = jdbc.queryForList("SELECT \"RutaSalida\" FROM \"" + TABLA_PROGRAMA + "\" WHERE \"IdPrograma\" = ?",
                String.class, idPrograma);
        if (r.isEmpty()) throw new AssertionError("BAT.Programa row " + idPrograma + " missing in the H2 fixture");
        return r.get(0);
    }

    /** Writes a marker row through the same DataSource, so it joins whatever transaction the runner has open. */
    public void marcar(String valor) {
        jdbc.update("INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES (?)", valor);
    }

    public List<String> marcas() {
        return jdbc.queryForList("SELECT \"Valor\" FROM \"TEST.Marca\" ORDER BY \"Orden\"", String.class);
    }

    /** Transaction manager that counts physical commits and rollbacks. */
    public static final class ContadorTransacciones extends DataSourceTransactionManager {
        private final AtomicInteger commits = new AtomicInteger();
        private final AtomicInteger rollbacks = new AtomicInteger();
        private final AtomicInteger inicios = new AtomicInteger();

        ContadorTransacciones(DataSource ds) {
            super(ds);
        }

        @Override
        protected void doBegin(Object transaction, org.springframework.transaction.TransactionDefinition definition) {
            inicios.incrementAndGet();
            super.doBegin(transaction, definition);
        }

        @Override
        protected void doCommit(DefaultTransactionStatus status) {
            commits.incrementAndGet();
            super.doCommit(status);
        }

        @Override
        protected void doRollback(DefaultTransactionStatus status) {
            rollbacks.incrementAndGet();
            super.doRollback(status);
        }

        public int commits() {
            return commits.get();
        }

        public int rollbacks() {
            return rollbacks.get();
        }

        public int inicios() {
            return inicios.get();
        }
    }

    /** Records legacy {@code jobListener.status(jobId, error, mensaje)} calls. */
    public static final class Estados implements JobStatusListener {
        public record Estado(long idPrograma, boolean error, String mensaje) {
        }

        private final List<Estado> llamadas = Collections.synchronizedList(new ArrayList<>());

        @Override
        public void status(long idPrograma, boolean error, String mensaje) {
            llamadas.add(new Estado(idPrograma, error, mensaje));
        }

        public List<Estado> llamadas() {
            synchronized (llamadas) {
                return List.copyOf(llamadas);
            }
        }
    }
}
