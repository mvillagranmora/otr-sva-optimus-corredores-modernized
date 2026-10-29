package cl.bcs.jobmanager.port.out;

import java.util.List;

/**
 * Output port for node registration and heartbeat operations in BAT.Nodos.
 */
public interface NodoPort {

    /**
     * Registers this node instance (deletes old record for same node/job,
     * inserts fresh entry with current PID and heartbeat timestamp).
     *
     * @param nodo      cluster node name
     * @param job       job type
     * @param pid       OS process id
     * @param processId optional process identifier string (heartbeat table)
     */
    void addNodo(String nodo, String job, int pid, String processId);

    /**
     * Updates the last-cycle timestamp for a node (heartbeat).
     *
     * @param pid       OS process id
     * @param processId optional process identifier string
     */
    void updateCycleNodo(int pid, String processId);

    /**
     * Returns true if the node has been flagged for reboot.
     *
     * @param pid OS process id
     * @return true if Reiniciar = 'S'
     */
    boolean isRebootRequested(int pid);

    /**
     * Returns PIDs of zombie nodes (nodes that have not heartbeated within
     * the given timeout in seconds).
     *
     * @param timeoutSeconds inactivity threshold in seconds
     * @param clean          if true, delete zombie entries from the table
     * @return list of zombie PIDs
     */
    List<Integer> getZombieNodos(int timeoutSeconds, boolean clean);

    /**
     * Pings the DB to verify connectivity.
     *
     * @return true if connection is alive
     */
    boolean pingDb();
}
