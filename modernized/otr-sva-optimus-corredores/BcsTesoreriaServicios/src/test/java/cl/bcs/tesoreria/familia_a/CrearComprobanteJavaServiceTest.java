package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearComprobanteJava} (entry point call(conn, json)) as {@link CrearComprobanteJavaService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-200 .. TES-A-234} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CrearComprobanteJavaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-200_ccj-un-ingreso-genera-un-comprobante-con-detalle-forma-de-pago-y-origen",
            "TES-A-201_ccj-un-egreso-genera-comprobante-de-egreso",
            "TES-A-202_ccj-dos-movimientos-agrupables-generan-un-solo-comprobante-con-dos-detalles",
            "TES-A-203_ccj-movimientos-no-agrupables-generan-un-comprobante-por-movimiento",
            "TES-A-204_ccj-con-secuencia-de-agrupacion-el-parametro-separa-por-concepto-no-aplica",
            "TES-A-205_ccj-netea-ingreso-y-egreso-en-un-comprobante",
            "TES-A-206_ccj-partida-de-ingreso-usa-las-formas-de-pago-de-entrada",
            "TES-A-207_ccj-partida-de-egreso-usa-las-formas-de-pago-de-entrada",
            "TES-A-208_ccj-simular-S-no-inserta-pero-informa-resultado",
            "TES-A-209_ccj-caja-informada-se-asigna-al-comprobante",
            "TES-A-210_ccj-forma-de-pago-por-caja-sin-caja-es-error",
            "TES-A-211_ccj-instrumento-informado-se-reporta-en-el-log",
            "TES-A-212_ccj-dias-de-retencion-calculan-la-fecha-afecta-saldo",
            "TES-A-213_ccj-afecta-saldo-sin-retencion-usa-la-fecha-de-movimiento",
            "TES-A-214_ccj-movimiento-inexistente-es-error",
            "TES-A-215_ccj-movimiento-ya-emitido-es-error",
            "TES-A-216_ccj-movimiento-bloqueado-es-error",
            "TES-A-217_ccj-movimiento-anulado-es-error",
            "TES-A-218_ccj-movimiento-pendiente-de-autorizacion-es-error",
            "TES-A-219_ccj-secuencia-de-agrupacion-ya-usada-por-otro-comprobante-no-se-detecta-defecto",
            "TES-A-220_ccj-faltan-movimientos-de-la-secuencia-de-agrupacion-no-se-detecta-defecto",
            "TES-A-221_ccj-sin-reglas-de-negocio-procesa-un-movimiento-bloqueado",
            "TES-A-222_ccj-ingreso-manual-con-requisitos-basicos-con-error-corta",
            "TES-A-223_ccj-ingreso-manual-con-requisitos-basicos-ok-continua",
            "TES-A-224_ccj-sin-movimientos-con-reglas-de-negocio-es-error",
            "TES-A-225_ccj-un-movimiento-invalido-y-otro-valido-genera-solo-el-valido",
            "TES-A-226_ccj-cliente-sin-formas-de-pago-es-error",
            "TES-A-227_ccj-concepto-que-genera-comprobante-automatico-no-exige-formas-de-pago",
            "TES-A-228_ccj-forma-de-pago-de-moneda-distinta-es-error",
            "TES-A-229_ccj-formas-de-pago-con-mezcla-de-monedas-es-error",
            "TES-A-230_ccj-forma-de-pago-mayor-al-comprobante-es-error",
            "TES-A-231_ccj-forma-de-pago-menor-al-comprobante-es-error",
            "TES-A-232_ccj-diferencia-menor-a-un-centavo-se-acepta",
            "TES-A-233_ccj-comprobante-desde-comprobantes-delega-en-el-procedimiento-almacenado",
            "TES-A-234_ccj-sin-secuencia-el-parametro-separa-por-concepto-genera-comprobantes-distintos");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        CrearComprobanteJavaService service = new CrearComprobanteJavaService(caso.jdbc(), d.diccionario(),
                new CrearComprobanteProcedimiento(caso.jdbc(), d.colaboradores()), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearComprobanteJava");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-200 ccj un ingreso genera un comprobante con detalle forma de pago y origen")
    void tesA200_ccjUnIngresoGeneraUnComprobanteConDetalleFormaDePagoYOrigen() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-201 ccj un egreso genera comprobante de egreso")
    void tesA201_ccjUnEgresoGeneraComprobanteDeEgreso() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-202 ccj dos movimientos agrupables generan un solo comprobante con dos detalles")
    void tesA202_ccjDosMovimientosAgrupablesGeneranUnSoloComprobanteConDosDetalles() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-203 ccj movimientos no agrupables generan un comprobante por movimiento")
    void tesA203_ccjMovimientosNoAgrupablesGeneranUnComprobantePorMovimiento() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-204 ccj con secuencia de agrupacion el parametro separa por concepto no aplica")
    void tesA204_ccjConSecuenciaDeAgrupacionElParametroSeparaPorConceptoNoAplica() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-205 ccj netea ingreso y egreso en un comprobante")
    void tesA205_ccjNeteaIngresoYEgresoEnUnComprobante() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-206 ccj partida de ingreso usa las formas de pago de entrada")
    void tesA206_ccjPartidaDeIngresoUsaLasFormasDePagoDeEntrada() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-207 ccj partida de egreso usa las formas de pago de entrada")
    void tesA207_ccjPartidaDeEgresoUsaLasFormasDePagoDeEntrada() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-208 ccj simular S no inserta pero informa resultado")
    void tesA208_ccjSimularSNoInsertaPeroInformaResultado() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-209 ccj caja informada se asigna al comprobante")
    void tesA209_ccjCajaInformadaSeAsignaAlComprobante() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-210 ccj forma de pago por caja sin caja es error")
    void tesA210_ccjFormaDePagoPorCajaSinCajaEsError() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-211 ccj instrumento informado se reporta en el log")
    void tesA211_ccjInstrumentoInformadoSeReportaEnElLog() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-212 ccj dias de retencion calculan la fecha afecta saldo")
    void tesA212_ccjDiasDeRetencionCalculanLaFechaAfectaSaldo() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-213 ccj afecta saldo sin retencion usa la fecha de movimiento")
    void tesA213_ccjAfectaSaldoSinRetencionUsaLaFechaDeMovimiento() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-214 ccj movimiento inexistente es error")
    void tesA214_ccjMovimientoInexistenteEsError() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-215 ccj movimiento ya emitido es error")
    void tesA215_ccjMovimientoYaEmitidoEsError() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-216 ccj movimiento bloqueado es error")
    void tesA216_ccjMovimientoBloqueadoEsError() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-217 ccj movimiento anulado es error")
    void tesA217_ccjMovimientoAnuladoEsError() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-218 ccj movimiento pendiente de autorizacion es error")
    void tesA218_ccjMovimientoPendienteDeAutorizacionEsError() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-219 ccj secuencia de agrupacion ya usada por otro comprobante no se detecta defecto")
    void tesA219_ccjSecuenciaDeAgrupacionYaUsadaPorOtroComprobanteNoSeDetectaDefecto() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-220 ccj faltan movimientos de la secuencia de agrupacion no se detecta defecto")
    void tesA220_ccjFaltanMovimientosDeLaSecuenciaDeAgrupacionNoSeDetectaDefecto() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-221 ccj sin reglas de negocio procesa un movimiento bloqueado")
    void tesA221_ccjSinReglasDeNegocioProcesaUnMovimientoBloqueado() throws Exception {
        verificar(CASOS.get(21));
    }

    @Test
    @DisplayName("TES-A-222 ccj ingreso manual con requisitos basicos con error corta")
    void tesA222_ccjIngresoManualConRequisitosBasicosConErrorCorta() throws Exception {
        verificar(CASOS.get(22));
    }

    @Test
    @DisplayName("TES-A-223 ccj ingreso manual con requisitos basicos ok continua")
    void tesA223_ccjIngresoManualConRequisitosBasicosOkContinua() throws Exception {
        verificar(CASOS.get(23));
    }

    @Test
    @DisplayName("TES-A-224 ccj sin movimientos con reglas de negocio es error")
    void tesA224_ccjSinMovimientosConReglasDeNegocioEsError() throws Exception {
        verificar(CASOS.get(24));
    }

    @Test
    @DisplayName("TES-A-225 ccj un movimiento invalido y otro valido genera solo el valido")
    void tesA225_ccjUnMovimientoInvalidoYOtroValidoGeneraSoloElValido() throws Exception {
        verificar(CASOS.get(25));
    }

    @Test
    @DisplayName("TES-A-226 ccj cliente sin formas de pago es error")
    void tesA226_ccjClienteSinFormasDePagoEsError() throws Exception {
        verificar(CASOS.get(26));
    }

    @Test
    @DisplayName("TES-A-227 ccj concepto que genera comprobante automatico no exige formas de pago")
    void tesA227_ccjConceptoQueGeneraComprobanteAutomaticoNoExigeFormasDePago() throws Exception {
        verificar(CASOS.get(27));
    }

    @Test
    @DisplayName("TES-A-228 ccj forma de pago de moneda distinta es error")
    void tesA228_ccjFormaDePagoDeMonedaDistintaEsError() throws Exception {
        verificar(CASOS.get(28));
    }

    @Test
    @DisplayName("TES-A-229 ccj formas de pago con mezcla de monedas es error")
    void tesA229_ccjFormasDePagoConMezclaDeMonedasEsError() throws Exception {
        verificar(CASOS.get(29));
    }

    @Test
    @DisplayName("TES-A-230 ccj forma de pago mayor al comprobante es error")
    void tesA230_ccjFormaDePagoMayorAlComprobanteEsError() throws Exception {
        verificar(CASOS.get(30));
    }

    @Test
    @DisplayName("TES-A-231 ccj forma de pago menor al comprobante es error")
    void tesA231_ccjFormaDePagoMenorAlComprobanteEsError() throws Exception {
        verificar(CASOS.get(31));
    }

    @Test
    @DisplayName("TES-A-232 ccj diferencia menor a un centavo se acepta")
    void tesA232_ccjDiferenciaMenorAUnCentavoSeAcepta() throws Exception {
        verificar(CASOS.get(32));
    }

    @Test
    @DisplayName("TES-A-233 ccj comprobante desde comprobantes delega en el procedimiento almacenado")
    void tesA233_ccjComprobanteDesdeComprobantesDelegaEnElProcedimientoAlmacenado() throws Exception {
        verificar(CASOS.get(33));
    }

    @Test
    @DisplayName("TES-A-234 ccj sin secuencia el parametro separa por concepto genera comprobantes distintos")
    void tesA234_ccjSinSecuenciaElParametroSeparaPorConceptoGeneraComprobantesDistintos() throws Exception {
        verificar(CASOS.get(34));
    }
}
