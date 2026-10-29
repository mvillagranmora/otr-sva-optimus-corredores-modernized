package cl.bcs.contabilidad.jobs.guarda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Fail-closed guard (job pattern, step 6) for the calls this module makes to collaborators that still live in TomEE: they commit ON THEIR OWN, so
 * a rollback of the job cannot undo them and the router carries no idempotency header. For each GUARDED call
 * ({@link ClasificacionColaborador#guardada}) made inside a run that has an {@code idPrograma}:
 * <ol>
 *   <li>BEFORE the call, an {@code H4G|INTENTO|...} mark, committed at once in an independent connection; if it cannot be written the call is
 *       NOT made ({@link GuardaTomeeException});</li>
 *   <li>the HTTP call, once, never retried;</li>
 *   <li>on success an {@code H4G|HECHO|...} mark; on any exception an {@code H4G|FALLO|...} mark and the original exception is rethrown (the job
 *       rolls back ITS work; the TomEE write may already be committed).</li>
 * </ol>
 * The guard DETECTS and STOPS; it never undoes a committed TomEE write. A later run of the same program is refused (see
 * {@code EjecutorPrograma}) when any call has a HECHO or an INTENTO with no close. Without an open run ({@link ContextoGuarda}: caller mode) and
 * for every name that is not guarded, this class only calls through.
 *
 * <p>Difference from the reference (Tesoreria): this module has no name-based {@code Colaboradores.invocar}; its typed ports are wrapped by small
 * decorators (for example {@link ProcesoBatchGuardado}) that hand the call to {@link #invocar(String, Supplier)}.
 */
public class ColaboradorTomEEGuardado {

    private static final Logger log = LoggerFactory.getLogger(ColaboradorTomEEGuardado.class);

    private final MarcasRepositorio marcas;
    private final ContextoGuarda contexto;

    public ColaboradorTomEEGuardado(MarcasRepositorio marcas, ContextoGuarda contexto) {
        this.marcas = Objects.requireNonNull(marcas, "marcas");
        this.contexto = Objects.requireNonNull(contexto, "contexto");
    }

    /** Makes {@code llamada} (the HTTP call of the collaborator {@code servicio}) under the guard when that name is guarded and a run is open. */
    public <T> T invocar(String servicio, Supplier<T> llamada) {
        Optional<CorridaGuardada> corrida = contexto.actual();
        if (!ClasificacionColaborador.guardada(servicio)) {
            return llamada.get();
        }
        if (corrida.isEmpty()) {
            if (contexto.exigePrograma() && !contexto.sinPrograma()) {
                throw sinContexto(servicio);
            }
            return llamada.get(); // caller mode or explicit "no program" scope: nothing to guard
        }
        CorridaGuardada c = corrida.get();
        MarcaGuarda intento = c.siguiente(servicio);
        marcas.registrar(intento, c.idTipoPrograma()); // no mark, no call
        T salida;
        try {
            salida = llamada.get();
        } catch (RuntimeException | Error e) {
            c.fallada(intento);
            cerrar(intento.en(MarcaGuarda.Fase.FALLO), c, e);
            throw e;
        }
        c.hecha(intento);
        try {
            marcas.registrar(intento.en(MarcaGuarda.Fase.HECHO), c.idTipoPrograma());
        } catch (GuardaTomeeException e) {
            log.error("Program #{}: {} #{} answered but its HECHO mark could not be written; the INTENTO stays open, the program will need review",
                    c.idPrograma(), servicio, intento.n(), e);
            throw e;
        }
        return salida;
    }

    /** Fail closed: mode programa, guarded name, no scope on this thread (another thread of a library?): refused before any call or mark. */
    private GuardaTomeeException sinContexto(String servicio) {
        GuardaTomeeException e = new GuardaTomeeException("llamada protegida sin contexto de programa (hilo distinto?): " + servicio
                + " is guarded and thread '" + Thread.currentThread().getName() + "' has no program run open; not called, no marks written");
        log.error("{}", e.getMessage(), e);
        return e;
    }

    /** Writes the FALLO mark; a failure there is logged and attached to the original exception, which is what the job sees. */
    private void cerrar(MarcaGuarda fallo, CorridaGuardada c, Throwable original) {
        try {
            marcas.registrar(fallo, c.idTipoPrograma());
        } catch (RuntimeException e) {
            log.error("Program #{}: the FALLO mark of {} #{} could not be written; the INTENTO stays open", c.idPrograma(), fallo.clase(), fallo.n(), e);
            original.addSuppressed(e);
        }
    }
}
