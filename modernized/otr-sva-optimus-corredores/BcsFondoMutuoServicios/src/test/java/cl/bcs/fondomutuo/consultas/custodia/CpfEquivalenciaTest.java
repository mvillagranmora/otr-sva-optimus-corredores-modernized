package cl.bcs.fondomutuo.consultas.custodia;

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

import cl.bcs.fondomutuo.consultas.custodia.CasoCpf.Perfil;

/**
 * FMM-CPF (F3 W1 front 8), rules FM-CPF-01..14 (RULES_FONDOMUTUO_F3_A.md 8.2): the new {@code ConsultarCustodiaPortafolioFondoMutuo} (through its JavaCall handler) against the goldens
 * RECORDED from the unmodified legacy (CPF + CalcularMontoNumeroCuota + ObtenerDiaSiguiente, all real) by record_cpf.sh on the same H2 and the same scripted collaborators (the custody
 * calculation, {@code CalcularParidad}, {@code EntregarPreciosTasas}, {@code ConsultarEmpresa}). Compared at the same points as the other fronts: output text (key order, number text such
 * as {@code 0E-8}, null keys written because the legacy uses {@code Utils.getGsonBuilder()}), exception (class; the message only for a plain {@code Exception}), CallServlet decision
 * (D-F3A-27: the output has no error counter, so ROLLBACK in EVERY case), the tables (read only: they must come back untouched), every collaborator call (name, arguments, answer: the
 * order of the calls is part of it), the sequences (none) and the session. The rows of {@code P_OUT_TBL} are compared FIRST by key
 * ({@code IdCliente_IdPortafolio_IdInstrumento_IdRegimenFondoMutuo}: a different content is reported as such) and THEN by position (D-F3A-04: the order is the iteration order of a
 * {@code HashMap<String,..>} and is stable for the same keys: a different order is reported as an order difference). A golden that is missing FAILS the case; a run in which no
 * comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CpfEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoCpf.casos();
        assertTrue(ids.size() >= 150, "the FMM-CPF matrix has at least 150 cases, found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CPF: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCpf.casos()) {
            if (!Files.exists(CasoCpf.raizCasos().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cpf.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CPF equivalence with the recorded legacy (FM-CPF-01..14)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoCpf.golden(id), CasoCpf.ejecutar(id, Perfil.ABIERTA));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CPF: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCpf.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCpf.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
        expected = conIdsCanonicos(id + " (golden)", expected);
        actual = conIdsCanonicos(id + " (new code)", actual);
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

    /**
     * D-F3A-04: the legacy assigns the {@code IdInterno} of the solicitudes (2, 3, ...) inside a {@code parallelStream().forEach}: WHICH row gets WHICH id is a property of the JDK's splitting
     * of the list (the recorded runs of the oracle give, for 2 / 3 / 4 rows, the pattern 3,2 / 4,3,2 / 5,4,2,3: reversed, not the list order) and of the thread timing, never a rule. The ids
     * are only used to cross-reference the valuation results with their rows. So the calls of the FIRST valuation (the ones before the custody calculation: {@code EntregarPreciosTasas}
     * {@code IdRegistro} = +-id and {@code CalcularParidad} {@code IdInterno} = +-(4 id + k), or +-id for IdTrabajo 1) are compared MODULO the renaming of those ids: the SET of ids must be exactly
     * 2..n+1 (starts at 2, unique, no gap: that IS pinned) and they are renamed by order of first appearance. Rows are compared as they are.
     */
    static JsonObject conIdsCanonicos(String etiqueta, JsonObject dump) {
        if (!dump.has("colaboradores") || !dump.get("colaboradores").isJsonArray()) return dump;
        JsonArray llamadas = dump.getAsJsonArray("colaboradores").deepCopy();
        int fin = llamadas.size();
        for (int i = 0; i < llamadas.size(); i++) {
            if ("ConsultarCustodiaPortafolioFFMM".equals(llamadas.get(i).getAsJsonObject().get("colaborador").getAsString())) {
                fin = i;
                break;
            }
        }
        java.util.LinkedHashMap<Long, Long> canonico = new java.util.LinkedHashMap<>();
        for (int i = 0; i < fin; i++) {
            JsonObject llamada = llamadas.get(i).getAsJsonObject();
            JsonObject arg = llamada.getAsJsonArray("argumentos").get(0).getAsJsonObject();
            if ("EntregarPreciosTasas".equals(llamada.get("colaborador").getAsString())) {
                for (JsonElement d : arg.getAsJsonArray("p_DatosInstrumentos")) canonico.putIfAbsent(Math.abs(d.getAsJsonObject().get("IdRegistro").getAsLong()), 0L);
            } else if ("CalcularParidad".equals(llamada.get("colaborador").getAsString())) {
                boolean cuatro = arg.get("p_IdTrabajo").getAsLong() == 2L;
                for (JsonElement c : arg.getAsJsonArray("p_CalculosEntrada")) {
                    long x = Math.abs(c.getAsJsonObject().get("IdInterno").getAsLong());
                    canonico.putIfAbsent(cuatro ? x / 4 : x, 0L);
                }
            }
        }
        if (canonico.isEmpty()) return dump;
        long siguiente = 2L;
        for (Long k : new ArrayList<>(canonico.keySet())) canonico.put(k, siguiente++);
        List<Long> originales = new ArrayList<>(canonico.keySet());
        java.util.Collections.sort(originales);
        for (int j = 0; j < originales.size(); j++) {
            assertEquals(2L + j, originales.get(j).longValue(), etiqueta + " :: the IdInterno of the solicitudes must be 2..n+1 without gaps (D-F3A-04), found " + originales);
        }
        for (int i = 0; i < fin; i++) {
            JsonObject llamada = llamadas.get(i).getAsJsonObject();
            JsonObject arg = llamada.getAsJsonArray("argumentos").get(0).getAsJsonObject();
            if ("EntregarPreciosTasas".equals(llamada.get("colaborador").getAsString())) {
                for (JsonElement d : arg.getAsJsonArray("p_DatosInstrumentos")) {
                    JsonObject o = d.getAsJsonObject();
                    long v = o.get("IdRegistro").getAsLong();
                    o.addProperty("IdRegistro", Long.signum(v) * canonico.get(Math.abs(v)));
                }
            } else if ("CalcularParidad".equals(llamada.get("colaborador").getAsString())) {
                boolean cuatro = arg.get("p_IdTrabajo").getAsLong() == 2L;
                for (JsonElement c : arg.getAsJsonArray("p_CalculosEntrada")) {
                    JsonObject o = c.getAsJsonObject();
                    long v = o.get("IdInterno").getAsLong();
                    long x = Math.abs(v);
                    o.addProperty("IdInterno", Long.signum(v) * (cuatro ? 4 * canonico.get(x / 4) + x % 4 : canonico.get(x)));
                }
            }
        }
        JsonObject copia = dump.deepCopy();
        copia.add("colaboradores", llamadas);
        return copia;
    }

    /** First by key (content), then by position (D-F3A-04): see the class comment. */
    private static void compararFilas(String id, JsonObject expected, JsonObject actual) {
        List<String> ke = new ArrayList<>();
        List<String> ka = new ArrayList<>();
        Map<String, String> fe = filas(expected, ke);
        Map<String, String> fa = filas(actual, ka);
        if (fe == null || fa == null) return;
        assertEquals(fe, fa, id + " :: rows by key (IdCliente_IdPortafolio_IdInstrumento_IdRegimenFondoMutuo)");
        assertEquals(ke, ka, id + " :: ORDER of P_OUT_TBL (D-F3A-04: HashMap iteration order of the keys)");
    }

    private static Map<String, String> filas(JsonObject dump, List<String> orden) {
        if (!dump.has("salida") || !dump.get("salida").isJsonObject() || !dump.getAsJsonObject("salida").has("P_OUT_TBL")) return null;
        Map<String, String> m = new LinkedHashMap<>();
        for (JsonElement f : dump.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL")) {
            JsonObject o = f.getAsJsonObject();
            String clave = campo(o, "IdCliente") + "_" + campo(o, "IdPortafolio") + "_" + campo(o, "IdInstrumento") + "_" + campo(o, "IdRegimenFondoMutuo");
            orden.add(clave);
            m.put(clave, PRETTY.toJson(canon(o)));
        }
        return m;
    }

    private static String campo(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? "null" : e.getAsString();
    }

    /** DESVIO-KIT-1 (see {@link CasoCpf#DESVIO_KIT_EMPRESA_SIN_CONFIGURACION}): the legacy NPE of an unknown / null company against the kit's ParametroNoEncontradoException. */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoCpf.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoCpf.slug(id)) && expected.has("excepcion")
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
