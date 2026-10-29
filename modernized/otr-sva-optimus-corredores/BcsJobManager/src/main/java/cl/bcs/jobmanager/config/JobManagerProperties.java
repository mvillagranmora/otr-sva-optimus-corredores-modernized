package cl.bcs.jobmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration of the dispatcher (prefix {@code jobmanager}).
 *
 * <p>INERT BY DEFAULT: {@code tipos-habilitados} is empty, so nothing is selected, posted or written. There is no
 * safe per-type cutover while the legacy Job Manager runs (JOBS_CUTOVER_ANALYSIS.md): listing types is the one
 * global production decision, taken only when the legacy manager of the node is stopped.
 */
@Component
@ConfigurationProperties(prefix = "jobmanager")
public class JobManagerProperties {

    /** Cluster node name; selects BAT.Programa.Nodo (HANA column is NVARCHAR(1): jobmanager.node must be ONE character; '' or '*' = default node). */
    private String node = "LOCAL";
    /** Only used to register the node in BAT.Nodos; it is NOT a selection filter (tipos-habilitados is). */
    private String taskType = "";
    private String processId = "";
    private int concurrentJobs = 4;
    /** PROGRAMA (dispatch queue) or AGENDA (diary; also needs agenda.habilitada=true). */
    private String mode = "PROGRAMA";
    /** IdTipoPrograma values this instance dispatches. Empty = inert. */
    private List<String> tiposHabilitados = new ArrayList<>();
    /** Key: IdTipoPrograma or class package prefix; value: base URL of the job module. Use "[a.b.c]" for dotted keys. */
    private Map<String, String> destinos = new LinkedHashMap<>();
    private String internalToken = "";
    private int connectTimeoutMs = 2_000;
    /** The job runs synchronously inside the call: generous. A read timeout is not a job failure. */
    private int readTimeoutMs = 1_800_000;
    /** Programs waiting for a free worker; the rest stays PEN and is selected again at the next poll. */
    private int queueCapacity = 100;
    private long shutdownWaitMs = 30_000L;
    /**
     * In-memory cooldown per program after a 404, 422 or 503 answer (configuration/data inconsistency), so it is not
     * re-posted and re-logged at every poll. Zero disables it. Not applied to connection errors, 409, 200 or 500.
     */
    private Duration cooldownNoEntregable = Duration.ofSeconds(60);
    private final Agenda agenda = new Agenda();

    /** Diary (creation of programs from BAT.Agenda): legacy-owned until the global cutover. */
    public static class Agenda {
        private boolean habilitada = false;
        public boolean isHabilitada() { return habilitada; }
        public void setHabilitada(boolean habilitada) { this.habilitada = habilitada; }
    }

    public String getNode() { return node; }
    public void setNode(String node) { this.node = node; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }
    public int getConcurrentJobs() { return concurrentJobs; }
    public void setConcurrentJobs(int concurrentJobs) { this.concurrentJobs = concurrentJobs; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public List<String> getTiposHabilitados() { return tiposHabilitados; }
    public void setTiposHabilitados(List<String> tiposHabilitados) { this.tiposHabilitados = tiposHabilitados; }
    public Map<String, String> getDestinos() { return destinos; }
    public void setDestinos(Map<String, String> destinos) { this.destinos = destinos; }
    public String getInternalToken() { return internalToken; }
    public void setInternalToken(String internalToken) { this.internalToken = internalToken; }
    public int getConnectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int getReadTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int queueCapacity) { this.queueCapacity = queueCapacity; }
    public long getShutdownWaitMs() { return shutdownWaitMs; }
    public void setShutdownWaitMs(long shutdownWaitMs) { this.shutdownWaitMs = shutdownWaitMs; }
    public Duration getCooldownNoEntregable() { return cooldownNoEntregable; }
    public void setCooldownNoEntregable(Duration cooldownNoEntregable) { this.cooldownNoEntregable = cooldownNoEntregable; }
    public Agenda getAgenda() { return agenda; }

    /** Enabled types, trimmed, blanks removed, in a stable order (empty list = inert). */
    public List<String> tiposActivos() {
        return tiposHabilitados == null ? List.of()
                : tiposHabilitados.stream().filter(t -> t != null && !t.isBlank()).map(String::trim).distinct().toList();
    }

    public boolean inerte() {
        return tiposActivos().isEmpty();
    }
}
