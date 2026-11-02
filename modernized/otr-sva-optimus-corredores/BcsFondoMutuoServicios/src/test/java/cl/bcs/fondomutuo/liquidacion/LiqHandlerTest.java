package cl.bcs.fondomutuo.liquidacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallHandler;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateConfiguracion;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;

/**
 * F3 W3 T6-B, the JavaCall handler of {@code cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate} (legacy {@code call(Connection, JsonObject)}, L:203-213; registry id 1689): the shape of the class (RULE-076: only the
 * {@code call} and {@code logica} are ported, never the legacy {@code main}), the allow-list (14), the list that opens it (closed by default: ServicioNoServidoException G0 BEFORE anything is read), the JSON codec
 * ({@code Utils.getGsonBuilder()} both ways: an absent field is null, NULL KEYS ARE WRITTEN), the RECOUNT of the errors that {@code call} does with {@code MensajeSalida.getNumeroErrores} (comparing {@code tipo}
 * by REFERENCE, D-REQ-11, FM-LIQ-01) and the exceptions of the port, which go up untouched. The equivalence of the whole chain through the handler is {@code LiqEquivalenciaTest}.
 *
 * <p>RED BEFORE CODE: the handler does not exist yet, every test that needs it fails with "T6-B not implemented: the production class ... does not exist yet" (see {@link ApiT6b}).
 */
class LiqHandlerTest {

    private static final String ENTRADA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"S\",\"campoDesconocido\":{\"x\":1},"
            + "\"p_Movimientos\":[{\"IdMovimiento\":7},{\"IdMovimiento\":8}],\"p_Filtros\":[{\"IdInterno\":3,\"TipoSolicitud\":\"APO\",\"FechaCertificacion\":\"2026-03-02\"}]}";

    private static final Object NO_TOCAR_PUERTO() {
        return ApiT6b.puertoFalso(e -> {
            throw new AssertionError("the service must not run");
        });
    }

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    private static Object salidaVacia() {
        return ApiT6b.nuevo(ApiT6b.SALIDA);
    }

    @SuppressWarnings("unchecked")
    private static void agregar(Object salida, String lista, Object elemento) {
        ((List<Object>) ApiT6b.campo(salida, lista)).add(elemento);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B handler shape: public class, @Component, implements JavaCallHandler, ONE public constructor (LiquidacionPort, Compuertas), fields final (RULE-076: no main)")
    void forma() {
        Class<?> c = ApiT6b.cargar(ApiT6b.HANDLER);
        assertTrue(Modifier.isPublic(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        assertTrue(c.isAnnotationPresent(Component.class), "a @Component (the dispatcher collects the handlers from the context)");
        assertTrue(JavaCallHandler.class.isAssignableFrom(c), "implements JavaCallHandler");
        assertEquals(1, c.getConstructors().length, "one public constructor");
        assertEquals(List.of(ApiT6b.cargar(ApiT6b.PUERTO), Compuertas.class), List.of(c.getConstructors()[0].getParameterTypes()));
        for (var f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            assertTrue(Modifier.isFinal(f.getModifiers()), "no mutable state: " + f);
        }
        for (var m : c.getDeclaredMethods()) {
            assertFalse(m.getName().equals("main"), "the legacy main() is never ported (RULE-076)");
        }
    }

    @Test
    @DisplayName("T6-B fqn(): the legacy class name, the constant of the dispatcher")
    void fqn() {
        assertEquals("cl.bcs.fondomutuo.LiquidacionSolicitudAporteRescate", ApiT6b.fqnDelDispatcher());
        assertEquals(ApiT6b.FQN_LEGACY, ApiT6b.fqnDelDispatcher());
        assertEquals(ApiT6b.FQN_LEGACY, ApiT6b.handler(NO_TOCAR_PUERTO(), compuertas("")).fqn());
    }

    @Test
    @DisplayName("T6-B allow-list: 14 FQN (13 + Liquidacion), the new one WRITES so it is NOT a read class, and the dispatcher accepts the handler")
    void listaBlancaYDispatcher() {
        String fqn = ApiT6b.fqnDelDispatcher();
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(fqn), "in the allow-list");
        assertEquals(14, JavaCallDispatcher.LISTA_BLANCA.size(), "13 + Liquidacion");
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(JavaCallDispatcher.CERTIFICAR_SOLICITUD_APORTE_RESCATE), "Certificar stays");
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(fqn), "its unit of work is read-write (UPDATE of the solicitudes)");
        assertEquals(8, JavaCallDispatcher.CLASES_LECTURA.size(), "the reads stay the eight of F3 W1");
        JavaCallDispatcher d = new JavaCallDispatcher(List.of(ApiT6b.handler(NO_TOCAR_PUERTO(), compuertas(""))));
        assertEquals(java.util.Set.of(fqn), d.servidas());
        assertSame(d.buscar(fqn).orElseThrow().getClass(), ApiT6b.cargar(ApiT6b.HANDLER));
    }

    @Test
    @DisplayName("T6-B list: the constant is LIQUIDACION_SOLICITUD_APORTE_RESCATE and the Manejador validator treats it as a UI write (LISTAS_ESCRITURA_UI: 3), never as a read nor as a list of the Manejador")
    void listaYValidador() {
        assertEquals(ApiT6b.LISTA_ESPERADA, ApiT6b.lista());
        assertTrue(ManejadorConfiguracion.LISTAS_ESCRITURA_UI.contains(ApiT6b.lista()), "accepted by the validator like CARGAR_VALOR_CUOTA");
        assertFalse(ManejadorConfiguracion.LISTAS_LECTURA.contains(ApiT6b.lista()), "it writes: not a read list");
        assertTrue(ManejadorConfiguracion.LISTAS_ESCRITURA_UI.contains("CARGAR_VALOR_CUOTA"), "the existing ones stay");
        assertTrue(ManejadorConfiguracion.LISTAS_ESCRITURA_UI.contains(CertificarAporteRescateConfiguracion.LISTA), "the existing ones stay");
        assertEquals(3, ManejadorConfiguracion.LISTAS_ESCRITURA_UI.size());
        assertEquals(8, ManejadorConfiguracion.LISTAS_LECTURA.size());
        assertFalse(java.util.Arrays.stream(cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista.values()).anyMatch(l -> l.name().equals(ApiT6b.lista())), "it is not a list of the Manejador");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // closed by default: G0 before anything is read
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @ParameterizedTest(name = "listas-servidas=[{0}] => G0 and nothing is read")
    @ValueSource(strings = { "", "NORMAL", "RESCATE_REINVERSION", "CARGAR_VALOR_CUOTA", "CERTIFICAR_SOLICITUD_APORTE_RESCATE", "NORMAL,RESCATE_REINVERSION,CARGAR_VALOR_CUOTA,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_SOLICITUDES",
            "LIQUIDACION_SOLICITUD_APORTE_RESCATE_X", "LIQUIDACION", "LIQUIDACION_SOLICITUD" })
    @DisplayName("T6-B closed by default: a list that does not name LIQUIDACION_SOLICITUD_APORTE_RESCATE raises ServicioNoServidoException G0 BEFORE the input is read (even a null JSON); the list of Certificar does not open it")
    void cerradaPorDefecto(String listas) {
        AtomicInteger llamadas = new AtomicInteger();
        Object puerto = ApiT6b.puertoFalso(e -> {
            llamadas.incrementAndGet();
            return salidaVacia();
        });
        JavaCallHandler h = ApiT6b.handler(puerto, compuertas(listas));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, null));
        assertEquals("G0", ex.compuerta());
        ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, JsonParser.parseString(ENTRADA).getAsJsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get(), "the service never runs with the list closed");
    }

    @Test
    @DisplayName("T6-B the list is read at each call from the Compuertas it was given, with no copy of its own: open => served, closed => 503")
    void soloLaListaLaAbre() throws Exception {
        JavaCallHandler cerrado = ApiT6b.handler(ApiT6b.puertoFalso(e -> salidaVacia()), compuertas("NORMAL"));
        JavaCallHandler abierto = ApiT6b.handler(ApiT6b.puertoFalso(e -> salidaVacia()), compuertas("NORMAL," + ApiT6b.lista()));
        assertThrows(ServicioNoServidoException.class, () -> cerrado.call(null, new JsonObject()));
        assertEquals("{\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[],\"P_OUT_ERRORESCONTADOR\":0}", abierto.call(null, new JsonObject()).toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // open: the legacy call
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T6-B open list, JSON in: Utils.getGsonBuilder() reads the legacy JSON (absent field = null, the defaults of the class: empty lists, unknown keys ignored) and the port gets it")
    void entradaConElGsonDelLegacy() throws Exception {
        AtomicReference<Object> visto = new AtomicReference<>();
        JavaCallHandler h = ApiT6b.handlerAbierto(ApiT6b.puertoFalso(e -> {
            visto.set(e);
            return salidaVacia();
        }));
        JsonObject in = JsonParser.parseString(ENTRADA).getAsJsonObject();
        h.call(null, in);
        Object e = visto.get();
        assertNotNull(e, "the port was called");
        assertEquals(1L, ApiT6b.campo(e, "p_IdEmpresa"));
        assertEquals(97L, ApiT6b.campo(e, "p_IdUsuario"));
        assertEquals("FMM", ApiT6b.campo(e, "p_IdModulo"));
        assertEquals("S", ApiT6b.campo(e, "p_SimularOperacion"));
        assertNull(ApiT6b.campo(e, "p_UsaReglasNegocio"), "absent => null (D-MAN-2)");
        List<?> movs = (List<?>) ApiT6b.campo(e, "p_Movimientos");
        assertEquals(2, movs.size());
        assertEquals(7L, ApiT6b.campo(movs.get(0), "IdMovimiento"));
        assertEquals(8L, ApiT6b.campo(movs.get(1), "IdMovimiento"));
        List<?> filtros = (List<?>) ApiT6b.campo(e, "p_Filtros");
        assertEquals(1, filtros.size());
        assertEquals(3L, ApiT6b.campo(filtros.get(0), "IdInterno"));
        assertEquals("APO", ApiT6b.campo(filtros.get(0), "TipoSolicitud"));
        assertEquals("2026-03-02", ApiT6b.campo(filtros.get(0), "FechaCertificacion"));
        assertNull(ApiT6b.campo(filtros.get(0), "IdEmisor"), "absent => null");
        assertEquals(JsonParser.parseString(ENTRADA), in, "the handler does not modify its input");
    }

    @Test
    @DisplayName("T6-B open list: the empty request keeps the defaults of the class (p_Movimientos and p_Filtros are empty lists, not null); an explicit null stays null")
    void listasPorDefectoYNulas() throws Exception {
        AtomicReference<Object> visto = new AtomicReference<>();
        JavaCallHandler h = ApiT6b.handlerAbierto(ApiT6b.puertoFalso(e -> {
            visto.set(e);
            return salidaVacia();
        }));
        h.call(null, new JsonObject());
        assertEquals(List.of(), ApiT6b.campo(visto.get(), "p_Movimientos"));
        assertEquals(List.of(), ApiT6b.campo(visto.get(), "p_Filtros"));
        h.call(null, JsonParser.parseString("{\"p_Movimientos\":null,\"p_Filtros\":null}").getAsJsonObject());
        assertNull(ApiT6b.campo(visto.get(), "p_Movimientos"));
        assertNull(ApiT6b.campo(visto.get(), "p_Filtros"));
    }

    @Test
    @DisplayName("T6-B open list, JSON out: the codec of the legacy WRITES the null keys, in the field order of the class (P_OUT_RESULTADO, P_OUT_MENSAJES, P_OUT_ERRORESCONTADOR)")
    void salidaConNulos() throws Exception {
        Object s = salidaVacia();
        Object r = ApiT6b.nuevo(ApiT6b.RESULTADO);
        ApiT6b.con(r, "IdInterno", 0L, "IdSolicitudAporteRescate", 7L, "IdSerieFondo", "SERIE-X", "Codigo", null, "Estado", "ING", "NombreEstado", "");
        agregar(s, "P_OUT_RESULTADO", r);
        JsonObject out = ApiT6b.llamarPorHandler(ApiT6b.puertoFalso(e -> s), JsonParser.parseString(ENTRADA).getAsJsonObject());
        assertEquals("{\"P_OUT_RESULTADO\":[{\"IdInterno\":0,\"IdSolicitudAporteRescate\":7,\"IdSerieFondo\":\"SERIE-X\",\"Codigo\":null,\"Estado\":\"ING\",\"NombreEstado\":\"\"}],"
                + "\"P_OUT_MENSAJES\":[],\"P_OUT_ERRORESCONTADOR\":0}", out.toString());
    }

    @Test
    @DisplayName("T6-B open list: the connection is the host's and the handler never touches it (null works), and two calls do not share state")
    void sinConexionNiEstado() throws Exception {
        AtomicInteger n = new AtomicInteger();
        JavaCallHandler h = ApiT6b.handlerAbierto(ApiT6b.puertoFalso(e -> {
            Object s = salidaVacia();
            Object r = ApiT6b.nuevo(ApiT6b.RESULTADO);
            ApiT6b.con(r, "IdSolicitudAporteRescate", (long) n.incrementAndGet());
            agregar(s, "P_OUT_RESULTADO", r);
            return s;
        }));
        assertTrue(h.call(null, new JsonObject()).toString().contains("\"IdSolicitudAporteRescate\":1"));
        assertTrue(h.call(null, new JsonObject()).toString().contains("\"IdSolicitudAporteRescate\":2"));
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // the recount of the errors (FM-LIQ-01, D-REQ-11): by REFERENCE
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("StringOperationCanBeSimplified")
    private static MensajeSalida mensaje(String tipo) {
        return new MensajeSalida(0L, tipo, "COD", "texto");
    }

    private static long recuento(Object salidaDelPuerto) throws Exception {
        return ApiT6b.llamarPorHandler(ApiT6b.puertoFalso(e -> salidaDelPuerto), new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong();
    }

    @Test
    @DisplayName("FM-LIQ-01 recount: call() IGNORES the counter of the port and counts the messages whose tipo IS the constant ERROR: counter 5 and no message => 0")
    void elContadorDelPuertoSeIgnora() throws Exception {
        Object s = salidaVacia();
        ApiT6b.con(s, "P_OUT_ERRORESCONTADOR", 5L);
        assertEquals(0L, recuento(s));
    }

    @Test
    @DisplayName("FM-LIQ-01 recount: messages built in-process with the constant (same reference) count: one ERROR => 1; ERROR + ERROR + ADVERTENCIA => 2")
    void losMensajesConLaConstanteCuentan() throws Exception {
        Object uno = salidaVacia();
        agregar(uno, "P_OUT_MENSAJES", mensaje(MensajeSalida.MENSAJE_ERROR));
        assertEquals(1L, recuento(uno));
        Object tres = salidaVacia();
        agregar(tres, "P_OUT_MENSAJES", mensaje(MensajeSalida.MENSAJE_ERROR));
        agregar(tres, "P_OUT_MENSAJES", mensaje(MensajeSalida.MENSAJE_ADVERTENCIA));
        agregar(tres, "P_OUT_MENSAJES", mensaje(MensajeSalida.MENSAJE_ERROR));
        assertEquals(2L, recuento(tres));
    }

    @Test
    @DisplayName("FM-LIQ-01 / D-REQ-11 recount: a message whose tipo is an equal String that is NOT the constant (what a parsed JSON of a collaborator gives) never counts: 3 messages 'ERROR' => 0; 'error' => 0")
    void elTipoSeCompararPorReferencia() throws Exception {
        Object s = salidaVacia();
        for (int i = 0; i < 3; i++) agregar(s, "P_OUT_MENSAJES", mensaje(new String("ERROR")));
        agregar(s, "P_OUT_MENSAJES", mensaje("error"));
        assertEquals(0L, recuento(s));
        assertEquals(4, ((List<?>) ApiT6b.campo(s, "P_OUT_MENSAJES")).size());
    }

    @Test
    @DisplayName("FM-LIQ-01 recount: the messages are written as they are (id, tipo, codigo, descripcion) and the recounted counter goes with them")
    void losMensajesSeEscribenIgual() throws Exception {
        Object s = salidaVacia();
        agregar(s, "P_OUT_MENSAJES", new MensajeSalida(4L, MensajeSalida.MENSAJE_ERROR, "NO_SE_INGRESARON_FILTROS", "No se ingresaron filtros"));
        JsonObject out = ApiT6b.llamarPorHandler(ApiT6b.puertoFalso(e -> s), new JsonObject());
        assertEquals("{\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[{\"id\":4,\"tipo\":\"ERROR\",\"codigo\":\"NO_SE_INGRESARON_FILTROS\",\"descripcion\":\"No se ingresaron filtros\"}],\"P_OUT_ERRORESCONTADOR\":1}",
                out.toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // exceptions of the port
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("FM-LIQ-20 exceptions: whatever the port throws goes up UNTOUCHED (same instance: runtime, checked, 503 of a gate, NoSuchElementException), the legacy call has no try/catch")
    void excepcionesSubenIntactas() {
        List<Exception> todas = List.of(new IllegalStateException("estado"), new IOException("io"), new SQLException("sql"), new Exception("plana"), new NullPointerException("npe"),
                new NoSuchElementException("No value present"), new ServicioNoServidoException("G0", "cuenta de inversion cerrada"));
        for (Exception esperada : todas) {
            JavaCallHandler h = ApiT6b.handlerAbierto(ApiT6b.puertoFalso(e -> {
                throw esperada;
            }));
            Exception real = assertThrows(Exception.class, () -> h.call(null, JsonParser.parseString(ENTRADA).getAsJsonObject()));
            assertSame(esperada, real, esperada.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("T6-B exceptions: an Error of the port (a failing assertion of a test double) is not swallowed either")
    void erroresSubenIntactos() {
        AssertionError esperado = new AssertionError("boom");
        JavaCallHandler h = ApiT6b.handlerAbierto(ApiT6b.puertoFalso(e -> {
            throw esperado;
        }));
        assertSame(esperado, assertThrows(AssertionError.class, () -> h.call(null, new JsonObject())));
    }
}
