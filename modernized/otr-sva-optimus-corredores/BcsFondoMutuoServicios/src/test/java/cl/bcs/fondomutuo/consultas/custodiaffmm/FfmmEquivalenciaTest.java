package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

/**
 * FM-CPFF (F3 W1 front 9, part A), rules FM-CPFF-01..20 (RULES_FONDOMUTUO_F3_C.md 8.1 .. 8.4): the new {@code ConsultarCustodiaPortafolioFFMM} (through the adapter of the port CPF consumes)
 * against the goldens RECORDED from the unmodified legacy class (real {@code SQL.query}, {@code ConfiguracionSistema}, {@code Fecha}, {@code Utils}) by record_ffmm.sh on the same H2 and the same
 * scripted {@code ConsultarEmpresa}. Compared at the same points as the other fronts: output text (key order, number text and SCALE such as {@code 0E+8} / {@code 980000.0000000000000000},
 * null keys written because the legacy uses {@code Utils.getGsonBuilder()}), exception (class; the message only for a plain {@code Exception}), CallServlet decision (D-F3A-27: no error counter =>
 * ROLLBACK in EVERY case), the tables (read only: they must come back untouched), every collaborator call (name, arguments, answer), the sequences (none) and the session. The rows of {@code p_out_tbl}
 * are compared FIRST by key (a different content is reported as such) and THEN by position (D-F3C-13: the order is the iteration order of a {@code HashMap<String,Instrumento>}: a different order is
 * reported as an order difference). A golden that is missing FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FfmmEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoFfmm.casos();
        assertTrue(ids.size() >= 100, "the FM-CPFF matrix has at least 100 cases (24 of RULES section 11 plus the limits), found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FM-CPFF: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoFfmm.casos()) {
            if (!Files.exists(CasoFfmm.raizCasos().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_ffmm.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FM-CPFF equivalence with the recorded legacy (FM-CPFF-01..20)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoFfmm.golden(id), CasoFfmm.ejecutar(id));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FM-CPFF: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoFfmm.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoFfmm.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
        compararFilas(id, expected, actual);
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
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

    /** First by key (content), then by position (D-F3C-13): see the class comment. */
    private static void compararFilas(String id, JsonObject expected, JsonObject actual) {
        List<String> ke = new ArrayList<>();
        List<String> ka = new ArrayList<>();
        Map<String, String> fe = filas(expected, ke);
        Map<String, String> fa = filas(actual, ka);
        assertEquals(fe == null, fa == null, id + " :: rows presence");
        if (fe == null) return;
        assertEquals(fe, fa, id + " :: rows by key (" + CLAVE + ")");
        assertEquals(ke, ka, id + " :: ORDER of p_out_tbl (D-F3C-13: HashMap iteration order of the keys)");
    }

    static final String CLAVE = "IdInstrumento_Nemotecnico_IdEmisor_IdCliente_IdPortafolio_IdRegimenFondoMutuo_Relacion_IdBoveda";

    static Map<String, String> filas(JsonObject dump, List<String> orden) {
        if (!dump.has("salida") || !dump.get("salida").isJsonObject() || !dump.getAsJsonObject("salida").has("p_out_tbl")) return null;
        Map<String, String> m = new LinkedHashMap<>();
        for (JsonElement f : dump.getAsJsonObject("salida").getAsJsonArray("p_out_tbl")) {
            JsonObject o = f.getAsJsonObject();
            String clave = String.join("_", campo(o, "IdInstrumento"), campo(o, "Nemotecnico"), campo(o, "IdEmisor"), campo(o, "IdCliente"), campo(o, "IdPortafolio"), campo(o, "IdRegimenFondoMutuo"),
                    campo(o, "Relacion"), campo(o, "IdBoveda"));
            orden.add(clave);
            m.put(clave, PRETTY.toJson(canon(o)));
        }
        return m;
    }

    private static String campo(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? "null" : e.getAsString();
    }

    /** DESVIO-KIT-1 (see {@link CasoFfmm#DESVIO_KIT_EMPRESA_SIN_CONFIGURACION}): the legacy NPE of an unknown / null company against the kit's ParametroNoEncontradoException. */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoFfmm.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoFfmm.slug(id)) && expected.has("excepcion")
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
