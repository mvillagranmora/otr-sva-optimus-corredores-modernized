package cl.bcs.cuentainversion.port.out;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translation dictionary of the system (table {@code PLA.Diccionario}, language chosen by {@code PLA.LocalizacionSistema}).
 * Messages are looked up by code and the {@code ::Key::} placeholders of the text are replaced by the given values.
 *
 * <p>Contract kept from the legacy {@code Diccionario}: an unknown code, or {@code valores == null}, renders
 * {@code "<codigo> sin traduccion"}; a {@code null} value replaces its placeholder by the empty string.
 */
public interface DiccionarioMensajes {

    String traducir(Long idEmpresa, String codigo, Map<String, String> valores);

    /** Message without placeholders. */
    default String traducir(Long idEmpresa, String codigo) {
        return traducir(idEmpresa, codigo, new LinkedHashMap<>());
    }

    /**
     * Builds the value map from alternating {@code key, value} arguments; a {@code null} value is left out (legacy fluent
     * {@code Diccionario.add(k, v)} ignores null values, so its placeholder is not replaced).
     */
    static Map<String, String> valores(Object... clavesYValores) {
        Map<String, String> m = new LinkedHashMap<>();
        for (int i = 0; i + 1 < clavesYValores.length; i += 2) {
            Object v = clavesYValores[i + 1];
            if (v != null) {
                m.put(String.valueOf(clavesYValores[i]), v.toString());
            }
        }
        return m;
    }
}
