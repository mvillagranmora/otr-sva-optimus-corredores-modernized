package cl.bcs.fondomutuo.valorcuota;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.manejador.ManejadorConfiguracion;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.manejador.TipoNoServido;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionDestino;

/**
 * The entry of CargarValorCuota (registry id 3035, the only class of F3 W2 and the only UI class that WRITES) is CLOSED BY DEFAULT (decision 5 of the user): unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link CargarValorCuotaConfiguracion#LISTA} ({@code CARGAR_VALOR_CUOTA}) the call is a 503 (gate G0) BEFORE anything is read
 * or written, and the router repeats the request in TomEE. Opening it needs no tesoreria / spot / flujo-caja / owp / custodia flag and no other gate: its only collaborator
 * outside the module is the HTTP log port, whose failure the legacy swallows (decision 2). These tests use a stub port that fails if it runs when it must not.
 */
class CvcCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.CargarValorCuota";
    private static final String VACIA = "{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[],\"P_OUT_MENSAJES\":[]}";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    private static CargarValorCuotaContrato.Salida vacia() {
        return new CargarValorCuotaContrato.Salida();
    }

    @Test
    @DisplayName("CVC gate (decision 5): lista vacia => 503 G0, el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        CargarValorCuotaPort servicio = e -> {
            llamadas.incrementAndGet();
            return vacia();
        };
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class,
                () -> new CargarValorCuotaJavaCallHandler(servicio, compuertas("")).call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("CVC gate: abrir otras listas (NORMAL, RESCATE_REINVERSION, las lecturas) no abre la carga de valor cuota")
    void otrasListasNoLaAbren() {
        CargarValorCuotaPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(servicio,
                compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_DETALLE_SOLICITUD,CONSULTAR_SOLICITUDES,CONSULTAR_VALOR_DOLAR,CARGAR_VALOR"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("CVC gate: la lista se llama CARGAR_VALOR_CUOTA y el FQN es el del legacy")
    void nombreDeLaLista() {
        assertEquals("CARGAR_VALOR_CUOTA", CargarValorCuotaConfiguracion.LISTA);
        assertEquals(FQN, CargarValorCuotaConfiguracion.FQN);
    }

    @Test
    @DisplayName("CVC gate: abierta por configuracion => delega sin otra bandera ni lectura de la conexion; los defectos del pojo del legacy llegan al servicio (FM-CVC-01)")
    void abierta_delegaConLosDefectosDelPojo() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        CargarValorCuotaPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals(-1L, e.p_IdPais, "p_IdPais = -1 by default (CVC:56)");
            assertEquals("FMM", e.p_IdModulo);
            assertNull(e.p_UsaReglasNegocio);
            assertEquals("N", e.p_SimularOperacion);
            assertEquals("S", e.p_AllFunds);
            assertEquals(1, e.p_ValorCuota.size());
            CargarValorCuotaContrato.ValorCuota v = e.p_ValorCuota.get(0);
            assertEquals("FM001A", v.IDSERIEFONDO);
            assertEquals("", v.ISIN, "ISIN defaults to the empty text (CVC:82)");
            assertEquals("", v.FECHACALCULO, "FECHACALCULO defaults to the empty text (CVC:81)");
            assertEquals(0L, v.REGISTRO, "REGISTRO defaults to 0 (CVC:83)");
            assertEquals(0, new java.math.BigDecimal("1500.50").compareTo(v.VALORCUOTA));
            assertEquals("1500.50", v.VALORCUOTA.toString(), "the number keeps the text it came with (scale 2)");
            assertNull(v.OBSERVACION);
            return vacia();
        };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_SimularOperacion\":\"N\",\"p_AllFunds\":\"S\","
                + "\"p_ValorCuota\":[{\"IDSERIEFONDO\":\"FM001A\",\"VALORCUOTA\":1500.50,\"Otro\":1}],\"Otro\":2}").getAsJsonObject();
        JsonObject out = new CargarValorCuotaJavaCallHandler(servicio, compuertas(CargarValorCuotaConfiguracion.LISTA)).call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals(VACIA, out.toString());
    }

    @Test
    @DisplayName("CVC gate: una entrada vacia llega con empresa y usuario nulos y la lista vacia (nunca nula)")
    void entradaVacia() throws Exception {
        CargarValorCuotaPort servicio = e -> {
            assertNull(e.p_IdEmpresa);
            assertNull(e.p_IdUsuario);
            assertNull(e.p_IdModulo);
            assertEquals(-1L, e.p_IdPais);
            assertNotNull(e.p_ValorCuota);
            assertTrue(e.p_ValorCuota.isEmpty());
            return vacia();
        };
        assertEquals(VACIA, new CargarValorCuotaJavaCallHandler(servicio, compuertas(CargarValorCuotaConfiguracion.LISTA)).call(null, new JsonObject()).toString());
    }

    @Test
    @DisplayName("CVC gate: p_ValorCuota null explicito llega como null (el legacy lanza NullPointerException en size())")
    void listaNulaExplicitaLlegaNula() throws Exception {
        CargarValorCuotaPort servicio = e -> {
            assertNull(e.p_ValorCuota);
            return vacia();
        };
        new CargarValorCuotaJavaCallHandler(servicio, compuertas(CargarValorCuotaConfiguracion.LISTA)).call(null, JsonParser.parseString("{\"p_ValorCuota\":null}").getAsJsonObject());
    }

    @Test
    @DisplayName("CVC gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        CargarValorCuotaPort servicio = e -> vacia();
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(servicio, compuertas(" normal , cargar_valor_cuota "));
        assertEquals(0L, h.call(null, new JsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("CVC allow-list: el FQN del legacy esta en la lista blanca, el handler responde a el y NO es una clase de lectura (su transaccion es de escritura)")
    void fqnEnLaListaBlancaYNoEsDeLectura() {
        assertEquals(FQN, JavaCallDispatcher.CARGAR_VALOR_CUOTA);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertFalse(JavaCallDispatcher.CLASES_LECTURA.contains(FQN), "the unit of work of a writing class must NOT be read only");
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(e -> vacia(), compuertas(""));
        assertEquals(FQN, h.fqn());
        assertSame(h, new JavaCallDispatcher(List.of(h)).buscar(FQN).orElseThrow());
        assertTrue(h.getClass().isAnnotationPresent(Component.class), "the handler is a Spring component (it is registered by the dispatcher)");
    }

    @Test
    @DisplayName("CVC contrato: la salida usa Utils.getGsonBuilder() (nulos escritos, fecha yyyy-MM-dd HH:mm:ss.SSS), no el new Gson() plano de las lecturas (FM-CVC-01, D-F3A-25)")
    void salidaConNulosYFechaConHora() throws Exception {
        java.util.TimeZone zona = java.util.TimeZone.getDefault();
        try {
            java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("UTC"));
            CargarValorCuotaPort servicio = e -> {
                CargarValorCuotaContrato.Salida s = new CargarValorCuotaContrato.Salida();
                CargarValorCuotaContrato.Resultado r = new CargarValorCuotaContrato.Resultado();
                r.IdSerieFondo = "FM001A";
                r.FechaVigencia = java.sql.Date.valueOf("2026-03-02");
                s.P_OUT_RESULTADO.add(r);
                return s;
            };
            JsonObject out = new CargarValorCuotaJavaCallHandler(servicio, compuertas(CargarValorCuotaConfiguracion.LISTA)).call(null, new JsonObject());
            assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[{\"IdSerieFondo\":\"FM001A\",\"IdMoneda\":null,\"ISIN\":null,\"FechaVigencia\":\"2026-03-02 00:00:00.000\","
                    + "\"ValorCuota\":0,\"ValorCuotaFechaAnterior\":0,\"Observacion\":null,\"DecimalesValorCuota\":null,\"FechaCalculo\":null,\"Estado\":null,\"DescEstado\":null,"
                    + "\"IdEmpresa\":null,\"EmisorValorCuota\":null,\"ParidadValorCuota\":1,\"ValorCuotaCalculado\":0,\"VariacionValorCuota\":0}],\"P_OUT_MENSAJES\":[]}", out.toString());
        } finally {
            java.util.TimeZone.setDefault(zona);
        }
    }

    @Test
    @DisplayName("CVC contrato: una excepcion del servicio sale tal cual (el anfitrion responde 500 y revierte); el handler no la traga ni la envuelve")
    void laExcepcionDelServicioSale() {
        NullPointerException npe = new NullPointerException("valor nulo");
        CargarValorCuotaPort servicio = e -> {
            throw npe;
        };
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(servicio, compuertas(CargarValorCuotaConfiguracion.LISTA));
        assertSame(npe, assertThrows(NullPointerException.class, () -> h.call(null, new JsonObject())));
    }

    @Test
    @DisplayName("CVC contrato (decision 6): Gson NUEVO por llamada; el handler no guarda ningun Gson ni estado entre llamadas")
    void sinGsonNiEstadoGuardado() throws Exception {
        for (Field f : CargarValorCuotaJavaCallHandler.class.getDeclaredFields()) {
            assertFalse(Gson.class.isAssignableFrom(f.getType()), "field " + f.getName() + " keeps a Gson");
            if (Modifier.isStatic(f.getModifiers())) assertTrue(Modifier.isFinal(f.getModifiers()) && f.getName().equals(f.getName().toUpperCase()), "static state: " + f.getName());
        }
        CargarValorCuotaJavaCallHandler h = new CargarValorCuotaJavaCallHandler(e -> vacia(), compuertas(CargarValorCuotaConfiguracion.LISTA));
        assertEquals(h.call(null, new JsonObject()).toString(), h.call(null, new JsonObject()).toString());
    }

    /** {@code ManejadorConfiguracion.validarListasServidas(listas, destinos, custodiaHabilitada)} is package-private (its own tests live in the manejador package): called by reflection. */
    private static void validar(Set<String> listas, Map<Lista, TipoSolicitudPort> destinos, boolean custodia) {
        try {
            java.lang.reflect.Method m = ManejadorConfiguracion.class.getDeclaredMethod("validarListasServidas", Set.class, Map.class, boolean.class);
            m.setAccessible(true);
            m.invoke(null, listas, destinos, custodia);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException re) throw re;
            throw new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("ManejadorConfiguracion.validarListasServidas(Set, Map, boolean) not found", e);
        }
    }

    private static Map<Lista, TipoSolicitudPort> destinos(boolean conRescateReinversion) {
        Map<Lista, TipoSolicitudPort> d = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) if (l != Lista.NORMAL) d.put(l, new TipoNoServido(l.name()));
        if (conRescateReinversion) {
            d.put(Lista.RESCATE_REINVERSION, new RescateReinversionDestino(in -> {
                throw new AssertionError("not called at start-up");
            }));
        }
        return d;
    }

    @Test
    @DisplayName("CVC gate (start-up): el validador de fondomutuo.solicitud.listas-servidas ACEPTA CARGAR_VALOR_CUOTA sola o con NORMAL, no la cuenta como otra lista del Manejador (regla RESCATE_REINVERSION + custodia cerrada), sigue rechazando un typo y NO es una lista de lectura")
    void elValidadorDelArranqueAceptaLaLista() {
        assertThatCode(() -> validar(Set.of("CARGAR_VALOR_CUOTA"), destinos(false), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("NORMAL", "CARGAR_VALOR_CUOTA"), destinos(false), false)).doesNotThrowAnyException();
        assertThatCode(() -> validar(Set.of("RESCATE_REINVERSION", "CARGAR_VALOR_CUOTA"), destinos(true), false)).doesNotThrowAnyException();
        assertThatThrownBy(() -> validar(Set.of("NORMAL", "CARGAR_VALOR_CUOTAS"), destinos(false), true)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unknown list").hasMessageContaining("CARGAR_VALOR_CUOTAS");
        assertThatThrownBy(() -> validar(Set.of("cargar_valor_cuota"), destinos(false), true)).isInstanceOf(IllegalStateException.class);
        assertFalse(ManejadorConfiguracion.LISTAS_LECTURA.contains("CARGAR_VALOR_CUOTA"), "it is a WRITING list: it must not be listed with the reads");
    }
}
