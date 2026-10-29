package cl.bcs.tesoreria.familia_b.pending;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.port.out.Secuencias;

/** PENDING test double: case.json {@code "secuencias": {"name": firstValue}}; each call returns the next value (first, first+1, ...). */
public class SecuenciasDeCaso implements Secuencias {
    private final Map<String, Long> siguientes = new HashMap<>();

    public SecuenciasDeCaso(Caso caso) {
        JsonObject s = caso.caso().has("secuencias") ? caso.caso().getAsJsonObject("secuencias") : new JsonObject();
        s.entrySet().forEach(e -> siguientes.put(e.getKey(), e.getValue().getAsLong()));
    }

    @Override
    public long siguiente(String nombre) {
        long v = siguientes.getOrDefault(nombre, 1L);
        siguientes.put(nombre, v + 1);
        return v;
    }
}
