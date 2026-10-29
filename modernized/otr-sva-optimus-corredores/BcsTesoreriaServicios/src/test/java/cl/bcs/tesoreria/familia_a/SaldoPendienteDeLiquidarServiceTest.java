package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.SaldoPendienteDeLiquidarService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.SaldoPendienteDeLiquidar} (entry point call(conn, json)) as {@link SaldoPendienteDeLiquidarService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-700 .. TES-A-720} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class SaldoPendienteDeLiquidarServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-700_spl-un-ingreso-pendiente-suma-desde-su-fecha-de-liquidacion",
            "TES-A-701_spl-un-egreso-pendiente-resta-del-saldo",
            "TES-A-702_spl-ingreso-y-egreso-del-mismo-dia-se-netean-en-la-misma-fila-de-agrupacion-por-tipo",
            "TES-A-703_spl-saldo-anterior-a-la-fecha-desde-se-suma-a-todos-los-dias",
            "TES-A-704_spl-la-forma-de-pago-ingresada-reemplaza-el-monto-del-movimiento",
            "TES-A-705_spl-movimientos-no-ingresados-no-cuentan",
            "TES-A-706_spl-un-comprobante-pendiente-suma-con-su-forma-de-pago",
            "TES-A-707_spl-comprobante-con-forma-de-pago-ya-asignada-a-partida-no-usa-su-monto",
            "TES-A-708_spl-movimiento-y-comprobante-pendientes-se-suman",
            "TES-A-709_spl-socio-sin-portafolio-informado-no-se-procesa-defecto",
            "TES-A-710_spl-socio-sin-moneda-informada-no-se-procesa-defecto",
            "TES-A-711_spl-persona-sin-cliente-no-genera-saldos",
            "TES-A-712_spl-portafolio-que-no-es-de-la-persona-no-genera-saldos",
            "TES-A-713_spl-dos-socios-generan-sus-filas-en-el-orden-de-la-entrada",
            "TES-A-714_spl-un-socio-repetido-se-procesa-una-vez",
            "TES-A-715_spl-fecha-hasta-anterior-a-desde-no-genera-filas",
            "TES-A-716_spl-sin-socios-devuelve-salida-vacia",
            "TES-A-717_spl-fecha-desde-ausente-lanza-excepcion",
            "TES-A-718_spl-portafolio-titular-en-cero-aplica-a-cualquier-portafolio",
            "TES-A-719_spl-moneda-distinta-de-la-del-socio-no-cuenta",
            "TES-A-720_spl-movimiento-liquidado-despues-del-rango-no-cuenta-pero-el-del-ultimo-dia-si");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        SaldoPendienteDeLiquidarService service = new SaldoPendienteDeLiquidarService(caso.jdbc());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("SaldoPendienteDeLiquidar");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-700 spl un ingreso pendiente suma desde su fecha de liquidacion")
    void tesA700_splUnIngresoPendienteSumaDesdeSuFechaDeLiquidacion() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-701 spl un egreso pendiente resta del saldo")
    void tesA701_splUnEgresoPendienteRestaDelSaldo() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-702 spl ingreso y egreso del mismo dia se netean en la misma fila de agrupacion por tipo")
    void tesA702_splIngresoYEgresoDelMismoDiaSeNeteanEnLaMismaFilaDeAgrupacionPorTipo() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-703 spl saldo anterior a la fecha desde se suma a todos los dias")
    void tesA703_splSaldoAnteriorALaFechaDesdeSeSumaATodosLosDias() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-704 spl la forma de pago ingresada reemplaza el monto del movimiento")
    void tesA704_splLaFormaDePagoIngresadaReemplazaElMontoDelMovimiento() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-705 spl movimientos no ingresados no cuentan")
    void tesA705_splMovimientosNoIngresadosNoCuentan() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-706 spl un comprobante pendiente suma con su forma de pago")
    void tesA706_splUnComprobantePendienteSumaConSuFormaDePago() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-707 spl comprobante con forma de pago ya asignada a partida no usa su monto")
    void tesA707_splComprobanteConFormaDePagoYaAsignadaAPartidaNoUsaSuMonto() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-708 spl movimiento y comprobante pendientes se suman")
    void tesA708_splMovimientoYComprobantePendientesSeSuman() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-709 spl socio sin portafolio informado no se procesa defecto")
    void tesA709_splSocioSinPortafolioInformadoNoSeProcesaDefecto() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-710 spl socio sin moneda informada no se procesa defecto")
    void tesA710_splSocioSinMonedaInformadaNoSeProcesaDefecto() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-711 spl persona sin cliente no genera saldos")
    void tesA711_splPersonaSinClienteNoGeneraSaldos() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-712 spl portafolio que no es de la persona no genera saldos")
    void tesA712_splPortafolioQueNoEsDeLaPersonaNoGeneraSaldos() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-713 spl dos socios generan sus filas en el orden de la entrada")
    void tesA713_splDosSociosGeneranSusFilasEnElOrdenDeLaEntrada() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-714 spl un socio repetido se procesa una vez")
    void tesA714_splUnSocioRepetidoSeProcesaUnaVez() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-715 spl fecha hasta anterior a desde no genera filas")
    void tesA715_splFechaHastaAnteriorADesdeNoGeneraFilas() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-716 spl sin socios devuelve salida vacia")
    void tesA716_splSinSociosDevuelveSalidaVacia() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-717 spl fecha desde ausente lanza excepcion")
    void tesA717_splFechaDesdeAusenteLanzaExcepcion() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-718 spl portafolio titular en cero aplica a cualquier portafolio")
    void tesA718_splPortafolioTitularEnCeroAplicaACualquierPortafolio() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-719 spl moneda distinta de la del socio no cuenta")
    void tesA719_splMonedaDistintaDeLaDelSocioNoCuenta() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-720 spl movimiento liquidado despues del rango no cuenta pero el del ultimo dia si")
    void tesA720_splMovimientoLiquidadoDespuesDelRangoNoCuentaPeroElDelUltimoDiaSi() throws Exception {
        verificar(CASOS.get(20));
    }
}
