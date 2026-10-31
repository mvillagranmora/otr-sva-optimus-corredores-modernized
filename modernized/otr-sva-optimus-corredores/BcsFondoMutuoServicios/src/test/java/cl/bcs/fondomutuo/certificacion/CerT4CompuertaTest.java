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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * T4 does NOT serve the class either (it cannot be served until T5). It adds THREE collaborators behind PORTS with no production implementation: Facturacion (G6: {@code FacturacionPort}, JSON
 * legacy contract, new gate {@code Compuertas.g6Facturacion}, closed by default), AR ({@code CrearAporteReinversionPort}: AR is ported by another front of the module; the port is the seam) and the
 * recursion ({@code CertificacionHijaPort}: the facade of T5 is its implementation). What these tests pin is the SHAPE of what T4 ships, the things that keep the not ported collaborators closed
 * until their own front, and the integrity of the evidence (goldens, matrix, fixtures):
 * <ul>
 * <li>the three ports have the assumed methods and NO production class implements them (a bean accidentally wired would fail to start); G6 exists, is closed by default and no compatibility
 *     constructor of {@link Compuertas} opens it;</li>
 * <li>the units of F-G and F-I (reinversion) write nothing (no write port, no connection, no gate inside them: the host evaluates the gates AFTER the validations and BEFORE the first write);
 *     the unit of the switch writes ONLY through its own write port (FM-CER-64); none holds state; the context carries the movements of custody the reinversion reads;</li>
 * <li>the goldens were recorded by the oracle, are stable in 20 runs, the matrix names every card and defect of the two fronts, and the tag of each case agrees with what its golden shows;</li>
 * <li>RULE-076: no id, connection alias or credential of the legacy {@code main()} or of a real environment in the fixtures.</li>
 * </ul>
 */
class CerT4CompuertaTest {

    private static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";
    private static final Set<String> TARJETAS = Set.of("FM-CER-48", "FM-CER-49", "FM-CER-50", "FM-CER-51", "FM-CER-52", "FM-CER-53", "FM-CER-54", "FM-CER-55", "FM-CER-56", "FM-CER-57", "FM-CER-58",
            "FM-CER-63", "FM-CER-64");
    private static final Set<String> DEFECTOS = Set.of("D-F3B-01", "D-F3B-03", "D-F3B-04", "D-F3B-13", "D-F3B-30", "D-F3B-32", "D-F3B-45", "D-F3B-46", "D-F3B-47", "D-F3B-48", "D-F3B-49", "D-F3B-50", "D-F3B-51", "D-F3B-52", "D-NOR-9");

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape of what T4 ships (RED until the units exist)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("port: FacturacionPort is an interface with TWO methods JsonObject crearMovimientoComprobante(JsonObject) and JsonObject crearComprobante(JsonObject) (the legacy JSON contract of Facturacion, G6)")
    void puertoDeFacturacion() {
        Class<?> c = ApiT4.cargar(ApiT4.PUERTO_FACTURACION);
        assertTrue(c.isInterface(), ApiT4.PUERTO_FACTURACION + " must be an interface");
        Set<String> nombres = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            nombres.add(m.getName());
            assertEquals(JsonObject.class, m.getReturnType(), m.getName());
            assertEquals(List.of(JsonObject.class), List.of(m.getParameterTypes()), m.getName());
        }
        assertEquals(Set.of("crearMovimientoComprobante", "crearComprobante"), nombres);
    }

    @Test
    @DisplayName("port: CrearAporteReinversionPort has ONE method JsonObject crear(JsonObject) (the legacy JSON contract of CrearSolicitudAporteReinversion); CertificacionHijaPort has ONE method Salida certificar(Entrada) throws Exception")
    void puertosDeReinversionYDeRecursion() throws Exception {
        Class<?> r = ApiT4.cargar(ApiT4.PUERTO_REINVERSION);
        assertTrue(r.isInterface());
        assertEquals(1, r.getDeclaredMethods().length);
        Method m = r.getDeclaredMethods()[0];
        assertEquals("crear", m.getName());
        assertEquals(JsonObject.class, m.getReturnType());
        assertEquals(List.of(JsonObject.class), List.of(m.getParameterTypes()));
        Class<?> h = ApiT4.cargar(ApiT4.PUERTO_HIJO);
        assertTrue(h.isInterface());
        assertEquals(1, h.getDeclaredMethods().length);
        Method c = h.getMethod("certificar", Entrada.class);
        assertEquals(Salida.class, c.getReturnType());
        assertTrue(Arrays.asList(c.getExceptionTypes()).contains(Exception.class), "certificar must declare throws Exception (the legacy recursion does)");
    }

    @Test
    @DisplayName("Facturacion / AR / recursion: NO production class implements the ports yet (an accidental bean would fail at start-up; the adapters come with T5 and with the port of AR)")
    void sinImplementacionDeProduccionDeLosPuertos() {
        List<String> implementadores = new ArrayList<>();
        for (String puerto : new String[] { ApiT4.PUERTO_FACTURACION, ApiT4.PUERTO_REINVERSION, ApiT4.PUERTO_HIJO }) {
            Class<?> p = ApiT4.cargar(puerto);
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AssignableTypeFilter(p));
            scanner.findCandidateComponents("cl.bcs.fondomutuo").forEach(bd -> implementadores.add(p.getSimpleName() + " <- " + bd.getBeanClassName()));
        }
        assertTrue(implementadores.isEmpty(), "no production implementation of the ports until T5: " + implementadores);
    }

    @Test
    @DisplayName("the units of F-G and of the reinversion write nothing and hold no state; the unit of the switch writes ONLY through CertificarAporteRescateEscrituraMonto; no unit depends on a connection or a gate; every step is public and takes the context")
    void unidadesSinEscrituraNiEstado() {
        for (String nombre : new String[] { ApiT4.FACTURACION, ApiT4.REINVERSION, ApiT4.SWITCH }) {
            Class<?> c = ApiT4.cargar(nombre);
            assertTrue(Modifier.isFinal(c.getModifiers()), nombre + " must be final");
            assertEquals(1, c.getConstructors().length, nombre + " must have one public constructor");
            Constructor<?> k = c.getConstructors()[0];
            for (Class<?> t : k.getParameterTypes()) {
                String n = t.getName();
                assertFalse(n.contains("ConexionUnidadDeTrabajo") || n.contains("DataSource") || n.contains("Connection") || n.contains("SecuenciaPort"), nombre + " must not depend on a connection: " + n);
                assertFalse(t == Compuertas.class, nombre + " must not evaluate gates itself: the host does, before the first write");
                boolean escribe = t.getName().endsWith("EscrituraMonto");
                assertTrue(!escribe || nombre.equals(ApiT4.SWITCH), nombre + " must not write (only the switch unit does: FM-CER-64)");
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
        for (String[] m : new String[][] { { ApiT4.FACTURACION, "facturar" }, { ApiT4.REINVERSION, "reinvertir" }, { ApiT4.SWITCH, "certificarAportesSwitch" }, { ApiT4.SWITCH, "cambiarMontoMonedaSistema" } }) {
            try {
                Method met = ApiT4.cargar(m[0]).getMethod(m[1], CertificarAporteRescateContexto.class);
                assertEquals(void.class, met.getReturnType(), m[1] + " works on the context (the legacy mutates the same objects in place)");
            } catch (NoSuchMethodException e) {
                throw new AssertionError(m[0] + " must have public void " + m[1] + "(CertificarAporteRescateContexto)", e);
            }
        }
    }

    @Test
    @DisplayName("the constructors take the ports above, the read port Datos, the existing FormatoMoneda (reinversion) and its own write port (switch): (FacturacionPort, Datos), (CrearAporteReinversionPort, Datos, FormatoMoneda), (CertificacionHijaPort, Datos, EscrituraMonto)")
    void constructores() {
        assertEquals(List.of(ApiT4.cargar(ApiT4.PUERTO_FACTURACION), CertificarAporteRescateDatos.class), List.of(ApiT4.cargar(ApiT4.FACTURACION).getConstructors()[0].getParameterTypes()));
        assertEquals(List.of(ApiT4.cargar(ApiT4.PUERTO_REINVERSION), CertificarAporteRescateDatos.class, cl.bcs.fondomutuo.cierre.FormatoMoneda.class),
                List.of(ApiT4.cargar(ApiT4.REINVERSION).getConstructors()[0].getParameterTypes()));
        assertEquals(List.of(ApiT4.cargar(ApiT4.PUERTO_HIJO), CertificarAporteRescateDatos.class, ApiT4.cargar(ApiT4.ESCRITURA_MONTO)),
                List.of(ApiT4.cargar(ApiT4.SWITCH).getConstructors()[0].getParameterTypes()));
    }

    @Test
    @DisplayName("the context carries the movements of custody that the retirement left (public List carterasRetiradas): the reinversion reads them (FM-CER-53)")
    void elContextoLlevaLasCarterasRetiradas() throws Exception {
        Field f;
        try {
            f = CertificarAporteRescateContexto.class.getField("carterasRetiradas");
        } catch (NoSuchFieldException e) {
            throw new AssertionError("T4 not implemented: CertificarAporteRescateContexto.carterasRetiradas does not exist yet (red before code; see API_ASSUMED_T4.md)", e);
        }
        assertTrue(java.util.List.class.isAssignableFrom(f.getType()), "carterasRetiradas must be a List of the movements of custody");
    }

    @Test
    @DisplayName("the movement of custody that the retirement answers carries Cantidad and FechaOrigenCompra (the reinversion needs them: ratio = Cantidad / quotas, origin date of the aporte)")
    void elMovimientoDeCustodiaTraeCantidadYFechaDeOrigen() {
        for (String campo : new String[] { "Cantidad", "FechaOrigenCompra" }) {
            try {
                CertificarAporteRescateColaboradores.MovimientoSalida.class.getField(campo);
            } catch (NoSuchFieldException e) {
                throw new AssertionError("T4 not implemented: CertificarAporteRescateColaboradores.MovimientoSalida." + campo + " does not exist yet (red before code; see API_ASSUMED_T4.md)", e);
            }
        }
    }

    @Test
    @DisplayName("the class is still NOT served in T4: not on the allow-list, no JavaCall handler class, no gate flag of Certificar")
    void claseNoServida() {
        assertFalse(JavaCallDispatcher.LISTA_BLANCA.contains(FQN_LEGACY));
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(FQN_LEGACY));
        assertThrows(ClassNotFoundException.class, () -> Class.forName("cl.bcs.fondomutuo.certificacion.CertificarAporteRescateJavaCallHandler"));
        assertFalse(new Compuertas("", false, false, false, false, false).listasServidas().stream().anyMatch(l -> l.contains("CERTIFICAR")));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // G6 (RED until the gate exists)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("G6 (Facturacion, FM-CER-48..51): Compuertas.g6Facturacion() exists and raises ServicioNoServidoException (503, the router repeats the request) with EVERYTHING closed")
    void g6ExisteYEstaCerradaPorDefecto() throws Exception {
        Method g6 = gate();
        Compuertas cerradas = new Compuertas("", false, false, false, false, false);
        assertThrows(ServicioNoServidoException.class, () -> invocar(g6, cerradas));
    }

    @Test
    @DisplayName("G6 fails closed: the compatibility constructors of Compuertas (6 and 8 arguments) never open it, even with G1..G4 and G-CUS open; G1 and G2 keep their meaning")
    void g6NoSeAbreConLosConstructoresDeCompatibilidad() throws Exception {
        Method g6 = gate();
        Compuertas todasLasDemasAbiertas = new Compuertas("", true, true, true, true, true);
        assertThrows(ServicioNoServidoException.class, () -> invocar(g6, todasLasDemasAbiertas));
        Compuertas ocho = new Compuertas("", true, true, true, true, true, true, true);
        assertThrows(ServicioNoServidoException.class, () -> invocar(g6, ocho));
        todasLasDemasAbiertas.g1MedioDistintoDeCin();
        todasLasDemasAbiertas.g2Spot();
        Compuertas g1 = new Compuertas("", true, false, false, false, false);
        g1.g1MedioDistintoDeCin();
        assertThrows(ServicioNoServidoException.class, g1::g2Spot);
    }

    @Test
    @DisplayName("G6: the message of the closed gate names G6 (the router logs it)")
    void g6NombraLaCompuerta() throws Exception {
        Method g6 = gate();
        ServicioNoServidoException e = assertThrows(ServicioNoServidoException.class, () -> invocar(g6, new Compuertas("", false, false, false, false, false)));
        assertTrue(String.valueOf(e.getMessage()).contains("G6"), "message: " + e.getMessage());
    }

    private static Method gate() {
        try {
            return Compuertas.class.getMethod("g6Facturacion");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("T4 not implemented: Compuertas.g6Facturacion() does not exist yet (red before code; see API_ASSUMED_T4.md)", e);
        }
    }

    private static void invocar(Method m, Object o) throws Throwable {
        try {
            m.invoke(o);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }

    @Test
    @Disabled("pending T5: the facade evaluates Compuertas.g6Facturacion() when Facturacion would receive a movement (a rescate with commission), Compuertas.g1MedioDistintoDeCin() / g2Spot() as in T3, and the gate of the port of AR, AFTER all the validations and BEFORE the first write; with them closed the request answers 503 and nothing is written")
    @DisplayName("T5 / G6: the gate is evaluated by the facade before the first write of F-G (503 with everything closed)")
    void evaluacionDeLasCompuertasPendienteT5() {
    }

    @Test
    @Disabled("pending T5 (Q-T4B-2): the facade is the implementation of CertificacionHijaPort and must BOUND the recursion: the legacy has no guard (a switch aporte that is a rescate whose AR answers switch again recurses until StackOverflowError); the tests pin one call per level")
    @DisplayName("T5 / recursion: the host guards the depth of the recursion of the switch")
    void guardaDeProfundidadPendienteT5() {
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // integrity of the evidence (GREEN)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("the oracle stood on the real legacy class: every T4 case has case.json, seed.sql and expected.json, the golden carries the provenance of OracleRunnerCerT4 (with the ONE observation line T4Spy.hijo) and the report says 0 unstable")
    void losGoldensSeGrabaronConElOraculo() throws Exception {
        List<String> ids = CasoCerT4.casos();
        assertTrue(ids.size() >= 150, "T4 matrix size " + ids.size());
        for (String id : ids) {
            Path d = CasoCerT4.directorio().resolve(id);
            assertTrue(Files.exists(d.resolve("seed.sql")), id + " :: seed.sql");
            JsonObject g = CasoCerT4.golden(id);
            String p = g.has("_provenance") ? g.get("_provenance").getAsString() : "";
            assertTrue(p.contains("runner-cer-t4/OracleRunnerCerT4.java executing the legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate") && p.contains("cl.bcs.oracle.T4Spy.hijo"), id + " :: golden without the provenance of the oracle");
        }
        String informe = Files.readString(CasoCerT4.recursos().resolve("determinism-cer-t4.txt"), StandardCharsets.UTF_8).lines().findFirst().orElse("");
        assertTrue(informe.contains("cases executed: " + ids.size()) && informe.contains("repeats per case: 20") && informe.endsWith("hard: 0"), "determinism report: " + informe);
    }

    @Test
    @DisplayName("the matrix names every card of F-G and F-I (FM-CER-48..58, 63, 64) and the defects D-F3B-01/03/04/13/30/32/45..52, D-NOR-9 and both fronts")
    void laMatrizNombraTodasLasTarjetas() {
        Set<String> refs = new TreeSet<>();
        for (String id : CasoCerT4.casos()) refs.addAll(CasoCerT4.refs(id));
        List<String> faltan = new ArrayList<>();
        for (String t : TARJETAS) if (!refs.contains(t)) faltan.add(t);
        for (String d : DEFECTOS) if (!refs.contains(d)) faltan.add(d);
        for (String f : new String[] { "front:F-G", "front:F-I" }) if (!refs.contains(f)) faltan.add(f);
        assertTrue(faltan.isEmpty(), "cards / defects / fronts without a case: " + faltan);
    }

    @Test
    @DisplayName("the tag of every case agrees with its golden: completo = the legacy ran to the end; excepcion = it threw inside F-C..F-I; excepcion-posterior = it finished F-I and threw in a later front")
    void laEtiquetaConcuerdaConElGolden() {
        List<String> malos = new ArrayList<>();
        for (String id : CasoCerT4.casos()) {
            JsonObject g = CasoCerT4.golden(id);
            boolean tiene = g.has("excepcion");
            CasoCerT4.Modo m = CasoCerT4.modo(id);
            if (m == CasoCerT4.Modo.COMPLETO && tiene) malos.add(id + " tagged completo but the legacy threw " + g.get("excepcion"));
            if (m != CasoCerT4.Modo.COMPLETO && !tiene) malos.add(id + " tagged " + m + " but the legacy ended normally");
            if (m == CasoCerT4.Modo.EXCEPCION && tiene && g.get("excepcion").getAsString().startsWith("SQLException")) malos.add(id + " the exception is the UPDATE: it is a later front");
            boolean llegoALaCustodia = false;
            for (JsonElement e : g.getAsJsonArray("colaboradores")) {
                String n = e.getAsJsonObject().get("colaborador").getAsString();
                if (n.equals("CalculaCustodiaRescate") || n.equals("ProcesarCustodiaFondoMutuo") || n.equals("SaldoFlujoCaja")) llegoALaCustodia = true;
            }
            if (!llegoALaCustodia) malos.add(id + " the legacy never reached F-C / F-D (an early exit of F-A): the harness would never reach the seam");
        }
        assertTrue(malos.isEmpty(), "inconsistent tags: " + malos);
    }

    @Test
    @DisplayName("the cases of T4 keep T1, T2 and T3 untouched: their ids continue the numbering of T3 (FMM-CER-575 on) and none repeats a slug of T1, T2 or T3")
    void numeracionContinuaLaDeT3() {
        Set<String> slugsAnteriores = new TreeSet<>();
        int maximoAnterior = 0;
        for (String id : CasoCer.casos()) {
            slugsAnteriores.add(CasoCer.slug(id));
            maximoAnterior = Math.max(maximoAnterior, Integer.parseInt(id.substring(8, 11)));
        }
        for (String id : CasoCerT2.casos()) {
            slugsAnteriores.add(CasoCerT2.slug(id));
            maximoAnterior = Math.max(maximoAnterior, Integer.parseInt(id.substring(8, 11)));
        }
        for (String id : CasoCerT3.casos()) {
            slugsAnteriores.add(CasoCerT3.slug(id));
            maximoAnterior = Math.max(maximoAnterior, Integer.parseInt(id.substring(8, 11)));
        }
        int minimo = Integer.MAX_VALUE;
        for (String id : CasoCerT4.casos()) {
            int n = Integer.parseInt(id.substring(8, 11));
            minimo = Math.min(minimo, n);
            assertFalse(slugsAnteriores.contains(CasoCerT4.slug(id)), id + " repeats a slug of T1, T2 or T3");
        }
        assertEquals(maximoAnterior + 1, minimo, "T4 continues the numbering of T3");
        assertEquals(575, minimo);
    }

    @Test
    @DisplayName("RULE-076: no id, connection alias or credential of the legacy main() / of a real environment is in the fixtures of the T4 cases")
    void sinDatosRealesEnLosFixtures() throws Exception {
        List<String> prohibidos = List.of("17788687", "17405614", "qa01", "desa01", "presura", "connections.properties", "getpropertiesconnection", "password", "passwd", "secret", "apikey", "bearer ", "jdbc:sap",
                "jdbc:hdb", "https://", "http://");
        List<String> hallazgos = new ArrayList<>();
        List<Path> archivos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(CasoCerT4.directorio())) {
            s.filter(Files::isRegularFile).forEach(archivos::add);
        }
        archivos.add(CasoCerT4.recursos().resolve("seed/base-cer-t4.sql"));
        for (Path p : archivos) {
            String texto = Files.readString(p, StandardCharsets.UTF_8).toLowerCase();
            for (String x : prohibidos) {
                if (texto.contains(x)) hallazgos.add(p.getFileName() + " contains '" + x + "'");
            }
        }
        assertTrue(hallazgos.isEmpty(), "real-looking data in the fixtures: " + hallazgos);
    }

    @Test
    @DisplayName("the goldens call the collaborators of T4 by the names of the oracle (CrearMovimientoComprobante, CrearComprobante, CrearSolicitudAporteReinversion and the recorded child certification) and the harness compares them")
    void losColaboradoresDeLaCosturaSonLosDelOraculo() {
        Set<String> vistos = new TreeSet<>();
        for (String id : CasoCerT4.casos()) {
            for (JsonElement e : CasoCerT4.golden(id).getAsJsonArray("colaboradores")) vistos.add(e.getAsJsonObject().get("colaborador").getAsString());
        }
        for (String c : CerCosturaT4.COLABORADORES_T4) assertTrue(vistos.contains(c), "no golden calls " + c);
    }

    @Test
    @DisplayName("the recorded child certification carries the calls of the child to ITS collaborators in the field internas (not in the main list): the new code does not run the child")
    void lasLlamadasDelHijoNoEstanEnLaListaPrincipal() {
        int hijos = 0;
        for (String id : CasoCerT4.casos()) {
            for (JsonElement e : CasoCerT4.golden(id).getAsJsonArray("colaboradores")) {
                JsonObject l = e.getAsJsonObject();
                if (!CasoCerT4.HIJO.equals(l.get("colaborador").getAsString())) continue;
                hijos++;
                assertTrue(l.has("internas") && l.get("internas").isJsonArray(), id + " :: the child record must carry internas");
                JsonArray args = l.getAsJsonArray("argumentos");
                assertEquals("N", args.get(0).getAsJsonObject().get("p_NotificaOWP").getAsString(), id + " :: the child never notifies OWP");
            }
        }
        assertTrue(hijos >= 10, "cases with a recorded child certification: " + hijos);
    }
}
