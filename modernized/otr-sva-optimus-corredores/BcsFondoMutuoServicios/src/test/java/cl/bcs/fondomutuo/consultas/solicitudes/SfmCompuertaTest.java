package cl.bcs.fondomutuo.consultas.solicitudes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * The entries of ConsultarSolicitudesFFMM and of its twin ConsultarSolicitudesFFMMWS are CLOSED BY DEFAULT (plan F3 section 1.7, same switch as the F2 lists): unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarSolicitudesConfiguracion#LISTA} (SFM) / {@link ConsultarSolicitudesWsConfiguracion#LISTA} (WS) the call is a
 * 503 (gate G0) before anything is read, and the router repeats the request in TomEE. Each class has its OWN list (opening one does not open the other: the WS is the
 * external consumer's entry). The classes are read only: opening them needs NO tesoreria / spot / flujo-caja / owp / custodia flag, no lock and no sequence. These tests use
 * stub ports that fail if they run when they must not.
 */
class SfmCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarSolicitudesFFMM";
    private static final String FQN_WS = "cl.bcs.fondomutuo.ConsultarSolicitudesFFMMWS";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-SFM closed-by-default gate: lista vacia => 503 G0 en SFM y en WS y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarSolicitudesPort sfm = e -> {
            llamadas.incrementAndGet();
            return new ConsultarSolicitudesContrato.Salida();
        };
        ConsultarSolicitudesWsPort ws = e -> {
            llamadas.incrementAndGet();
            return new ConsultarSolicitudesWsContrato.Salida();
        };
        ServicioNoServidoException a = assertThrows(ServicioNoServidoException.class, () -> new ConsultarSolicitudesJavaCallHandler(sfm, compuertas("")).call(null, new JsonObject()));
        ServicioNoServidoException b = assertThrows(ServicioNoServidoException.class, () -> new ConsultarSolicitudesWsJavaCallHandler(ws, compuertas("")).call(null, new JsonObject()));
        assertEquals("G0", a.compuerta());
        assertEquals("G0", b.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-SFM gate: abrir otras listas (NORMAL, RESCATE_REINVERSION, CONSULTAR_FECHA_SOLICITUD) no abre la consulta de solicitudes; abrir SFM no abre el WS ni al reves")
    void cadaListaAbreSoloSuClase() {
        ConsultarSolicitudesPort sfm = e -> {
            throw new AssertionError("the SFM service must not run");
        };
        ConsultarSolicitudesWsPort ws = e -> {
            throw new AssertionError("the WS service must not run");
        };
        String otras = "NORMAL,RESCATE_REINVERSION,CONSULTAR_FECHA_SOLICITUD";
        assertThrows(ServicioNoServidoException.class, () -> new ConsultarSolicitudesJavaCallHandler(sfm, compuertas(otras)).call(null, new JsonObject()));
        assertThrows(ServicioNoServidoException.class, () -> new ConsultarSolicitudesWsJavaCallHandler(ws, compuertas(otras)).call(null, new JsonObject()));
        assertThrows(ServicioNoServidoException.class,
                () -> new ConsultarSolicitudesWsJavaCallHandler(ws, compuertas(ConsultarSolicitudesConfiguracion.LISTA)).call(null, new JsonObject()));
        assertThrows(ServicioNoServidoException.class,
                () -> new ConsultarSolicitudesJavaCallHandler(sfm, compuertas(ConsultarSolicitudesWsConfiguracion.LISTA)).call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-SFM gate: las listas se llaman CONSULTAR_SOLICITUDES y CONSULTAR_SOLICITUDES_WS y los FQN son los del legacy")
    void nombresDeLasListas() {
        assertEquals("CONSULTAR_SOLICITUDES", ConsultarSolicitudesConfiguracion.LISTA);
        assertEquals("CONSULTAR_SOLICITUDES_WS", ConsultarSolicitudesWsConfiguracion.LISTA);
        assertEquals(FQN, ConsultarSolicitudesConfiguracion.FQN);
        assertEquals(FQN_WS, ConsultarSolicitudesWsConfiguracion.FQN);
    }

    @Test
    @DisplayName("FM-SFM gate: abierta por configuracion => delega, sin otra bandera y sin leer la conexion; la entrada toma los valores por defecto del legacy y la salida usa el contrato del legacy (new Gson plano)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarSolicitudesPort sfm = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals(100L, e.p_IdCliente);
            assertEquals("", e.p_TipoSolicitud);     // constructor defaults of the legacy when the key is absent
            assertEquals("", e.p_Origen);
            assertEquals("", e.p_Folio);
            assertEquals("", e.p_TipoAhorro);
            assertEquals("", e.p_TipoCuenta);
            assertEquals("", e.p_IdSerieFondo);
            assertEquals("", e.p_Estado);
            assertEquals("2026-03-01", e.p_FechaDesde);
            assertEquals("", e.p_FechaHasta);
            return new ConsultarSolicitudesContrato.Salida();
        };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_IdCliente\":100,\"p_FechaDesde\":\"2026-03-01\",\"Otro\":1}").getAsJsonObject();
        JsonObject out = new ConsultarSolicitudesJavaCallHandler(sfm, compuertas(ConsultarSolicitudesConfiguracion.LISTA)).call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_SOLICITUDES\":[]}", out.toString());
        ConsultarSolicitudesWsPort ws = e -> {
            llamadas.incrementAndGet();
            assertEquals(100L, e.p_IdCliente);
            assertEquals("", e.p_Estado);
            return new ConsultarSolicitudesWsContrato.Salida();
        };
        JsonObject outWs = new ConsultarSolicitudesWsJavaCallHandler(ws, compuertas(ConsultarSolicitudesWsConfiguracion.LISTA)).call(null, in);
        assertEquals(2, llamadas.get());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_SOLICITUDES\":[]}", outWs.toString());
    }

    @Test
    @DisplayName("FM-SFM gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarSolicitudesPort sfm = e -> new ConsultarSolicitudesContrato.Salida();
        ConsultarSolicitudesWsPort ws = e -> new ConsultarSolicitudesWsContrato.Salida();
        String listas = " normal , consultar_solicitudes , Consultar_Solicitudes_WS ";
        assertEquals(0L, new ConsultarSolicitudesJavaCallHandler(sfm, compuertas(listas)).call(null, JsonParser.parseString("{}").getAsJsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong());
        assertEquals(0L, new ConsultarSolicitudesWsJavaCallHandler(ws, compuertas(listas)).call(null, JsonParser.parseString("{}").getAsJsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("FM-SFM allow-list: los FQN del legacy estan en la lista blanca del despachador y cada handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM);
        assertEquals(FQN_WS, JavaCallDispatcher.CONSULTAR_SOLICITUDES_FFMM_WS);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN_WS));
        assertEquals(FQN, new ConsultarSolicitudesJavaCallHandler(e -> new ConsultarSolicitudesContrato.Salida(), compuertas("")).fqn());
        assertEquals(FQN_WS, new ConsultarSolicitudesWsJavaCallHandler(e -> new ConsultarSolicitudesWsContrato.Salida(), compuertas("")).fqn());
    }
}
