package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface TriggersReplicaPort {
    void desactivar(Connection conn) throws Exception;

    void reactivar(Connection conn) throws Exception;
}
