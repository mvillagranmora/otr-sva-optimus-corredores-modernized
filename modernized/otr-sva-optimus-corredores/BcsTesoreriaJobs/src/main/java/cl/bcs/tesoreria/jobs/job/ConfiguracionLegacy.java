package cl.bcs.tesoreria.jobs.job;

import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * What the legacy jobs read from the platform ({@code ConfiguracionSistema}, {@code Utils.getConfiguracion},
 * {@code FechaDB}) on top of the {@link ConfiguracionSistema} port, which answers {@code null} for a parameter that is not
 * configured: the legacy threw then, with the message of ConfiguracionSistema.java:70-71.
 */
final class ConfiguracionLegacy {

    static final String PLATAFORMA = "PLATAFORMA";

    private ConfiguracionLegacy() {
    }

    /** Legacy {@code ConfiguracionSistema.getParametroConfiguracionString}: a missing parameter is an {@link Exception}. */
    static String parametro(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre)
            throws Exception {
        String valor = configuracion.parametro(idEmpresa, modulo, nombre);
        if (valor == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }

    /** Legacy {@code Utils.getConfiguracion(con, schema, idEmpresa, codigo)} (Utils.java:126-138): platform parameter. */
    static String plataforma(ConfiguracionSistema configuracion, Long idEmpresa, String codigo) throws Exception {
        return parametro(configuracion, idEmpresa, PLATAFORMA, codigo);
    }

    /** Legacy {@code FechaDB.ObtenerSegundosZonaHoraria}. */
    static int segundosZonaHoraria(ConfiguracionSistema configuracion, Long idEmpresa) throws Exception {
        return (int) Long.parseLong(parametro(configuracion, idEmpresa, PLATAFORMA, "PLA.SegundosZonaHoraria"));
    }

    /** Legacy {@code Fecha.toFormat(Timestamp, pattern)}: the instant in the JVM default zone. */
    static String formato(Instant instante, String patron) {
        return DateTimeFormatter.ofPattern(patron).withZone(ZoneId.systemDefault()).format(instante);
    }
}
