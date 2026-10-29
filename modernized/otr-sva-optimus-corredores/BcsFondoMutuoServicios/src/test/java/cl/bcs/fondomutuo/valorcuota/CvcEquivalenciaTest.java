package cl.bcs.fondomutuo.valorcuota;

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

import cl.bcs.fondomutuo.valorcuota.CasoCvc.Perfil;

/**
 * FMM-CVC (F3 W2), rules FM-CVC-01..14 and defects D-F3A-16..23 (RULES_FONDOMUTUO_F3_A.md 8.8 and 9): the new {@code CargarValorCuota} (through its JavaCall handler) against the
 * goldens RECORDED from the unmodified legacy by record_cvc.sh on the same H2 and the same scripted collaborator (AgregarLog). Because the class WRITES, the comparison is wider
 * than the one of the read fronts: output text (key order, number text such as {@code 1500.50}, the {@code yyyy-MM-dd HH:mm:ss.SSS} dates of {@code Utils.getGsonBuilder()}),
 * exception (class; the message only for a plain {@code Exception}), the CallServlet decision (commit when the counter is 0, rollback otherwise), the tables BEFORE the request,
 * right AFTER the class returned (what it wrote before the host decided) and AFTER the decision, every collaborator call (AgregarLog: name, arguments, answer), the sequence
 * requests, the earlier requests of the same session and the session. A golden that is missing FAILS the case; a run in which no comparison executed FAILS
 * ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CvcEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoCvc.casos();
        assertTrue(ids.size() >= 150, "the FMM-CVC matrix has at least 150 cases, found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CVC: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCvc.casos()) {
            if (!Files.exists(CasoCvc.recursos().resolve("cases").resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cvc.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CVC equivalence with the recorded legacy (FM-CVC-01..14, D-F3A-16..23)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoCvc.golden(id), CasoCvc.ejecutar(id, Perfil.ABIERTA));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CVC: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCvc.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCvc.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
        for (String clave : new String[] { "salidaCruda", "tablasIniciales", "tablasTrasLaLlamada", "tablas", "colaboradores", "secuenciasSolicitadas", "previas", "sesion",
                "decisionServlet" }) {
            assertEquals(texto(expected.get(clave)), texto(actual.get(clave)), id + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), id + " :: exception presence: " + actual.get("excepcion"));
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (desvio) {
                assertTrue(a.startsWith("Exception: Parametro ") && a.endsWith(" no encontrado"), id + " :: DESVIO-KIT-1 expects the kit's 'Parametro ... no encontrado': " + a);
            } else if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class (" + a + ")");
            }
        }
    }

    /** DESVIO-KIT-1 (see {@link CasoCvc#DESVIO_KIT_EMPRESA_SIN_CONFIGURACION}): the legacy NPE of a company with no configuration against the kit's ParametroNoEncontradoException. */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoCvc.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoCvc.slug(id)) && expected.has("excepcion")
                && expected.get("excepcion").getAsString().startsWith("NullPointerException:") && actual.has("excepcion")
                && actual.get("excepcion").getAsString().startsWith("Exception: Parametro ");
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
