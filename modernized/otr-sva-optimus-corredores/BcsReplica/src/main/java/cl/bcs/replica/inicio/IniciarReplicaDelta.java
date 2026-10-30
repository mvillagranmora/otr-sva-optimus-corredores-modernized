package cl.bcs.replica.inicio;

import java.time.Clock;

import cl.bcs.replica.port.out.FechaSistemaPort;
import cl.bcs.replica.port.out.SecuenciaPort;

public class IniciarReplicaDelta extends IniciarReplicaBase {
    public IniciarReplicaDelta(SecuenciaPort secuencias, FechaSistemaPort fechas, Clock reloj) {
        super(secuencias, fechas, reloj, "QueryReplicaDelta");
    }
}
