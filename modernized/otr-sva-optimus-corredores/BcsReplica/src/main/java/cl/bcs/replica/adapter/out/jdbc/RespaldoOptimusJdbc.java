package cl.bcs.replica.adapter.out.jdbc;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.RespaldoOptimusPort;

public class RespaldoOptimusJdbc implements RespaldoOptimusPort {
    @Override
    public void respaldarDiaActual(Connection conn, String fechaSistema) throws Exception {
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql").replace(":FechaSaldo", fechaSistema));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaEfectivaHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql").replace(":FechaSaldo", fechaSistema));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaMovimientoHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql").replace(":FechaCustodia", fechaSistema));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaOperacionHistorica.sql"));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql").replace(":FechaCustodia", fechaSistema));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaLiquidacionHistorica.sql"));
    }

    @Override
    public void copiarDiaInhabil(Connection conn, String fechaOrigen, String fechaDestino) throws Exception {
        List<Map<String, Object>> fechas = List.of(Sql.fila("FechaOrigen", fechaOrigen, "FechaDestino", fechaDestino));
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaEfectivaCurrent.sql").replace(":FechaSaldo", fechaDestino));
        Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaEfectivaDiaInhabil.sql"), fechas);
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCuentaInversionFechaMovimientoCurrent.sql").replace(":FechaSaldo", fechaDestino));
        Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertCuentaInversionFechaMovimientoDiaInhabil.sql"), fechas);
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaOperacionCurrent.sql").replace(":FechaCustodia", fechaDestino));
        Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaOperacionDiaInhabil.sql"), fechas);
        Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteCustodiaFechaLiquidacionCurrent.sql").replace(":FechaCustodia", fechaDestino));
        Sql.batch(conn, Sql.recurso("EnviarTablaReplica_InsertCustodiaFechaLiquidacionDiaInhabil.sql"), fechas);
    }
}
