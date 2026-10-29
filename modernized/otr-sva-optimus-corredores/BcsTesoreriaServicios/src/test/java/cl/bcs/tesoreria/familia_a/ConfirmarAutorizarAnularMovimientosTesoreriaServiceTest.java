package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ConfirmarAutorizarAnularMovimientosTesoreriaService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConfirmarAutorizarAnularMovimientosTesoreria} (entry point call(conn, json)) as {@link ConfirmarAutorizarAnularMovimientosTesoreriaService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-350 .. TES-A-357} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ConfirmarAutorizarAnularMovimientosTesoreriaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-350_cam-autoriza-la-anulacion-anula-el-movimiento-y-su-forma-de-pago",
            "TES-A-351_cam-movimiento-inexistente-es-estado-incorrecto",
            "TES-A-352_cam-anula-cualquier-movimiento-existente-incluso-ya-emitido",
            "TES-A-353_cam-dos-movimientos-registra-un-log-por-cada-uno",
            "TES-A-354_cam-un-movimiento-existente-y-otro-no-termina-con-error-despues-de-anular-el-existente",
            "TES-A-356_cam-sin-fecha-de-sistema-configurada-lanza-excepcion",
            "TES-A-357_cam-lista-vacia-no-hace-nada");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConfirmarAutorizarAnularMovimientosTesoreriaService service = new ConfirmarAutorizarAnularMovimientosTesoreriaService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConfirmarAutorizarAnularMovimientosTesoreria");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-350 cam autoriza la anulacion anula el movimiento y su forma de pago")
    void tesA350_camAutorizaLaAnulacionAnulaElMovimientoYSuFormaDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-351 cam movimiento inexistente es estado incorrecto")
    void tesA351_camMovimientoInexistenteEsEstadoIncorrecto() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-352 cam anula cualquier movimiento existente incluso ya emitido")
    void tesA352_camAnulaCualquierMovimientoExistenteInclusoYaEmitido() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-353 cam dos movimientos registra un log por cada uno")
    void tesA353_camDosMovimientosRegistraUnLogPorCadaUno() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-354 cam un movimiento existente y otro no termina con error despues de anular el existente")
    void tesA354_camUnMovimientoExistenteYOtroNoTerminaConErrorDespuesDeAnularElExistente() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-356 cam sin fecha de sistema configurada lanza excepcion")
    void tesA356_camSinFechaDeSistemaConfiguradaLanzaExcepcion() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-357 cam lista vacia no hace nada")
    void tesA357_camListaVaciaNoHaceNada() throws Exception {
        verificar(CASOS.get(6));
    }
}
