package cl.bcs.fondomutuo.consultas.detalle;

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

import cl.bcs.fondomutuo.consultas.detalle.CasoDet.Perfil;

/**
 * FMM-DET (F3 W1 front 4), rules FM-DET-01..04 (RULES_FONDOMUTUO_F3_A.md 8.6): the new {@code ConsultarDetalleSolicitud} (through its JavaCall handler) against the goldens
 * RECORDED from the unmodified legacy (DET + its delegate ConsultarSolicitudAporteRescateUI + CalcularMontoNumeroCuota, all real) by record_det.sh on the same H2 and the
 * same scripted collaborators ({@code CalcularParidad}, {@code EntregarPreciosTasas}). Compared at the same points as {@code FsaEquivalenciaTest}: output text (key order, number
 * text such as {@code 0E-8}, Gson default date text), exception (class; the message only for a plain {@code Exception}), CallServlet decision, the tables (read only: they
 * must come back untouched), every collaborator call (name, arguments, answer), the sequences (none) and the session. A golden that is missing FAILS the case; a run in which
 * no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DetEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoDet.casos();
        assertTrue(ids.size() >= 90, "the FMM-DET matrix has at least 90 cases, found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FMM-DET: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoDet.casos()) {
            if (!Files.exists(CasoDet.recursos().resolve("cases").resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_det.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-DET equivalence with the recorded legacy (FM-DET-01..04)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoDet.golden(id), CasoDet.ejecutar(id, Perfil.ABIERTA));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-DET: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoDet.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoDet.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
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

    /** DESVIO-KIT-1 (see {@link CasoDet#DESVIO_KIT_EMPRESA_SIN_CONFIGURACION}): the legacy NPE of an unknown / null company against the kit's ParametroNoEncontradoException. */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoDet.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoDet.slug(id)) && expected.has("excepcion")
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
