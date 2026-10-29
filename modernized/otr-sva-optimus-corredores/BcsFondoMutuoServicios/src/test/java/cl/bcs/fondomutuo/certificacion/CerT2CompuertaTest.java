package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * T2 does NOT serve the class either (it cannot be served until T5) and adds two collaborators that are NOT ported behind PORTS: the custody calculation / entry / retirement
 * ({@code CalculaCustodiaRescate}, {@code ProcesarCustodiaFondoMutuo}, which the legacy reaches through {@code IngresoCustodia} / {@code RetiroCustodia}) and, already ported as a port, the cash
 * flow ({@code SaldoFlujoCaja}, gate G3). What these tests pin is the SHAPE of what T2 ships, the things that keep the not ported collaborators closed until their own front, and the
 * integrity of the evidence (goldens, matrix, fixtures):
 * <ul>
 * <li>the two new ports have ONE method each and NO production implementation exists (a bean accidentally wired would fail to start; the real adapters come with the custody line, behind
 *     the gate {@code G-CUS}, in T5), and G3 / G-CUS are closed by default;</li>
 * <li>the units of F-C and F-D write nothing (no write port, no connection, no gate inside them: the host evaluates the gates AFTER the validations and BEFORE the first write), hold no state,
 *     and are not reachable from outside (allow-list);</li>
 * <li>the goldens were recorded by the oracle on the unmodified legacy, are stable in 20 runs, the matrix names every card and defect of the two fronts, and the tag of each case agrees with
 *     what its golden shows;</li>
 * <li>RULE-076: no id, connection alias or credential of the legacy {@code main()} or of a real environment in the fixtures.</li>
 * </ul>
 */
class CerT2CompuertaTest {

    private static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";
    private static final Set<String> TARJETAS = Set.of("FM-CER-21", "FM-CER-22", "FM-CER-24", "FM-CER-25", "FM-CER-26", "FM-CER-27", "FM-CER-28", "FM-CER-31", "FM-CER-32", "FM-CER-33", "FM-CER-34",
            "FM-CER-35", "FM-CER-36", "FM-CER-37", "FM-CER-38", "FM-CER-39", "FM-CER-40", "FM-CER-41");

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape of what T2 ships (RED until the units exist)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("ports: ProcesarCustodiaFondoMutuoPort and CalculaCustodiaRescatePort are interfaces with ONE method JsonObject f(JsonObject) (the legacy JSON contract of the collaborator)")
    void puertosDeUnSoloMetodo() {
        for (String[] p : new String[][] { { ApiT2.PUERTO_CUSTODIA, "procesar" }, { ApiT2.PUERTO_CALCULO_CUSTODIA, "calcular" } }) {
            Class<?> c = ApiT2.cargar(p[0]);
            assertTrue(c.isInterface(), p[0] + " must be an interface");
            assertEquals(1, c.getDeclaredMethods().length, p[0] + " must have exactly one method");
            Method m = c.getDeclaredMethods()[0];
            assertEquals(p[1], m.getName());
            assertEquals(JsonObject.class, m.getReturnType());
            assertEquals(List.of(JsonObject.class), List.of(m.getParameterTypes()));
        }
    }

    @Test
    @DisplayName("G-CUS: NO production class implements the two custody ports yet (an accidental bean would fail at start-up; the in-process adapters come with the custody line, behind G-CUS, in T5)")
    void sinImplementacionDeProduccionDeLosPuertosDeCustodia() {
        for (String puerto : new String[] { ApiT2.PUERTO_CUSTODIA, ApiT2.PUERTO_CALCULO_CUSTODIA }) {
            Class<?> p = ApiT2.cargar(puerto);
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(p));
            List<String> implementadores = new ArrayList<>();
            scanner.findCandidateComponents("cl.bcs.fondomutuo").forEach(bd -> implementadores.add(bd.getBeanClassName()));
            assertTrue(implementadores.isEmpty(), "no production implementation of " + p.getSimpleName() + " until T5 (gate G-CUS): " + implementadores);
        }
    }

    @Test
    @DisplayName("the units of F-C and F-D are read / call only: no write port, no connection, no gate, no state; every step is public and takes the context")
    void unidadesSinEscrituraNiEstado() {
        for (String nombre : new String[] { ApiT2.APORTES, ApiT2.RESCATES }) {
            Class<?> c = ApiT2.cargar(nombre);
            assertTrue(Modifier.isFinal(c.getModifiers()) || !Modifier.isAbstract(c.getModifiers()), nombre + " must be a concrete class");
            assertEquals(1, c.getConstructors().length, nombre + " must have one public constructor");
            Constructor<?> k = c.getConstructors()[0];
            for (Class<?> t : k.getParameterTypes()) {
                String n = t.getName();
                assertFalse(n.contains("Escritura") || n.contains("ConexionUnidadDeTrabajo") || n.contains("DataSource") || n.contains("Connection") || n.contains("SecuenciaPort"),
                        nombre + " must not depend on a write port or a connection: " + n);
                assertFalse(t == Compuertas.class, nombre + " must not evaluate gates itself: the host does, before the first write");
            }
            for (Field f : c.getDeclaredFields()) {
                if (f.isSynthetic()) continue;
                if (Modifier.isStatic(f.getModifiers())) {
                    assertTrue(Modifier.isFinal(f.getModifiers()) && !com.google.gson.Gson.class.isAssignableFrom(f.getType()) && !java.util.Collection.class.isAssignableFrom(f.getType())
                            && !java.util.Map.class.isAssignableFrom(f.getType()), nombre + " has mutable static state: " + f.getName());
                } else {
                    assertTrue(Modifier.isFinal(f.getModifiers()), nombre + " holds mutable state between requests (D-NOR-10): " + f.getName());
                }
            }
        }
        for (String[] m : new String[][] { { ApiT2.APORTES, "validarSaldo" }, { ApiT2.APORTES, "recalcularCostoUF" }, { ApiT2.APORTES, "ingresarCustodia" }, { ApiT2.RESCATES, "prepararRescates" },
                { ApiT2.RESCATES, "retirarCustodiaYCostear" } }) {
            try {
                Method met = ApiT2.cargar(m[0]).getMethod(m[1], CertificarAporteRescateContexto.class);
                assertEquals(void.class, met.getReturnType(), m[1] + " works on the context (the legacy mutates the same objects in place)");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(m[0] + " must have public void " + m[1] + "(CertificarAporteRescateContexto)", e);
            }
        }
    }

    @Test
    @DisplayName("the class is still NOT served in T2: not on the allow-list, no JavaCall handler class, no gate flag of Certificar")
    void claseNoServida() {
        assertFalse(JavaCallDispatcher.LISTA_BLANCA.contains(FQN_LEGACY));
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(FQN_LEGACY));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.fondomutuo.certificacion.CertificarAporteRescateJavaCallHandler"));
        assertFalse(new Compuertas("", false, false, false, false, false).listasServidas().stream().anyMatch(l -> l.contains("CERTIFICAR")));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // gates that already exist (GREEN: they hold from day one)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("G3 (SaldoFlujoCaja, FM-CER-21/22) and G-CUS (custody, FM-CER-27/31/39) are CLOSED by default: the host must evaluate them before the first write (503, the router repeats the request)")
    void compuertasCerradasPorDefecto() {
        Compuertas cerradas = new Compuertas("", false, false, false, false, false);
        assertThrows(ServicioNoServidoException.class, cerradas::g3SaldoFlujoCaja);
        assertThrows(ServicioNoServidoException.class, cerradas::gCustodia);
        Compuertas g3 = new Compuertas("", false, false, true, false, false);
        g3.g3SaldoFlujoCaja();
        assertThrows(ServicioNoServidoException.class, g3::gCustodia);
    }

    @Test
    @Disabled("pending T5: the facade evaluates Compuertas.g3SaldoFlujoCaja() when an aporte reaches the cash flow (FM-CER-21/22) and Compuertas.gCustodia() when custody is called (FM-CER-27, 31, 39), AFTER all the validations and BEFORE the first write; with them closed the request answers 503 and nothing is written")
    @DisplayName("T5 / G3 and G-CUS: the gates are evaluated by the facade before the first write of F-C / F-D (503 with everything closed)")
    void evaluacionDeLasCompuertasPendienteT5() {
    }

    @Test
    @Disabled("pending the custody line (T5): in-process adapters of ProcesarCustodiaFondoMutuoPort / CalculaCustodiaRescatePort (custody library of the module) that join the ambient transaction and never commit")
    @DisplayName("T5 / G-CUS: the custody ports have an in-process adapter only when the gate is open")
    void adaptadoresDeCustodiaPendientesT5() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // integrity of the evidence (GREEN)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("the oracle stood on the real legacy class: every T2 case has case.json, seed.sql and expected.json, the golden carries the provenance of OracleRunnerCer and the report says 0 unstable")
    void losGoldensSeGrabaronConElOraculo() throws Exception {
        List<String> ids = CasoCerT2.casos();
        assertTrue(ids.size() >= 150, "T2 matrix size " + ids.size());
        for (String id : ids) {
            Path d = CasoCerT2.directorio().resolve(id);
            assertTrue(Files.exists(d.resolve("seed.sql")), id + " :: seed.sql");
            JsonObject g = CasoCerT2.golden(id);
            assertTrue(g.has("_provenance") && g.get("_provenance").getAsString().contains("runner-cer/OracleRunnerCer.java executing the unmodified legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate"),
                    id + " :: golden without the provenance of the oracle");
        }
        String informe = Files.readString(CasoCerT2.recursos().resolve("determinism-cer-t2.txt"), StandardCharsets.UTF_8).lines().findFirst().orElse("");
        assertTrue(informe.contains("cases executed: " + ids.size()) && informe.contains("repeats per case: 20") && informe.endsWith("hard: 0"), "determinism report: " + informe);
    }

    @Test
    @DisplayName("the matrix names every card of F-C and F-D (FM-CER-21, 22, 24..28, 31..41) and the defects D-F3B-01/03/05/06/08/09/10/11/37..40, D-NOR-9/11/16, D-COM-1/5, D-FEC-6")
    void laMatrizNombraTodasLasTarjetas() {
        Set<String> refs = new TreeSet<>();
        for (String id : CasoCerT2.casos()) refs.addAll(CasoCerT2.refs(id));
        List<String> faltan = new ArrayList<>();
        for (String t : TARJETAS) if (!refs.contains(t)) faltan.add(t);
        for (String d : new String[] { "D-F3B-01", "D-F3B-03", "D-F3B-05", "D-F3B-06", "D-F3B-08", "D-F3B-09", "D-F3B-10", "D-F3B-11", "D-F3B-37", "D-F3B-38", "D-F3B-39", "D-F3B-40", "D-NOR-9", "D-NOR-11",
                "D-NOR-16", "D-COM-1", "D-COM-5", "D-FEC-6" }) {
            if (!refs.contains(d)) faltan.add(d);
        }
        assertTrue(faltan.isEmpty(), "cards / defects without a case: " + faltan);
    }

    @Test
    @DisplayName("the tag of every case agrees with its golden: completo = the legacy ran to the end; excepcion = it threw inside F-C / F-D; excepcion-posterior = it finished F-C / F-D and threw in a later front")
    void laEtiquetaConcuerdaConElGolden() {
        List<String> malos = new ArrayList<>();
        for (String id : CasoCerT2.casos()) {
            JsonObject g = CasoCerT2.golden(id);
            boolean tiene = g.has("excepcion");
            CasoCerT2.Modo m = CasoCerT2.modo(id);
            if (m == CasoCerT2.Modo.COMPLETO && tiene) malos.add(id + " tagged completo but the legacy threw " + g.get("excepcion"));
            if (m != CasoCerT2.Modo.COMPLETO && !tiene) malos.add(id + " tagged " + m + " but the legacy ended normally");
            if (m == CasoCerT2.Modo.EXCEPCION && tiene && g.get("excepcion").getAsString().startsWith("SQLException")) malos.add(id + " the exception is the UPDATE: it is a later front");
            if (m == CasoCerT2.Modo.EXCEPCION_POSTERIOR) {
                var cols = g.getAsJsonArray("colaboradores");
                String ultimo = cols.size() == 0 ? "" : cols.get(cols.size() - 1).getAsJsonObject().get("colaborador").getAsString();
                if (!CerCosturaT2.COLABORADORES_T2.contains(ultimo)) malos.add(id + " tagged excepcion-posterior but the last collaborator is " + ultimo);
            }
        }
        assertTrue(malos.isEmpty(), "inconsistent tags: " + malos);
    }

    @Test
    @DisplayName("the cases of T2 keep T1 untouched: their ids continue the numbering of T1 (FMM-CER-199 on) and none repeats a slug of T1")
    void numeracionContinuaLaDeT1() {
        List<String> t1 = CasoCer.casos();
        Set<String> slugsT1 = new TreeSet<>();
        for (String id : t1) slugsT1.add(CasoCer.slug(id));
        int minimo = Integer.MAX_VALUE;
        int maximoT1 = 0;
        for (String id : t1) maximoT1 = Math.max(maximoT1, Integer.parseInt(id.substring(8, 11)));
        for (String id : CasoCerT2.casos()) {
            int n = Integer.parseInt(id.substring(8, 11));
            minimo = Math.min(minimo, n);
            assertFalse(slugsT1.contains(CasoCerT2.slug(id)), id + " repeats a slug of T1");
        }
        assertEquals(maximoT1 + 1, minimo, "T2 continues the numbering of T1");
    }

    @Test
    @DisplayName("RULE-076: no id, connection alias or credential of the legacy main() / of a real environment is in the fixtures of the T2 cases")
    void sinDatosRealesEnLosFixtures() throws Exception {
        List<String> prohibidos = List.of("17788687", "17405614", "qa01", "desa01", "presura", "connections.properties", "getpropertiesconnection", "password", "passwd", "secret", "apikey", "bearer ", "jdbc:sap",
                "jdbc:hdb", "https://", "http://");
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(CasoCerT2.directorio())) {
            s.filter(Files::isRegularFile).forEach(archivos::add);
        }
        archivos.add(CasoCerT2.recursos().resolve("seed/base-cer-t2.sql"));
        for (Path p : archivos) {
            String texto = Files.readString(p, StandardCharsets.UTF_8).toLowerCase();
            for (String x : prohibidos) {
                if (texto.contains(x)) hallazgos.add(p.getFileName() + " contains '" + x + "'");
            }
        }
        assertTrue(hallazgos.isEmpty(), "real-looking data in the fixtures: " + hallazgos);
    }

    @Test
    @DisplayName("the harness reads the legacy collaborators of F-C / F-D by the names of the oracle's doubles (the golden of a case has calls to SaldoFlujoCaja, CalculaCustodiaRescate, ProcesarCustodiaFondoMutuo ...)")
    void losColaboradoresDeLaCosturaSonLosDelOraculo() {
        Set<String> vistos = new TreeSet<>();
        for (String id : CasoCerT2.casos()) {
            for (JsonElement e : CasoCerT2.golden(id).getAsJsonArray("colaboradores")) vistos.add(e.getAsJsonObject().get("colaborador").getAsString());
        }
        for (String c : CerCosturaT2.COLABORADORES_T2) assertTrue(vistos.contains(c), "no golden calls " + c);
        assertTrue(vistos.contains("ProcesarIngresoEgresoTesoreria") && vistos.contains("ProcesarIngresoOperacionSpot"), "the consumers that show GeneraSpot / RelacionTraspaso / retention are in the goldens");
    }
}
