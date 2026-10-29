package cl.bcs.fondomutuo.rescatereinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.rescatereinversion.CasoRre.Perfil;

/**
 * FMM-RRE (F2 W1 front 2): the new {@code CrearSolicitudRescateReinversion} (direct entry and through the Manejador) against the goldens RECORDED from the
 * unmodified legacy by record_rre.sh on the same H2 and scripted collaborators. Compared as for Normal: output text (key order, BigDecimal scale), exception
 * (class; message only for a plain {@code Exception}), CallServlet decision, the four tables (requests, destination rows, persistent lock, AGF), every
 * collaborator call (name, arguments, answer), the sequences and the session (local tables left, autonomous commits). One approved difference: DEV-6-H1
 * (the local table is dropped when the lock fails), listed in aprobadas.json.
 */
class RreEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();

    static Stream<String> casos() {
        List<String> ids = CasoRre.casos();
        assertTrue(ids.size() >= 30, "the FMM-RRE matrix has at least 30 cases, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FMM-RRE equivalence with the recorded legacy")
    void equivalente(String id) throws Exception {
        comparar(id, CasoRre.golden(id), CasoRre.ejecutar(id, Perfil.ABIERTAS));
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        JsonObject aprobadas = CasoRre.leer(CasoRre.recursos().resolve("aprobadas.json"));
        boolean tablaSoltada = aprobadas.getAsJsonObject("tablaLocalSoltadaEnFallo").has(id);
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
            if (tablaSoltada && clave.equals("sesion")) {
                // DEV-6-H1 (approved by the user 2026-10-26, see aprobadas.json): the legacy golden keeps the leaked session-local table; the new code
                // drops it. Nothing else of the session may differ.
                JsonObject esperada = expected.getAsJsonObject("sesion").deepCopy();
                assertEquals(1, esperada.getAsJsonArray("tablasLocalesSinDrop").size(), id + " :: the golden records the leak");
                esperada.add("tablasLocalesSinDrop", new JsonArray());
                assertEquals(texto(esperada), texto(actual.get(clave)), id + " :: sesion (leak fixed, DEV-6-H1)");
                continue;
            }
            assertEquals(texto(expected.get(clave)), texto(actual.get(clave)), id + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), id + " :: exception presence: " + actual.get("excepcion"));
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class (" + a + ")");
            }
        }
    }

    static String texto(JsonElement e) {
        if (e == null) return "<absent>";
        if (e.isJsonArray() && !e.getAsJsonArray().isEmpty() && e.getAsJsonArray().get(0).isJsonObject() && e.getAsJsonArray().get(0).getAsJsonObject().has("colaborador")) {
            JsonArray n = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) {
                JsonObject o = x.getAsJsonObject();
                JsonObject m = new JsonObject();
                m.add("colaborador", o.get("colaborador"));
                m.add("argumentos", o.get("argumentos"));
                m.add("respuesta", o.get("respuesta"));
                n.add(m);
            }
            return PRETTY.toJson(canon(n));
        }
        if (e.isJsonPrimitive()) return e.getAsString();
        return PRETTY.toJson(canon(e));
    }

    static JsonElement canon(JsonElement e) {
        if (e == null || e.isJsonNull()) return JsonNull.INSTANCE;
        if (e.isJsonArray()) {
            JsonArray a = new JsonArray();
            for (JsonElement x : e.getAsJsonArray()) a.add(canon(x));
            return a;
        }
        if (e.isJsonObject()) {
            TreeMap<String, JsonElement> m = new TreeMap<>();
            for (String k : e.getAsJsonObject().keySet()) m.put(k, canon(e.getAsJsonObject().get(k)));
            JsonObject o = new JsonObject();
            m.forEach(o::add);
            return o;
        }
        return e;
    }
}
