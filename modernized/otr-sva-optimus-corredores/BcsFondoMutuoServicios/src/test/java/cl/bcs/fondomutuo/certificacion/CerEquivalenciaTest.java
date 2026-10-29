package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

/**
 * FMM-CER (F3 W3 T1), cards FM-CER-01..20 and 23 and defects D-F3B-01/08/12/16/31, D-NOR-9/16 (RULES_FONDOMUTUO_F3_B.md 8.1 and 9): the new {@code CertificarSolicitudAporteRescate}
 * facade (fronts F-A and F-B, REAL; everything after F-B behind the {@link CertificarAporteRescateFasesPosteriores} double) against the goldens RECORDED from the unmodified legacy by
 * record_cer.sh on the same H2 and the same scripted collaborators.
 *
 * <ul>
 * <li>A TERMINAL case (the legacy ends, or throws, inside F-A / F-B: requirements, empty list, duplicates, nothing found, no solicitud passes the eight rules = early exit with the log
 *     and counter 1, a failing parameter or read) is compared AS A WHOLE, like the earlier fronts: output text, exception (class; the message only for a plain
 *     {@code Exception}), the CallServlet decision, the tables before / right after the call / after the decision, every collaborator call, the sequence requests, the earlier requests
 *     of the session and the session.</li>
 * <li>A case that CONTINUES past F-B is compared up to the seam by {@link CerCostura}: the new facade must stop at the seam with a context that agrees with what the legacy then did.</li>
 * </ul>
 * A golden that is missing FAILS the case; a run in which no comparison executed FAILS ("equivalence cases executed: 0" is not proven).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerEquivalenciaTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();
    private static final AtomicInteger TERMINALES = new AtomicInteger();
    private static final AtomicInteger CONTINUAN = new AtomicInteger();

    /** (id, the rules the case pins: its `ref`): the display name of every case NAMES its rules (FM-CER-nn, D-F3B-nn) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCer.casos();
        assertTrue(ids.size() >= 250, "the FMM-CER matrix has at least 250 case directories, found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, String.join(" ", CasoCer.refs(id))));
    }

    @Test
    @Order(0)
    @DisplayName("FMM-CER: every case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCer.casos()) {
            if (!Files.exists(CasoCer.recursos().resolve("cases").resolve(id).resolve("expected.json"))) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_cer.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("FMM-CER equivalence with the recorded legacy (FM-CER-01..20, 23)")
    void equivalente(String id, String reglas) throws Exception {
        JsonObject golden = CasoCer.golden(id);
        CasoCer.Corrida corrida = CasoCer.ejecutar(id);
        if (CasoCer.modo(id) == CasoCer.Modo.TERMINAL) {
            compararCompleto(id, golden, corrida.volcado);
            TERMINALES.incrementAndGet();
        } else {
            CerCostura.comparar(id, golden, corrida);
            CONTINUAN.incrementAndGet();
        }
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("FMM-CER: every case was compared (equivalence cases executed: N)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCer.casos().size() + " (terminal, compared as a whole: " + TERMINALES.get()
                + "; continue past F-B, compared up to the seam: " + CONTINUAN.get() + ")");
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCer.casos().size(), EJECUTADOS.get(), "a case was not compared");
        assertTrue(TERMINALES.get() > 0 && CONTINUAN.get() > 0, "both kinds of case must have run");
    }

    /** The whole dump of a TERMINAL case: the sections the oracle writes and the exception. */
    static void compararCompleto(String id, JsonObject expected, JsonObject actual) {
        boolean desvio = desvioKit(id, expected, actual);
        for (String clave : new String[] { "salidaCruda", "tablasIniciales", "tablasTrasLaLlamada", "tablas", "colaboradores", "secuenciasSolicitadas", "previas", "sesion", "decisionServlet" }) {
            // the collaborator calls are compared as {colaborador, argumentos, respuesta} (the oracle's double also records the method name)
            boolean llamadas = clave.equals("colaboradores");
            assertEquals(CerCostura.txt(llamadas ? CerCostura.simples(expected.getAsJsonArray(clave)) : expected.get(clave)),
                    CerCostura.txt(llamadas ? CerCostura.simples(actual.getAsJsonArray(clave)) : actual.get(clave)), id + " :: " + clave);
        }
        assertEquals(expected.has("excepcion"), actual.has("excepcion"), id + " :: exception presence: " + actual.get("excepcion"));
        if (expected.has("excepcion")) {
            String e = expected.get("excepcion").getAsString();
            String a = actual.get("excepcion").getAsString();
            String ec = e.substring(0, e.indexOf(':'));
            String ac = a.substring(0, a.indexOf(':'));
            if (desvio) {
                assertTrue(ac.equals("Exception") && a.startsWith("Exception: Parametro ") && a.endsWith(" no encontrado") || ac.equals("NullPointerException"),
                        id + " :: DESVIO-KIT-1 expects the kit's 'Parametro ... no encontrado' or the legacy NullPointerException: " + a);
            } else if (ec.equals("Exception")) {
                assertEquals(e.substring(e.indexOf(':') + 1), a.substring(a.indexOf(':') + 1), id + " :: exception message");
            } else {
                assertEquals(ec, ac, id + " :: exception class (" + a + ")");
            }
        }
    }

    /** DESVIO-KIT-1 (see {@link CasoCer#DESVIO_KIT_EMPRESA_SIN_CONFIGURACION}): the legacy NPE of a company with no configuration against the kit's ParametroNoEncontradoException. */
    private static boolean desvioKit(String id, JsonObject expected, JsonObject actual) {
        return CasoCer.DESVIO_KIT_EMPRESA_SIN_CONFIGURACION.contains(CasoCer.slug(id)) && expected.has("excepcion")
                && expected.get("excepcion").getAsString().startsWith("NullPointerException:") && actual.has("excepcion")
                && (actual.get("excepcion").getAsString().startsWith("Exception: Parametro ") || actual.get("excepcion").getAsString().startsWith("NullPointerException:"));
    }
}
