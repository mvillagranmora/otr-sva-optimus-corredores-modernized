package cl.bcs.fondomutuo.consultas.instrumentos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.fondomutuo.adapter.in.javacall.JavaCallDispatcher;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;

/**
 * IFM: the entry of {@code ConsultarInstrumentosFondosMutuos} is CLOSED BY DEFAULT (plan F3 section 1.7): unless {@code fondomutuo.solicitud.listas-servidas}
 * names {@link ConsultarInstrumentosConfiguracion#LISTA} ("CONSULTAR_INSTRUMENTOS", its OWN list: opening the others does not open this one) the call is a 503
 * (gate G0) before anything is read, and the router repeats the request in TomEE. The class is read only: opening it needs NO tesoreria / spot / flujo-caja / owp /
 * custodia flag. These tests use a stub port that fails if it runs when it must not.
 */
class IfmCompuertaTest {

    private static final String FQN = "cl.bcs.fondomutuo.ConsultarInstrumentosFondosMutuos";

    private static Compuertas compuertas(String listas) {
        return new Compuertas(listas, false, false, false, false, false);
    }

    @Test
    @DisplayName("FM-IFM closed-by-default gate: lista vacia => 503 G0 y el servicio no se ejecuta ni se toca la conexion")
    void cerradaPorDefecto_503SinEjecutarNada() {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarInstrumentosPort servicio = e -> {
            llamadas.incrementAndGet();
            return new ConsultarInstrumentosContrato.Salida();
        };
        ConsultarInstrumentosJavaCallHandler h = new ConsultarInstrumentosJavaCallHandler(servicio, compuertas(""));
        ServicioNoServidoException ex = assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
        assertEquals("G0", ex.compuerta());
        assertEquals(0, llamadas.get());
    }

    @Test
    @DisplayName("FM-IFM gate: abrir otras listas (NORMAL, RESCATE_REINVERSION y las de las otras consultas de F3) no abre esta consulta")
    void otrasListasNoLaAbren() {
        ConsultarInstrumentosPort servicio = e -> {
            throw new AssertionError("the service must not run");
        };
        ConsultarInstrumentosJavaCallHandler h = new ConsultarInstrumentosJavaCallHandler(servicio,
                compuertas("NORMAL,RESCATE_REINVERSION,CONSULTAR_FECHA_SOLICITUD,CONSULTAR_FONDO_REINVERSION,CONSULTAR_VALOR_DOLAR"));
        assertThrows(ServicioNoServidoException.class, () -> h.call(null, new JsonObject()));
    }

    @Test
    @DisplayName("FM-IFM gate: el nombre de la lista es CONSULTAR_INSTRUMENTOS (propio de la clase)")
    void nombreDeLaLista() {
        assertEquals("CONSULTAR_INSTRUMENTOS", ConsultarInstrumentosConfiguracion.LISTA);
    }

    @Test
    @DisplayName("FM-IFM-01 gate: abierta por configuracion => delega, sin otra bandera de escritura y sin leer la conexion; entrada y salida con el contrato del legacy (Gson plano)")
    void abierta_delegaYSerializaConElContratoDelLegacy() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarInstrumentosPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(1L, e.p_IdEmpresa);
            assertEquals(7L, e.p_IdUsuario);
            assertEquals("FMM", e.p_IdModulo);
            // the legacy InFondos constructor defaults: texts "", the list of regimes empty and OcultarBloqueados "N"
            assertEquals("S001", e.p_EntradaFondosMutuos.IdSerieFondo);
            assertEquals("", e.p_EntradaFondosMutuos.IdEmisor);
            assertEquals(List.of("18Q"), e.p_EntradaFondosMutuos.IdRegimenFondoMutuo);
            assertEquals("", e.p_EntradaFondosMutuos.IdSerieFondoOrigen);
            assertEquals("N", e.p_EntradaFondosMutuos.OcultarBloqueados);
            return new ConsultarInstrumentosContrato.Salida();
        };
        ConsultarInstrumentosJavaCallHandler h = new ConsultarInstrumentosJavaCallHandler(servicio, compuertas(ConsultarInstrumentosConfiguracion.LISTA));
        JsonObject in = JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_IdUsuario\":7,\"p_IdModulo\":\"FMM\",\"p_EntradaFondosMutuos\":{\"IdSerieFondo\":\"S001\","
                + "\"IdRegimenFondoMutuo\":[\"18Q\"],\"Otro\":1}}").getAsJsonObject();
        JsonObject out = h.call(null, in);
        assertEquals(1, llamadas.get());
        assertEquals("{\"FondosMutuos\":[],\"Mensajes\":[],\"ContadorErrores\":0}", out.toString());   // D-F3A-12: ContadorErrores, not P_OUT_ERRORESCONTADOR
    }

    @Test
    @DisplayName("FM-IFM-01: p_EntradaFondosMutuos ausente o nulo llega al servicio como nulo (el servicio responde UI_SIN_VALORES); el handler no valida")
    void entradaAusenteLlegaNula() throws Exception {
        AtomicInteger llamadas = new AtomicInteger();
        ConsultarInstrumentosPort servicio = e -> {
            llamadas.incrementAndGet();
            assertEquals(null, e.p_EntradaFondosMutuos);
            return new ConsultarInstrumentosContrato.Salida();
        };
        ConsultarInstrumentosJavaCallHandler h = new ConsultarInstrumentosJavaCallHandler(servicio, compuertas(ConsultarInstrumentosConfiguracion.LISTA));
        h.call(null, JsonParser.parseString("{\"p_IdEmpresa\":1}").getAsJsonObject());
        h.call(null, JsonParser.parseString("{\"p_IdEmpresa\":1,\"p_EntradaFondosMutuos\":null}").getAsJsonObject());
        assertEquals(2, llamadas.get());
    }

    @Test
    @DisplayName("FM-IFM gate: la lista se compara sin importar mayusculas ni espacios, como las demas listas servidas (kit Compuertas)")
    void listaConEspaciosYMinusculas() throws Exception {
        ConsultarInstrumentosPort servicio = e -> new ConsultarInstrumentosContrato.Salida();
        ConsultarInstrumentosJavaCallHandler h = new ConsultarInstrumentosJavaCallHandler(servicio, compuertas(" normal , consultar_instrumentos "));
        assertEquals("{\"FondosMutuos\":[],\"Mensajes\":[],\"ContadorErrores\":0}", h.call(null, JsonParser.parseString("{}").getAsJsonObject()).toString());
    }

    @Test
    @DisplayName("FM-IFM allow-list: el FQN del legacy esta en la lista blanca del despachador y el handler responde a el")
    void fqnEnLaListaBlanca() {
        assertEquals(FQN, JavaCallDispatcher.CONSULTAR_INSTRUMENTOS_FONDOS_MUTUOS);
        assertEquals(FQN, ConsultarInstrumentosConfiguracion.FQN);
        assertTrue(JavaCallDispatcher.LISTA_BLANCA.contains(FQN));
        assertEquals(FQN, new ConsultarInstrumentosJavaCallHandler(e -> new ConsultarInstrumentosContrato.Salida(), compuertas("")).fqn());
    }
}
