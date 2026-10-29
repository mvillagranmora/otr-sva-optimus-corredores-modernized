package cl.bcs.apv.familia_a.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Sequences of the case: case.json "secuencias" gives the first value of each named sequence (default 1); every call hands out the next value, as
 * the legacy oracle shim of {@code Utils.getSecuencia} did. The requested names are kept in order.
 */
public class SecuenciasDeCaso implements Secuencias {

    private final Map<String, Long> siguiente = new HashMap<>();
    private final List<String> solicitadas = new ArrayList<>();

    public SecuenciasDeCaso(Caso caso) {
        JsonObject s = caso.caso().has("secuencias") ? caso.caso().getAsJsonObject("secuencias") : new JsonObject();
        for (Map.Entry<String, JsonElement> e : s.entrySet()) {
            siguiente.put(e.getKey(), e.getValue().getAsLong());
        }
    }

    @Override
    public long siguiente(String nombre) {
        solicitadas.add(nombre);
        long valor = siguiente.getOrDefault(nombre, 1L);
        siguiente.put(nombre, valor + 1);
        return valor;
    }

    public List<String> solicitadas() {
        return solicitadas;
    }
}
