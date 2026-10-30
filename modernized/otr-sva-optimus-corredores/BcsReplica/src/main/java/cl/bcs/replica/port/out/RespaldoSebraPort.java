package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface RespaldoSebraPort {
    void respaldarPeriodo(Connection conn, String codPer) throws Exception;

    void respaldarDiario(Connection conn, String codPer, String codDia) throws Exception;
}
