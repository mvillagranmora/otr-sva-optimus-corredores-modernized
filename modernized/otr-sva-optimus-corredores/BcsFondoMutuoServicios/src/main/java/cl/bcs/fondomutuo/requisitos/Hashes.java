package cl.bcs.fondomutuo.requisitos;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import cl.bcs.fondomutuo.cierre.ListaHash;

/**
 * Legacy {@code Utils.List2Hash(list, field)}. Step 4A merged the two copies (3A {@code cierre.ListaHash} and this one) into the shared one: this class keeps its
 * name and signature for the 3B callers and delegates (a null list and a repeated key raise the same legacy {@code Exception}s; no behaviour change).
 */
public final class Hashes {

    private Hashes() {
    }

    public static <K, V> HashMap<K, V> aHash(List<V> lista, Function<V, K> clave) throws Exception {
        return ListaHash.aHash(lista, clave);
    }
}
