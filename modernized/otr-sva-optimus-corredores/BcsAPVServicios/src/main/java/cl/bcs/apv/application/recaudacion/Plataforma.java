package cl.bcs.apv.application.recaudacion;

import java.util.HashMap;
import java.util.Map;

import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Helpers over the platform ports with the semantics of the legacy {@code ConfiguracionSistema} / {@code Diccionario} statics: an unconfigured
 * parameter THROWS (the port answers null).
 */
final class Plataforma {

    private Plataforma() {
    }

    /** Legacy {@code getParametroConfiguracionString}. */
    static String parametro(ConfiguracionSistema config, Long idEmpresa, String modulo, String nombre) throws Exception {
        String v = config.parametro(idEmpresa, modulo, nombre);
        if (v == null) {
            throw new Exception("Parametro " + nombre + " en modulo " + modulo + " no encontrado");
        }
        return v;
    }

    /** Legacy {@code getParametroConfiguracionLong}. */
    static long parametroLong(ConfiguracionSistema config, Long idEmpresa, String modulo, String nombre) throws Exception {
        return Long.parseLong(parametro(config, idEmpresa, modulo, nombre));
    }

    /** {@code PLATAFORMA / PLA.FechaSistema} (legacy {@code Utils.getFechaSistema}): the business date, yyyy-MM-dd. */
    static String fechaSistema(ConfiguracionSistema config, Long idEmpresa) throws Exception {
        return parametro(config, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
    }

    /** Legacy {@code new MensajeSalida(id, tipo, codigo, valores, con, idEmpresa)} (a null map renders "CODE sin traduccion"). */
    static MensajeLegacy mensaje(DiccionarioMensajes diccionario, Long idEmpresa, Long id, String tipo, String codigo, Map<String, String> valores) {
        return new MensajeLegacy(id, tipo, codigo, diccionario.mensaje(idEmpresa, codigo, valores));
    }

    /** Legacy {@code Diccionario.getMensaje(...).add(k, v)...msg()}: a null value is ignored, any other is stored as its text. */
    static Valores valores() {
        return new Valores();
    }

    static final class Valores extends HashMap<String, String> {
        private static final long serialVersionUID = 1L;

        Valores con(String llave, Object valor) {
            if (valor != null) {
                put(llave, valor.toString());
            }
            return this;
        }
    }
}
