package cl.bcs.tesoreria.jobs.guarda;

import cl.bcs.tesoreria.jobs.programa.EjecutorPrograma;
import cl.bcs.tesoreria.jobs.programa.ProgramaModo;
import cl.bcs.tesoreria.jobs.programa.ProgramaRepositorioJdbc;
import cl.bcs.tesoreria.jobs.programa.ProgramaStatusListener;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.support.ProgramaH2;
import cl.bcs.tesoreria.jobs.web.JobCallController;
import cl.bcs.tesoreria.port.out.Colaboradores;
import com.google.gson.JsonObject;
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
 * Fixture of the step 6 tests: the H2 BAT schema of the H4 pattern (real DDL of BAT.LogPrograma included), the guard around a fake "TomEE" port
 * and a real {@code JobRunner} + {@code EjecutorPrograma} + {@code JobCallController} (MockMvc), so the whole path of
 * {@code POST /internal/job-call} is exercised.
 */
abstract class GuardaBase {

    static final String CLASE = "cl.bcs.tesoreria.Prueba";
    static final String TIPO = "PROCESARDISTRIBUCION";

    final ProgramaH2 db = new ProgramaH2();
    final ProgramaRepositorioJdbc repo = new ProgramaRepositorioJdbc(db.dataSource());
    final MarcasRepositorioJdbc marcas = new MarcasRepositorioJdbc(db.dataSource());
    final ContextoGuarda contexto = new ContextoGuarda();
    final GuardaTomee guarda = new GuardaTomee(marcas, contexto);
    /** Names that reached the fake TomEE, in order. */
    final List<String> tomee = new CopyOnWriteArrayList<>();
    /** What the fake TomEE does for each call (default: answers an empty JSON object). */
    volatile Consumer<String> comportamientoTomee = nombre -> { };
    /** The HTTP port of the module, as the guard sees it. */
    final Colaboradores http = (servicio, entrada) -> {
        tomee.add(servicio);
        comportamientoTomee.accept(servicio);
        return new JsonObject();
    };
    final Colaboradores colaboradores = guarda.envolver(http);

    @AfterEach
    void cerrarBd() {
        db.close();
    }

    void programa(long id) {
        db.tipo(TIPO + id, CLASE, 1).agenda(id).programa(id, TIPO + id, id);
    }

    /** A job that writes a row in its own transaction, calls the collaborators in order, then returns {@code resultado}. */
    TesoreriaJob job(boolean resultado, String... nombres) {
        return new TesoreriaJob() {
            @Override
            public String clase() {
                return CLASE;
            }

            @Override
            public boolean transaction(JobContext ctx) {
                db.jdbc().update("INSERT INTO \"TEST.Marca\" (\"Valor\") VALUES ('trabajo')");
                for (String nombre : nombres) {
                    colaboradores.invocar(nombre, new JsonObject());
                }
                return resultado;
            }
        };
    }

    MockMvc mvc(TesoreriaJob job) {
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
