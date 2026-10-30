package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface ConsultarEmpresaPort {
    Long idPais(Connection conn, Long idEmpresa) throws Exception;
}
