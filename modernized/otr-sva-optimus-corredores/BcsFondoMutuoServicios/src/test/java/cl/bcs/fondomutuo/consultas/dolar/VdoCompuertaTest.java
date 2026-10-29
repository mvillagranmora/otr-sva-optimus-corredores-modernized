package cl.bcs.fondomutuo.consultas.dolar;

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
 * VDO: the entry of {@code ConsultarValorDolarFondoMutuo} is CLOSED BY DEFAULT (plan F3 section 1.7): unless {@code fondomutuo.solicitud.listas-servidas}
 * names {@link ConsultarDolarConfiguracion#LISTA} ("CONSULTAR_VALOR_DOLAR", its OWN list: opening the others does not open this one) the call is a 503 (gate G0)
 * before anything is read, and the router repeats the request in TomEE. The class only READS (the one collaborator, Plataforma CalcularParidad, is a read through the
 * router): opening it needs NO tesoreria / spot / flujo-caja / owp / custodia flag. These tests use a stub port that fails if it runs when it must not.
 */
class VdoCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarValorDolarFondoMutuo";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-VDO closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarDolarPort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarDolarContrato.Salida();
        };
        ConsultarDolarJavaCallHandler h = new ConsultarDolarJavaCallHandler(servicio, compuertas(""));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-VDO gate: abrir otras listas (NORMAL, RESCATE_REINVERSION y las de las otras consultas de F3) no abre esta consulta")
    void otrasListasNoLaAbren() {
        ConsultarDolarPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarDolarJavaCallHandler h = new ConsultarDolarJavaCallHandler(servicio,
                compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_FONDO_REINVERSION,CONSULTAR_INSTRUMENTOS"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-VDO gate: el nombre de la lista es CONSULTAR_VALOR_DOLAR (propio de la clase)")
    void nombreDeLaLista() {
        assertEquals("CONSULTAR_VALOR_DOLAR", ConsultarDolarConfiguracion.LISTA);
    }

    @Test
    @DisplayName("FM-VDO-01 gate: abierta por configuracion => delega, sin otra bandera de escritura y sin leer la conexion; entrada y salida con el contrato del legacy (Gson plano)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarDolarPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            assertEquals(1, e.p_Series.size());
            assertEquals("V001", e.p_Series.get(0).IdSerieFondo);
            assertEquals("APO", e.p_Series.get(0).IdTipoSolicitud);
            assertEquals("2026-03-10", e.p_Series.get(0).Fecha);
            return new ConsultarDolarContrato.Salida();
        };
        ConsultarDolarJavaCallHandler h = new ConsultarDolarJavaCallHandler(servicio, compuertas(ConsultarDolarConfiguracion.LISTA));
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_Series\":"
                + "[{\"IdSerieFondo\":\"V001\",\"IdTipoSolicitud\":\"APO\",\"Fecha\":\"2026-03-10\",\"Otro\":1}]}").getAsJsonObject();
        JsonObject out = h.call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[]}", out.toString());
    }

    @Test
    @DisplayName("FM-VDO-01: p_Series ausente llega como lista vacia (constructor del legacy); p_Series nulo explicito llega nulo (el servicio lanza NullPointerException)")
    void listaAusenteVaciaYNulaExplicita() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarDolarPort servicio = e -> {
            if (llamadas.incrementAndGet() == 1) assertEquals(0, e.p_Series.size());
            else assertEquals(null, e.p_Series);
            return new ConsultarDolarContrato.Salida();
        };
        ConsultarDolarJavaCallHandler h = new ConsultarDolarJavaCallHandler(servicio, compuertas(ConsultarDolarConfiguracion.LISTA));
        h.call(null, JsonParser.parseString("{\"p_IdEmpresa\":1}").getAsJsonObject());
        h.call(null, JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_Series\":null}").getAsJsonObject());
        assertEquals(2, llamadas.get());
    }

    @Test
    @DisplayName("FM-VDO gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarDolarPort servicio = e -> new ConsultarDolarContrato.Salida();
        ConsultarDolarJavaCallHandler h = new ConsultarDolarJavaCallHandler(servicio, compuertas(" normal , consultar_valor_dolar "));
        assertEquals("{\"P_OUT_ERRORESCONTADOR\":0,\"P_OUT_RESULTADO\":[]}", h.call(null, JsonParser.parseString("{}").getAsJsonObject()).toString());
    }

    @Test
    @DisplayName("FM-VDO allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_VALOR_DOLAR_FONDO_MUTUO);
        assertEquals(FQN, ConsultarDolarConfiguracion.FQN);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarDolarJavaCallHandler(e -> new ConsultarDolarContrato.Salida(), compuertas("")).fqn());
    }
}
