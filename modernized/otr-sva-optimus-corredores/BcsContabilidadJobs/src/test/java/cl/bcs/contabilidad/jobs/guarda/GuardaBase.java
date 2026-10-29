package cl.bcs.contabilidad.jobs.guarda;

import cl.bcs.contabilidad.jobs.programa.EjecutorPrograma;
import cl.bcs.contabilidad.jobs.programa.ProgramaModo;
import cl.bcs.contabilidad.jobs.programa.ProgramaRepositorioJdbc;
import cl.bcs.contabilidad.jobs.programa.ProgramaStatusListener;
import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.jobs.runtime.JobRunner;
import cl.bcs.contabilidad.jobs.support.ProgramaH2;
import cl.bcs.contabilidad.jobs.web.JobCallController;
import org.junit.jupiter.api.AfterEach;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Fixture of the step 6 tests: the H2 BAT schema of the H4 pattern (real DDL of BAT.LogPrograma included), the guard around a fake "TomEE" call
 * and a real {@code JobRunner} + {@code EjecutorPrograma} + {@code JobCallController} (MockMvc), so the whole path of
 * {@code POST /internal/job-call} is exercised. Difference from the Tesoreria fixture: this module has no name-based collaborator port, so the
 * fake TomEE is a function of the collaborator name handed to {@link ColaboradorTomEEGuardado#invocar(String, java.util.function.Supplier)}.
 */
abstract class GuardaBase {

    static final String CLASE = "cl.bcs.contabilidad.Prueba";
    static final String TIPO = "INTERFAZCONTABLE";

    final ProgramaH2 db = new ProgramaH2();
    final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());
    final MarcasRepositorioJdbc marcas = new MarcasRepositorioJdbc(db.dataSource());
    final ContextoGuarda contexto = new ContextoGuarda();
    final GuardaTomee guarda = new GuardaTomee(marcas, contexto);
    final ColaboradorTomEEGuardado llamadas = guarda.llamadas();
    /** Names that reached the fake TomEE, in order. */
    final List<String> tomee = new CopyOnWriteArrayList<>();
    /** What the fake TomEE does for each call (default: answers). */
    volatile Consumer<String> comportamientoTomee = nombre -> { };

    @AfterEach
    void cerrarBd() {
        db.close();
    }

    /** One call to the collaborator {@code nombre} through the guard, as the HTTP adapters of the ports do. */
    String invocar(String nombre) {
        return llamadas.invocar(nombre, () -> {
            tomee.add(nombre);
            comportamientoTomee.accept(nombre);
            return nombre;
        });
    }

    void programa(long id) {
        db.tipo(TIPO + id, CLASE, 1).agenda(id).programa(id, TIPO + id, id);
    }

    /** A job that writes a row in its own transaction, calls the collaborators in order, then returns {@code resultado}. */
    ContabilidadJob job(boolean resultado, String... nombres) {
        return new ContabilidadJob() {
            @Override
            public String clase() {
                return CLASE;
            }

            @Override
            public boolean transaction(JobContext ctx) {
                db.jdbc().update("INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES ('trabajo')");
                for (String nombre : nombres) {
                    invocar(nombre);
                }
                return resultado;
            }
        };
    }

    MockMvc mvc(ContabilidadJob job) {
        JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), new ProgramaStatusListener(repo));
        EjecutorPrograma ejecutor = new EjecutorPrograma(runner, repo, ProgramaModo.PROGRAMA, guarda);
        return MockMvcBuilders.standaloneSetup(new JobCallController(ejecutor)).build();
    }

    MockHttpServletResponse llamar(MockMvc mvc, long idPrograma) throws Exception {
        return mvc.perform(post("/internal/job-call").contentType(MediaType.APPLICATION_JSON).content("{\"idPrograma\":" + idPrograma + "}"))
                .andReturn().getResponse();
    }

    /** The {@code Mensaje} of every log row of the program, in insertion order. */
    List<String> mensajes(long idPrograma) {
        return db.jdbc().queryForList("SELECT \"Mensaje\" FROM \"BSCL.Plataforma.BD::BAT.LogPrograma\" WHERE \"IdPrograma\" = ?", String.class, idPrograma);
    }

    /** A run of the program already left this mark (what a dead run or an earlier run leaves). */
    void marca(MarcaGuarda.Fase fase, long idPrograma, int intento, String clase, int n) {
        marcas.registrar(new MarcaGuarda(fase, idPrograma, intento, clase, n), TIPO + idPrograma);
    }
}
