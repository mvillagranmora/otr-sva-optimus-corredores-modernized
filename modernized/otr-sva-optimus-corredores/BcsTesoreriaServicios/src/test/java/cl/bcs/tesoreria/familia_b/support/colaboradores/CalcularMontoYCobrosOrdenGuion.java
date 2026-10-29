package cl.bcs.tesoreria.familia_b.support.colaboradores;

import java.math.BigDecimal;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.GuionColaborador;

/**
 * Port of the oracle fake of CalcularMontoYCobrosOrden: every order is answered {IdInterno, CantidadOperacion = {@code colaboradores.calcularMontoOrden.cantidad} (10)};
 * {@code "omitir": true} answers no order.
 */
public class CalcularMontoYCobrosOrdenGuion implements GuionColaborador {

    @Override
    public String clave() {
        return "calcularMontoOrden";
    }

    @Override
    public JsonObject responder(JsonObject entrada, JsonObject guiones) {
        JsonObject script = Guiones.objeto(guiones, clave());
        JsonArray ordenes = new JsonArray();
        if (!script.has("omitir")) {
            for (JsonElement e : entrada.getAsJsonArray("p_Ordenes")) {
                JsonObject s = new JsonObject();
                s.add("IdInterno", e.getAsJsonObject().get("IdInterno"));
                s.addProperty("CantidadOperacion", script.has("cantidad") ? script.get("cantidad").getAsBigDecimal() : new BigDecimal("10"));
                ordenes.add(s);
            }
        }
        JsonObject out = new JsonObject();
        out.add("p_out_Ordenes", ordenes);
        return out;
    }
}
