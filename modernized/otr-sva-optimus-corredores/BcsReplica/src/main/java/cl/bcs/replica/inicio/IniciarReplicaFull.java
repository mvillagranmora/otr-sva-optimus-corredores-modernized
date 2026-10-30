package cl.bcs.replica.inicio;

import java.time.Clock;

import cl.bcs.replica.port.out.FechaSistemaPort;
import cl.bcs.replica.port.out.SecuenciaPort;

public class IniciarReplicaFull extends IniciarReplicaBase {
    public IniciarReplicaFull(SecuenciaPort secuencias, FechaSistemaPort fechas, Clock reloj) {
        super(secuencias, fechas, reloj, "QueryReplica");
    }
}
