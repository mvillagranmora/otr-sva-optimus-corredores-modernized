package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface ConfiguracionSistemaPort {
    long parametroLong(Connection conn, Long idEmpresa, String modulo, String parametro) throws Exception;

    String parametroString(Connection conn, Long idEmpresa, String modulo, String parametro) throws Exception;
}
