package cl.bcs.contabilidad.jobs.guarda;

import cl.bcs.contabilidad.domain.port.out.ProcesoBatchPort;
import cl.bcs.contabilidad.jobs.runtime.JobResultado;

import java.util.Objects;
import java.util.Optional;

/**
 * The run-level half of the guard, used by {@code EjecutorPrograma} (the decorator {@link ColaboradorTomEEGuardado} is the call-level half):
 * the check BEFORE a run, the run scope, and the verdict AFTER it. Texts become the {@code Resultado} of the program after the prefix
 * {@code REQUIERE REVISION: } and are cut to the column size by the repository.
 */
public class GuardaTomee {

    private static final int MAX_ITEMS = 8;

    private final MarcasRepositorio marcas;
    private final ContextoGuarda contexto;

    public GuardaTomee(MarcasRepositorio marcas, ContextoGuarda contexto) {
        this.marcas = Objects.requireNonNull(marcas, "marcas");
        this.contexto = Objects.requireNonNull(contexto, "contexto");
    }

    public ContextoGuarda contexto() {
        return contexto;
    }

    /** The call-level half: the guard that shares this guard's marks and run scope. */
    public ColaboradorTomEEGuardado llamadas() {
        return new ColaboradorTomEEGuardado(marcas, contexto);
    }

    /** {@code http} (the HTTP adapter of the batch program registration) wrapped in the guard. */
    public ProcesoBatchPort envolver(ProcesoBatchPort http) {
        return new ProcesoBatchGuardado(http, llamadas());
    }

    /**
     * Before the run (after the claim, before the job transaction): the reason to refuse it, or empty when the program has no HECHO, no INTENTO
     * without close and no unreadable mark in any earlier attempt. The marks of the same {@code idPrograma} only: other programs never matter.
     */
    public Optional<String> motivoPrevio(long idPrograma) {
        EstadoMarcas e = marcas.estado(idPrograma);
        if (e.limpio()) {
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder("not re-run: an earlier run of this program called TomEE collaborators (guard marks H4G in LogPrograma). ");
        if (!e.sinCierre().isEmpty()) {
            sb.append("Outcome UNKNOWN (the run died after the call started): ").append(EstadoMarcas.describir(e.sinCierre(), MAX_ITEMS)).append(". ");
        }
        if (!e.hechas().isEmpty()) {
            sb.append("Already COMMITTED in TomEE: ").append(EstadoMarcas.describir(e.hechas(), MAX_ITEMS)).append(". ");
        }
        if (e.ilegibles() > 0) {
            sb.append(e.ilegibles()).append(" unreadable mark(s). ");
        }
        return Optional.of(sb.append("Verify in TomEE; a re-run would repeat the writes. Do not reset this row; use ReprocesarPrograma only after a human decision.").toString());
    }

    /** Starts the guard scope of the run on this thread. */
    public void abrir(long idPrograma, int intento, String idTipoPrograma) {
        contexto.abrir(new CorridaGuardada(idPrograma, intento, idTipoPrograma));
    }

    /** Caller mode: the explicit "no program" scope of the run on this thread (guarded calls pass through). Closed with {@link #cerrar()}. */
    public void abrirSinPrograma() {
        contexto.abrirSinPrograma();
    }

    /** Ends the scope. Always called, also when the claim or the run failed. */
    public void cerrar() {
        contexto.cerrar();
    }

    /**
     * After the run: the reason to put the program in review, or empty. A run that did NOT commit (rollback or error) but whose guarded calls
     * were answered (committed in TomEE) or failed (outcome not certain: a timeout or a 5xx can follow a commit) leaves TomEE and the job out of
     * step: it cannot be left as a plain TER/ERR. A COMMIT is the normal end.
     */
    public Optional<String> motivoPosterior(JobResultado resultado) {
        Optional<CorridaGuardada> corrida = contexto.actual();
        if (corrida.isEmpty() || resultado.desenlace() == JobResultado.Desenlace.COMMIT) {
            return Optional.empty();
        }
        var hechas = corrida.get().hechas();
        var falladas = corrida.get().falladas();
        if (hechas.isEmpty() && falladas.isEmpty()) {
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder("the run ended in " + resultado.desenlace() + " (its own writes were rolled back) after TomEE calls. ");
        if (!hechas.isEmpty()) {
            sb.append("COMMITTED in TomEE: ").append(EstadoMarcas.describir(hechas, MAX_ITEMS)).append(". ");
        }
        if (!falladas.isEmpty()) {
            sb.append("Failed, outcome NOT certain: ").append(EstadoMarcas.describir(falladas, MAX_ITEMS)).append(". ");
        }
        return Optional.of(sb.append("Do not re-run; compare TomEE with the job output.").toString());
    }
}
