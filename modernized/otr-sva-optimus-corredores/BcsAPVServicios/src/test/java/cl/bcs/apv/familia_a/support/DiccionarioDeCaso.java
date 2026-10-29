package cl.bcs.apv.familia_a.support;

import java.util.Map;
import java.util.TreeMap;

import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Injectable message dictionary with the text the legacy oracle shim renders: {@code "[CODE]{k1=v1, k2=v2}"} (keys sorted, a null value as an empty
 * text); the goldens contain exactly that, so a golden "descripcion" proves WHICH code and WHICH values were passed. A {@code null} value map gives
 * {@code "CODE sin traduccion"}, as the legacy.
 */
public class DiccionarioDeCaso implements DiccionarioMensajes {

    @Override
    public String mensaje(Long idEmpresa, String codigo, Map<String, String> valores) {
        if (valores == null) {
            return codigo + " sin traduccion";
        }
        Map<String, String> ordenado = new TreeMap<>();
        valores.forEach((k, v) -> ordenado.put(k, v == null ? "" : v));
        return "[" + codigo + "]" + ordenado;
    }
}
