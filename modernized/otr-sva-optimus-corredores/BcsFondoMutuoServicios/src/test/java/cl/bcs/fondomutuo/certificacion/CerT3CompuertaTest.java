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
import cl.bcs.fondomutuo.normal.ProcesosExternos;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;

/**
 * T3 does NOT serve the class either (it cannot be served until T5). It adds ONE collaborator that is not ported behind a PORT ({@code CrearCargoAbono} of CuentaInversion, which the
 * legacy runs on the same connection: a library in process, not reachable from this module today) and reuses the two typed ports of the module that already exist for the other two
 * ({@code ProcesosExternos.TesoreriaPort}, gate G1, in process through the Tesoreria library; {@code ProcesosExternos.SpotPort}, gate G2, "not served" by default). What these tests pin is the
 * SHAPE of what T3 ships, the things that keep the not ported collaborators closed until their own front, and the integrity of the evidence (goldens, matrix, fixtures):
 * <ul>
 * <li>the new port has ONE method and NO production implementation exists (a bean accidentally wired would fail to start; the in-process adapter comes with the CuentaInversion library, T5);
 *     G1 and G2 are closed by default and keep their meaning;</li>
 * <li>the units of F-E, F-F and F-H write nothing (no write port, no connection, no gate inside them: the host evaluates the gates AFTER the validations and BEFORE the first write), hold no
 *     state, and are not reachable from outside (allow-list);</li>
 * <li>the goldens were recorded by the oracle on the unmodified legacy, are stable in 20 runs, the matrix names every card and defect of the three fronts, and the tag of each case agrees with
 *     what its golden shows;</li>
 * <li>RULE-076: no id, connection alias or credential of the legacy {@code main()} or of a real environment in the fixtures.</li>
 * </ul>
 */
class CerT3CompuertaTest {

    private static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";
    private static final Set<String> TARJETAS = Set.of("FM-CER-29", "FM-CER-30", "FM-CER-42", "FM-CER-43", "FM-CER-44", "FM-CER-45", "FM-CER-46", "FM-CER-47", "FM-CER-59", "FM-CER-60");

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape of what T3 ships (RED until the units exist)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("port: CrearCargoAbonoPort is an interface with ONE method JsonObject crear(JsonObject) (the legacy JSON contract of CuentaInversion CrearCargoAbono)")
    void puertoDeCuentaDeUnSoloMetodo() {
        Class<?> c = ApiT3.cargar(ApiT3.PUERTO_CUENTA);
        assertTrue(c.isInterface(), ApiT3.PUERTO_CUENTA + " must be an interface");
        assertEquals(1, c.getDeclaredMethods().length, "exactly one method");
        Method m = c.getDeclaredMethods()[0];
        assertEquals("crear", m.getName());
        assertEquals(JsonObject.class, m.getReturnType());
        assertEquals(List.of(JsonObject.class), List.of(m.getParameterTypes()));
    }

    @Test
    @DisplayName("CrearCargoAbono: NO production class implements the port yet (an accidental bean would fail at start-up; the in-process adapter comes with the CuentaInversion library, T5)")
    void sinImplementacionDeProduccionDelPuertoDeCuenta() {
        Class<?> p = ApiT3.cargar(ApiT3.PUERTO_CUENTA);
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AssignableTypeFilter(p));
        List<String> implementadores = new ArrayList<>();
        scanner.findCandidateComponents("cl.bcs.fondomutuo").forEach(bd -> implementadores.add(bd.getBeanClassName()));
        assertTrue(implementadores.isEmpty(), "no production implementation of " + p.getSimpleName() + " until T5: " + implementadores);
    }

    @Test
    @DisplayName("the units of F-E, F-F and F-H are read / call only: no write port, no connection, no gate, no state; every step is public and takes the context")
    void unidadesSinEscrituraNiEstado() {
        for (String nombre : new String[] { ApiT3.CUENTA, ApiT3.TESORERIA, ApiT3.SPOT }) {
            Class<?> c = ApiT3.cargar(nombre);
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
        for (String[] m : new String[][] { { ApiT3.CUENTA, "moverAportes" }, { ApiT3.CUENTA, "moverRescates" }, { ApiT3.TESORERIA, "moverTesoreria" }, { ApiT3.SPOT, "moverSpot" } }) {
            try {
                Method met = ApiT3.cargar(m[0]).getMethod(m[1], CertificarAporteRescateContexto.class);
                assertEquals(void.class, met.getReturnType(), m[1] + " works on the context (the legacy mutates the same objects in place)");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(m[0] + " must have public void " + m[1] + "(CertificarAporteRescateContexto)", e);
            }
        }
    }

    @Test
    @DisplayName("the constructors take the typed ports the module ALREADY has for Tesoreria (ProcesosExternos.TesoreriaPort) and Spot (ProcesosExternos.SpotPort), the kit ConfiguracionSistemaPort for the concept of the SII payment, and the read port Datos for GeneraCargoAbono")
    void reutilizaLosPuertosTipadosDelModulo() throws Exception {
        assertEquals(2, ApiT3.cargar(ApiT3.TESORERIA).getConstructors()[0].getParameterCount());
        assertEquals(List.of(ProcesosExternos.TesoreriaPort.class, ConfiguracionSistemaPort.class), List.of(ApiT3.cargar(ApiT3.TESORERIA).getConstructors()[0].getParameterTypes()));
        assertEquals(List.of(ProcesosExternos.SpotPort.class), List.of(ApiT3.cargar(ApiT3.SPOT).getConstructors()[0].getParameterTypes()));
        assertEquals(List.of(ApiT3.cargar(ApiT3.PUERTO_CUENTA), CertificarAporteRescateDatos.class), List.of(ApiT3.cargar(ApiT3.CUENTA).getConstructors()[0].getParameterTypes()));
    }

    @Test
    @DisplayName("the class is still NOT served in T3: not on the allow-list, no JavaCall handler class, no gate flag of Certificar")
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
    @DisplayName("G1 (Tesoreria, FM-CER-44..47) and G2 (Spot, FM-CER-59/60) are CLOSED by default: the host must evaluate them before the first write (503, the router repeats the request)")
    void compuertasG1G2CerradasPorDefecto() {
        Compuertas cerradas = new Compuertas("", false, false, false, false, false);
        assertThrows(ServicioNoServidoException.class, cerradas::g1MedioDistintoDeCin);
        assertThrows(ServicioNoServidoException.class, cerradas::g2Spot);
        Compuertas g1 = new Compuertas("", true, false, false, false, false);
        g1.g1MedioDistintoDeCin();
        assertThrows(ServicioNoServidoException.class, g1::g2Spot);
        Compuertas g2 = new Compuertas("", false, true, false, false, false);
        g2.g2Spot();
        assertThrows(ServicioNoServidoException.class, g2::g1MedioDistintoDeCin);
    }

    @Test
    @DisplayName("CrearCargoAbono is not reachable from this module: the use case of BcsCuentaInversionServicios is not on its classpath (a port is needed; Q-T3-1)")
    void crearCargoAbonoNoEsAlcanzableDesdeElModulo() {
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.cuentainversion.port.in.CrearCargoAbonoUseCase"));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.cuentainversion.domain.CrearCargoAbonoEntrada"));
    }

    @Test
    @Disabled("pending T5: the facade evaluates Compuertas.g1MedioDistintoDeCin() when Tesoreria receives an entry (a payment medium other than CIN, a traspaso, the SII payment) and Compuertas.g2Spot() when a rescate GeneraSpot S reaches Spot, AFTER all the validations and BEFORE the first write; with them closed the request answers 503 and nothing is written")
    @DisplayName("T5 / G1 and G2: the gates are evaluated by the facade before the first write of F-F / F-H (503 with everything closed)")
    void evaluacionDeLasCompuertasPendienteT5() {
    }

    @Test
    @Disabled("pending the CuentaInversion library (T5): a gate for the cuenta de inversion does not exist in Compuertas (the legacy runs CrearCargoAbono on the same connection, it is not a gate of the plan); Q-T3-1 asks whether a new gate and an in-process adapter are wanted")
    @DisplayName("T5 / CrearCargoAbono: the port has an in-process adapter (CuentaInversion library of the module, joining the ambient transaction and never committing) only when its gate is open")
    void adaptadorDeCuentaInversionPendienteT5() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // integrity of the evidence (GREEN)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("the oracle stood on the real legacy class: every T3 case has case.json, seed.sql and expected.json, the golden carries the provenance of OracleRunnerCer and the report says 0 unstable")
    void losGoldensSeGrabaronConElOraculo() throws Exception {
        List<String> ids = CasoCerT3.casos();
        assertTrue(ids.size() >= 150, "T3 matrix size " + ids.size());
        for (String id : ids) {
            Path d = CasoCerT3.directorio().resolve(id);
            assertTrue(Files.exists(d.resolve("seed.sql")), id + " :: seed.sql");
            JsonObject g = CasoCerT3.golden(id);
            assertTrue(g.has("_provenance") && g.get("_provenance").getAsString().contains("runner-cer/OracleRunnerCer.java executing the unmodified legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate"),
                    id + " :: golden without the provenance of the oracle");
        }
        String informe = Files.readString(CasoCerT3.recursos().resolve("determinism-cer-t3.txt"), StandardCharsets.UTF_8).lines().findFirst().orElse("");
        assertTrue(informe.contains("cases executed: " + ids.size()) && informe.contains("repeats per case: 20") && informe.endsWith("hard: 0"), "determinism report: " + informe);
    }

    @Test
    @DisplayName("the matrix names every card of F-E, F-F and F-H (FM-CER-29, 30, 42..47, 59, 60) and the defects D-F3B-01/03/07/08/09/14/17/41..44, D-NOR-9")
    void laMatrizNombraTodasLasTarjetas() {
        Set<String> refs = new TreeSet<>();
        for (String id : CasoCerT3.casos()) refs.addAll(CasoCerT3.refs(id));
        List<String> faltan = new ArrayList<>();
        for (String t : TARJETAS) if (!refs.contains(t)) faltan.add(t);
        for (String d : new String[] { "D-F3B-01", "D-F3B-03", "D-F3B-07", "D-F3B-08", "D-F3B-09", "D-F3B-14", "D-F3B-17", "D-F3B-41", "D-F3B-42", "D-F3B-43", "D-F3B-44", "D-NOR-9" }) {
            if (!refs.contains(d)) faltan.add(d);
        }
        for (String f : new String[] { "front:F-E", "front:F-F", "front:F-H" }) {
            if (!refs.contains(f)) faltan.add(f);
        }
        assertTrue(faltan.isEmpty(), "cards / defects / fronts without a case: " + faltan);
    }

    @Test
    @DisplayName("the tag of every case agrees with its golden: completo = the legacy ran to the end; excepcion = it threw inside F-C..F-H; excepcion-posterior = it finished F-H and threw in a later front")
    void laEtiquetaConcuerdaConElGolden() {
        List<String> malos = new ArrayList<>();
        for (String id : CasoCerT3.casos()) {
            JsonObject g = CasoCerT3.golden(id);
            boolean tiene = g.has("excepcion");
            CasoCerT3.Modo m = CasoCerT3.modo(id);
            if (m == CasoCerT3.Modo.COMPLETO && tiene) malos.add(id + " tagged completo but the legacy threw " + g.get("excepcion"));
            if (m != CasoCerT3.Modo.COMPLETO && !tiene) malos.add(id + " tagged " + m + " but the legacy ended normally");
            if (m == CasoCerT3.Modo.EXCEPCION && tiene && g.get("excepcion").getAsString().startsWith("SQLException")) malos.add(id + " the exception is the UPDATE: it is a later front");
            var cols = g.getAsJsonArray("colaboradores");
            boolean llegoALaCustodia = false;
            for (JsonElement e : cols) {
                String n = e.getAsJsonObject().get("colaborador").getAsString();
                if (n.equals("CalculaCustodiaRescate") || n.equals("ProcesarCustodiaFondoMutuo") || n.equals("SaldoFlujoCaja")) llegoALaCustodia = true;
            }
            if (!llegoALaCustodia) malos.add(id + " the legacy never reached F-C / F-D (an early exit of F-A): the harness would never reach the seam");
        }
        assertTrue(malos.isEmpty(), "inconsistent tags: " + malos);
    }

    @Test
    @DisplayName("the cases of T3 keep T1 and T2 untouched: their ids continue the numbering of T2 (FMM-CER-378 on) and none repeats a slug of T1 or T2")
    void numeracionContinuaLaDeT2() {
        Set<String> slugsAnteriores = new TreeSet<>();
        for (String id : CasoCer.casos()) slugsAnteriores.add(CasoCer.slug(id));
        int maximoAnterior = 0;
        for (String id : CasoCer.casos()) maximoAnterior = Math.max(maximoAnterior, Integer.parseInt(id.substring(8, 11)));
        for (String id : CasoCerT2.casos()) {
            slugsAnteriores.add(CasoCerT2.slug(id));
            maximoAnterior = Math.max(maximoAnterior, Integer.parseInt(id.substring(8, 11)));
        }
        int minimo = Integer.MAX_VALUE;
        for (String id : CasoCerT3.casos()) {
            int n = Integer.parseInt(id.substring(8, 11));
            minimo = Math.min(minimo, n);
            assertFalse(slugsAnteriores.contains(CasoCerT3.slug(id)), id + " repeats a slug of T1 or T2");
        }
        assertEquals(maximoAnterior + 1, minimo, "T3 continues the numbering of T2");
        assertEquals(378, minimo);
    }

    @Test
    @DisplayName("RULE-076: no id, connection alias or credential of the legacy main() / of a real environment is in the fixtures of the T3 cases")
    void sinDatosRealesEnLosFixtures() throws Exception {
        List<String> prohibidos = List.of("17788687", "17405614", "qa01", "desa01", "presura", "connections.properties", "getpropertiesconnection", "password", "passwd", "secret", "apikey", "bearer ", "jdbc:sap",
                "jdbc:hdb", "https://", "http://");
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(CasoCerT3.directorio())) {
            s.filter(Files::isRegularFile).forEach(archivos::add);
        }
        archivos.add(CasoCerT3.recursos().resolve("seed/base-cer-t3.sql"));
        for (Path p : archivos) {
            String texto = Files.readString(p, StandardCharsets.UTF_8).toLowerCase();
            for (String x : prohibidos) {
                if (texto.contains(x)) hallazgos.add(p.getFileName() + " contains '" + x + "'");
            }
        }
        assertTrue(hallazgos.isEmpty(), "real-looking data in the fixtures: " + hallazgos);
    }

    @Test
    @DisplayName("the goldens call the three collaborators of T3 by the names of the oracle's doubles (CrearCargoAbono, ProcesarIngresoEgresoTesoreria, ProcesarIngresoOperacionSpot) and the harness compares them")
    void losColaboradoresDeLaCosturaSonLosDelOraculo() {
        Set<String> vistos = new TreeSet<>();
        for (String id : CasoCerT3.casos()) {
            for (JsonElement e : CasoCerT3.golden(id).getAsJsonArray("colaboradores")) vistos.add(e.getAsJsonObject().get("colaborador").getAsString());
        }
        for (String c : CerCosturaT3.COLABORADORES_T3) assertTrue(vistos.contains(c), "no golden calls " + c);
    }
}
