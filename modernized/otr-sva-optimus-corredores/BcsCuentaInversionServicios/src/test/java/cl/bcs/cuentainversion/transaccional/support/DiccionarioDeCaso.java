package cl.bcs.cuentainversion.transaccional.support;

import java.util.Map;
import java.util.TreeMap;

import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;

/**
 * Deterministic dictionary the oracle used ({@code legacy-oracle/shims/.../Diccionario.java}): a message renders as
 * {@code [CODE]{k1=v1, k2=v2}} (keys sorted, null value -> empty) so a golden proves WHICH code and WHICH values were passed.
 * {@code valores == null} renders {@code CODE sin traduccion} as in the legacy.
 */
public final class DiccionarioDeCaso implements DiccionarioMensajes {

    @Override
    public String traducir(Long idEmpresa, String codigo, Map<String, String> valores) {
        if (valores == null) {
            return codigo + " sin traduccion";
        }
        Map<String, String> ordenado = new TreeMap<>();
        valores.forEach((k, v) -> ordenado.put(k, v == null ? "" : v));
        return "[" + codigo + "]" + ordenado;
    }
}
