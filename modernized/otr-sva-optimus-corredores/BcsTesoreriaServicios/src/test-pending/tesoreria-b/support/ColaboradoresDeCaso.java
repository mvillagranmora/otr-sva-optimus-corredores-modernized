package cl.bcs.tesoreria.familia_b.pending;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * PENDING test double of the {@link Colaboradores} port: answers each service with the object scripted in case.json {@code "colaboradores"} under the key
 * the oracle shim reads (first letter lower case: RevisarRequisitosBasicos -> requisitosBasicos, CrearIngresoEgreso -> crearIngresoEgreso, ...; see the shim Javadoc) and
 * records {servicio, entrada} in call order: that list is compared with the golden "colaboradores" trace.
 */
public class ColaboradoresDeCaso implements Colaboradores {

    private final JsonObject guion;
    private final List<JsonObject> traza = new ArrayList<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guion = caso.colaboradores();
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        JsonObject r = new JsonObject();
        r.addProperty("servicio", servicio);
        r.add("entrada", entrada);
        traza.add(r);
        String clave = Character.toLowerCase(servicio.charAt(0)) + servicio.substring(1);
        return guion != null && guion.has(clave) && guion.get(clave).isJsonObject() ? guion.getAsJsonObject(clave) : new JsonObject();
    }

    public List<JsonObject> traza() {
        return traza;
    }
}
