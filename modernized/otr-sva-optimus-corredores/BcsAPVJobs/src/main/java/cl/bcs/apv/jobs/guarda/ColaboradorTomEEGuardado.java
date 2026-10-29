package cl.bcs.apv.jobs.guarda;

import cl.bcs.apv.domain.port.out.Colaboradores;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;

/**
 * Fail-closed guard (job pattern, step 6) around the HTTP {@link Colaboradores} port: a collaborator that still lives in TomEE commits ON ITS
 * OWN, so a rollback of the job cannot undo it and the router carries no idempotency header. For each GUARDED call
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
 * for every name that is not guarded, this class only delegates.
 */
public class ColaboradorTomEEGuardado implements Colaboradores {

    private static final Logger log = LoggerFactory.getLogger(ColaboradorTomEEGuardado.class);

    private final Colaboradores delegado;
    private final MarcasRepositorio marcas;
    private final ContextoGuarda contexto;

    public ColaboradorTomEEGuardado(Colaboradores delegado, MarcasRepositorio marcas, ContextoGuarda contexto) {
        this.delegado = Objects.requireNonNull(delegado, "delegado");
        this.marcas = Objects.requireNonNull(marcas, "marcas");
        this.contexto = Objects.requireNonNull(contexto, "contexto");
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        Optional<CorridaGuardada> corrida = contexto.actual();
        if (!ClasificacionColaborador.guardada(servicio)) {
            return delegado.invocar(servicio, entrada);
        }
        if (corrida.isEmpty()) {
            if (contexto.exigePrograma() && !contexto.sinPrograma()) {
                throw sinContexto(servicio);
            }
            return delegado.invocar(servicio, entrada); // caller mode or explicit "no program" scope: nothing to guard
        }
        CorridaGuardada c = corrida.get();
        MarcaGuarda intento = c.siguiente(servicio);
        marcas.registrar(intento, c.idTipoPrograma()); // no mark, no call
        JsonObject salida;
        try {
            salida = delegado.invocar(servicio, entrada);
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
