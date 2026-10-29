package cl.bcs.tesoreria.familia_b.pending;

import java.util.Map;
import java.util.TreeMap;

import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/** PENDING test double that renders the oracle shim format: {@code [CODE]{k=v, ...}} (keys sorted); a null value map renders {@code CODE sin traduccion}. */
public class DiccionarioDeCaso implements DiccionarioMensajes {
    @Override
    public String mensaje(Long idEmpresa, String codigo, Map<String, String> valores) {
        if (valores == null) {
            return codigo + " sin traduccion";
        }
        return "[" + codigo + "]" + new TreeMap<>(valores);
    }
}
