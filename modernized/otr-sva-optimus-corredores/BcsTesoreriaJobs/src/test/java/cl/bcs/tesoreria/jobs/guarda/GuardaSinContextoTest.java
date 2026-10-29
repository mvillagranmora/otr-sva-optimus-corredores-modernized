package cl.bcs.tesoreria.jobs.guarda;

import cl.bcs.tesoreria.jobs.programa.EjecutorPrograma;
import cl.bcs.tesoreria.jobs.programa.ProgramaModo;
import cl.bcs.tesoreria.jobs.runtime.TesoreriaJob;
import cl.bcs.tesoreria.jobs.runtime.JobContext;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Step 6b: the scope of the guard is a ThreadLocal, so a guarded collaborator called from ANOTHER thread (a {@code parallelStream} of a library,
 * a {@code CompletableFuture}, a pool) has no run. In mode {@code programa} that must FAIL CLOSED (refused before any HTTP call, no marks: there
 * is no idPrograma to key them); in caller mode, or inside the explicit "no program" scope, it is a pass-through as before; names that are not
 * guarded and threads that never call a guarded name are unaffected.
 */
class GuardaSinContextoTest extends GuardaBase {

    private static final String GUARDADO = "CrearOrdenes";
    private static final String LIBRE = "ConsultarEstadoModulo";
    private static final String MENSAJE = "llamada protegida sin contexto de programa (hilo distinto?)";
    private static final String SQL_LOG = "SELECT COUNT(*) FROM \"BSCL.Plataforma.BD::BAT.LogPrograma\"";

    private final ExecutorService otroHilo = Executors.newSingleThreadExecutor();

    /** One call to the collaborator {@code nombre} through the guard of this fixture. */
    private void invocarGuardado(String nombre) {
        colaboradores.invocar(nombre, new JsonObject());
    }

    /** One call to {@code nombre} through ANOTHER guard (other run context) over the same fake TomEE port. */
    private void invocarCon(GuardaTomee otra, String nombre) {
        otra.envolver(http).invocar(nombre, new JsonObject());
    }


    @AfterEach
    void cerrarHilos() {
        otroHilo.shutdownNow();
    }

    /** What {@code accion} throws on another (plain) thread, or null when it returns normally. */
    private Throwable enOtroHilo(Runnable accion) throws Exception {
        return otroHilo.submit(() -> intentar(accion)).get(10, TimeUnit.SECONDS);
    }

    private static Throwable intentar(Runnable accion) {
        try {
            accion.run();
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    private void sinMarcasNiLlamadas(long idPrograma) {
        assertThat(tomee).isEmpty();
        assertThat(mensajes(idPrograma)).isEmpty();
        assertThat(marcas.estado(idPrograma).limpio()).isTrue();
        assertThat(db.jdbc().queryForObject(SQL_LOG, Integer.class)).isZero();
    }

    @Test
    @DisplayName("G6b-01: mode programa, run open on the job thread: a guarded name from a SECOND thread is refused, no HTTP call, no marks; the job thread keeps working")
    void otroHiloEsRechazado() throws Exception {
        programa(31);
        guarda.abrir(31, 1, TIPO + 31);

        Throwable t = enOtroHilo(() -> invocarGuardado(GUARDADO));

        assertThat(t).isInstanceOf(GuardaTomeeException.class).hasMessageContaining(MENSAJE).hasMessageContaining(GUARDADO);
        sinMarcasNiLlamadas(31);
        invocarGuardado(GUARDADO); // the thread that owns the run is not affected
        assertThat(tomee).containsExactly(GUARDADO);
        assertThat(mensajes(31)).containsExactly("H4G|INTENTO|31|1|" + GUARDADO + "|1", "H4G|HECHO|31|1|" + GUARDADO + "|1");
    }

    @Test
    @DisplayName("G6b-02: a parallelStream running inside a ForkJoinPool (library style): every worker is refused, no HTTP call, no marks")
    void parallelStreamEnForkJoinEsRechazado() throws Exception {
        programa(32);
        guarda.abrir(32, 1, TIPO + 32);
        ForkJoinPool pool = new ForkJoinPool(3);
        try {
            List<Throwable> resultados = pool.submit(() -> IntStream.range(0, 24).parallel().mapToObj(i -> intentar(() -> invocarGuardado(GUARDADO))).toList())
                    .get(20, TimeUnit.SECONDS);

            assertThat(resultados).hasSize(24).allSatisfy(t -> assertThat(t).isInstanceOf(GuardaTomeeException.class).hasMessageContaining(MENSAJE));
        } finally {
            pool.shutdownNow();
        }
        sinMarcasNiLlamadas(32);
    }

    @Test
    @DisplayName("G6b-03: ForkJoinPool.commonPool and CompletableFuture.supplyAsync workers are refused too")
    void commonPoolYCompletableFutureSonRechazados() throws Exception {
        programa(33);
        guarda.abrir(33, 1, TIPO + 33);

        Throwable comun = ForkJoinPool.commonPool().submit(() -> intentar(() -> invocarGuardado(GUARDADO))).get(20, TimeUnit.SECONDS);
        Throwable asincrono = CompletableFuture.supplyAsync(() -> intentar(() -> invocarGuardado(GUARDADO))).get(20, TimeUnit.SECONDS);

        assertThat(comun).isInstanceOf(GuardaTomeeException.class).hasMessageContaining(MENSAJE);
        assertThat(asincrono).isInstanceOf(GuardaTomeeException.class).hasMessageContaining(MENSAJE);
        sinMarcasNiLlamadas(33);
    }

    @Test
    @DisplayName("G6b-04: mode programa, thread that never opened a run (no run active at all): a guarded name is refused as well")
    void sinCorridaActivaTambienSeRechaza() throws Exception {
        assertThat(intentar(() -> invocarGuardado(GUARDADO))).isInstanceOf(GuardaTomeeException.class).hasMessageContaining(MENSAJE);
        assertThat(enOtroHilo(() -> invocarGuardado(GUARDADO))).isInstanceOf(GuardaTomeeException.class);
        assertThat(tomee).isEmpty();
        assertThat(db.jdbc().queryForObject(SQL_LOG, Integer.class)).isZero();
    }

    @Test
    @DisplayName("G6b-05: caller mode (programa.modo=parametros-del-llamador): a guarded name passes through on any thread, no marks")
    void modoLlamadorPasa() throws Exception {
        GuardaTomee llamador = new GuardaTomee(marcas, new ContextoGuarda(false));

        invocarCon(llamador, GUARDADO);
        assertThat(enOtroHilo(() -> invocarCon(llamador, GUARDADO))).isNull();
        Throwable fj = ForkJoinPool.commonPool().submit(() -> intentar(() -> invocarCon(llamador, GUARDADO))).get(20, TimeUnit.SECONDS);

        assertThat(fj).isNull();
        assertThat(tomee).containsExactly(GUARDADO, GUARDADO, GUARDADO);
        assertThat(db.jdbc().queryForObject(SQL_LOG, Integer.class)).isZero();
    }

    @Test
    @DisplayName("G6b-06: the explicit 'no program' scope passes through on its own thread (no marks); a thread with no scope is still refused; closing it restores the refusal")
    void alcanceSinProgramaExplicito() throws Exception {
        guarda.abrirSinPrograma();
        assertThat(contexto.sinPrograma()).isTrue();

        invocarGuardado(GUARDADO);
        Throwable otro = enOtroHilo(() -> invocarGuardado(GUARDADO));

        assertThat(tomee).containsExactly(GUARDADO);
        assertThat(otro).isInstanceOf(GuardaTomeeException.class);
        assertThat(db.jdbc().queryForObject(SQL_LOG, Integer.class)).isZero();
        guarda.cerrar();
        assertThat(contexto.sinPrograma()).isFalse();
        assertThat(intentar(() -> invocarGuardado(GUARDADO))).isInstanceOf(GuardaTomeeException.class);
        assertThat(tomee).hasSize(1);
    }

    @Test
    @DisplayName("G6b-07: names that are not guarded still work from other threads, with a run open or not (reads, AgregarLog-like, unknown)")
    void noGuardadosSiguenFuncionando() throws Exception {
        invocarGuardado(LIBRE); // no scope at all on this thread
        assertThat(enOtroHilo(() -> invocarGuardado(LIBRE))).isNull();
        programa(35);
        guarda.abrir(35, 1, TIPO + 35);
        assertThat(enOtroHilo(() -> invocarGuardado(LIBRE))).isNull();
        assertThat(ForkJoinPool.commonPool().submit(() -> intentar(() -> invocarGuardado(LIBRE))).get(20, TimeUnit.SECONDS)).isNull();
        assertThat(CompletableFuture.supplyAsync(() -> intentar(() -> invocarGuardado("Desconocido"))).get(20, TimeUnit.SECONDS)).isNull();

        assertThat(tomee).containsExactly(LIBRE, LIBRE, LIBRE, LIBRE, "Desconocido");
        assertThat(mensajes(35)).isEmpty();
    }

    @Test
    @DisplayName("G6b-08: EjecutorPrograma in caller mode opens the explicit 'no program' scope for the run and always closes it")
    void ejecutorEnModoLlamadorAbreYCierra() {
        ContextoGuarda ctx = new ContextoGuarda(false);
        GuardaTomee llamador = new GuardaTomee(marcas, ctx);
        List<Boolean> vistoEnElJob = new ArrayList<>();
        AtomicBoolean pasoElLlamado = new AtomicBoolean();
        TesoreriaJob job = new TesoreriaJob() {
            @Override
            public String clase() {
                return CLASE;
            }

            @Override
            public boolean transaction(JobContext jc) {
                vistoEnElJob.add(ctx.sinPrograma());
                invocarCon(llamador, GUARDADO);
                pasoElLlamado.set(true);
                return true;
            }
        };
        JobRunner runner = new JobRunner(db.transacciones(), db.jdbc(), List.of(job), (id, error, mensaje) -> { });
        EjecutorPrograma ejecutor = new EjecutorPrograma(runner, null, ProgramaModo.PARAMETROS_DEL_LLAMADOR, llamador);

        ejecutor.ejecutar(1L, CLASE, Map.of());

        assertThat(vistoEnElJob).containsExactly(true);
        assertThat(pasoElLlamado).isTrue();
        assertThat(tomee).containsExactly(GUARDADO);
        assertThat(ctx.sinPrograma()).as("scope closed after the run").isFalse();
        assertThat(db.jdbc().queryForObject(SQL_LOG, Integer.class)).isZero();
    }
}
