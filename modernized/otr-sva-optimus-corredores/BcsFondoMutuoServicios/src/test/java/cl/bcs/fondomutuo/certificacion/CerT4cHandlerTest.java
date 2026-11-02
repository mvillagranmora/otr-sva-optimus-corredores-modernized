package cl.bcs.fondomutuo.certificacion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.List;
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
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ResultadoCertificacion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;

/**
 * F3 W3 T4-C, the JavaCall handler of {@code cl.bcs.fondomutuo.CertificarSolicitudAporteRescate} (legacy {@code call(Connection, JsonObject)}, C:360-368): the shape of the class (RULE-076: only the
 * {@code call} is ported, never the legacy {@code main}), the allow-list (13), the list that opens it (closed by default: ServicioNoServidoException G0 BEFORE anything is read), the JSON codec (the plain
 * {@code new Gson()} both ways: an absent field is null, nulls are NOT written) and the exceptions of the port, which go up untouched. The equivalence of the whole chain through the handler is
 * {@code CerT4cEquivalenciaTest}.
 *
 * <p>RED BEFORE CODE: the handler does not exist yet, every test that needs it fails with "T4-C not implemented: the production class ... does not exist yet" (see {@link ApiT4c}).
 */
class CerT4cHandlerTest {

    private static final String ENTRADA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FM\",\"p_SimularOperacion\":\"S\",\"campoDesconocido\":{\"x\":1},"
            + "\"p_Solicitudes\":[{\"IdSolicitudAporteRescate\":7},{\"IdSolicitudAporteRescate\":8,\"IdTipoCanalIngreso\":\"OWP\"}]}";

    private static final CertificarAporteRescatePort NO_TOCAR = e -> {
        throw new AssertionError("the service must not run");
    };

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C handler shape: public class, @Component, implements JavaCallHandler, ONE public constructor (CertificarAporteRescatePort, Compuertas), fields final (RULE-076: no main)")
    void forma() {
        Class<?> c = ApiT4c.cargar(ApiT4c.HANDLER);
        assertTrue(Modifier.isPublic(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        assertTrue(c.isAnnotationPresent(Component.class), "a @Component (the dispatcher collects the handlers from the context)");
        assertTrue(JavaCallHandler.class.isAssignableFrom(c), "implements JavaCallHandler");
        assertEquals(1, c.getConstructors().length, "one public constructor");
        assertEquals(List.of(CertificarAporteRescatePort.class, Compuertas.class), List.of(c.getConstructors()[0].getParameterTypes()));
        for (var f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            assertTrue(Modifier.isFinal(f.getModifiers()), "no mutable state: " + f);
        }
        for (var m : c.getDeclaredMethods()) {
            assertFalse(m.getName().equals("main"), "the legacy main() is never ported (RULE-076)");
        }
    }

    @Test
    @DisplayName("T4-C fqn(): the legacy class name, the constant of the dispatcher")
    void fqn() {
        assertEquals("cl.bcs.fondomutuo.CertificarSolicitudAporteRescate", ApiT4c.fqnDelDispatcher());
        assertEquals(ApiT4c.FQN_LEGACY, ApiT4c.fqnDelDispatcher());
        assertEquals(ApiT4c.FQN_LEGACY, ApiT4c.handler(NO_TOCAR, compuertas("")).fqn());
    }

    @Test
    @DisplayName("T4-C allow-list: 13 FQN, the new one WRITES so it is NOT a read class, and the dispatcher accepts the handler")
    void listaBlancaYDispatcher() {
        String fqn = ApiT4c.fqnDelDispatcher();
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(fqn), "in the allow-list");
        assertEquals(13, JavaCallDispatcher.LISTA_BLANCA.size(), "12 + Certificar");
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(fqn), "its unit of work is read-write");
        assertEquals(8, JavaCallDispatcher.CLASES_LECTURA.size(), "the reads stay the eight of F3 W1");
        JavaCallDispatcher d = new JavaCallDispatcher(List.of(ApiT4c.handler(NO_TOCAR, compuertas(""))));
        assertEquals(java.util.Set.of(fqn), d.servidas());
        assertSame(d.buscar(fqn).orElseThrow().getClass(), ApiT4c.cargar(ApiT4c.HANDLER));
    }

    @Test
    @DisplayName("T4-C list: the constant is CERTIFICAR_SOLICITUD_APORTE_RESCATE and the Manejador validator treats it as a UI write, never as a read nor as a list of the Manejador")
    void listaYValidador() {
        assertEquals("CERTIFICAR_SOLICITUD_APORTE_RESCATE", ApiT4c.lista());
        assertTrue(ManejadorConfiguracion.LISTAS_ESCRITURA_UI.contains(ApiT4c.lista()), "accepted by the validator like CARGAR_VALOR_CUOTA");
        assertFalse(ManejadorConfiguracion.LISTAS_LECTURA.contains(ApiT4c.lista()), "it writes: not a read list");
        assertTrue(ManejadorConfiguracion.LISTAS_ESCRITURA_UI.contains("CARGAR_VALOR_CUOTA"), "the existing one stays");
        assertEquals(2, ManejadorConfiguracion.LISTAS_ESCRITURA_UI.size());
        assertFalse(java.util.Arrays.stream(cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista.values()).anyMatch(l -> l.name().equals(ApiT4c.lista())),
                "it is not a list of the Manejador");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // closed by default: G0 before anything is read
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @ParameterizedTest(name = "listas-servidas=[{0}] => G0 and nothing is read")
    @ValueSource(strings = { "", "NORMAL", "RESCATE_REINVERSION", "CARGAR_VALOR_CUOTA", "NORMAL,RESCATE_REINVERSION,CARGAR_VALOR_CUOTA,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_SOLICITUDES,CONSULTAR_DETALLE_SOLICITUD",
            "CERTIFICAR_SOLICITUD_APORTE_RESCATE_X", "CERTIFICAR_SOLICITUD", "CERTIFICAR" })
    @DisplayName("T4-C closed by default: a list that does not name CERTIFICAR_SOLICITUD_APORTE_RESCATE raises ServicioNoServidoException G0 BEFORE the input is read (even a null JSON)")
    void cerradaPorDefecto(String listas) {
        AtomicInteger llamadas = new AtomicInteger();
        CertificarAporteRescatePort servicio = e -> {
            llamadas.incrementAndGet();
            return new Salida();
        };
        JavaCallHandler h = ApiT4c.handler(servicio, compuertas(listas));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, null));
        assertEquals("G0", ex.compuerta());
        ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, JsonParser.parseString(ENTRADA).getAsJsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get(), "the service never runs with the list closed");
    }

    @Test
    @DisplayName("T4-C the list is read at each call from the Compuertas it was given, with no copy of its own: open => served, closed => 503")
    void soloLaListaLaAbre() throws Exception {
        JavaCallHandler cerrado = ApiT4c.handler(e -> new Salida(), compuertas("NORMAL"));
        JavaCallHandler abierto = ApiT4c.handler(e -> new Salida(), compuertas("NORMAL," + ApiT4c.lista()));
        assertThrows(ServicioNoServidoException.class, () -> cerrado.call(null, new JsonObject()));
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", abierto.call(null, new JsonObject()).toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // open: the legacy call
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C open list, JSON in: the plain Gson reads the legacy JSON (absent field = null, the defaults of the class, unknown keys ignored) and the port gets it")
    void entradaConGsonPlano() throws Exception {
        AtomicReference<Entrada> visto = new AtomicReference<>();
        JavaCallHandler h = ApiT4c.handlerAbierto(e -> {
            visto.set(e);
            return new Salida();
        });
        JsonObject in = JsonParser.parseString(ENTRADA).getAsJsonObject();
        h.call(null, in);
        Entrada e = visto.get();
        assertEquals(1L, e.p_IdEmpresa);
        assertEquals(97L, e.p_IdUsuario);
        assertEquals("FM", e.p_IdModulo);
        assertEquals("S", e.p_SimularOperacion);
        assertNull(e.p_UsaReglasNegocio, "absent => null (D-MAN-2)");
        assertNull(e.p_FechaCertificacion);
        assertNull(e.p_IdTipoCanalIngreso);
        assertEquals("S", e.p_NotificaOWP, "default of the class");
        assertEquals("N", e.p_CertificacionAporteSwitchSimulado, "default of the class");
        assertEquals(2, e.p_Solicitudes.size());
        assertEquals(7L, e.p_Solicitudes.get(0).IdSolicitudAporteRescate);
        assertEquals("", e.p_Solicitudes.get(0).IdTipoCanalIngreso, "default of the class");
        assertEquals("OWP", e.p_Solicitudes.get(1).IdTipoCanalIngreso);
        assertEquals(JsonParser.parseString(ENTRADA), in, "the handler does not modify its input");
    }

    @Test
    @DisplayName("T4-C open list, JSON out: the plain Gson writes the Salida (field order of the class, NO null keys, no date format)")
    void salidaConGsonPlano() throws Exception {
        Salida s = new Salida();
        s.P_OUT_ERRORESCONTADOR = 2L;
        ResultadoCertificacion r = new ResultadoCertificacion();
        r.IdSolicitudAporteRescate = 7L;
        r.IdSerieFondo = "SERIE-X";
        r.Estado = "CRT";
        s.P_OUT_RESULTADO.add(r);
        JsonObject out = ApiT4c.handlerAbierto(e -> s).call(null, JsonParser.parseString(ENTRADA).getAsJsonObject());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":2,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[{\"IdSolicitudAporteRescate\":7,\"IdSolicitudAporteRescateRelacionado\":0,\"IdSerieFondo\":\"SERIE-X\","
                + "\"Estado\":\"CRT\",\"DocumentoIdentidad\":\"\",\"CodigoPortafolio\":\"\"}]}", out.toString());
        assertFalse(out.toString().contains("null"), "nulls are not written (Codigo, NombreEstado, DescripcionCodigo are null in the Salida)");
        assertFalse(out.getAsJsonArray("P_OUT_RESULTADO").get(0).getAsJsonObject().has("Codigo"));
    }

    @Test
    @DisplayName("T4-C open list: the connection is the host's and the handler never touches it (null works), the empty request is served, and two calls do not share state")
    void sinConexionNiEstado() throws Exception {
        AtomicInteger n = new AtomicInteger();
        JavaCallHandler h = ApiT4c.handlerAbierto(e -> {
            Salida s = new Salida();
            s.P_OUT_ERRORESCONTADOR = (long) n.incrementAndGet() + (e.p_IdEmpresa == null ? 0 : e.p_IdEmpresa);
            return s;
        });
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":1,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", h.call(null, new JsonObject()).toString());
        JsonObject uno = new JsonObject();
        uno.addProperty("p_IdEmpresa", 10);
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":12,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", h.call(null, uno).toString());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":3,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", h.call(null, new JsonObject()).toString());
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // exceptions of the port
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    @Test
    @DisplayName("T4-C exceptions: whatever the port throws goes up UNTOUCHED (same instance: runtime, checked, 503 of a gate), the legacy call has no try/catch")
    void excepcionesSubenIntactas() {
        List<Exception> todas = List.of(new IllegalStateException("estado"), new IOException("io"), new SQLException("sql"), new Exception("plana"),
                new ServicioNoServidoException("G8", "notificaciones cerradas"), new ServicioNoServidoException("G-CUS", "custodia"));
        for (Exception esperada : todas) {
            JavaCallHandler h = ApiT4c.handlerAbierto(e -> {
                throw esperada;
            });
            Exception real = assertThrows(Exception.class, () -> h.call(null, JsonParser.parseString(ENTRADA).getAsJsonObject()));
            assertSame(esperada, real, esperada.getClass().getSimpleName());
        }
    }

    @Test
    @DisplayName("T4-C exceptions: an Error of the port (a failing assertion of a test double) is not swallowed either")
    void erroresSubenIntactos() {
        AssertionError esperado = new AssertionError("boom");
        JavaCallHandler h = ApiT4c.handlerAbierto(e -> {
            throw esperado;
        });
        assertSame(esperado, assertThrows(AssertionError.class, () -> h.call(null, new JsonObject())));
    }
}
