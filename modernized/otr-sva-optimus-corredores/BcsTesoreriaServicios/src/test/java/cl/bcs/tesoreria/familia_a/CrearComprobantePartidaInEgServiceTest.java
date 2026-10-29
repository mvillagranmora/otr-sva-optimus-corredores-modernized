package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CrearComprobantePartidaInEgService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearComprobantePartidaInEg} (entry point call(conn, json)) as {@link CrearComprobantePartidaInEgService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-260 .. TES-A-266} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CrearComprobantePartidaInEgServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-260_pie-desde-comprobantes-crea-la-partida-en-estado-GIR-con-forma-de-pago",
            "TES-A-261_pie-desde-movimientos-crea-la-partida-en-estado-ING",
            "TES-A-262_pie-dos-movimientos-crean-dos-partidas-con-ids-correlativos",
            "TES-A-263_pie-sin-movimientos-no-crea-nada",
            "TES-A-264_pie-movimiento-de-otra-empresa-no-se-considera",
            "TES-A-265_pie-ambos-indicadores-N-deja-los-valores-por-defecto-de-comprobantes",
            "TES-A-266_pie-salidas-del-procedimiento-de-requisitos-se-mapean-a-mensajes");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        CrearComprobantePartidaInEgService service = new CrearComprobantePartidaInEgService(caso.jdbc(), d.secuencias(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearComprobantePartidaInEg");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-260 pie desde comprobantes crea la partida en estado GIR con forma de pago")
    void tesA260_pieDesdeComprobantesCreaLaPartidaEnEstadoGIRConFormaDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-261 pie desde movimientos crea la partida en estado ING")
    void tesA261_pieDesdeMovimientosCreaLaPartidaEnEstadoING() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-262 pie dos movimientos crean dos partidas con ids correlativos")
    void tesA262_pieDosMovimientosCreanDosPartidasConIdsCorrelativos() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-263 pie sin movimientos no crea nada")
    void tesA263_pieSinMovimientosNoCreaNada() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-264 pie movimiento de otra empresa no se considera")
    void tesA264_pieMovimientoDeOtraEmpresaNoSeConsidera() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-265 pie ambos indicadores N deja los valores por defecto de comprobantes")
    void tesA265_pieAmbosIndicadoresNDejaLosValoresPorDefectoDeComprobantes() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-266 pie salidas del procedimiento de requisitos se mapean a mensajes")
    void tesA266_pieSalidasDelProcedimientoDeRequisitosSeMapeanAMensajes() throws Exception {
        verificar(CASOS.get(6));
    }
}
