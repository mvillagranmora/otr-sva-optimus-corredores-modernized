package cl.bcs.tesoreria.familia_b.support;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * Test double of the {@link Colaboradores} port: answers each service with the scripted behaviour of the oracle fake of the same name (case.json
 * {@code "colaboradores"}) and records {servicio, entrada} in call order; that list is compared with the golden "colaboradores" trace.
 * The scripted doubles live in {@code support/colaboradores/<Servicio>Guion}. A checked exception of the fake (the legacy throws plain
 * {@code Exception}) is rethrown as is (sneaky) because the port declares none: the goldens pin its simple class name.
 */
public class ColaboradoresDeCaso implements Colaboradores {

    private final JsonObject guiones;
    private final List<JsonObject> traza = new ArrayList<>();

    public ColaboradoresDeCaso(Caso caso) {
        this.guiones = caso.colaboradores();
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        GuionColaborador guion = guion(servicio);
        JsonObject r = new JsonObject();
        r.addProperty("servicio", servicio);
        r.add("entrada", guion.paraTraza(entrada, guiones));
        traza.add(r);
        try {
            return guion.responder(entrada, guiones);
        } catch (Exception e) {
            throw ColaboradoresDeCaso.<RuntimeException>sneaky(e);
        }
    }

    public List<JsonObject> traza() {
        return traza;
    }

    private static GuionColaborador guion(String servicio) {
        String clase = "cl.bcs.tesoreria.familia_b.support.colaboradores." + servicio + "Guion";
        try {
            return (GuionColaborador) Class.forName(clase).getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("no scripted double for the collaborator " + servicio + " (expected " + clase + "): port its oracle fake", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T sneaky(Throwable t) throws T {
        throw (T) t;
    }
}
