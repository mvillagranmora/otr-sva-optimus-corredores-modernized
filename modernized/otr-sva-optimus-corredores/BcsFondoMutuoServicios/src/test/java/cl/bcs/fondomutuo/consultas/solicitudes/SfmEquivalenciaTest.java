package cl.bcs.fondomutuo.consultas.solicitudes;

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

import cl.bcs.fondomutuo.consultas.solicitudes.CasoSfm.Perfil;

/**
 * FMM-SFM (F3 W1 front 3), rules FM-SFM-01..08 (RULES_FONDOMUTUO_F3_A.md 8.3): the new {@code ConsultarSolicitudesFFMM} and {@code ConsultarSolicitudesFFMMWS}
 * (through their JavaCall handlers) against the goldens RECORDED from the unmodified legacy classes by record_sfm.sh on the same H2 (the view FMM.ConsultarSolicitudesFFMM
 * and the three Tesoreria tables are stand-in tables: see {@code README.md}). Compared at the same points as {@code FsaEquivalenciaTest}: output text (key order, number
 * text such as {@code 0E-8}, Gson default date text), exception (class; the message only for a plain {@code Exception}), CallServlet decision, the tables (read only:
 * they must come back untouched), the collaborator calls (none), the sequences (none) and the session (local tables left, autonomous commits). A golden that is missing
 * FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SfmEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoSfm.casos();
        assertTrue(ids.size() >= 300, "the FMM-SFM matrix has at least 300 cases (SFM and WS twins), found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FMM-SFM: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoSfm.casos()) {
            if (!Files.exists(CasoSfm.recursos().resolve("cases").resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_sfm.sh): " + sin);
    }

    @Test
    @Order(0)
    @DisplayName("FMM-SFM: both classes are covered (FM-SFM-01..08 for SFM and the twin WS cases FM-SFM-07/08)")
    void ambasClasesEstanCubiertas() {
        long sfm = CasoSfm.casos().stream().filter(c -> CasoSfm.servicio(c).equals(CasoSfm.SFM)).count();
        long ws = CasoSfm.casos().stream().filter(c -> CasoSfm.servicio(c).equals(CasoSfm.WS)).count();
        assertTrue(sfm >= 150, "SFM cases: " + sfm);
        assertTrue(ws >= 150, "WS cases: " + ws);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-SFM equivalence with the recorded legacy (FM-SFM-01..08)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoSfm.golden(id), CasoSfm.ejecutar(id, Perfil.ABIERTA));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-SFM: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoSfm.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoSfm.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
            if (clave.equals("salidaCruda") || clave.equals("decisionServlet")) {
                if (desvioKit(id, expected, actual)) continue;
            }
            assertEquals(texto(expected.get(clave)), texto(actual.get(clave)), id + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), id + " :: exception presence: " + actual.get("excepcion"));
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (desvioKit(id, expected, actual)) {
                assertTrue(a.startsWith("Exception: Parametro ") && a.endsWith(" no encontrado"), id + " :: DESVIO-KIT-1 expects the kit's 'Parametro ... no encontrado': " + a);
            } else if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class (" + a + ")");
            }
        }
    }

    /**
     * DESVIO-KIT-1: the legacy NullPointerException of an unknown / null company in ConsultarSolicitudes* (and ConsultarDetalleSolicitud) against the kit's
     * ParametroNoEncontradoException. Only the listed slugs, only when the golden is that NPE and the actual is the kit's "Parametro ... no encontrado".
     */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        String slug = CasoSfm.slug(id);
        if (slug.startsWith("ws-")) slug = slug.substring(3);
        return CasoSfm.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(slug) && expected.has("excepcion") && expected.get("excepcion").getAsString().startsWith("NullPointerException:")
                && actual.has("excepcion") && actual.get("excepcion").getAsString().startsWith("Exception: Parametro ");
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
