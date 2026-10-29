package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.domain.Programa;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Hands eligible programs to the job modules. Guarantees: a program is never posted twice concurrently (in-flight set,
 * keyed by IdPrograma); a bounded executor (workers + bounded queue) so a slow job does not block the polling loop;
 * daemon programs are never dispatched (RULE-061); graceful shutdown. It never writes to the database and never retries:
 * a program that was not claimed by the job module stays PEN and the next poll selects it again.
 */
@Component
public class ProgramaDespachador {

    /** Result of {@link #despachar}. */
    public enum Aceptacion { ENCOLADO, EN_CURSO, DEMONIO_OMITIDO, SIN_DESTINO, EN_ENFRIAMIENTO, COLA_LLENA, APAGANDO }

    private static final Logger log = LoggerFactory.getLogger(ProgramaDespachador.class);

    private final JobCallClient cliente;
    private final DestinoResolver destinos;
    private final long esperaApagadoMs;
    private final Set<Long> enCurso = ConcurrentHashMap.newKeySet();
    private final ThreadPoolExecutor pool;
    private final EnfriamientoNoEntregable enfriamiento;

    @Autowired
    public ProgramaDespachador(JobManagerProperties props, JobCallClient cliente) {
        this(props, cliente, Clock.systemUTC());
    }

    public ProgramaDespachador(JobManagerProperties props, JobCallClient cliente, Clock reloj) {
        this.cliente = cliente;
        this.enfriamiento = new EnfriamientoNoEntregable(props.getCooldownNoEntregable(),
                EnfriamientoNoEntregable.MAX_POR_DEFECTO, reloj);
        this.destinos = new DestinoResolver(props.getDestinos());
        this.esperaApagadoMs = props.getShutdownWaitMs();
        int hilos = Math.max(1, props.getConcurrentJobs());
        this.pool = new ThreadPoolExecutor(hilos, hilos, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(Math.max(1, props.getQueueCapacity())),
                r -> {
                    Thread t = new Thread(r, "job-dispatch");
                    t.setDaemon(true);
                    return t;
                });
    }

    public Aceptacion despachar(Programa p) {
        long id = p.getIdPrograma();
        if (p.isDemonio()) {
            log.warn("Program #{} type={} belongs to a daemon agenda: daemon mode is not supported (RULE-061), not dispatched",
                    id, p.getIdTipoPrograma());
            return Aceptacion.DEMONIO_OMITIDO;
        }
        Optional<String> destino = destinos.resolver(p);
        if (destino.isEmpty()) {
            log.error("Program #{} type={} class={}: no destination in jobmanager.destinos, not dispatched",
                    id, p.getIdTipoPrograma(), p.getNombreClasePrograma());
            return Aceptacion.SIN_DESTINO;
        }
        switch (enfriamiento.consultar(id)) {
            case EN_ENFRIAMIENTO_PRIMERA_OMISION -> {
                log.info("Program #{} in cooldown after a 404/422/503 answer (jobmanager.cooldown-no-entregable); skipped silently until it expires", id);
                return Aceptacion.EN_ENFRIAMIENTO;
            }
            case EN_ENFRIAMIENTO -> {
                log.debug("Program #{} in cooldown, skipped", id);
                return Aceptacion.EN_ENFRIAMIENTO;
            }
            case LIBRE -> { }
        }
        if (!enCurso.add(id)) {
            log.debug("Program #{} already in flight, skipped", id);
            return Aceptacion.EN_CURSO;
        }
        try {
            pool.execute(() -> ejecutar(p, destino.get()));
            return Aceptacion.ENCOLADO;
        } catch (RejectedExecutionException e) {
            enCurso.remove(id);
            boolean apagando = pool.isShutdown();
            log.warn("Program #{} not queued ({}); it stays PEN and is selected again", id,
                    apagando ? "shutting down" : "queue full");
            return apagando ? Aceptacion.APAGANDO : Aceptacion.COLA_LLENA;
        }
    }

    private void ejecutar(Programa p, String destino) {
        try {
            log.info("Dispatching program #{} type={} to {}", p.getIdPrograma(), p.getIdTipoPrograma(), destino);
            JobCallDesenlace r = cliente.llamar(destino, p.getIdPrograma(), p.getNombreClasePrograma());
            if (r == JobCallDesenlace.INCONSISTENTE || r == JobCallDesenlace.CLASE_NO_EJECUTADA) {
                enfriamiento.registrar(p.getIdPrograma());
            }
        } catch (RuntimeException e) {
            log.error("Program #{}: unexpected error while dispatching: {}", p.getIdPrograma(), e.toString());
        } finally {
            enCurso.remove(p.getIdPrograma());
        }
    }

    /** Drops the cooldown of programs that are not in the current eligible set any more. */
    public void depurarEnfriamiento(Set<Long> elegibles) {
        enfriamiento.depurar(elegibles);
    }

    public int enfriamientoTotal() {
        return enfriamiento.tamano();
    }

    public boolean estaEnCurso(long idPrograma) {
        return enCurso.contains(idPrograma);
    }

    public int enCursoTotal() {
        return enCurso.size();
    }

    @PreDestroy
    public void apagar() {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(esperaApagadoMs, TimeUnit.MILLISECONDS)) {
                log.warn("{} dispatch call(s) still open after {} ms; interrupting (the job modules keep the state)",
                        enCurso.size(), esperaApagadoMs);
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
