package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.Timestamp;

/** ORACLE SHIM of legacy FechaDB: fixed clock, 2025-04-01 10:15:30. */
public class FechaDB {
    public static Timestamp ObtenerFechaHoraTs(Connection con, long idEmpresa) {
        return Timestamp.valueOf("2025-04-01 10:15:30");
    }

    public static String ObtenerHoraSistemaLocal(Connection con, long idEmpresa) {
        return "10:15:30";
    }
}
