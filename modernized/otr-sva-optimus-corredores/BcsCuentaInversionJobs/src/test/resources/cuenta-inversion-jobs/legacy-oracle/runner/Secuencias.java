import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Sequence consumption as compared: consecutive requests of the same sequence are added up and requests of zero values are dropped, because the
 * legacy asks for N values in one call (Utils.getSecuencia(conn, name, N), recorded as "name#N") and the modern Secuencias port gives one value per call.
 */
final class Secuencias {

    private Secuencias() {
    }

    static JsonArray agregadas(List<String> llamadas) {
        JsonArray out = new JsonArray();
        String ultimo = null;
        long cantidad = 0;
        for (String l : llamadas) {
            int i = l.lastIndexOf('#');
            String nombre = l.substring(0, i);
            long n = "null".equals(l.substring(i + 1)) ? 0 : Long.parseLong(l.substring(i + 1));
            if (n <= 0) continue;
            if (nombre.equals(ultimo)) {
                cantidad += n;
            } else {
                emitir(out, ultimo, cantidad);
                ultimo = nombre;
                cantidad = n;
            }
        }
        emitir(out, ultimo, cantidad);
        return out;
    }

    private static void emitir(JsonArray out, String nombre, long cantidad) {
        if (nombre == null) return;
        JsonObject o = new JsonObject();
        o.addProperty("nombre", nombre);
        o.addProperty("cantidad", cantidad);
        out.add(o);
    }
}
