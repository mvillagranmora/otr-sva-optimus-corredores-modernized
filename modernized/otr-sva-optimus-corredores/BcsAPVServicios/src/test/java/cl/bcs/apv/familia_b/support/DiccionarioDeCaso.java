package cl.bcs.apv.familia_b.support;

import java.util.Map;
import java.util.TreeMap;

import cl.bcs.apv.domain.port.out.DiccionarioMensajes;

/**
 * Renders the oracle shim format {@code [CODE]{k=v, ...}} (keys sorted, a null value renders empty as the shim does); a null value map renders
 * {@code CODE sin traduccion}.
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
