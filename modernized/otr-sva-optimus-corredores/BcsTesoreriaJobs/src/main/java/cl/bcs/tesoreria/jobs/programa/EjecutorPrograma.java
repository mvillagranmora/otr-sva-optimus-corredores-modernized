package cl.bcs.tesoreria.jobs.programa;

import cl.bcs.tesoreria.jobs.guarda.GuardaTomee;
import cl.bcs.tesoreria.jobs.guarda.GuardaTomeeException;
import cl.bcs.tesoreria.jobs.runtime.JobNoMigradoException;
import cl.bcs.tesoreria.jobs.runtime.JobResultado;
import cl.bcs.tesoreria.jobs.runtime.JobRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Entry of {@code POST /internal/job-call} (job pattern H4): decides WHAT runs and under which parameters, then hands over to
 * {@link JobRunner}.
 *
 * <p>Mode {@link ProgramaModo#PROGRAMA} (production): the legacy Job Manager read class and parameters from the program row, so
 * whoever triggers the run does not choose them. Steps, in this order, each failing without starting anything:
 * <ol>
 *   <li>the row must exist in {@code BAT.Programa} (404) and its type must name a class (422);</li>
 *   <li>a {@code clase} sent by the caller must equal the class of the row (422);</li>
 *   <li>the class must be one this module runs (503, as before);</li>
 *   <li>the parameters come from the view (ValorBlob included); a non-empty {@code parametros} sent by the caller must equal them (422);</li>
 *   <li>per-JVM guard (409), then the atomic claim in the database (409 if already claimed), then the job runs.</li>
 * </ol>
 * Mode {@link ProgramaModo#PARAMETROS_DEL_LLAMADOR}: the caller's class and parameters are used as they are; nothing is read or claimed.
 *
 * <p>Step 6 (only with a {@link GuardaTomee}, mode {@code programa}): right after the claim and before the job transaction the marks of the
 * program are checked ({@link ProgramaRequiereRevisionException} when a re-run would repeat TomEE writes) and the guard scope of the run opens;
 * after the run a non-commit with TomEE calls leaves the program in "requires review". Without it nothing here changes.
 */
public class EjecutorPrograma {

    private static final Logger log = LoggerFactory.getLogger(EjecutorPrograma.class);

    private final JobRunner runner;
    private final ProgramaRepositorio repositorio;
    private final ProgramaModo modo;
    /** Step 6: null = guard off (or caller mode): the executor behaves exactly as before. */
    private final GuardaTomee guarda;

    public EjecutorPrograma(JobRunner runner, ProgramaRepositorio repositorio, ProgramaModo modo) {
        this(runner, repositorio, modo, null);
    }

    public EjecutorPrograma(JobRunner runner, ProgramaRepositorio repositorio, ProgramaModo modo, GuardaTomee guarda) {
        this.guarda = guarda;
        this.runner = Objects.requireNonNull(runner, "runner");
        this.modo = Objects.requireNonNull(modo, "modo");
        this.repositorio = modo == ProgramaModo.PROGRAMA ? Objects.requireNonNull(repositorio, "repositorio") : repositorio;
    }

    /** The contract of the golden and runner tests: caller class and parameters, no database access. */
    public static EjecutorPrograma parametrosDelLlamador(JobRunner runner) {
        return new EjecutorPrograma(runner, null, ProgramaModo.PARAMETROS_DEL_LLAMADOR);
    }

    public ProgramaModo modo() {
        return modo;
    }

    /** True when the request body must carry {@code clase} (only in caller mode). */
    public boolean exigeClase() {
        return modo == ProgramaModo.PARAMETROS_DEL_LLAMADOR;
    }

    public JobResultado ejecutar(long idPrograma, String claseDeclarada, Map<String, String> parametrosDeclarados) {
        if (modo == ProgramaModo.PARAMETROS_DEL_LLAMADOR) {
            if (guarda == null) {
                return runner.ejecutar(claseDeclarada, idPrograma, parametrosDeclarados);
            }
            guarda.abrirSinPrograma(); // step 6b: caller mode is an explicit scope, so a thread with no scope is an anomaly only in mode programa
            try {
                return runner.ejecutar(claseDeclarada, idPrograma, parametrosDeclarados);
            } finally {
                guarda.cerrar();
            }
        }
        Programa programa = repositorio.cargarPrograma(idPrograma).orElseThrow(() -> new ProgramaNoEncontradoException(idPrograma));
        if (programa.nombreClase() == null || programa.nombreClase().isBlank()) {
            throw new ProgramaIncoherenteException("Program #" + idPrograma + " has no class in BAT.TipoPrograma");
        }
        String clase = programa.nombreClase();
        if (claseDeclarada != null && !claseDeclarada.isBlank() && !claseDeclarada.equals(clase)) {
            throw new ProgramaIncoherenteException("'clase' does not match the class of program #" + idPrograma);
        }
        if (!runner.soporta(clase)) {
            throw new JobNoMigradoException(clase);
        }
        Map<String, String> parametros = repositorio.parametros(idPrograma);
        if (parametrosDeclarados != null && !parametrosDeclarados.isEmpty() && !parametrosDeclarados.equals(parametros)) {
            throw new ProgramaIncoherenteException("'parametros' do not match the parameters of program #" + idPrograma);
        }
        try {
            JobResultado resultado = runner.ejecutarReclamando(clase, idPrograma, () -> {
                if (!repositorio.reclamar(idPrograma)) {
                    throw new ProgramaNoReclamableException(idPrograma, estadoActual(idPrograma));
                }
                if (guarda != null) {
                    abrirGuarda(idPrograma, programa);
                }
                return new HashMap<>(parametros);
            });
            return guarda == null ? resultado : revisarDespues(resultado);
        } finally {
            if (guarda != null) {
                guarda.cerrar();
            }
        }
    }

    /**
     * Step 6 (after the claim, before the job transaction): marks of the SAME idPrograma with a HECHO or an INTENTO without close mean TomEE
     * writes that a re-run would repeat: the program goes to "requires review" and does not run. Otherwise the guard scope of the run opens.
     */
    private void abrirGuarda(long idPrograma, Programa antes) {
        Optional<String> motivo;
        try {
            motivo = guarda.motivoPrevio(idPrograma);
        } catch (GuardaTomeeException e) { // the marks cannot be read: the earlier outcome is unknown, so fail closed
            log.error("Program #{}: the guard marks could not be read", idPrograma, e);
            motivo = Optional.of("the guard marks of this program could not be read (database error); earlier TomEE writes cannot be ruled out.");
        }
        if (motivo.isPresent()) {
            log.error("Program #{} NOT run, set to requires-review: {}", idPrograma, motivo.get());
            repositorio.requiereRevision(idPrograma, motivo.get());
            throw new ProgramaRequiereRevisionException(idPrograma, motivo.get());
        }
        int intento = repositorio.cargarPrograma(idPrograma).map(Programa::intento).orElse(antes.intento() + 1);
        guarda.abrir(idPrograma, intento, antes.idTipoPrograma());
    }

    /**
     * Step 6 (after the run): a run that did not commit after TomEE calls were answered or failed is left as "requires review", over the
     * TER / ERR the listener wrote. A failure writing it is logged; the marks still keep a re-run out.
     */
    private JobResultado revisarDespues(JobResultado resultado) {
        Optional<String> motivo = guarda.motivoPosterior(resultado);
        if (motivo.isPresent()) {
            log.error("Program #{} set to requires-review after its run: {}", resultado.idPrograma(), motivo.get());
            try {
                repositorio.requiereRevision(resultado.idPrograma(), motivo.get());
            } catch (RuntimeException e) {
                log.error("Program #{}: could not write the requires-review state; the guard marks still refuse a re-run", resultado.idPrograma(), e);
            }
        }
        return resultado;
    }

    private String estadoActual(long idPrograma) {
        try {
            return repositorio.cargarPrograma(idPrograma).map(Programa::resumen).orElse("row not found");
        } catch (RuntimeException e) {
            return "state unknown";
        }
    }
}
