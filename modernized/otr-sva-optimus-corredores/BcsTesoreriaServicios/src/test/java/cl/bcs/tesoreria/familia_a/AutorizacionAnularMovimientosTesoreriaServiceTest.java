package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.AutorizacionAnularMovimientosTesoreriaService;
import cl.bcs.tesoreria.javacall.ConfirmarRechazarAnularMovimientosTesoreriaService;
import cl.bcs.tesoreria.javacall.ConfirmarAutorizarAnularMovimientosTesoreriaService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.AutorizacionAnularMovimientosTesoreria} (entry point call(conn, json)) as {@link AutorizacionAnularMovimientosTesoreriaService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-380 .. TES-A-387} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class AutorizacionAnularMovimientosTesoreriaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-380_aam-autorizar-anulacion-anula-el-movimiento-e-informa-operacion-autorizada",
            "TES-A-381_aam-autorizar-anulacion-de-movimiento-inexistente-devuelve-los-mensajes-de-error",
            "TES-A-382_aam-autorizar-dos-eventos-uno-valido-y-uno-inexistente",
            "TES-A-383_aam-rechazar-anulacion-rechaza-el-evento-y-libera-el-movimiento",
            "TES-A-384_aam-rechazar-anulacion-de-movimiento-sin-evento-devuelve-estado-incorrecto",
            "TES-A-385_aam-autorizar-con-numero-no-numerico-lanza-NumberFormatException",
            "TES-A-386_aam-autorizar-lista-vacia-devuelve-salida-vacia",
            "TES-A-387_aam-autorizar-sin-fecha-de-sistema-informa-error-generico");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConfirmarAutorizarAnularMovimientosTesoreriaService autorizar = new ConfirmarAutorizarAnularMovimientosTesoreriaService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        ConfirmarRechazarAnularMovimientosTesoreriaService rechazar = new ConfirmarRechazarAnularMovimientosTesoreriaService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        AutorizacionAnularMovimientosTesoreriaService service = new AutorizacionAnularMovimientosTesoreriaService(d.diccionario(), autorizar, rechazar);
        String metodo = caso.metodo();
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "autorizarEvento".equals(metodo) ? service.autorizarEvento(caso.entrada().deepCopy()) : service.rechazarEvento(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AutorizacionAnularMovimientosTesoreria");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-380 aam autorizar anulacion anula el movimiento e informa operacion autorizada")
    void tesA380_aamAutorizarAnulacionAnulaElMovimientoEInformaOperacionAutorizada() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-381 aam autorizar anulacion de movimiento inexistente devuelve los mensajes de error")
    void tesA381_aamAutorizarAnulacionDeMovimientoInexistenteDevuelveLosMensajesDeError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-382 aam autorizar dos eventos uno valido y uno inexistente")
    void tesA382_aamAutorizarDosEventosUnoValidoYUnoInexistente() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-383 aam rechazar anulacion rechaza el evento y libera el movimiento")
    void tesA383_aamRechazarAnulacionRechazaElEventoYLiberaElMovimiento() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-384 aam rechazar anulacion de movimiento sin evento devuelve estado incorrecto")
    void tesA384_aamRechazarAnulacionDeMovimientoSinEventoDevuelveEstadoIncorrecto() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-385 aam autorizar con numero no numerico lanza NumberFormatException")
    void tesA385_aamAutorizarConNumeroNoNumericoLanzaNumberFormatException() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-386 aam autorizar lista vacia devuelve salida vacia")
    void tesA386_aamAutorizarListaVaciaDevuelveSalidaVacia() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-387 aam autorizar sin fecha de sistema informa error generico")
    void tesA387_aamAutorizarSinFechaDeSistemaInformaErrorGenerico() throws Exception {
        verificar(CASOS.get(7));
    }
}
