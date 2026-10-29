package cl.bcs.apv.application.comun;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.apv.domain.port.out.ConfiguracionSistema;

/**
 * Typed access to the configuration parameters with the legacy contract of {@code ConfiguracionSistema.getParametroConfiguracion*}: a parameter the
 * company does not have is a {@code java.lang.Exception} "Parametro X en modulo Y no encontrado" (thrown unchanged through {@link Sneaky}), not a null.
 * The port answers null; {@link #opcional} is for the services that handle the absence themselves.
 */
public final class Parametros {

    public static final String PLATAFORMA = "PLATAFORMA";
    public static final String APV = "APV";

    private Parametros() {
    }

    public static String opcional(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) {
        return configuracion.parametro(idEmpresa, modulo, nombre);
    }

    public static String texto(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) {
        String valor = configuracion.parametro(idEmpresa, modulo, nombre);
        if (valor == null) {
            throw Sneaky.lanzar(new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado"));
        }
        return valor;
    }

    public static long entero(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) {
        return Long.parseLong(texto(configuracion, idEmpresa, modulo, nombre));
    }

    public static BigDecimal decimal(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) {
        return new BigDecimal(texto(configuracion, idEmpresa, modulo, nombre));
    }

    public static Set<String> conjunto(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre, String separador) {
        return Arrays.stream(texto(configuracion, idEmpresa, modulo, nombre).split(separador)).collect(Collectors.toSet());
    }

    /** Legacy {@code FechaDB.ObtenerFechaHoraSistemaUTC} date part: {@code PLATAFORMA/PLA.FechaSistema}. */
    public static String fechaSistema(ConfiguracionSistema configuracion, Long idEmpresa) {
        return texto(configuracion, idEmpresa, PLATAFORMA, "PLA.FechaSistema");
    }
}
