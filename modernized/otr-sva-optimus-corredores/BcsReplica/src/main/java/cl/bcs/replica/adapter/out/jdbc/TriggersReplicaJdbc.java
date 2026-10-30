package cl.bcs.replica.adapter.out.jdbc;

import java.sql.Connection;

import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.TriggersReplicaPort;

public class TriggersReplicaJdbc implements TriggersReplicaPort {
    @Override
    public void desactivar(Connection conn) throws Exception {
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DropTriggerCustodia.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DropTriggerCuentaInversion.sql"));
    }

    @Override
    public void reactivar(Connection conn) throws Exception {
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_CreateTriggerCustodia.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_CreateTriggerCuentaInversion.sql"));
    }
}
