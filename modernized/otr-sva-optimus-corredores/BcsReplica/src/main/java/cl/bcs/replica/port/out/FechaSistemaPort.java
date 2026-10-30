package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface FechaSistemaPort {
    String fechaSistema(Connection conn, String esquema, long idEmpresa) throws Exception;
}
