package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.RequisitosGirarComprobanteIngresoService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.RequisitosGirarComprobanteIngreso} (entry point call(conn, json)) as {@link RequisitosGirarComprobanteIngresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-500 .. TES-A-525} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class RequisitosGirarComprobanteIngresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-500_rgi-un-comprobante-ingresado-valido-informa-concepto-moneda-fecha-caja-y-medios",
            "TES-A-501_rgi-sin-comprobantes-es-error-y-no-informa-datos",
            "TES-A-502_rgi-comprobante-inexistente-es-invalido-e-inexistente",
            "TES-A-503_rgi-id-de-comprobante-cero-es-invalido",
            "TES-A-504_rgi-comprobante-liquidado-es-error",
            "TES-A-505_rgi-comprobante-bloqueado-es-error",
            "TES-A-506_rgi-comprobante-girado-es-error",
            "TES-A-507_rgi-comprobante-girado-parcialmente-se-acepta",
            "TES-A-508_rgi-comprobante-de-egreso-es-error",
            "TES-A-509_rgi-fechas-de-liquidacion-distintas-es-error",
            "TES-A-510_rgi-monedas-distintas-es-error",
            "TES-A-511_rgi-comprobante-sin-forma-de-pago-es-error",
            "TES-A-512_rgi-formas-de-pago-solo-liquidadas-o-giradas-cuentan-como-sin-forma-de-pago",
            "TES-A-513_rgi-cajas-distintas-es-error",
            "TES-A-514_rgi-caja-cero-no-cuenta-para-la-comparacion-y-se-informa-caja-1-por-defecto",
            "TES-A-515_rgi-origenes-de-ingreso-distintos-es-error",
            "TES-A-516_rgi-cuenta-de-la-corredora-inexistente-no-genera-error",
            "TES-A-517_rgi-banco-de-la-cuenta-sin-persona-es-error",
            "TES-A-518_rgi-simular-S-no-cambia-la-validacion-de-la-cuenta-porque-el-servicio-fija-N",
            "TES-A-519_rgi-conceptos-distintos-informa-MULCPT",
            "TES-A-520_rgi-solo-informa-los-medios-de-pago-presentes-en-todos-los-comprobantes",
            "TES-A-521_rgi-medios-de-pago-excluyen-formas-de-pago-modificadas-giradas-y-liquidadas",
            "TES-A-522_rgi-varios-errores-se-acumulan-y-el-contador-queda-en-1",
            "TES-A-523_rgi-dos-formas-de-pago-del-mismo-medio-se-suman",
            "TES-A-524_rgi-lista-de-comprobantes-ausente-lanza-SQLException-del-batch",
            "TES-A-525_rgi-comprobante-sin-fila-en-la-vista-no-informa-moneda-ni-caja");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_ERRORESDETALLE", "p_out_MediosDePagoAGirar"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        RequisitosGirarComprobanteIngresoService service = new RequisitosGirarComprobanteIngresoService(caso.jdbc(), d.diccionario());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("RequisitosGirarComprobanteIngreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-500 rgi un comprobante ingresado valido informa concepto moneda fecha caja y medios")
    void tesA500_rgiUnComprobanteIngresadoValidoInformaConceptoMonedaFechaCajaYMedios() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-501 rgi sin comprobantes es error y no informa datos")
    void tesA501_rgiSinComprobantesEsErrorYNoInformaDatos() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-502 rgi comprobante inexistente es invalido e inexistente")
    void tesA502_rgiComprobanteInexistenteEsInvalidoEInexistente() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-503 rgi id de comprobante cero es invalido")
    void tesA503_rgiIdDeComprobanteCeroEsInvalido() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-504 rgi comprobante liquidado es error")
    void tesA504_rgiComprobanteLiquidadoEsError() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-505 rgi comprobante bloqueado es error")
    void tesA505_rgiComprobanteBloqueadoEsError() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-506 rgi comprobante girado es error")
    void tesA506_rgiComprobanteGiradoEsError() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-507 rgi comprobante girado parcialmente se acepta")
    void tesA507_rgiComprobanteGiradoParcialmenteSeAcepta() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-508 rgi comprobante de egreso es error")
    void tesA508_rgiComprobanteDeEgresoEsError() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-509 rgi fechas de liquidacion distintas es error")
    void tesA509_rgiFechasDeLiquidacionDistintasEsError() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-510 rgi monedas distintas es error")
    void tesA510_rgiMonedasDistintasEsError() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-511 rgi comprobante sin forma de pago es error")
    void tesA511_rgiComprobanteSinFormaDePagoEsError() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-512 rgi formas de pago solo liquidadas o giradas cuentan como sin forma de pago")
    void tesA512_rgiFormasDePagoSoloLiquidadasOGiradasCuentanComoSinFormaDePago() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-513 rgi cajas distintas es error")
    void tesA513_rgiCajasDistintasEsError() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-514 rgi caja cero no cuenta para la comparacion y se informa caja 1 por defecto")
    void tesA514_rgiCajaCeroNoCuentaParaLaComparacionYSeInformaCaja1PorDefecto() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-515 rgi origenes de ingreso distintos es error")
    void tesA515_rgiOrigenesDeIngresoDistintosEsError() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-516 rgi cuenta de la corredora inexistente no genera error")
    void tesA516_rgiCuentaDeLaCorredoraInexistenteNoGeneraError() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-517 rgi banco de la cuenta sin persona es error")
    void tesA517_rgiBancoDeLaCuentaSinPersonaEsError() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-518 rgi simular S no cambia la validacion de la cuenta porque el servicio fija N")
    void tesA518_rgiSimularSNoCambiaLaValidacionDeLaCuentaPorqueElServicioFijaN() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-519 rgi conceptos distintos informa MULCPT")
    void tesA519_rgiConceptosDistintosInformaMULCPT() throws Exception {
        verificar(CASOS.get(19));
    }

    @Test
    @DisplayName("TES-A-520 rgi solo informa los medios de pago presentes en todos los comprobantes")
    void tesA520_rgiSoloInformaLosMediosDePagoPresentesEnTodosLosComprobantes() throws Exception {
        verificar(CASOS.get(20));
    }

    @Test
    @DisplayName("TES-A-521 rgi medios de pago excluyen formas de pago modificadas giradas y liquidadas")
    void tesA521_rgiMediosDePagoExcluyenFormasDePagoModificadasGiradasYLiquidadas() throws Exception {
        verificar(CASOS.get(21));
    }

    @Test
    @DisplayName("TES-A-522 rgi varios errores se acumulan y el contador queda en 1")
    void tesA522_rgiVariosErroresSeAcumulanYElContadorQuedaEn1() throws Exception {
        verificar(CASOS.get(22));
    }

    @Test
    @DisplayName("TES-A-523 rgi dos formas de pago del mismo medio se suman")
    void tesA523_rgiDosFormasDePagoDelMismoMedioSeSuman() throws Exception {
        verificar(CASOS.get(23));
    }

    @Test
    @DisplayName("TES-A-524 rgi lista de comprobantes ausente lanza SQLException del batch")
    void tesA524_rgiListaDeComprobantesAusenteLanzaSQLExceptionDelBatch() throws Exception {
        verificar(CASOS.get(24));
    }

    @Test
    @DisplayName("TES-A-525 rgi comprobante sin fila en la vista no informa moneda ni caja")
    void tesA525_rgiComprobanteSinFilaEnLaVistaNoInformaMonedaNiCaja() throws Exception {
        verificar(CASOS.get(25));
    }
}
