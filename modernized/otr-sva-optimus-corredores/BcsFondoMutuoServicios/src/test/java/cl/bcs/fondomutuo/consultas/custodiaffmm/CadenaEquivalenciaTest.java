package cl.bcs.fondomutuo.consultas.custodiaffmm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
import com.google.gson.JsonObject;

/**
 * THE CHAIN (F3 W1 front 9, part B), cards FM-CPFF-21 / FM-CPFF-22 (RULES_FONDOMUTUO_F3_C.md 8.5): the NEW CPF wired to the NEW FFMM (through the adapter of the port CPF consumes), both over the
 * REAL JDBC adapters of ONE H2, against the goldens RECORDED from the unmodified legacy CPF running over the REAL unmodified legacy FFMM (record_cpfchain.sh). This is what the CPF front does NOT
 * prove: there the custody calculation is a scripted double, so CPF's reading of the custody rows was only tested with rows the case invented. Compared at the same points as the other fronts (output text,
 * exception, servlet decision, tables, every collaborator call IN ORDER (ConsultarEmpresa is called by FFMM first and by CPF later), sequences, session); the rows of {@code P_OUT_TBL} FIRST by key
 * ({@code IdCliente_IdPortafolio_IdInstrumento_IdRegimenFondoMutuo}) and THEN by position (the order is the iteration order of CPF's HashMap, D-F3A-04). A missing golden FAILS; a run in which no
 * comparison executed FAILS ("chain cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CadenaEquivalenciaTest {

    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static Stream<String> casos() {
        List<String> ids = CasoCadena.casos();
        assertTrue(ids.size() >= 40, "the chain matrix has at least 40 cases, found " + ids.size());
        return ids.stream();
    }

    @Test
    @Order(0)
    @DisplayName("FM-CHAIN: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCadena.casos()) {
            if (!Files.exists(CasoCadena.raizCasos().resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cpfchain.sh): " + sin);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FM-CHAIN equivalence: CPF + FFMM new = CPF + FFMM legacy (FM-CPFF-21 / 22)")
    void equivalente(String id) throws Exception {
        comparar(id, CasoCadena.golden(id), CasoCadena.ejecutar(id));
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FM-CHAIN: every case was compared (chain cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("chain cases executed: " + EJECUTADOS.get() + " of " + CasoCadena.casos().size());
        assertTrue(EJECUTADOS.get() > 0, "chain cases executed: 0 => not proven");
        assertEquals(CasoCadena.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    /** N = 20 runs (property {@code cadena.corridas}) of every chain case with two or more saldos or with a tie of FFMM groups under one CPF key: each equal to the golden (HashMap order of BOTH classes). */
    static Stream<String> casosConVariosSaldosOEmpate() {
        return CasoCadena.casos().stream().filter(id -> {
            JsonObject golden = CasoCadena.golden(id);
            boolean variasFilas = golden.has("salida") && golden.getAsJsonObject("salida").has("P_OUT_TBL") && golden.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL").size() >= 2;
            boolean empate = CasoCadena.slug(id).startsWith("desempate-");
            boolean variasSolicitudes = CasoCadena.seed(id).split("INSERT INTO \"BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate\"", -1).length - 1 >= 2;
            return variasFilas || empate || variasSolicitudes;
        });
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosConVariosSaldosOEmpate")
    @Order(3)
    @DisplayName("FM-CHAIN determinism FM-CPFF-22.4 / D-F3C-13 / D-F3A-04: N runs, each equal to the golden")
    void veinteCorridas(String id) throws Exception {
        int n = Integer.getInteger("cadena.corridas", 20);
        JsonObject golden = CasoCadena.golden(id);
        for (int i = 1; i <= n; i++) {
            comparar(id + " (run " + i + "/" + n + ")", golden, CasoCadena.ejecutar(id));
        }
    }

    @Test
    @Order(4)
    @DisplayName("FM-CHAIN determinism: the oracle's own report is present, from 20 runs per case, with no unstable case")
    void elReporteDelOraculoNoTieneCasosInestables() throws Exception {
        java.nio.file.Path reporte = CasoCadena.recursos().resolve("determinism-cpfchain.txt");
        assertTrue(Files.exists(reporte), "determinism-cpfchain.txt is missing (run legacy-oracle/sync_cpfchain.py)");
        List<String> lineas = Files.readAllLines(reporte, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(lineas.get(0).startsWith("cases executed: "), lineas.get(0));
        assertTrue(lineas.get(0).contains("repeats per case: 20"), "the goldens must come from 20 runs per case: " + lineas.get(0));
        long duros = lineas.stream().skip(1).filter(l -> l.contains(" UNSTABLE: ")).count();
        assertEquals(0L, duros, "an UNSTABLE case of the oracle has a golden here: " + lineas);
    }

    static void comparar(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
        expected = conIdsCanonicos(id + " (golden)", expected);
        actual = conIdsCanonicos(id + " (new code)", actual);
        compararFilas(id, expected, actual);
        for (String clave : new String[] { "salidaCruda", "tablas", "colaboradores", "secuenciasSolicitadas", "sesion", "decisionServlet" }) {
            assertEquals(FfmmEquivalenciaTest.texto(expected.get(clave)), FfmmEquivalenciaTest.texto(actual.get(clave)), id + " :: " + clave);
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
     * D-F3A-04: the legacy CPF assigns the {@code IdInterno} of the solicitudes (2, 3, ...) inside a {@code parallelStream().forEach}: WHICH row gets WHICH id is the JDK's splitting order, never a
     * rule. The calls of the FIRST valuation (the ones BEFORE the first {@code ConsultarEmpresa}, which in the chain is the one of FFMM) are compared MODULO the renaming of those ids: the SET of ids
     * must be exactly 2..n+1 (that IS pinned) and they are renamed by order of first appearance. Same technique as {@code CpfEquivalenciaTest.conIdsCanonicos}.
     */
    static JsonObject conIdsCanonicos(String etiqueta, JsonObject dump) {
        if (!dump.has("colaboradores") || !dump.get("colaboradores").isJsonArray()) return dump;
        JsonArray llamadas = dump.getAsJsonArray("colaboradores").deepCopy();
        int fin = llamadas.size();
        for (int i = 0; i < llamadas.size(); i++) {
            if ("ConsultarEmpresa".equals(llamadas.get(i).getAsJsonObject().get("colaborador").getAsString())) {
                fin = i;
                break;
            }
        }
        LinkedHashMap<Long, Long> canonico = new LinkedHashMap<>();
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

    /** First by key (content), then by position (D-F3A-04). */
    private static void compararFilas(String id, JsonObject expected, JsonObject actual) {
        List<String> ke = new ArrayList<>();
        List<String> ka = new ArrayList<>();
        Map<String, String> fe = filas(expected, ke);
        Map<String, String> fa = filas(actual, ka);
        assertEquals(fe == null, fa == null, id + " :: rows presence");
        if (fe == null) return;
        assertEquals(fe, fa, id + " :: rows by key (IdCliente_IdPortafolio_IdInstrumento_IdRegimenFondoMutuo)");
        assertEquals(ke, ka, id + " :: ORDER of P_OUT_TBL (D-F3A-04: HashMap iteration order of the keys)");
    }

    static Map<String, String> filas(JsonObject dump, List<String> orden) {
        if (!dump.has("salida") || !dump.get("salida").isJsonObject() || !dump.getAsJsonObject("salida").has("P_OUT_TBL")) return null;
        Map<String, String> m = new LinkedHashMap<>();
        for (JsonElement f : dump.getAsJsonObject("salida").getAsJsonArray("P_OUT_TBL")) {
            JsonObject o = f.getAsJsonObject();
            String clave = campo(o, "IdCliente") + "_" + campo(o, "IdPortafolio") + "_" + campo(o, "IdInstrumento") + "_" + campo(o, "IdRegimenFondoMutuo");
            orden.add(clave);
            m.put(clave, PRETTY.toJson(FfmmEquivalenciaTest.canon(o)));
        }
        return m;
    }

    private static String campo(JsonObject o, String k) {
        JsonElement e = o.get(k);
        return e == null || e.isJsonNull() ? "null" : e.getAsString();
    }

    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoCadena.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoCadena.slug(id)) && expected.has("excepcion")
                && expected.get("excepcion").getAsString().startsWith("NullPointerException:") && actual.has("excepcion")
                && actual.get("excepcion").getAsString().startsWith("Exception: Parametro ");
    }
}
