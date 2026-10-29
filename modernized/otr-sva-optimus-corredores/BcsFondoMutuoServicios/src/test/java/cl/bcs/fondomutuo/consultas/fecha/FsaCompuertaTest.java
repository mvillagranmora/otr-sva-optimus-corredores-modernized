package cl.bcs.fondomutuo.consultas.fecha;

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
 * The entry of the class is CLOSED BY DEFAULT (plan F3 section 1.7, same switch as the F2 lists): unless {@code fondomutuo.solicitud.listas-servidas} names
 * {@link ConsultarFechaConfiguracion#LISTA} the call is a 503 (gate G0) before anything is read, and the router repeats the request in TomEE. The class is read
 * only: opening it needs NO tesoreria / spot / flujo-caja / owp / custodia flag. These tests use a stub port that fails if it runs when it must not.
 */
class FsaCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-FSA closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarFechaPort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarFechaContrato.Salida();
        };
        ConsultarFechaJavaCallHandler h = new ConsultarFechaJavaCallHandler(servicio, compuertas(""));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-FSA gate: abrir otras listas (NORMAL, RESCATE_REINVERSION) no abre la consulta de fechas")
    void otrasListasNoLaAbren() {
        ConsultarFechaPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarFechaJavaCallHandler h = new ConsultarFechaJavaCallHandler(servicio, compuertas("NORMAL,RESCATE_REINVERSION"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-FSA gate: abierta por configuracion => delega, sin otra bandera de escritura y sin leer la conexion; la salida usa el contrato del legacy")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarFechaPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals(1, e.p_SolicitudAporteRescate.size());
            assertEquals("", e.p_SolicitudAporteRescate.get(0).IdMoneda);        // legacy constructor default when the key is absent
            assertEquals("", e.p_SolicitudAporteRescate.get(0).IdMonedaPago);
            return new ConsultarFechaContrato.Salida();
        };
        ConsultarFechaJavaCallHandler h = new ConsultarFechaJavaCallHandler(servicio, compuertas(ConsultarFechaConfiguracion.LISTA));
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_SolicitudAporteRescate\":"
                + "[{\"IdInterno\":1,\"FechaEmision\":\"2026-03-02\",\"IdSerieFondo\":\"FSN01\",\"TipoSolicitud\":\"APO\",\"Otro\":1}]}").getAsJsonObject();
        JsonObject out = h.call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_MENSAJES\":[],\"P_OUT_RESULTADO\":[]}", out.toString());
    }

    @Test
    @DisplayName("FM-FSA gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarFechaPort servicio = e -> new ConsultarFechaContrato.Salida();
        ConsultarFechaJavaCallHandler h = new ConsultarFechaJavaCallHandler(servicio, compuertas(" normal , consultar_fecha_solicitud "));
        assertEquals(0L, h.call(null, JsonParser.parseString("{}").getAsJsonObject()).get("P_OUT_ERRORESCONTADOR").getAsLong());
    }

    @Test
    @DisplayName("FM-FSA allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_FECHA_SOLICITUD_APORTE_RESCATE);
        assertEquals(FQN, ConsultarFechaConfiguracion.FQN);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarFechaJavaCallHandler(e -> new ConsultarFechaContrato.Salida(), compuertas("")).fqn());
    }
}
