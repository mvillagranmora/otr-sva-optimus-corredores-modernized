package cl.bcs.replica.port.out;

import java.sql.Connection;
import java.util.List;

public interface SecuenciaPort {
    List<Long> siguientes(Connection conn, String secuencia, Long cantidad) throws Exception;
}
