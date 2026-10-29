package cl.bcs.cuentainversion.transaccional.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;

import cl.bcs.cuentainversion.port.out.SecuenciasPort;

/**
 * Sequences scripted by the case ({@code case.json "secuencias": {name: firstValue}}), as the Utils.getSecuencia of the
 * oracle returned them: consecutive values from the first one. Requests are recorded as {@code "name#count"} like the golden
 * "secuenciasSolicitadas".
 */
public final class SecuenciasDeCaso implements SecuenciasPort {

    private final Map<String, Long> siguiente = new HashMap<>();
    private final List<String> solicitadas = new ArrayList<>();

    public SecuenciasDeCaso(Caso caso) {
        if (caso.caso().has("secuencias")) {
            for (Map.Entry<String, JsonElement> e : caso.caso().getAsJsonObject("secuencias").entrySet()) {
                siguiente.put(e.getKey(), e.getValue().getAsLong());
            }
        }
    }

    public List<String> solicitadas() {
        return solicitadas;
    }

    @Override
    public List<Long> siguientes(String nombre, Long cantidad) {
        solicitadas.add(nombre + "#" + cantidad);
        List<Long> salida = new ArrayList<>();
        if (cantidad == null || cantidad <= 0) {
            return salida;
        }
        long next = siguiente.getOrDefault(nombre, 1L);
        for (long i = 0; i < cantidad; i++) {
            salida.add(next++);
        }
        siguiente.put(nombre, next);
        return salida;
    }
}
