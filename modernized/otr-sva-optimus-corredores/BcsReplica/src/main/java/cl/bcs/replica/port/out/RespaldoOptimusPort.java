package cl.bcs.replica.port.out;

import java.sql.Connection;

public interface RespaldoOptimusPort {
    void respaldarDiaActual(Connection conn, String fechaSistema) throws Exception;

    void copiarDiaInhabil(Connection conn, String fechaOrigen, String fechaDestino) throws Exception;
}
