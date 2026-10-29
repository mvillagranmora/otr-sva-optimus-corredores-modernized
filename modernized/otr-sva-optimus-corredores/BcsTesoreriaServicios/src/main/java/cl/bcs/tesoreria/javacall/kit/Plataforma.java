package cl.bcs.tesoreria.javacall.kit;

import java.util.HashMap;
import java.util.Map;

import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Helpers over the platform ports with the semantics of the legacy {@code ConfiguracionSistema} / {@code Diccionario} statics.
 * The ports return {@code null} for an unconfigured parameter; the legacy threw, and the services must too.
 */
public final class Plataforma {

    private Plataforma() {
    }

    /** Legacy {@code getParametroConfiguracionString}: throws {@code Exception("Parametro X en modulo Y no encontrado")} when not configured. */
    public static String parametro(ConfiguracionSistema config, Long idEmpresa, String modulo, String nombre) throws Exception {
        String v = config.parametro(idEmpresa, modulo, nombre);
        if (v == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return v;
    }

    /** Legacy {@code getParametroConfiguracionLong}. */
    public static long parametroLong(ConfiguracionSistema config, Long idEmpresa, String modulo, String nombre) throws Exception {
        return Long.parseLong(parametro(config, idEmpresa, modulo, nombre));
    }

    /** {@code PLATAFORMA / PLA.FechaSistema}: the business date, {@code yyyy-MM-dd}. */
    public static String fechaSistema(ConfiguracionSistema config, Long idEmpresa) throws Exception {
        return parametro(config, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    }

    /** Message text of a code: legacy {@code new MensajeSalida(id, tipo, codigo, valores, con, idEmpresa)} / {@code Diccionario.getMensaje(...).add(..).msg()}. */
    public static MensajeSalida mensaje(DiccionarioMensajes diccionario, Long idEmpresa, Long id, String tipo, String codigo, Map<String, String> valores) {
        return new MensajeSalida(id, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, valores));
    }

    /** Fluent placeholder map of the legacy {@code Diccionario.add(k, v)}: a null value is ignored, any other is stored as its text. */
    public static Valores valores() {
        return new Valores();
    }

    public static final class Valores extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;

        public Valores con(String llave, Object valor) {
            if (valor != null) {
                put(llave, valor.toString());
            }
            return this;
        }
    }
}
