package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.Timestamp;

import cl.bcs.utils.Fecha;

/**
 * ORACLE SHIM of legacy FechaDB (BcsPlataformaServicios/.../pla/utils/FechaDB.java). The legacy reads CURRENT_UTCTIMESTAMP from the
 * database and adds elapsed milliseconds (wall clock dependent); here the UTC clock is FIXED at 2025-04-01 10:15:30 so results are
 * deterministic. Composition of the returned values is the legacy one:
 * <ul>
 *   <li>ObtenerFechaHoraSistemaUTC = PLA.FechaSistema + " " + UTC time (FechaDB.java:43-49);</li>
 *   <li>ObtenerFechaHoraSistemaTs = Fecha.toDateTime(ObtenerFechaHoraSistemaUTC) (FechaDB.java:127-130);</li>
 *   <li>ObtenerFechaHoraUTCTs = the UTC timestamp (FechaDB.java:51-55);</li>
 *   <li>ObtenerSegundosZonaHoraria = PLATAFORMA/PLA.SegundosZonaHoraria as long (FechaDB.java:152-154).</li>
 * </ul>
 */
public class FechaDB {
    public static final Timestamp UTC = Timestamp.valueOf("2025-04-01 10:15:30");

    public static String ObtenerFechaHoraSistemaUTC(Connection con, long idEmpresa) throws Exception {
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        return fechaSistema + " " + Fecha.toStringTime(UTC);
    }

    public static Timestamp ObtenerFechaHoraUTCTs(Connection con) throws Exception {
        return UTC;
    }

    public static String ObtenerHoraUTC(Connection con) throws Exception {
        return Fecha.toStringTime(UTC);
    }

    public static Timestamp ObtenerFechaHoraSistemaTs(Connection con, long idEmpresa) throws Exception {
        return Fecha.toDateTime(ObtenerFechaHoraSistemaUTC(con, idEmpresa));
    }

    public static Timestamp ObtenerFechaHoraTs(Connection con, long idEmpresa) throws Exception {
        return UTC;
    }

    public static int ObtenerSegundosZonaHoraria(Connection con, long idEmpresa) throws Exception {
        return (int) ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
    }

    public static String ObtenerFechaHoraSistemaLocal(Connection con, long idEmpresa) throws Exception {
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        int offset = ObtenerSegundosZonaHoraria(con, idEmpresa);
        return fechaSistema + " " + Fecha.toStringTime(Fecha.addSeconds(UTC, offset));
    }

    public static String ObtenerHoraSistemaLocal(Connection con, long idEmpresa) throws Exception {
        int offset = ObtenerSegundosZonaHoraria(con, idEmpresa);
        return Fecha.toStringTime(Fecha.addSeconds(UTC, offset));
    }
}
