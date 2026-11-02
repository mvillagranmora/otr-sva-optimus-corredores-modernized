package cl.bcs.fondomutuo.aportereinversion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.aportereinversion.CasoCsr.Perfil;
import cl.bcs.fondomutuo.certificacion.ApiT4c;
import cl.bcs.fondomutuo.certificacion.CrearAporteReinversionPort;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * F3 W3 T4-C, the in-process adapter of AR for Certificar ({@code AporteReinversionJsonAdapter}: {@code CrearAporteReinversionPort} as JSON in / JSON out over the REAL {@code AporteReinversionPort}). The
 * CSR goldens (recorded from the UNMODIFIED legacy {@code CrearSolicitudAporteReinversion}, 20 runs per case) run with the adapter in the place of {@code ApiCsr.llamar}: the case {@code entrada} JSON goes in,
 * the {@code JsonObject} comes out, and {@link CsrCostura} compares exactly as {@code CsrEquivalenciaTest} does ({@code salidaCruda}, tables, collaborator calls, sequences, session, the decision of the host
 * and the exception: an exception of the legacy is the same exception CLASS, so the adapter must not wrap what the port throws). The adapter holds no state. The gates G1, G2, G3 and G-CUS belong to the
 * real port (profile ABIERTAS here), not to the adapter.
 *
 * <p>RED BEFORE CODE: the adapter does not exist yet, so every case fails with "T4-C not implemented: the production class ... does not exist yet". A run with 0 cases compared FAILS. With
 * {@code -Dcer.autocomprobacion=true} (or {@code -Dcsr.autocomprobacion=true}) the run IS the golden: a self-check of the comparison, it proves nothing about the adapter.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CerT4cAdaptadorArTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    private static boolean autocomprobacion() {
        return Boolean.getBoolean("cer.autocomprobacion") || Boolean.getBoolean("csr.autocomprobacion");
    }

    /** (id, the rule cards the case pins: its `ref`) */
    static Stream<Arguments> casos() {
        List<String> ids = CasoCsr.casos();
        assertTrue(ids.size() >= 220, "the FMM-CSR matrix has at least 220 case directories (224 at the time of writing), found " + ids.size());
        return ids.stream().map(id -> Arguments.of(id, CasoCsr.ref(id)));
    }

    @Test
    @Order(0)
    @DisplayName("T4-C adapter: every CSR case has its recorded golden (a missing golden is a failure, never a skip)")
    void todosLosCasosTienenGolden() {
        List<String> sin = new ArrayList<>();
        for (String id : CasoCsr.casos()) {
            if (!CasoCsr.tieneGolden(id)) sin.add(id);
        }
        assertTrue(sin.isEmpty(), "cases without golden (record them with legacy-oracle/record_csr.sh): " + sin);
    }

    @ParameterizedTest(name = "{0} [{1}]")
    @MethodSource("casos")
    @Order(1)
    @DisplayName("T4-C adapter equivalence with the recorded legacy AR, THROUGH the JSON adapter (FM-REI-41..68)")
    void equivalentePorElAdaptador(String id, String reglas) throws Exception {
        JsonObject expected = CasoCsr.golden(id);
        JsonObject actual = autocomprobacion() ? expected : CasoCsr.ejecutarPorAdaptador(id, Perfil.ABIERTAS);
        CsrCostura.comparar(id, expected, actual);
        EJECUTADOS.incrementAndGet();
    }

    @Test
    @Order(2)
    @DisplayName("T4-C adapter: every case was compared (equivalence cases executed: N of M)")
    void todosLosCasosSeEjecutaron() {
        System.out.println("equivalence cases executed: " + EJECUTADOS.get() + " of " + CasoCsr.casos().size()
                + (autocomprobacion() ? " [SELF-CHECK: the runs were the goldens, the adapter was NOT exercised]" : ""));
        assertTrue(EJECUTADOS.get() > 0, "equivalence cases executed: 0 => not proven");
        assertEquals(CasoCsr.casos().size(), EJECUTADOS.get(), "a case was not compared");
    }

    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    // shape and unit behaviour of the adapter (a stub of the real port: nothing here reads a database)
    // ------------------------------------------------------------------------------------------------------------------------------------------------------------
    private static final String ENTRADA = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":97,\"p_IdModulo\":\"FM\",\"p_SimularOperacion\":\"S\",\"campoDesconocido\":[1,2],"
            + "\"p_SolicitudesAporte\":[{\"IdInterno\":5,\"Monto\":1250.50,\"IdSerieFondo\":\"SERIE-X\"}]}";

    @Test
    @DisplayName("T4-C adapter shape: public concrete class, implements CrearAporteReinversionPort, ONE public constructor (AporteReinversionPort), final fields only (no state)")
    void forma() {
        Class<?> c = ApiT4c.cargar(ApiT4c.ADAPTADOR);
        assertTrue(Modifier.isPublic(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()));
        assertTrue(CrearAporteReinversionPort.class.isAssignableFrom(c), "implements the JSON port Certificar consumes");
        assertEquals(1, c.getConstructors().length);
        assertEquals(List.of(AporteReinversionPort.class), List.of(c.getConstructors()[0].getParameterTypes()));
        for (var f : c.getDeclaredFields()) {
            if (f.isSynthetic()) continue;
            assertTrue(Modifier.isFinal(f.getModifiers()), "no mutable state: " + f);
            assertFalse(Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive() && f.getType() != String.class, "no static collaborator or cache: " + f);
        }
    }

    @Test
    @DisplayName("T4-C adapter, JSON -> Entrada -> logica -> Salida -> JSON with the plain Gson: absent = null, unknown keys ignored, nulls not written, the input is not modified")
    void convierteConGsonPlano() throws Exception {
        AtomicReference<AporteReinversionContrato.Entrada> visto = new AtomicReference<>();
        CrearAporteReinversionPort adaptador = ApiT4c.adaptador(e -> {
            visto.set(e);
            AporteReinversionContrato.Salida s = new AporteReinversionContrato.Salida();
            s.P_OUT_ERRORESCONTADOR = 3L;
            return s;
        });
        JsonObject in = JsonParser.parseString(ENTRADA).getAsJsonObject();
        JsonObject out = adaptador.crear(in);
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":3,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", out.toString());
        AporteReinversionContrato.Entrada e = visto.get();
        assertEquals(1L, e.p_IdEmpresa);
        assertEquals(97L, e.p_IdUsuario);
        assertEquals("FM", e.p_IdModulo);
        assertEquals("S", e.p_SimularOperacion);
        assertEquals(null, e.p_UsaReglasNegocio, "absent => null");
        assertEquals(1, e.p_SolicitudesAporte.size());
        assertEquals(5L, e.p_SolicitudesAporte.get(0).IdInterno);
        assertEquals(0, new BigDecimal("1250.50").compareTo(e.p_SolicitudesAporte.get(0).Monto));
        assertEquals("SERIE-X", e.p_SolicitudesAporte.get(0).IdSerieFondo);
        assertEquals(JsonParser.parseString(ENTRADA), in, "the adapter does not modify its input");
    }

    @Test
    @DisplayName("T4-C adapter: the port is called exactly once per call and the adapter keeps nothing between calls")
    void unaLlamadaPorCall() throws Exception {
        AtomicInteger n = new AtomicInteger();
        CrearAporteReinversionPort adaptador = ApiT4c.adaptador(e -> {
            AporteReinversionContrato.Salida s = new AporteReinversionContrato.Salida();
            s.P_OUT_ERRORESCONTADOR = (long) n.incrementAndGet();
            return s;
        });
        assertEquals("1", adaptador.crear(new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsString());
        assertEquals("2", adaptador.crear(JsonParser.parseString(ENTRADA).getAsJsonObject()).get("P_OUT_ERRORESCONTADOR").getAsString());
        assertEquals("3", adaptador.crear(new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsString());
    }

    @Test
    @DisplayName("T4-C adapter exceptions: the port's exception goes up UNCHANGED, same instance (runtime, checked IOException / SQLException, the 503 of the gates G1, G2, G3, G-CUS): Certificar decides, not the adapter")
    void excepcionesSubenIntactas() {
        List<Exception> todas = List.of(new IllegalStateException("estado"), new IOException("io"), new SQLException("sql"), new Exception("plana"),
                new ServicioNoServidoException("G1", "tesoreria"), new ServicioNoServidoException("G-CUS", "custodia"));
        for (Exception esperada : todas) {
            CrearAporteReinversionPort adaptador = ApiT4c.adaptador(e -> {
                throw esperada;
            });
            Exception real = assertThrows(Exception.class, () -> adaptador.crear(JsonParser.parseString(ENTRADA).getAsJsonObject()));
            assertSame(esperada, real, esperada.getClass().getSimpleName());
        }
    }
}
