package cl.bcs.cuentainversion.jobs.guarda;

import java.util.Optional;

/**
 * Holder of the guard scope of the run in progress on the current thread. {@code EjecutorPrograma} opens it (always closing it in a
 * {@code finally}); the decorator {@link ColaboradorTomEEGuardado} reads it. The scope on a thread is one of:
 * <ul>
 *   <li>a {@link CorridaGuardada} ({@link #abrir}): a run with an {@code idPrograma}; guarded calls leave marks;</li>
 *   <li>the explicit "no program" marker ({@link #abrirSinPrograma}): caller mode, intentionally unguarded; guarded calls pass through;</li>
 *   <li>nothing: the thread never opened a run. In mode {@code programa} ({@link #exigePrograma()}) a GUARDED call from such a thread is an
 *       anomaly (typically a {@code parallelStream} or another thread started by a library) and the decorator REFUSES it (fail closed); in caller
 *       mode there is nothing to guard and it passes through. Threads that never call a guarded name are not affected either way.</li>
 * </ul>
 */
public class ContextoGuarda {

    private final boolean exigePrograma;
    private final ThreadLocal<CorridaGuardada> actual = new ThreadLocal<>();
    private final ThreadLocal<Boolean> sinPrograma = new ThreadLocal<>();

    /** Mode {@code programa} (fail closed): a guarded call without any scope on its thread is refused. */
    public ContextoGuarda() {
        this(true);
    }

    /** @param exigePrograma true in mode {@code programa}; false in caller mode (no {@code idPrograma} by design, nothing to refuse) */
    public ContextoGuarda(boolean exigePrograma) {
        this.exigePrograma = exigePrograma;
    }

    /** True when the module runs in mode {@code programa}: a guarded call with no scope on its thread must be refused. */
    public boolean exigePrograma() {
        return exigePrograma;
    }

    public void abrir(CorridaGuardada corrida) {
        sinPrograma.remove();
        actual.set(corrida);
    }

    /** Explicit scope of a run that has no program by design (caller mode): guarded calls pass through. */
    public void abrirSinPrograma() {
        actual.remove();
        sinPrograma.set(Boolean.TRUE);
    }

    public Optional<CorridaGuardada> actual() {
        return Optional.ofNullable(actual.get());
    }

    /** True when this thread holds the explicit "no program" scope. */
    public boolean sinPrograma() {
        return sinPrograma.get() != null;
    }

    public void cerrar() {
        actual.remove();
        sinPrograma.remove();
    }
}
