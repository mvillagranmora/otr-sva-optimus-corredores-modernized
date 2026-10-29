package cl.bcs.tesoreria.jobs.programa;

import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.support.ProgramaH2;
import cl.bcs.tesoreria.jobs.web.JobCallController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Job pattern H4 against the scheduler P0 rules of BcsJobManager (BUSINESS_RULES.md RULE-055..068) that this module
 * meets through {@code /internal/job-call}. RULE-055, 056 and 057 are pinned by the existing H4 tests (their display
 * names cite them); this class pins how the module stands on the other four.
 */
class ProgramaReglasP0Test {

    private static final String CLASE = "cl.bcs.tesoreria.Prueba";
    private static final String TIPO = "GENERARARCHIVOPAGO";

    private final ProgramaH2 db = new ProgramaH2();
    private final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());
    private final AtomicInteger corridas = new AtomicInteger();

    @AfterEach
    void cerrar() {
        db.close();
    }

    private MockMvc mvc(String fallo) {
        TesoreriaJob job = new TesoreriaJob() {
            @Override
            public String clase() {
                return CLASE;
            }

            @Override
            public boolean transaction(JobContext ctx) {
                corridas.incrementAndGet();
                if (fallo != null) {
                    throw new IllegalStateException(fallo);
                }
                return true;
            }
        };
        JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), new ProgramaStatusListener(repo));
        return MockMvcBuilders.standaloneSetup(new JobCallController(new EjecutorPrograma(runner, repo, ProgramaModo.PROGRAMA))).build();
    }

    private static MockHttpServletResponse llamar(MockMvc mvc, long id) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content("{\"idPrograma\":" + id + "}"))
                .andReturn().getResponse();
    }

    /** The SQL texts of ProgramaSql (package-private constants), read by reflection so a new statement is checked too. */
    private static List<String> sentencias() throws Exception {
        List<String> out = new java.util.ArrayList<>();
        for (Field f : ProgramaSql.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == String.class) {
                f.setAccessible(true);
                out.add((String) f.get(null));
            }
        }
        return out;
    }

    @Test
    @DisplayName("RULE-058 / RULE-059: no automatic retry (legacy parity, updateTryProgramm commented out at BcsJobManager.java:105): a failed program stays ERR, Bloqueo S, Intento 1; a new call cannot re-run it, and no statement of the module puts a program back to PEN")
    void rule058Rule059SinReintentoAutomatico() throws Exception {
        db.tipo(TIPO, CLASE, 3).agenda(600).programa(600, TIPO, 600);

        assertThat(llamar(mvc("boom"), 600).getStatus()).isEqualTo(500);
        assertThat(db.fila(600)).containsEntry("idEstado", "ERR").containsEntry("Bloqueo", "S").containsEntry("Intento", 1);

        assertThat(llamar(mvc(null), 600).getStatus()).isEqualTo(409);
        assertThat(corridas.get()).isEqualTo(1);
        assertThat(db.fila(600)).containsEntry("idEstado", "ERR").containsEntry("Intento", 1);
        for (String sql : sentencias()) {
            String set = sql.contains(" WHERE ") ? sql.substring(0, sql.indexOf(" WHERE ")) : sql;
            assertThat(set).as(sql).doesNotContain("\"idEstado\" = 'PEN'").doesNotContain("\"Bloqueo\" = 'N'");
        }
    }

    @Test
    @DisplayName("RULE-062: a program terminated by a daemon restart (TER, Bloqueo S, 'Proceso abortado/reiniciado', TransactionManager.java:174-192) is never run by job-call: 409 and the row is left as the restart wrote it")
    void rule062ProgramaAbortadoPorReinicioNoCorre() throws Exception {
        db.tipo(TIPO, CLASE, 3).programa(601, TIPO, 0).programa(602, TIPO, 0);
        db.estado(601, "TER", 0, "S").estado(602, "TER", 1, "S");
        db.jdbc().update("UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"Resultado\" = 'Proceso abortado/reiniciado' WHERE \"IdPrograma\" IN (601, 602)");

        for (long id : new long[] {601, 602}) {
            assertThat(llamar(mvc(null), id).getStatus()).isEqualTo(409);
            assertThat(db.fila(id)).containsEntry("idEstado", "TER").containsEntry("Bloqueo", "S")
                    .containsEntry("Resultado", "Proceso abortado/reiniciado");
        }
        assertThat(corridas.get()).isZero();
    }

    @Test
    @DisplayName("RULE-057 / RULE-062: the database claim itself refuses a row that changed after it was read with the same Intento (a restart's TER + Bloqueo S, or a legacy node's Bloqueo S on a PEN row): 0 rows updated and the row is untouched")
    void rule057Rule062ReclamoEnBaseRechazaFilaCambiada() {
        db.tipo(TIPO, CLASE, 3).programa(605, TIPO, 0).programa(606, TIPO, 0);
        db.estado(605, "TER", 0, "S").estado(606, "PEN", 0, "S");

        for (long id : new long[] {605, 606}) {
            int filas = db.jdbc().update(ProgramaSql.RECLAMAR, 1, id, 0);   // the same binds reclamar() sends after reading Intento 0
            assertThat(filas).as("program " + id).isZero();
            assertThat(db.fila(id)).containsEntry("Bloqueo", "S").containsEntry("Intento", 0);
        }
        assertThat(db.fila(605)).containsEntry("idEstado", "TER");
        assertThat(db.fila(606)).containsEntry("idEstado", "PEN");
    }

    @Test
    @DisplayName("RULE-066:the node reboot-on-demand (BAT.Nodos.Reiniciar -> System.exit(0), BcsJobManager.java:183-190) belongs to the dispatcher and is not in this module: no System.exit, Runtime.halt or BAT.Nodos access in src/main")
    void rule066SinReinicioDeNodo() throws Exception {
        try (Stream<Path> fuentes = Files.walk(Path.of("src", "main", "java"))) {
            for (Path p : fuentes.filter(f -> f.toString().endsWith(".java")).toList()) {
                String texto = Files.readString(p, StandardCharsets.UTF_8);
                assertThat(texto).as(p.toString()).doesNotContain("System.exit(").doesNotContain(".halt(").doesNotContain("BAT.Nodos");
            }
        }
    }

    @Test
    @DisplayName("RULE-068: no SQL is built from input (the legacy executionCount concatenated NombreParametro/Valor, TransactionManager.java:526-535): every program statement is a fixed text with ? binds, and a parameter value shaped as an injection is read as plain data")
    void rule068SinSqlConcatenado() throws Exception {
        for (String sql : sentencias()) {
            assertThat(sql).as(sql).doesNotContain("%s").doesNotContain("{").doesNotContain("''");
        }
        String repoFuente = Files.readString(Path.of("src", "main", "java", "cl", "bcs", "tesoreria", "jobs", "programa",
                "ProgramaRepositorioJdbc.java"), StandardCharsets.UTF_8);
        assertThat(repoFuente).doesNotContain("createStatement(").doesNotContainPattern("prepareStatement\\((?!ProgramaSql\\.)");

        db.tipo(TIPO, CLASE, 1).programa(603, TIPO, 0).programa(604, TIPO, 0);
        db.parametro(603, 1, "IdEmpresa", "1' OR '1'='1", null).parametro(604, 2, "IdEmpresa", "2", null);
        assertThat(repo.parametros(603)).containsExactlyEntriesOf(Map.of("IdEmpresa", "1' OR '1'='1"));
        assertThat(repo.parametros(604)).containsExactlyEntriesOf(Map.of("IdEmpresa", "2"));
    }
}
