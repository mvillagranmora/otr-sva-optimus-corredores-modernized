package cl.bcs.fondomutuo.normal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
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

import cl.bcs.fondomutuo.normal.CasoNormal.Perfil;

/**
 * FM-NOR-01..44 (plan 7.1): the new {@code CrearSolicitudAporteRescateNormal} against the goldens RECORDED from the unmodified legacy on the same H2 and the same
 * scripted collaborators. Compared: the output text (key order and BigDecimal scale count), the exception (class; the message only for a plain legacy
 * Exception), the decision of CallServlet, the tables (requests, lock, AGF), every collaborator call (name, arguments, answer), the sequences and the session
 * (local tables left, autonomous commits). Cases listed in {@code aprobadas.json} are the approved differences: the 503 of a closed gate, nothing written.
 */
class NormalEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final String SOLICITUDES = "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";

    static Stream<String> casos() {
        List<String> ids = CasoNormal.casos();
        assertTrue(ids.size() >= 30, "the matrix has at least 30 cases of Normal, found " + ids.size());
        return ids.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @DisplayName("FM-NOR equivalence with the recorded legacy")
    void equivalente(String id) throws Exception {
        JsonObject expected = CasoNormal.golden(id);
        JsonObject aprobadas = CasoNormal.leer(CasoNormal.recursos().resolve("aprobadas.json"));
        JsonObject actual = CasoNormal.ejecutar(id, Perfil.ABIERTAS);
        JsonObject caso = CasoNormal.leer(CasoNormal.recursos().resolve("cases").resolve(id).resolve("case.json"));
        if (caso.has("approvedDifference")) {
            // an approved difference written in the case itself (with its provenance): the golden stays the legacy's; the new code must answer the
            // approved decision, write nothing, and really differ from the golden (it is reported as an approved difference, never as "same")
            JsonObject ap = caso.getAsJsonObject("approvedDifference");
            assertTrue(ap.has("procedencia") && ap.get("procedencia").getAsString().contains("aprobada por el usuario"), id + " :: approval without provenance");
            assertEquals(ap.get("decisionServlet").getAsString(), actual.get("decisionServlet").getAsString(), id + " :: approved " + ap.get("dev").getAsString());
            assertTrue(!expected.get("decisionServlet").getAsString().equals(actual.get("decisionServlet").getAsString()), id + " :: approved difference must differ");
            sinEscrituras(id, actual);
            return;
        }
        if (aprobadas.has(id)) {
            String gate = aprobadas.getAsJsonObject(id).get("compuerta").getAsString();
            assertEquals("ROLLBACK_503", actual.get("decisionServlet").getAsString(), id + " :: approved difference " + gate);
            assertEquals(gate, actual.get("compuerta").getAsString(), id + " :: gate");
            sinEscrituras(id, actual);
            return;
        }
        boolean tablaSoltada = aprobadas.has("tablaLocalSoltadaEnFallo") && aprobadas.getAsJsonObject("tablaLocalSoltadaEnFallo").has(id);
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
            if (tablaSoltada && clave.equals("sesion")) {
                // DEV-6-H1 (approved): the legacy golden keeps the leaked session-local table; the new code drops it. Nothing else of the session may differ.
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
            boolean inestable = aprobadas.has("excepcionInestable") && aprobadas.getAsJsonObject("excepcionInestable").has(id);
            if (inestable) {
                // documented in aprobadas.json "excepcionInestable" (reason written there): the legacy text varies between recordings, the class does not
                assertTrue(aprobadas.getAsJsonObject("excepcionInestable").getAsJsonObject(id).get("motivo").getAsString().length() > 40, id + " :: reason");
                assertEquals(ec, ac, id + " :: exception class (unstable text)");
            } else if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class");
            }
        }
    }

    /** DEV-F4: the 503 leaves no request, no AGF row and no lock behind, and the session-local table is dropped. */
    static void sinEscrituras(String id, JsonObject actual) {
        JsonObject tablas = actual.getAsJsonObject("tablas");
        for (String t : tablas.keySet()) {
            assertEquals(0, tablas.getAsJsonArray(t).size(), id + " :: " + t + " must be empty after the 503");
        }
        assertEquals(0, actual.getAsJsonObject("sesion").getAsJsonArray("tablasLocalesSinDrop").size(), id + " :: local table dropped");
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

    static Path unused() {
        return null;
    }
}
