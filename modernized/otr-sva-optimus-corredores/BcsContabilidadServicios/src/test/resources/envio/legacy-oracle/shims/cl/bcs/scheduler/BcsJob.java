package cl.bcs.scheduler;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/** ORACLE SHIM of the scheduler base class (not in this repository). */
public abstract class BcsJob {
    public final Map<String, String> params = new HashMap<>();
    public String rutaSalida;

    public abstract boolean transaction(Connection conn) throws Exception;

    public abstract void onError(Throwable t);

    public abstract void pause();

    public String getParam(String k) { return params.get(k); }

    public Long getJobId() { return 1L; }

    public void updateRutaSalida(Connection conn, String archivo, Long jobId) { this.rutaSalida = archivo; }
}
