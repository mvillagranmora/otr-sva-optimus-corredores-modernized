package cl.bcs.plataforma.login.h2;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.gson.JsonObject;

import cl.bcs.plataforma.login.ProcedimientoAlmacenado;

/**
 * The stored procedures of the HANA catalog, scripted for the tests (H2 has none, and the real ones write: the recovery procedures generate the
 * provisional password and send the notification). Each procedure is an answer function that also receives the connection of the request, so a test
 * can make it write on that connection and then see the write confirmed or rolled back with the rest of the request (T-22, T-13). Every call is
 * recorded in order.
 */
public final class ProcedimientosGuionados implements ProcedimientoAlmacenado {

    /** one recorded call */
    public record Llamada(String ruta, JsonObject entrada) {
    }

    private final Map<String, BiFunction<Connection, JsonObject, JsonObject>> respuestas = new HashMap<>();
    private final List<Llamada> llamadas = new ArrayList<>();

    public ProcedimientosGuionados() {
        // the clock of the platform: system date 2026-03-02
        responder("BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema", (c, in) -> salida("P_OUT_FECHAHORASISTEMA", "2026-03-02 09:30:00"));
    }

    public ProcedimientosGuionados responder(String ruta, BiFunction<Connection, JsonObject, JsonObject> respuesta) {
        respuestas.put(ruta, respuesta);
        return this;
    }

    public ProcedimientosGuionados responderSiempre(String ruta, JsonObject salida) {
        return responder(ruta, (c, in) -> salida);
    }

    public List<Llamada> llamadas() {
        return llamadas;
    }

    public List<Llamada> llamadasA(String ruta) {
        return llamadas.stream().filter(l -> l.ruta().equals(ruta)).toList();
    }

    @Override
    public JsonObject llamar(Connection con, String ruta, JsonObject entrada) throws Exception {
        llamadas.add(new Llamada(ruta, entrada.deepCopy()));
        BiFunction<Connection, JsonObject, JsonObject> r = respuestas.get(ruta);
        if (r == null) {
            throw new IllegalStateException("procedure not scripted: " + ruta);
        }
        return r.apply(con, entrada);
    }

    public static JsonObject salida(String clave, String valor) {
        JsonObject o = new JsonObject();
        o.addProperty(clave, valor);
        return o;
    }

    public static JsonObject salida(String... paresClaveValor) {
        JsonObject o = new JsonObject();
        for (int i = 0; i < paresClaveValor.length; i += 2) {
            o.addProperty(paresClaveValor[i], paresClaveValor[i + 1]);
        }
        return o;
    }
}
