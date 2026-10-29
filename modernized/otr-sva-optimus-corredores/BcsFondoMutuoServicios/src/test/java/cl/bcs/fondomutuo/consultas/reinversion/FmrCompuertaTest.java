package cl.bcs.fondomutuo.consultas.reinversion;

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
 * FMR: the entry of {@code ConsultarFondoMutuoReinversion} is CLOSED BY DEFAULT (plan F3 section 1.7, same switch as the F2 lists and as the FSA front): unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link ConsultarReinversionConfiguracion#LISTA} ("CONSULTAR_FONDO_REINVERSION", its OWN list: opening the
 * others does not open this one) the call is a 503 (gate G0) before anything is read, and the router repeats the request in TomEE. The class is read only: opening
 * it needs NO tesoreria / spot / flujo-caja / owp / custodia flag. These tests use a stub port that fails if it runs when it must not.
 */
class FmrCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarFondoMutuoReinversion";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-FMR closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarReinversionPort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarReinversionContrato.Salida();
        };
        ConsultarReinversionJavaCallHandler h = new ConsultarReinversionJavaCallHandler(servicio, compuertas(""));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-FMR gate: abrir otras listas (NORMAL, RESCATE_REINVERSION y las de las otras consultas de F3) no abre esta consulta")
    void otrasListasNoLaAbren() {
        ConsultarReinversionPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarReinversionJavaCallHandler h = new ConsultarReinversionJavaCallHandler(servicio,
                compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_INSTRUMENTOS,CONSULTAR_VALOR_DOLAR"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-FMR gate: el nombre de la lista es CONSULTAR_FONDO_REINVERSION (propio de la clase)")
    void nombreDeLaLista() {
        assertEquals("CONSULTAR_FONDO_REINVERSION", ConsultarReinversionConfiguracion.LISTA);
    }

    @Test
    @DisplayName("FM-FMR-01 gate: abierta por configuracion => delega, sin otra bandera de escritura y sin leer la conexion; entrada y salida con el contrato del legacy (Gson plano)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarReinversionPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals("ACT", e.p_IdEstado);
            assertEquals("18Q", e.p_IdRegimenFondoMutuo);
            assertEquals("S1", e.p_IdSerieFondoOrigen);
            assertEquals(null, e.p_IdMoneda);   // a key that is absent stays null (Gson plano): FM-FMR-02 reads a null as "no filter"
            return new ConsultarReinversionContrato.Salida();
        };
        ConsultarReinversionJavaCallHandler h = new ConsultarReinversionJavaCallHandler(servicio, compuertas(ConsultarReinversionConfiguracion.LISTA));
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_IdEstado\":\"ACT\",\"p_IdRegimenFondoMutuo\":\"18Q\","
                + "\"p_IdSerieFondoOrigen\":\"S1\",\"Otro\":1}").getAsJsonObject();
        JsonObject out = h.call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals("{\"P_OUT_LISTA\":[]}", out.toString());   // D-F3A-27: no counter, no messages
    }

    @Test
    @DisplayName("FM-FMR-01 / D-F3A-25: p_IdEmpresa y p_IdUsuario son long primitivos: ausentes valen 0")
    void empresaAusenteEsCero() throws Exception {
        ConsultarReinversionPort servicio = e -> {
            assertEquals(0L, e.p_IdEmpresa);
            assertEquals(0L, e.p_IdUsuario);
            return new ConsultarReinversionContrato.Salida();
        };
        new ConsultarReinversionJavaCallHandler(servicio, compuertas(ConsultarReinversionConfiguracion.LISTA)).call(null, JsonParser.parseString("{}").getAsJsonObject());
    }

    @Test
    @DisplayName("FM-FMR gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarReinversionPort servicio = e -> new ConsultarReinversionContrato.Salida();
        ConsultarReinversionJavaCallHandler h = new ConsultarReinversionJavaCallHandler(servicio, compuertas(" normal , consultar_fondo_reinversion "));
        assertEquals("{\"P_OUT_LISTA\":[]}", h.call(null, JsonParser.parseString("{}").getAsJsonObject()).toString());
    }

    @Test
    @DisplayName("FM-FMR allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_FONDO_MUTUO_REINVERSION);
        assertEquals(FQN, ConsultarReinversionConfiguracion.FQN);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarReinversionJavaCallHandler(e -> new ConsultarReinversionContrato.Salida(), compuertas("")).fqn());
    }
}
