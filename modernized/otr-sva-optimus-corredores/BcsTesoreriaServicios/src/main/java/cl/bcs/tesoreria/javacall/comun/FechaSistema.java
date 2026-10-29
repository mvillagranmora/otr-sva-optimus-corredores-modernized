package cl.bcs.tesoreria.javacall.comun;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.utils.Fecha;

/**
 * System date and time of the platform (legacy {@code FechaDB}): the business date comes from the configuration parameter
 * {@code PLATAFORMA/PLA.FechaSistema} and the time of day from the {@link Reloj} (UTC). Timestamps are built from the UTC wall clock, so the result
 * does not depend on the zone of the JVM.
 */
public final class FechaSistema {

    private FechaSistema() {
    }

    /** UTC date and time of the clock as a {@link Timestamp} whose wall clock is the UTC one (legacy {@code ObtenerFechaHoraUTCTs}). */
    public static Timestamp horaUtcTs(Reloj reloj) {
        return Timestamp.valueOf(LocalDateTime.ofInstant(reloj.ahora(), ZoneOffset.UTC));
    }

    /** Legacy {@code ObtenerHoraUTC}. */
    public static String horaUtc(Reloj reloj) {
        return Fecha.toStringTime(horaUtcTs(reloj));
    }

    /** Legacy {@code ObtenerFechaHoraSistemaUTC}: system date + UTC time of day, {@code yyyy-MM-dd HH:mm:ss.SSS}. */
    public static String fechaHoraSistemaUtc(ConfiguracionSistema configuracion, Reloj reloj, long idEmpresa) throws Exception {
        return fechaSistema(configuracion, idEmpresa) + " " + horaUtc(reloj);
    }

    /** Legacy {@code ObtenerFechaHoraSistemaTs}. */
    public static Timestamp fechaHoraSistemaTs(ConfiguracionSistema configuracion, Reloj reloj, long idEmpresa) throws Exception {
        return Fecha.toDateTime(fechaHoraSistemaUtc(configuracion, reloj, idEmpresa));
    }

    /** The business date {@code PLA.FechaSistema} ({@code yyyy-MM-dd}). */
    public static String fechaSistema(ConfiguracionSistema configuracion, long idEmpresa) throws Exception {
        return Parametros.texto(configuracion, idEmpresa, Parametros.PLATAFORMA, "PLA.FechaSistema");
    }

    /** Legacy {@code ObtenerSegundosZonaHoraria}. */
    public static int segundosZonaHoraria(ConfiguracionSistema configuracion, long idEmpresa) throws Exception {
        return (int) Parametros.entero(configuracion, idEmpresa, Parametros.PLATAFORMA, "PLA.SegundosZonaHoraria");
    }

    /** Legacy {@code ObtenerFechaHoraSistemaLocal}: system date + local time of day. */
    public static String fechaHoraSistemaLocal(ConfiguracionSistema configuracion, Reloj reloj, long idEmpresa) throws Exception {
        return fechaSistema(configuracion, idEmpresa) + " " + horaSistemaLocal(configuracion, reloj, idEmpresa);
    }

    /** Legacy {@code ObtenerHoraSistemaLocal}. */
    public static String horaSistemaLocal(ConfiguracionSistema configuracion, Reloj reloj, long idEmpresa) throws Exception {
        return Fecha.toStringTime(Fecha.addSeconds(horaUtcTs(reloj), segundosZonaHoraria(configuracion, idEmpresa)));
    }
}
