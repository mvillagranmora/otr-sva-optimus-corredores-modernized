package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * The GATES of T5 and the SHAPE of what T5 ships (red until the facade exists), plus the integrity of the evidence (green):
 * <ul>
 * <li>decision 1 (G8): the asynchronous notification (FM-CER-62) and OWP (FM-CER-65) leave through HTTP OUTSIDE the transaction, behind TYPED ports and the gate G8
 *     ({@code Compuertas.g8Notificaciones()}, closed by default): with the gate shut the answer is {@code ServicioNoServidoException} (503) BEFORE the first write, with nothing written, and the
 *     facade does not touch the notification ports; the early exit of FM-CER-14 (nothing is notified) is not affected;</li>
 * <li>review M1 (G6): {@code Compuertas.g6Facturacion()} is evaluated BEFORE the first write by the host: a run that would invoice (the port is present) answers 503 with no writes, and a run that
 *     does not invoice is the legacy run, gate shut or not; the same for G1, G2, G3 and G-CUS (the {@code @Disabled} tests of T2 / T3 / T4 that wait for the facade);</li>
 * <li>decision 2 / review M2 (depth): the facade FAILS CLOSED when the recursion of the switch is deeper than 1 (the legacy has no limit: the only intended behaviour difference of T5);</li>
 * <li>G5: a write collaborator that the ROUTER answers 503 for after the writes (AgregarLog) goes up untouched (503, the host rolls back everything of the connection; nothing compensates);</li>
 * <li>shape: the facade, the two typed ports, the write port of the UPDATE, the unit of front J, the 17 new fields of the dependencies, the 10-argument {@code Compuertas}; no state; no unit
 *     evaluates a gate (the host does); RULE-076: no id, connection alias or credential in the fixtures.</li>
 * </ul>
 */
class CerT5CompuertaTest {

    private static final String FQN_LEGACY = "cl.bcs.fondomutuo.CertificarSolicitudAporteRescate";

    /** The collaborators that WRITE or notify: with a gate shut none of them may have been called, and the tables must be as the seed. */
    private static final Set<String> ESCRITORES = Set.of("ProcesarCustodiaFondoMutuo", "CrearCargoAbono", "ProcesarIngresoEgresoTesoreria", "ProcesarIngresoOperacionSpot", "CrearMovimientoComprobante",
            "CrearComprobante", "CrearSolicitudAporteReinversion", "NotificarEstadoOrdenFondoMutuo", "NotificarEstadoOrdenesOWP", "AgregarLog", CasoCerT5.HIJO);

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape of what T5 ships (RED until the code exists)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("facade: CertificarAporteRescateFachada is final, implements CertificarAporteRescatePort AND CertificacionHijaPort (the recursion), holds no mutable state and takes no gate in a unit")
    void laFachada() {
        Class<?> f = ApiT5.cargar(ApiT5.FACHADA);
        assertTrue(Modifier.isFinal(f.getModifiers()), "the facade must be final");
        assertTrue(CertificarAporteRescatePort.class.isAssignableFrom(f), "the facade is the port of the request");
        assertTrue(CertificacionHijaPort.class.isAssignableFrom(f), "the facade is the implementation of the recursion (T4-B: CertificacionHijaPort is implemented by the facade of T5)");
        for (Field campo : f.getDeclaredFields()) {
            if (campo.isSynthetic()) continue;
            assertTrue(Modifier.isFinal(campo.getModifiers()), "the facade holds mutable state between requests (D-NOR-10): " + campo.getName());
        }
    }

    @Test
    @DisplayName("Dependencias: CertificarAporteRescateFabrica.Dependencias has the 17 new public fields (the harness builds the facade through them)")
    void lasDependenciasNuevas() {
        List<String> faltan = new ArrayList<>();
        for (String campo : ApiT5.CAMPOS_NUEVOS) {
            try {
                CertificarAporteRescateFabrica.Dependencias.class.getField(campo);
            } catch (NoSuchFieldException e) {
                faltan.add(campo);
            }
        }
        if (!faltan.isEmpty()) {
            throw new AssertionError("T5 not implemented: CertificarAporteRescateFabrica.Dependencias lacks the fields " + faltan + " (red before code; see API_ASSUMED_T5.md)");
        }
    }

    @Test
    @DisplayName("port (FM-CER-62): NotificacionCertificacionPort has ONE method void notificar(NotificarEstadoOrdenEntrada) throws Exception, typed (NOT JsonObject); the entry has the legacy fields in order")
    void puertoDeLaNotificacionAsincrona() throws Exception {
        Class<?> p = ApiT5.cargar(ApiT5.PUERTO_NOTIFICACION);
        Class<?> entrada = ApiT5.cargar(ApiT5.ENTRADA_NOTIFICACION);
        assertTrue(p.isInterface());
        assertEquals(1, p.getDeclaredMethods().length);
        Method m = p.getMethod("notificar", entrada);
        assertEquals(void.class, m.getReturnType());
        assertTrue(Arrays.asList(m.getExceptionTypes()).contains(Exception.class), "notificar must declare throws Exception (the real call reads two parameters and may throw)");
        assertNotEquals(JsonObject.class, entrada, "the port is TYPED");
        assertEquals(List.of("p_IdEmpresa:Long", "p_IdUsuario:Long", "p_IdModulo:String", "p_Solicitudes:List<Long>"), campos(entrada), "legacy EntradaNotificarEstadoOrdenFondoMutuo");
    }

    @Test
    @DisplayName("port (FM-CER-65): NotificacionOrdenesOwpPort has ONE method void notificar(List<OrdenOwp>) throws Exception; OrdenOwp has exactly the 15 fields the legacy entry carries")
    void puertoDeLaNotificacionOwp() throws Exception {
        Class<?> p = ApiT5.cargar(ApiT5.PUERTO_OWP);
        Class<?> orden = ApiT5.cargar(ApiT5.ORDEN_OWP);
        assertTrue(p.isInterface());
        assertEquals(1, p.getDeclaredMethods().length);
        Method m = p.getMethod("notificar", List.class);
        assertEquals(void.class, m.getReturnType());
        assertTrue(Arrays.asList(m.getExceptionTypes()).contains(Exception.class));
        assertEquals(orden, ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[0], "List<OrdenOwp>");
        assertEquals(Set.of("IdEmpresa:Long", "IdUsuario:Long", "Solsec:Long", "Fecha:String", "Estado:String", "Mercado:String", "NTitulos:BigDecimal", "Precio:BigDecimal", "Comision:BigDecimal",
                "Gastos:BigDecimal", "Divisa:String", "TipoCambio:BigDecimal", "SolsecAsoc:String", "Nemo:String", "IdClasificacionTipoInstrumento:String"), new LinkedHashSet<>(campos(orden)),
                "the golden of the call shows exactly these 15 keys (NotificarEstadoOrdenes_Entrada of the legacy)");
    }

    @Test
    @DisplayName("write port (FM-CER-61): CertificarAporteRescateEscrituraCierre has ONE method actualizarSolicitudes(List) and a JDBC adapter over ConexionUnidadDeTrabajo that never commits")
    void puertoDeEscrituraDelUpdate() {
        Class<?> p = ApiT5.cargar(ApiT5.ESCRITURA_CIERRE);
        assertTrue(p.isInterface());
        assertEquals(1, p.getDeclaredMethods().length);
        assertEquals("actualizarSolicitudes", p.getDeclaredMethods()[0].getName());
        assertEquals(List.of(List.class), List.of(p.getDeclaredMethods()[0].getParameterTypes()));
        Class<?> jdbc = ApiT5.cargar(ApiT5.ESCRITURA_CIERRE_JDBC);
        assertTrue(p.isAssignableFrom(jdbc));
        try {
            jdbc.getConstructor(cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo.class);
        } catch (NoSuchMethodException e) {
            throw new AssertionError(ApiT5.ESCRITURA_CIERRE_JDBC + " must have a public constructor (ConexionUnidadDeTrabajo)", e);
        }
    }

    @Test
    @DisplayName("unit of front J: CertificarAporteRescateCierre is final, holds no mutable state, depends on no connection and on no gate (the facade evaluates the gates), and has public void steps on the context")
    void laUnidadDelCierre() {
        Class<?> c = ApiT5.cargar(ApiT5.CIERRE);
        assertTrue(Modifier.isFinal(c.getModifiers()));
        assertEquals(1, c.getConstructors().length, "one public constructor");
        Constructor<?> k = c.getConstructors()[0];
        for (Class<?> t : k.getParameterTypes()) {
            String n = t.getName();
            assertFalse(n.contains("ConexionUnidadDeTrabajo") || n.contains("DataSource") || n.contains("Connection"), "the unit must not depend on a connection: " + n);
            assertFalse(t == Compuertas.class, "the unit must not evaluate gates itself: the facade does, before the first write");
        }
        for (Field f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            assertTrue(Modifier.isFinal(f.getModifiers()), "mutable state in the unit (D-NOR-10): " + f.getName());
        }
        long pasos = Arrays.stream(c.getMethods()).filter(m -> m.getDeclaringClass() == c && m.getParameterCount() == 1 && m.getParameterTypes()[0] == CertificarAporteRescateContexto.class).count();
        assertTrue(pasos >= 3, "the unit has at least the three steps of the closing (UPDATE + notification, OWP, log + output + simulation), found " + pasos);
    }

    @Test
    @DisplayName("no mutable static state in the new classes (D-NOR-10): every static field is final and not a Gson, a Map or a List")
    void sinEstadoEstatico() {
        List<String> malos = new ArrayList<>();
        for (String nombre : new String[] { ApiT5.FACHADA, ApiT5.CIERRE, ApiT5.ESCRITURA_CIERRE_JDBC }) {
            for (Field f : ApiT5.cargar(nombre).getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) || f.isSynthetic()) continue;
                boolean mutable = !Modifier.isFinal(f.getModifiers()) || com.google.gson.Gson.class.isAssignableFrom(f.getType()) || java.util.Collection.class.isAssignableFrom(f.getType())
                        || java.util.Map.class.isAssignableFrom(f.getType());
                if (mutable) malos.add(nombre + "." + f.getName());
            }
        }
        assertTrue(malos.isEmpty(), "mutable static state: " + malos);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // G8 (decision 1)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("G8 (notifications, FM-CER-62 and 65): Compuertas.g8Notificaciones() exists, raises ServicioNoServidoException naming G8 with EVERYTHING closed and is the LAST argument of a 10-argument constructor")
    void g8ExisteYEstaCerrada() throws Throwable {
        Method g8 = ApiT5.g8();
        Compuertas cerradas = new Compuertas("", false, false, false, false, false);
        ServicioNoServidoException e = assertThrows(ServicioNoServidoException.class, () -> invocar(g8, cerradas));
        assertEquals("G8", e.compuerta());
        invocar(g8, ApiT5.compuertas(false, false, false, false, false, false, true));
        assertThrows(ServicioNoServidoException.class, () -> invocar(g8, ApiT5.compuertas(true, true, true, true, true, true, false)));
    }

    @Test
    @DisplayName("G8 fails closed: the compatibility constructors of Compuertas (6, 8 and 9 arguments) never open it, even with every other gate open")
    void g8NoSeAbreConLosConstructoresDeCompatibilidad() throws Throwable {
        Method g8 = ApiT5.g8();
        assertThrows(ServicioNoServidoException.class, () -> invocar(g8, new Compuertas("", true, true, true, true, true)));
        assertThrows(ServicioNoServidoException.class, () -> invocar(g8, new Compuertas("", true, true, true, true, true, true, true)));
        assertThrows(ServicioNoServidoException.class, () -> invocar(g8, new Compuertas("", true, true, true, true, true, true, true, true)));
    }

    static Stream<Arguments> casosQueNotifican() {
        List<String> ids = CasoCerT5.casos().stream().filter(id -> llamo(CasoCerT5.golden(id), "NotificarEstadoOrdenFondoMutuo")).toList();
        assertTrue(ids.size() >= 100, "at least 100 T5 cases notify (every run that reaches front J): " + ids.size());
        return ids.stream().map(id -> Arguments.of(id));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosQueNotifican")
    @DisplayName("G8 closed => 503 BEFORE the first write with nothing written, for every run that would notify (FM-CER-62, 65, 70, decision 1)")
    void g8CerradaDa503SinEscribirNada(String id) throws Exception {
        cerradaDa503(id, "G8", "G8");
    }

    static Stream<Arguments> casosSinNotificacion() {
        // the runs that never reach front J and write nothing: the early exits (FM-CER-14, 01..04) are the legacy run, with G8 shut or not
        List<String> ids = CasoCerT5.casos().stream().filter(id -> {
            JsonObject g = CasoCerT5.golden(id);
            return !llamo(g, "NotificarEstadoOrdenFondoMutuo") && !g.has("excepcion") && !escribe(g);
        }).toList();
        assertTrue(ids.size() >= 5, "the early-exit cases: " + ids.size());
        return ids.stream().map(id -> Arguments.of(id));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosSinNotificacion")
    @DisplayName("G8 closed does not touch a run that notifies nothing: the early exit is the legacy run (FM-CER-14)")
    void g8CerradaNoAfectaLaSalidaTemprana(String id) throws Exception {
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas().cerrando("G8"));
        CerCosturaT5.comparar(id, CasoCerT5.golden(id), c);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // G6 (review M1) and G1, G2, G3, G-CUS: the host evaluates them BEFORE the first write
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> casosDeUnaCompuerta() {
        List<Arguments> out = new ArrayList<>();
        for (String[] g : new String[][] { { "G6", "G6" }, { "G1", "G1" }, { "G2", "G2" }, { "G3", "G3" }, { "GCUS", "G-CUS" } }) {
            List<String> ids = CasoCerT5.conTag("T5-compuerta:" + g[0]);
            assertFalse(ids.isEmpty(), "no T5 case tagged T5-compuerta:" + g[0]);
            for (String id : ids) out.add(Arguments.of(g[0], g[1], id));
        }
        return out.stream();
    }

    @ParameterizedTest(name = "{0} closed: {2}")
    @MethodSource("casosDeUnaCompuerta")
    @DisplayName("M1 / G1 G2 G3 G6 G-CUS: the gate of a collaborator the run would reach is evaluated BEFORE the first write: 503 and nothing written, the port is present (FM-CER-48..51, 21..22, 27, 31, 44..47, 59)")
    void laCompuertaCerradaDa503SinEscribirNada(String clave, String compuerta, String id) throws Exception {
        cerradaDa503(id, clave, compuerta);
    }

    static Stream<Arguments> casosSinFacturacion() {
        List<String> ids = CasoCerT5.casos().stream().filter(id -> {
            JsonObject g = CasoCerT5.golden(id);
            return !g.has("excepcion") && g.has("salida") && escribe(g) && !llamo(g, "CrearMovimientoComprobante") && !llamo(g, "CrearComprobante");
        }).limit(12).toList();
        assertTrue(ids.size() >= 5);
        return ids.stream().map(id -> Arguments.of(id));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosSinFacturacion")
    @DisplayName("M1 green case: G6 shut does not touch a run that invoices nothing (the golden shows no call to Facturacion): it is the legacy run")
    void g6CerradaNoAfectaUnaCorridaSinFacturacion(String id) throws Exception {
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas().cerrando("G6"));
        CerCosturaT5.comparar(id, CasoCerT5.golden(id), c);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // G5: the router answers 503 for a write collaborator after the writes
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("G5: AgregarLog (LogPort, by the router) answers 503 AFTER the UPDATE and the notifications: the ServicioNoServidoException goes up untouched, the host rolls back the whole connection and nothing is compensated (FM-CER-66, 70)")
    void elRouterCaeDespuesDeEscribir() throws Exception {
        String id = CasoCerT5.conTag("T5-compuerta:G5").get(0);
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas().routerCae("AgregarLog"));
        assertTrue(c.excepcion instanceof ServicioNoServidoException, "the 503 of the router must reach the host unchanged: " + c.excepcion);
        JsonObject v = c.volcado;
        assertEquals("ROLLBACK_HTTP503", v.get("decisionServlet").getAsString());
        assertNotEquals(CerCosturaT5.txt(v.get("tablasIniciales")), CerCosturaT5.txt(v.get("tablasTrasLaLlamada")), "the UPDATE and the custody / cuenta writes HAD been made when the router failed");
        assertEquals(CerCosturaT5.txt(v.get("tablasIniciales")), CerCosturaT5.txt(v.get("tablas")), "the host rolls everything back");
        var cols = v.getAsJsonArray("colaboradores");
        assertEquals("AgregarLog", cols.get(cols.size() - 1).getAsJsonObject().get("colaborador").getAsString(), "AgregarLog is the last call: nothing compensates");
        assertEquals("error: ServicioNoServidoException", cols.get(cols.size() - 1).getAsJsonObject().get("respuesta").getAsString());
        assertTrue(llamo(v, "NotificarEstadoOrdenFondoMutuo"), "the asynchronous notification had already left (D-F3B-02): it is outside the transaction");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the depth guard (decision 2, review M2)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    static Stream<Arguments> casosDeProfundidad() {
        List<String> ids = CasoCerT5.conTag("T5-profundidad");
        assertTrue(ids.size() >= 2);
        return ids.stream().map(id -> Arguments.of(id));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casosDeProfundidad")
    @DisplayName("M2 / decision 2 / D-F3B-50: a recursion deeper than 1 FAILS CLOSED with ServicioNoServidoException (503) before the grandchild does anything; the legacy ran the three levels")
    void laRecursionMasProfundaQueUnoFallaCerrada(String id) throws Exception {
        JsonObject golden = CasoCerT5.golden(id);
        assertFalse(golden.has("excepcion"), "the legacy ran the three levels to the end: it has no guard");
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas());
        assertTrue(c.excepcion instanceof ServicioNoServidoException, "fail closed: " + c.excepcion);
        // the grandchild never wrote: the calls are the legacy ones up to its entry, and the record of the grandchild is the refusal with no inner calls
        var cols = c.volcado.getAsJsonArray("colaboradores");
        JsonObject hijo = cols.get(cols.size() - 1).getAsJsonObject();
        assertEquals(CasoCerT5.HIJO, hijo.get("colaborador").getAsString());
        assertEquals("error: ServicioNoServidoException", hijo.get("respuesta").getAsString());
        JsonObject nieto = hijo.getAsJsonArray("internas").get(hijo.getAsJsonArray("internas").size() - 1).getAsJsonObject();
        assertEquals(CasoCerT5.HIJO, nieto.get("colaborador").getAsString());
        assertEquals("error: ServicioNoServidoException", nieto.get("respuesta").getAsString());
        assertEquals(0, nieto.getAsJsonArray("internas").size(), "the grandchild did not call any collaborator");
        CerCosturaT5.comparar(id, golden, c);
    }

    @Test
    @DisplayName("depth guard: a recursion of depth exactly 1 (the child) is NOT refused: the cases of FM-CER-63 are the legacy run (CerT5EquivalenciaTest)")
    void laRecursionDeProfundidadUnoNoSeRechaza() throws Exception {
        String id = CasoCerT5.idPorSlug("rec-un-aporte-switch-se-certifica-por-el-hijo-que-hace-su-propio-update-notificacion-y-log");
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas());
        assertTrue(c.termino(), "the child of depth 1 runs: " + c.excepcionTexto);
        assertTrue(llamo(c.volcado, CasoCerT5.HIJO));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // not served in T5 either (the JavaCall handler is a later step)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C: the class is on the allow-list but NOT served by default (not a read class, no list of the Manejador, no list open in the default configuration)")
    void claseNoServida() {
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN_LEGACY), "T4-C: the class is on the allow-list (its list stays closed by default)");
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(FQN_LEGACY));
        assertFalse(new Compuertas("", false, false, false, false, false).listasServidas().stream().anyMatch(l -> l.contains("CERTIFICAR")));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // integrity of the evidence (GREEN)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("the matrix names every card FM-CER-61..71 and the defects of front J, has at least 150 cases and every tag the tests look up (T5-compuerta:G1, G2, G3, G5, G6, G8, GCUS, T5-profundidad, T5-m3:a, b, c, T5-async-parametros)")
    void laMatrizNombraTodasLasTarjetas() {
        Set<String> refs = new LinkedHashSet<>();
        for (String id : CasoCerT5.casos()) refs.addAll(CasoCerT5.refs(id));
        for (int n = 61; n <= 71; n++) assertTrue(refs.contains(String.format("FM-CER-%02d", n)), "no case pins FM-CER-" + n);
        for (String d : List.of("D-F3B-01", "D-F3B-02", "D-F3B-16", "D-F3B-31", "D-F3B-47", "D-F3B-50", "D-NOR-9")) assertTrue(refs.contains(d), "no case pins " + d);
        for (String t : List.of("T5-compuerta:G1", "T5-compuerta:G2", "T5-compuerta:G3", "T5-compuerta:G5", "T5-compuerta:G6", "T5-compuerta:G8", "T5-compuerta:GCUS", "T5-profundidad", "T5-m3:a", "T5-m3:b",
                "T5-m3:c", "T5-async-parametros")) {
            assertTrue(refs.contains(t), "no case carries the tag " + t);
        }
        assertTrue(CasoCerT5.casos().size() >= 150, "cases: " + CasoCerT5.casos().size());
    }

    @Test
    @DisplayName("the tag of every case agrees with what its golden shows (completo: no exception and an output; excepcion: an exception and no output) and every golden carries the provenance of the T5 oracle")
    void lasEtiquetasConcuerdanConElGolden() {
        for (String id : CasoCerT5.casos()) {
            JsonObject g = CasoCerT5.golden(id);
            assertTrue(g.has("_provenance") && g.get("_provenance").getAsString().contains("runner-cer-t5/OracleRunnerCerT5.java executing the legacy cl.bcs.fondomutuo.CertificarSolicitudAporteRescate"),
                    id + " :: golden without the provenance of the oracle");
            if (CasoCerT5.esExcepcion(id)) assertTrue(g.has("excepcion") && !g.has("salida"), id + " :: tagged excepcion");
            else assertTrue(!g.has("excepcion") && g.has("salida"), id + " :: tagged completo but the legacy threw " + g.get("excepcion"));
            assertTrue(CasoCerT5.conTag(id, "front:F-J"), id);
        }
    }

    @Test
    @DisplayName("the determinism report says: every case ran 20 times in the oracle and none is unstable (an unstable case gets no golden)")
    void elInformeDeDeterminismo() throws Exception {
        Path rep = CasoCerT5.recursos().resolve("determinism-cer-t5.txt");
        assertTrue(Files.exists(rep), "determinism-cer-t5.txt missing (run legacy-oracle/sync_cer_t5.py)");
        String primera = Files.readAllLines(rep, StandardCharsets.UTF_8).get(0);
        assertTrue(primera.contains("repeats per case: 20") && primera.contains("hard: 0"), primera);
        assertTrue(primera.startsWith("cases executed: " + CasoCerT5.casos().size() + ","), "the report covers every case: " + primera);
    }

    @ParameterizedTest
    @ValueSource(strings = { "17788687", "17405614", "qa01", "desa01", "presura", "connections.properties", "getPropertiesConnection", "password", "passwd", "secret", "apikey", "bearer ", "jdbc:sap",
            "jdbc:hdb", "https://", "http://" })
    @DisplayName("RULE-076: no id, connection alias, URL or credential of the legacy main() / of a real environment is in the T5 fixtures (the CallBatch values are FAKE text)")
    void sinDatosRealesEnLosFixtures(String prohibido) throws Exception {
        List<String> hallazgos = new ArrayList<>();
        try (Stream<Path> s = Files.walk(CasoCerT5.directorio())) {
            for (Path p : s.filter(Files::isRegularFile).toList()) {
                if (Files.readString(p, StandardCharsets.UTF_8).toLowerCase().contains(prohibido)) hallazgos.add(CasoCerT5.directorio().relativize(p) + " contains '" + prohibido + "'");
            }
        }
        assertTrue(hallazgos.isEmpty(), "real-looking data in the fixtures: " + hallazgos);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static void cerradaDa503(String id, String clave, String compuerta) throws Exception {
        CasoCerT5.Corrida c = CasoCerT5.ejecutarReal(id, CasoCerT5.Opciones.abiertas().cerrando(clave));
        assertTrue(c.excepcion instanceof ServicioNoServidoException, id + " :: with " + clave + " closed the answer is ServicioNoServidoException (503, the router repeats the request); it was " + c.excepcionTexto);
        assertEquals(compuerta, ((ServicioNoServidoException) c.excepcion).compuerta(), id + " :: the closed gate that is named");
        JsonObject v = c.volcado;
        assertEquals("ROLLBACK_HTTP503", v.get("decisionServlet").getAsString(), id);
        assertEquals(CerCosturaT5.txt(v.get("tablasIniciales")), CerCosturaT5.txt(v.get("tablasTrasLaLlamada")), id + " :: the gate is evaluated BEFORE the first write: nothing was written");
        assertEquals("[]", CerCosturaT5.txt(v.get("secuenciasSolicitadas")), id + " :: no sequence was consumed");
        for (JsonElement e : v.getAsJsonArray("colaboradores")) {
            String nombre = e.getAsJsonObject().get("colaborador").getAsString();
            assertFalse(ESCRITORES.contains(nombre), id + " :: " + nombre + " was called before the gate " + compuerta + " was evaluated");
        }
    }

    /** a top-level collaborator call with that name */
    static boolean llamo(JsonObject volcado, String colaborador) {
        if (!volcado.has("colaboradores")) return false;
        for (JsonElement e : volcado.getAsJsonArray("colaboradores")) {
            if (colaborador.equals(e.getAsJsonObject().get("colaborador").getAsString())) return true;
        }
        return false;
    }

    /** the legacy run made at least one call to a writer or notifier */
    static boolean escribe(JsonObject volcado) {
        for (String w : ESCRITORES) {
            if (llamo(volcado, w)) return true;
        }
        return false;
    }

    private static List<String> campos(Class<?> c) {
        List<String> out = new ArrayList<>();
        for (Field f : c.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) || !Modifier.isPublic(f.getModifiers())) continue;
            String tipo = f.getGenericType() instanceof ParameterizedType pt ? f.getType().getSimpleName() + "<" + ((Class<?>) pt.getActualTypeArguments()[0]).getSimpleName() + ">" : f.getType().getSimpleName();
            out.add(f.getName() + ":" + tipo);
        }
        return out;
    }

    private static void invocar(Method m, Object o) throws Throwable {
        try {
            m.invoke(o);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
