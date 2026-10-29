package cl.bcs.jobmanager.domain;

/**
 * Domain entity representing a running node in BAT.Nodos.
 */
public class NodeInfo {

    private String nodo;
    private String job;
    private int pid;
    private String fechaUltimoCiclo;
    private String reiniciar;
    private String processId;

    public NodeInfo() {}

    public NodeInfo(String nodo, String job, int pid, String processId) {
        this.nodo = nodo;
        this.job = job;
        this.pid = pid;
        this.processId = processId;
        this.reiniciar = "N";
    }

    public String getNodo() { return nodo; }
    public void setNodo(String nodo) { this.nodo = nodo; }

    public String getJob() { return job; }
    public void setJob(String job) { this.job = job; }

    public int getPid() { return pid; }
    public void setPid(int pid) { this.pid = pid; }

    public String getFechaUltimoCiclo() { return fechaUltimoCiclo; }
    public void setFechaUltimoCiclo(String fechaUltimoCiclo) { this.fechaUltimoCiclo = fechaUltimoCiclo; }

    public String getReiniciar() { return reiniciar; }
    public void setReiniciar(String reiniciar) { this.reiniciar = reiniciar; }

    public String getProcessId() { return processId; }
    public void setProcessId(String processId) { this.processId = processId; }
}
