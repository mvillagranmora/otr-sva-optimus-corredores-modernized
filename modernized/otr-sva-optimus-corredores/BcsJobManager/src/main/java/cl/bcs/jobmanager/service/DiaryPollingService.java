package cl.bcs.jobmanager.service;

import cl.bcs.jobmanager.config.JobManagerProperties;
import cl.bcs.jobmanager.domain.Agenda;
import cl.bcs.jobmanager.port.out.AgendaPort;
import cl.bcs.jobmanager.port.out.NodoPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Diary (BcsJobManager.runDiary/poolDiary): creates programs from BAT.Agenda through BAT.AgregarPrograma.
 *
 * <p>OFF BY DEFAULT and LEGACY-OWNED until the global cutover: the legacy Job Manager creates the programs, and two
 * creators would duplicate them. It runs only with {@code jobmanager.mode=AGENDA} AND
 * {@code jobmanager.agenda.habilitada=true}. There is no retry here (RULE-059 removed: the legacy never retried) and no
 * daemon handling (RULE-061: daemon agendas are logged and skipped).
 */
@Service
public class DiaryPollingService {

    private static final Logger log = LoggerFactory.getLogger(DiaryPollingService.class);
    private static final long HEARTBEAT_INTERVAL_MS = 10_000L;
    private static final int ZOMBIE_TIMEOUT_SECS = 30;

    private final AgendaPort agendaPort;
    private final NodoPort nodoPort;
    private final JobManagerProperties props;
    private final AtomicBoolean firstDiary = new AtomicBoolean(true);
    private volatile long lastHeartbeat = System.currentTimeMillis();

    public DiaryPollingService(AgendaPort agendaPort, NodoPort nodoPort, JobManagerProperties props) {
        this.agendaPort = Objects.requireNonNull(agendaPort);
        this.nodoPort = Objects.requireNonNull(nodoPort);
        this.props = Objects.requireNonNull(props);
    }

    boolean activo() {
        return "AGENDA".equalsIgnoreCase(props.getMode()) && props.getAgenda().isHabilitada();
    }

    @Scheduled(fixedDelayString = "${jobmanager.poll-interval-ms:1000}")
    public void pollDiary() {
        if (!activo()) {
            return;
        }
        if (firstDiary.compareAndSet(true, false)) {
            try {
                agendaPort.firstExecution();
                log.info("First-execution correction applied.");
            } catch (Exception e) {
                log.warn("firstExecution correction failed: {}", e.getMessage());
            }
        }
        if (System.currentTimeMillis() - lastHeartbeat > HEARTBEAT_INTERVAL_MS) {
            lastHeartbeat = System.currentTimeMillis();
            try {
                for (int pid : nodoPort.getZombieNodos(ZOMBIE_TIMEOUT_SECS, true)) {
                    log.warn("Zombie node pid={} (heartbeat expired)", pid);
                }
            } catch (Exception e) {
                log.warn("Zombie check failed: {}", e.getMessage());
            }
        }
        try {
            List<Agenda> agendas = agendaPort.readEnabledAgendas();
            for (Agenda agenda : agendas) {
                procesar(agenda);
            }
        } catch (Exception e) {
            log.error("Error in pollDiary: {}", e.getMessage(), e);
        }
    }

    private void procesar(Agenda agenda) {
        try {
            if (!DiaryService.isEnabled(agenda) || agenda.getSiguienteFechaEjecucion() == null
                    || agenda.getUltimaFechaEjecucion() == null || !DiaryService.shouldTrigger(agenda)) {
                return;
            }
            long id = agenda.getIdAgenda();
            if (DiaryService.isDaemon(agenda)) {
                log.warn("Agenda #{} is a daemon: daemon mode is not supported (RULE-061), skipped", id);
                return;
            }
            Map<String, String> params = agendaPort.readParams(id);
            int segundos = agendaPort.getDateForPrograma(id);
            if (segundos > 0) {
                agendaPort.updateNextExecutionDate(id, segundos);
                agendaPort.addPrograma((int) id, agenda.getIdTipoPrograma(), segundos, params);
                log.info("Program agenda #{} triggered for next execution in {} secs", id, segundos);
            }
        } catch (Exception e) {
            log.error("Error processing agenda #{}: {}", agenda.getIdAgenda(), e.getMessage(), e);
        }
    }
}
