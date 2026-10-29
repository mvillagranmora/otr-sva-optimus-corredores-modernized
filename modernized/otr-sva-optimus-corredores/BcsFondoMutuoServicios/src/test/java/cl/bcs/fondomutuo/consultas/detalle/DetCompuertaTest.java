package cl.bcs.fondomutuo.consultas.detalle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 * The entry of ConsultarDetalleSolicitud is CLOSED BY DEFAULT (plan F3 section 1.7, same switch as the F2 lists): unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link ConsultarDetalleConfiguracion#LISTA} the call is a 503 (gate G0) before anything is read, and the router repeats the request in TomEE. The class is read only: opening
 * it needs NO tesoreria / spot / flujo-caja / owp / custodia flag, no lock and no sequence (its two Plataforma collaborators are read-only HTTP ports). These tests use a stub
 * port that fails if it runs when it must not.
 */
class DetCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarDetalleSolicitud";
    private static final String VACIA = "{\"P_OUT_RESULTADO\":[],\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[]}";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-DET closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarDetallePort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarDetalleContrato.Salida();
        };
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> new ConsultarDetalleJavaCallHandler(servicio, compuertas("")).call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-DET gate: abrir otras listas (NORMAL, RESCATE_REINVERSION, CONSULTAR_SOLICITUDES) no abre el detalle de solicitud")
    void otrasListasNoLaAbren() {
        ConsultarDetallePort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarDetalleJavaCallHandler h = new ConsultarDetalleJavaCallHandler(servicio, compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_SOLICITUDES,CONSULTAR_SOLICITUDES_WS"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-DET gate: la lista se llama CONSULTAR_DETALLE_SOLICITUD y el FQN es el del legacy")
    void nombreDeLaLista() {
        assertEquals("CONSULTAR_DETALLE_SOLICITUD", ConsultarDetalleConfiguracion.LISTA);
        assertEquals(FQN, ConsultarDetalleConfiguracion.FQN);
    }

    @Test
    @DisplayName("FM-DET gate: abierta por configuracion => delega sin otra bandera y sin leer la conexion; el id viaja como Long, sin valores por defecto, y la salida usa el contrato del legacy (new Gson plano)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarDetallePort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals(399L, e.p_SolicitudesAporteRescate);
            return new ConsultarDetalleContrato.Salida();
        };
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_SolicitudesAporteRescate\":399,\"Otro\":1}").getAsJsonObject();
        JsonObject out = new ConsultarDetalleJavaCallHandler(servicio, compuertas(ConsultarDetalleConfiguracion.LISTA)).call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals(VACIA, out.toString());
    }

    @Test
    @DisplayName("FM-DET gate: una entrada vacia llega al servicio con todos los campos nulos (el pojo del legacy no tiene valores por defecto)")
    void entradaVaciaLlegaConNulos() throws Exception {
        ConsultarDetallePort servicio = e -> {
            assertNull(e.p_IdEmpresa);
            assertNull(e.p_IdUsuario);
            assertNull(e.p_IdModulo);
            assertNull(e.p_SolicitudesAporteRescate);
            return new ConsultarDetalleContrato.Salida();
        };
        assertEquals(VACIA, new ConsultarDetalleJavaCallHandler(servicio, compuertas(ConsultarDetalleConfiguracion.LISTA)).call(null, new JsonObject()).toString());
    }

    @Test
    @DisplayName("FM-DET gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarDetallePort servicio = e -> new ConsultarDetalleContrato.Salida();
        ConsultarDetalleJavaCallHandler h = new ConsultarDetalleJavaCallHandler(servicio, compuertas(" normal , consultar_detalle_solicitud "));
        assertEquals(0L, h.call(null, JsonParser.parseString("{}").getAsJsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("FM-DET allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_DETALLE_SOLICITUD);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarDetalleJavaCallHandler(e -> new ConsultarDetalleContrato.Salida(), compuertas("")).fqn());
    }
}
