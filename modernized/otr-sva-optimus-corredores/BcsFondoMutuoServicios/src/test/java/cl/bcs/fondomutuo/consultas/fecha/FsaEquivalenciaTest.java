package cl.bcs.fondomutuo.consultas.fecha;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.consultas.fecha.CasoFsa.Perfil;

/**
 * FMM-FSA (F3 W1 front 1): the new {@code ConsultarFechaSolicitudAporteRescate} (through its JavaCall handler) against the goldens RECORDED from the unmodified
 * legacy by record_fsa.sh on the same H2 and the same scripted collaborator ({@code ConsultarEmpresa}). Compared at the same points as
 * {@code RreEquivalenciaTest}: output text (key order, date text), exception (class; the message only for a plain {@code Exception}), CallServlet decision,
 * the tables (read only: they must come back untouched), every collaborator call (name, arguments, answer), the sequences (none) and the session (local tables
 * left, autonomous commits). A golden that is missing FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FsaEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoFsa.casos();
        assertTrue(ids.size() >= 100, "the FMM-FSA matrix has at least 100 cases, found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FMM-FSA: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoFsa.casos()) {
            if (!Files.exists(CasoFsa.recursos().resolve("cases").resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_fsa.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-FSA equivalence with the recorded legacy")
    void equivalente(String id) throws Exception {
        comparar(id, CasoFsa.golden(id), CasoFsa.ejecutar(id, Perfil.ABIERTA));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-FSA: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoFsa.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoFsa.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
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
