package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.domain.Programa;
import cl.bcs.jobmanager.port.out.NodoPort;
import cl.bcs.jobmanager.port.out.ProgramaPort;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Polling loop of the dispatcher (replaces BcsJobManager.runJob()).
 *
 * <p>INERT BY DEFAULT: with {@code jobmanager.tipos-habilitados} empty it does nothing, not even a query or a node
 * registration. Otherwise each poll selects the eligible programs of the enabled types (read-only, the legacy
 * readProgramm predicate) and hands them to {@link ProgramaDespachador}. It does NOT claim programs, does NOT update
 * their state and does NOT retry: the job module claims atomically and writes TER/ERR. The legacy Job Manager never
 * calls modernized code, and while it runs there is no safe per-type cutover: enabling types is one global decision
 * (see README "Global cutover checklist").
 */
@Service
public class JobDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(JobDispatcherService.class);
    private static final long HEARTBEAT_INTERVAL_MS = 10_000L;

    private final ProgramaPort programaPort;
    private final NodoPort nodoPort;
    private final JobManagerProperties props;
    private final ProgramaDespachador despachador;

    private final AtomicBoolean firstRun = new AtomicBoolean(true);
    private volatile long lastHeartbeat = System.currentTimeMillis();
    private volatile int currentPid = -1;

    public JobDispatcherService(ProgramaPort programaPort, NodoPort nodoPort,
                                JobManagerProperties props, ProgramaDespachador despachador) {
        this.programaPort = Objects.requireNonNull(programaPort);
        this.nodoPort = Objects.requireNonNull(nodoPort);
        this.props = Objects.requireNonNull(props);
        this.despachador = Objects.requireNonNull(despachador);
    }

    @PostConstruct
    void anunciarEstado() {
        if (props.inerte()) {
            log.warn("BcsJobManager is INERT: jobmanager.tipos-habilitados is empty, no program will be selected or dispatched");
        } else {
            log.warn("BcsJobManager ACTIVE for types {} on node '{}': the legacy Job Manager of this node must be stopped",
                    props.tiposActivos(), props.getNode());
        }
    }

    @Scheduled(fixedDelayString = "${jobmanager.poll-interval-ms:1000}")
    public void pollJobs() {
        pollOnce(Instant.now());
    }

    /** One poll; returns how many programs were queued for dispatch. */
    public int pollOnce(Instant now) {
        if (!"PROGRAMA".equalsIgnoreCase(props.getMode()) || props.inerte()) {
            return 0;
        }
        registrarNodoYLatido();
        int encolados = 0;
        try {
            List<Programa> programas = programaPort.readEligiblePrograms(
                    JobManagerService.normalizeName(props.getNode()), props.tiposActivos(), now);
            despachador.depurarEnfriamiento(programas.stream().map(Programa::getIdPrograma).collect(Collectors.toSet()));
            for (Programa p : programas) {
                if (despachador.despachar(p) == ProgramaDespachador.Aceptacion.ENCOLADO) {
                    encolados++;
                }
            }
        } catch (Exception e) {
            log.error("Error in pollJobs: {}", e.getMessage(), e);
        }
        return encolados;
    }

    private void registrarNodoYLatido() {
        if (firstRun.compareAndSet(true, false)) {
            long pid = ProcessHandle.current().pid();
            currentPid = (int) (pid > Integer.MAX_VALUE ? pid % Integer.MAX_VALUE : pid);
            try {
                nodoPort.addNodo(props.getNode(), props.getTaskType(), currentPid, props.getProcessId());
                log.info("Node registered: node={} pid={}", props.getNode(), currentPid);
            } catch (Exception e) {
                log.error("Failed to register node: {}", e.getMessage(), e);
            }
        }
        if (System.currentTimeMillis() - lastHeartbeat > HEARTBEAT_INTERVAL_MS) {
            lastHeartbeat = System.currentTimeMillis();
            try {
                nodoPort.updateCycleNodo(currentPid, props.getProcessId());
                if (nodoPort.isRebootRequested(currentPid)) {
                    log.warn("Reboot requested for node={} pid={}", props.getNode(), currentPid);
                    System.exit(0);
                }
            } catch (Exception e) {
                log.warn("Heartbeat/reboot check failed: {}", e.getMessage());
            }
        }
    }
}
