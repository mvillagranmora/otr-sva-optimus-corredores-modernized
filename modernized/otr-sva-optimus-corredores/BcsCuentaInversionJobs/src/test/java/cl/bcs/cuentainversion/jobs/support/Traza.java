package cl.bcs.cuentainversion.jobs.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The ordered trace of what the code under test handed to its collaborators, in the shape the oracle records
 * ({@code {servicio, entrada}}), plus the scripted failures ("lanza:&lt;Type&gt;:&lt;message&gt;", see the oracle Hooks).
 */
public final class Traza {

    private final List<JsonObject> llamadas = Collections.synchronizedList(new ArrayList<>());
    private final List<String> secuencias = Collections.synchronizedList(new ArrayList<>());
    private final JsonObject colaboradores;

    public Traza(JsonObject colaboradores) {
        this.colaboradores = colaboradores;
    }

    public void registrar(String servicio, JsonElement entrada) {
        JsonObject o = new JsonObject();
        o.addProperty("servicio", servicio);
        o.add("entrada", Canon.enmascararAleatorios(entrada.deepCopy()));
        llamadas.add(o);
    }

    public JsonObject colaboradores() {
        return colaboradores;
    }

    /** colaboradores.&lt;clave&gt; as an object (empty when absent). */
    public JsonObject objeto(String clave) {
        JsonElement e = colaboradores.get(clave);
        return e == null || e.isJsonNull() ? new JsonObject() : e.getAsJsonObject();
    }

    public String texto(String clave) {
        JsonElement e = colaboradores.get(clave);
        return e == null || e.isJsonNull() ? null : e.getAsString();
    }

    /** Throws the scripted failure of {@code clave} ("lanza:Exception:msg"), if any. */
    public void lanzaSiCorresponde(String clave) throws Exception {
        String t = texto(clave);
        if (t == null || !t.startsWith("lanza:")) {
            return;
        }
        String[] p = t.split(":", 3);
        String msg = p.length > 2 ? p[2] : null;
        switch (p[1]) {
            case "Exception" -> throw new Exception(msg);
            case "RuntimeException" -> throw new RuntimeException(msg);
            case "IllegalStateException" -> throw new IllegalStateException(msg);
            case "SQLException" -> throw new java.sql.SQLException(msg);
            case "NullPointerException" -> throw new NullPointerException(msg);
            case "Error" -> throw new Error(msg);
            default -> throw new IllegalArgumentException("unknown scripted exception " + p[1]);
        }
    }

    public void secuencia(String nombre) {
        secuencias.add(nombre);
    }

    public JsonArray llamadas() {
        JsonArray a = new JsonArray();
        synchronized (llamadas) {
            llamadas.forEach(a::add);
        }
        return a;
    }

    /** Consecutive requests of the same sequence added up ("cantidad"), as the oracle aggregates "name#N" requests. */
    public JsonArray secuencias() {
        JsonArray out = new JsonArray();
        String ultimo = null;
        long n = 0;
        for (String s : List.copyOf(secuencias)) {
            if (s.equals(ultimo)) {
                n++;
            } else {
                emitir(out, ultimo, n);
                ultimo = s;
                n = 1;
            }
        }
        emitir(out, ultimo, n);
        return out;
    }

    private static void emitir(JsonArray out, String nombre, long n) {
        if (nombre != null) {
            JsonObject o = new JsonObject();
            o.addProperty("nombre", nombre);
            o.addProperty("cantidad", n);
            out.add(o);
        }
    }
}
