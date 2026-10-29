package cl.bcs.plataforma.pla.utils;

import java.sql.Connection;
import java.sql.Timestamp;

import cl.bcs.oracle.Hooks;
import cl.bcs.utils.Fecha;

/**
 * ORACLE SHIM of legacy FechaDB (BcsPlataformaServicios/.../pla/utils/FechaDB.java). The legacy reads CURRENT_UTCTIMESTAMP
 * from the database (cached one hour, plus the elapsed milliseconds); here the UTC clock is the case's "reloj" (Hooks.UTC),
 * fixed, so results are deterministic. The composition of every returned value is the legacy one:
 * <ul>
 *   <li>ObtenerFechaHoraSistemaUTC = PLA.FechaSistema + " " + UTC time (FechaDB.java:43-49; it READS the configuration, so a
 *       missing PLA.FechaSistema fails here);</li>
 *   <li>ObtenerFechaHoraTs = calls ObtenerFechaHoraSistemaUTC (for its configuration side effect) and returns the raw UTC
 *       timestamp (FechaDB.java:174-177);</li>
 *   <li>ObtenerHoraSistemaLocal = UTC time + PLATAFORMA/PLA.SegundosZonaHoraria seconds (FechaDB.java:100-121); it reads
 *       PLA.FechaSistema first, then PLA.SegundosZonaHoraria.</li>
 * </ul>
 */
public class FechaDB {

    public static String ObtenerFechaHoraSistemaUTC(Connection con, long idEmpresa) throws Exception {
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        return fechaSistema + " " + Fecha.toStringTime(Hooks.UTC);
    }

    public static Timestamp ObtenerFechaHoraUTCTs(Connection con) throws Exception {
        return Hooks.UTC;
    }

    public static String ObtenerHoraUTC(Connection con) throws Exception {
        return Fecha.toStringTime(Hooks.UTC);
    }

    public static Timestamp ObtenerFechaHoraSistemaTs(Connection con, long idEmpresa) throws Exception {
        return Fecha.toDateTime(ObtenerFechaHoraSistemaUTC(con, idEmpresa));
    }

    public static Timestamp ObtenerFechaHoraTs(Connection con, long idEmpresa) throws Exception {
        ObtenerFechaHoraSistemaUTC(con, idEmpresa);
        return Hooks.UTC;
    }

    public static int ObtenerSegundosZonaHoraria(Connection con, long idEmpresa) throws Exception {
        return (int) ConfiguracionSistema.getParametroConfiguracionLong(con, idEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
    }

    public static String ObtenerFechaHoraSistemaLocal(Connection con, long idEmpresa) throws Exception {
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        int offset = ObtenerSegundosZonaHoraria(con, idEmpresa);
        return fechaSistema + " " + Fecha.toStringTime(Fecha.addSeconds(Hooks.UTC, offset));
    }

    public static String ObtenerHoraSistemaLocal(Connection con, long idEmpresa) throws Exception {
        String fechaSistema = ConfiguracionSistema.getParametroConfiguracionString(con, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        int offset = ObtenerSegundosZonaHoraria(con, idEmpresa);
        return Fecha.toStringTime(Fecha.addSeconds(Hooks.UTC, offset));
    }
}
