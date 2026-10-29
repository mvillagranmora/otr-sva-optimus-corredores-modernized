package cl.bcs.fondomutuo.cierre;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Legacy {@code Utils.List2Hash(list, keyfield)} with the key given as a function instead of a reflective field name: a {@code null} list is
 * {@code Exception("List must be not null")}, an empty one gives an empty map, a repeated key is {@code Exception("key field is not unique")}.
 */
public final class ListaHash {

    private ListaHash() {
    }

    public static <K, V> HashMap<K, V> aHash(List<V> lista, Function<V, K> clave) throws Exception {
        HashMap<K, V> hobj = new HashMap<K, V>();
        if (lista == null) throw new Exception("List must be not null");
        if (lista.size() == 0) return hobj;
        for (V obj : lista) {
            K kf = clave.apply(obj);
            if (hobj.containsKey(kf)) {
                throw new Exception("key field is not unique");
            }
            hobj.put(kf, obj);
        }
        return hobj;
    }
}
