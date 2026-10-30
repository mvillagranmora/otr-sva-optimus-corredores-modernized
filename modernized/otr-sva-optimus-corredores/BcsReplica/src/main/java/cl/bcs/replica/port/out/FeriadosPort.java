package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface FeriadosPort {
    boolean esDiaHabil(Connection conn, String fecha, Long idPais) throws Exception;
}
