package cl.bcs.replica.adapter.out.jdbc;

import java.sql.Connection;
import java.util.List;

import cl.bcs.replica.kit.Sql;
import cl.bcs.replica.port.out.RespaldoSebraPort;

public class RespaldoSebraJdbc implements RespaldoSebraPort {
    private static final List<String> PERIODO = List.of("TBCPDTPC", "TBCPMSPC", "TBCTCIRF", "TBCTCURF", "TBCTCIRV", "TBCTCURV", "TBCTCIFM", "TBCTCUFM", "TBCTCTCL", "TBCTTRTP", "TBCTCIIF", "TBCTGTIA",
            "TBCTTRPT");
    private static final List<String> DIARIO = List.of("TBCTCIFM", "TBCTCIRV", "TBCTCIIF", "TBCTCIRF");

    @Override
    public void respaldarPeriodo(Connection conn, String codPer) throws Exception {
        for (String t : PERIODO) {
            Sql.exec(conn, Sql.recurso("EnviarTablaReplica_Delete" + t + ".sql").replace(":COD_PER", codPer));
            Sql.exec(conn, Sql.recurso("EnviarTablaReplica_Insert" + t + ".sql").replace(":COD_PER", codPer));
        }
    }

    @Override
    public void respaldarDiario(Connection conn, String codPer, String codDia) throws Exception {
        for (String t : DIARIO) {
            Sql.exec(conn, Sql.recurso("EnviarTablaReplica_DeleteDiario" + t + ".sql").replace(":COD_DIA", codDia));
            Sql.exec(conn, Sql.recurso("EnviarTablaReplica_InsertDiario" + t + ".sql").replace(":COD_PER", codPer).replace(":COD_DIA", codDia));
        }
    }
}
