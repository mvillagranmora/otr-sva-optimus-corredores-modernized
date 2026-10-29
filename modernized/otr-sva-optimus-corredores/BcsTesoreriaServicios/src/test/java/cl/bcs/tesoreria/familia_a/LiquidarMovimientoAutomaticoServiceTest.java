package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.LiquidarMovimientoAutomaticoService;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.LiquidarMovimientoAutomatico} (entry point call(conn, json)) as {@link LiquidarMovimientoAutomaticoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-810 .. TES-A-822} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class LiquidarMovimientoAutomaticoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-810_lma-modifica-formas-de-pago-crea-el-comprobante-y-lo-liquida",
            "TES-A-811_lma-sin-movimientos-es-error",
            "TES-A-812_lma-errores-del-procedimiento-de-modificacion-se-informan-y-cortan",
            "TES-A-813_lma-advertencia-de-monto-no-cuadra-en-canal-WEB-corta-sin-crear-comprobante",
            "TES-A-814_lma-advertencia-de-monto-no-cuadra-en-otro-canal-continua-y-liquida",
            "TES-A-815_lma-otra-advertencia-en-canal-WEB-no-corta",
            "TES-A-816_lma-movimiento-anulado-igual-crea-el-comprobante-porque-no-usa-reglas-de-negocio",
            "TES-A-817_lma-error-al-liquidar-informa-los-errores-de-la-liquidacion",
            "TES-A-818_lma-sin-formas-de-pago-no-modifica-formas-y-liquida",
            "TES-A-819_lma-forma-de-pago-con-referencia-inexistente-lanza-NPE",
            "TES-A-820_lma-fecha-vacia-usa-la-fecha-de-sistema",
            "TES-A-821_lma-simular-S-por-call-hace-rollback",
            "TES-A-822_lma-dos-movimientos-se-liquidan-en-la-misma-llamada");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        try (var ctx = CieContexto.de(caso, d, LiquidarMovimientoAutomaticoService.class, CrearComprobanteService.class, LiquidarComprobanteService.class)) {
            LiquidarMovimientoAutomaticoService service = ctx.getBean(LiquidarMovimientoAutomaticoService.class);
            Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
            EJECUTADOS.ejecutado(nombre);
            Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
        }
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("LiquidarMovimientoAutomatico");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-810 lma modifica formas de pago crea el comprobante y lo liquida")
    void tesA810_lmaModificaFormasDePagoCreaElComprobanteYLoLiquida() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-811 lma sin movimientos es error")
    void tesA811_lmaSinMovimientosEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-812 lma errores del procedimiento de modificacion se informan y cortan")
    void tesA812_lmaErroresDelProcedimientoDeModificacionSeInformanYCortan() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-813 lma advertencia de monto no cuadra en canal WEB corta sin crear comprobante")
    void tesA813_lmaAdvertenciaDeMontoNoCuadraEnCanalWEBCortaSinCrearComprobante() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-814 lma advertencia de monto no cuadra en otro canal continua y liquida")
    void tesA814_lmaAdvertenciaDeMontoNoCuadraEnOtroCanalContinuaYLiquida() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-815 lma otra advertencia en canal WEB no corta")
    void tesA815_lmaOtraAdvertenciaEnCanalWEBNoCorta() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-816 lma movimiento anulado igual crea el comprobante porque no usa reglas de negocio")
    void tesA816_lmaMovimientoAnuladoIgualCreaElComprobantePorqueNoUsaReglasDeNegocio() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-817 lma error al liquidar informa los errores de la liquidacion")
    void tesA817_lmaErrorAlLiquidarInformaLosErroresDeLaLiquidacion() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-818 lma sin formas de pago no modifica formas y liquida")
    void tesA818_lmaSinFormasDePagoNoModificaFormasYLiquida() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-819 lma forma de pago con referencia inexistente lanza NPE")
    void tesA819_lmaFormaDePagoConReferenciaInexistenteLanzaNPE() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-820 lma fecha vacia usa la fecha de sistema")
    void tesA820_lmaFechaVaciaUsaLaFechaDeSistema() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-821 lma simular S por call hace rollback")
    void tesA821_lmaSimularSPorCallHaceRollback() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-822 lma dos movimientos se liquidan en la misma llamada")
    void tesA822_lmaDosMovimientosSeLiquidanEnLaMismaLlamada() throws Exception {
        verificar(CASOS.get(12));
    }
}
