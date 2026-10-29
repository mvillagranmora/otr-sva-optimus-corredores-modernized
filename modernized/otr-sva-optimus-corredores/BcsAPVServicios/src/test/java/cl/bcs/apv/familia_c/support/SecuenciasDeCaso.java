package cl.bcs.apv.familia_c.support;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.apv.domain.port.out.Secuencias;

/** case.json {@code "secuencias": {"name": firstValue}}; each call returns the next value (first, first+1, ...); unknown sequences start at 1. */
public class SecuenciasDeCaso implements Secuencias {
    private final Map<String, Long> siguientes = new HashMap<>();

    public SecuenciasDeCaso(Caso caso) {
        JsonObject s = caso.caso().has("secuencias") ? caso.caso().getAsJsonObject("secuencias") : new JsonObject();
        s.entrySet().forEach(e -> siguientes.put(e.getKey(), e.getValue().getAsLong()));
    }

    private final java.util.List<String> pedidas = new java.util.ArrayList<>();

    /** Sequence names requested, one entry per value taken (compared with the golden "secuenciasSolicitadas", expanded). */
    public java.util.List<String> pedidas() {
        return pedidas;
    }

    @Override
    public long siguiente(String nombre) {
        pedidas.add(nombre);
        long v = siguientes.getOrDefault(nombre, 1L);
        siguientes.put(nombre, v + 1);
        return v;
    }
}
