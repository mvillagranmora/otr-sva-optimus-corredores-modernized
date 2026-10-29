package cl.bcs.tesoreria.javacall.comun;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.tesoreria.port.out.ConfiguracionSistema;

/**
 * Typed access to the configuration parameters with the legacy contract of {@code ConfiguracionSistema.getParametroConfiguracion*}: a parameter that
 * the company does not have is an error ({@code java.lang.Exception} "Parametro X en modulo Y no encontrado"), not a null.
 * The port itself answers null, so each service decides; these helpers are the "mandatory" case.
 */
public final class Parametros {

    public static final String PLATAFORMA = "PLATAFORMA";
    public static final String TESORERIA = "TESORERIA";
    public static final String CUENTAINVERSION = "CUENTAINVERSION";

    private Parametros() {
    }

    public static String texto(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        String valor = configuracion.parametro(idEmpresa, modulo, nombre);
        if (valor == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return valor;
    }

    public static long entero(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        return Long.parseLong(texto(configuracion, idEmpresa, modulo, nombre));
    }

    public static BigDecimal decimal(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre) throws Exception {
        return new BigDecimal(texto(configuracion, idEmpresa, modulo, nombre));
    }

    public static Set<String> conjunto(ConfiguracionSistema configuracion, Long idEmpresa, String modulo, String nombre, String separador) throws Exception {
        return Arrays.stream(texto(configuracion, idEmpresa, modulo, nombre).split(separador)).collect(Collectors.toSet());
    }
}
