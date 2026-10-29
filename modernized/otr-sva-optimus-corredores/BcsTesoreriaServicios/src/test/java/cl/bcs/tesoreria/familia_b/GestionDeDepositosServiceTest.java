package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.dep.GestionDeDepositosService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.dep.GestionDeDepositos} (entry point {@code call}) as {@code GestionDeDepositosService#execute(JsonObject)}:
 * sends (EDP), liquidates (LIQ) or annuls (ANU) deposit payrolls or their details.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-243..266}. Collaborators RevisarRequisitosBasicos and LiquidarComprobante are recorded fakes; ActualizarSaldosCuentaBanco
 * runs as the real legacy code. The message dictionary has no seam in the modern service yet (the oracle shim answers a bracketed code plus values).
 */
@ExtendWith(ZonaUtc.class)
class GestionDeDepositosServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-243_edp-deposito-ing-pasa-a-edp-con-sus-detalles",
            "TES-B-244_edp-detalle-deja-el-estado-del-deposito-en-nulo",
            "TES-B-245_liq-deposito-con-forma-de-pago-no-liquidada-llama-liquidar-comprobante",
            "TES-B-246_liq-con-errores-de-liquidar-comprobante-devuelve-sus-mensajes",
            "TES-B-247_liq-deposito-con-formas-de-pago-liquidadas-marca-depositado-y-actualiza-saldos",
            "TES-B-248_liq-detalle-nunca-recolecta-ids-por-el-espacio-inicial",
            "TES-B-249_liq-un-detalle-de-dos-deja-el-deposito-en-LIQ-parcial",
            "TES-B-250_anu-deposito-ing-lo-anula-y-libera-las-formas-de-pago",
            "TES-B-251_anu-un-detalle-reduce-el-monto-del-deposito",
            "TES-B-252_anu-ultimo-detalle-anula-el-deposito-completo",
            "TES-B-253_anu-deposito-edp-es-error-NOMINA_DEPOSITADA",
            "TES-B-254_liq-deposito-ing-es-error-NOMINA_SIN_DEPOSITAR",
            "TES-B-255_deposito-liquidado-es-error-DEPOSITO_LIQUIDADO",
            "TES-B-256_deposito-anulado-es-error-DEPOSITO_ANULADO",
            "TES-B-257_detalle-liquidado-es-error-DETALLE_DEPOSITO_LIQUIDADO",
            "TES-B-258_detalle-anulado-es-error-DETALLE_DEPOSITO_ANULADO",
            "TES-B-259_deposito-inexistente-lanza-NPE-en-la-validacion",
            "TES-B-260_detalle-inexistente-lanza-NPE-en-la-validacion",
            "TES-B-261_uno-valido-y-uno-invalido-procesa-el-valido-y-reporta-el-error",
            "TES-B-262_requisitos-basicos-con-error-devuelve-solo-esos-mensajes",
            "TES-B-263_sin-registros-con-reglas-informa-DEPOSITO_NO_EXISTE",
            "TES-B-264_sin-registros-sin-reglas-no-hace-nada",
            "TES-B-265_liq-deposito-con-cheque-no-liquidada-y-cuenta-cero",
            "TES-B-266_tipo-de-ejecucion-desconocido-actualiza-estados-con-ese-valor");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new GestionDeDepositosService(caso.jdbc(), new DiccionarioDeCaso(), new ActualizarSaldosCuentaBancoService(caso.jdbc()), col[0])
                    .execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GestionDeDepositos");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // dep/GestionDeDepositos.java:322-335,477-479: EDP on a deposit: deposit and details go to EDP (deposit ING -> EDP)
    @Test
    void tesB243_edpDepositoIngPasaAEdpConSusDetalles() {
        verificar(CASOS.get(0));
    }

    // dep/GestionDeDepositos.java:332-335 + sql/GestionDeDepositos_UpdateDeposito_EntidadDepositoDetalle.sql: EDP on a detail: the deposit status CASE has no EDP branch, so the deposit status becomes NULL (legacy defect)
    @Test
    void tesB244_edpDetalleDejaElEstadoDelDepositoEnNulo() {
        verificar(CASOS.get(1));
    }

    // dep/GestionDeDepositos.java:350-431: LIQ with forma de pago not LIQ: LiquidarComprobante is called and its success short-circuits the Depositado update
    @Test
    void tesB245_liqDepositoConFormaDePagoNoLiquidadaLlamaLiquidarComprobante() {
        verificar(CASOS.get(2));
    }

    // dep/GestionDeDepositos.java:424-431: LiquidarComprobante returns errors: its messages are the output, no success messages
    @Test
    void tesB246_liqConErroresDeLiquidarComprobanteDevuelveSusMensajes() {
        verificar(CASOS.get(3));
    }

    // dep/GestionDeDepositos.java:435-466: LIQ with every forma de pago already LIQ: Depositado/FechaAfectaSaldo updated and ActualizarSaldosCuentaBanco run
    @Test
    void tesB247_liqDepositoConFormasDePagoLiquidadasMarcaDepositadoYActualizaSaldos() {
        verificar(CASOS.get(4));
    }

    // dep/GestionDeDepositos.java:366: LIQ by detail: the id collection compares a name with a leading space so no detail id is collected (legacy defect)
    @Test
    void tesB248_liqDetalleNuncaRecolectaIdsPorElEspacioInicial() {
        verificar(CASOS.get(5));
    }

    // dep/GestionDeDepositos.java:332-335: LIQ of one detail of two: deposit becomes LIP
    @Test
    void tesB249_liqUnDetalleDeDosDejaElDepositoEnLIQParcial() {
        verificar(CASOS.get(6));
    }

    // dep/GestionDeDepositos.java:341-344: ANU deposit: forma de pago released (CRT when the medio allows it, ING otherwise)
    @Test
    void tesB250_anuDepositoIngLoAnulaYLiberaLasFormasDePago() {
        verificar(CASOS.get(7));
    }

    // dep/GestionDeDepositos.java:332-335: ANU of one detail of two: deposit amount reduced
    @Test
    void tesB251_anuUnDetalleReduceElMontoDelDeposito() {
        verificar(CASOS.get(8));
    }

    // dep/GestionDeDepositos.java:332-335: ANU of the last detail: deposit ANU with its total amount
    @Test
    void tesB252_anuUltimoDetalleAnulaElDepositoCompleto() {
        verificar(CASOS.get(9));
    }

    // dep/GestionDeDepositos.java:290: ANU of an EDP deposit: NOMINA_DEPOSITADA
    @Test
    void tesB253_anuDepositoEdpEsErrorNOMINADEPOSITADA() {
        verificar(CASOS.get(10));
    }

    // dep/GestionDeDepositos.java:291: LIQ of an ING deposit: NOMINA_SIN_DEPOSITAR
    @Test
    void tesB254_liqDepositoIngEsErrorNOMINASINDEPOSITAR() {
        verificar(CASOS.get(11));
    }

    // dep/GestionDeDepositos.java:296: deposit already LIQ: DEPOSITO_LIQUIDADO
    @Test
    void tesB255_depositoLiquidadoEsErrorDEPOSITOLIQUIDADO() {
        verificar(CASOS.get(12));
    }

    // dep/GestionDeDepositos.java:295: deposit already ANU: DEPOSITO_ANULADO
    @Test
    void tesB256_depositoAnuladoEsErrorDEPOSITOANULADO() {
        verificar(CASOS.get(13));
    }

    // dep/GestionDeDepositos.java:300: detail already LIQ: DETALLE_DEPOSITO_LIQUIDADO
    @Test
    void tesB257_detalleLiquidadoEsErrorDETALLEDEPOSITOLIQUIDADO() {
        verificar(CASOS.get(14));
    }

    // dep/GestionDeDepositos.java:301: detail already ANU: DETALLE_DEPOSITO_ANULADO
    @Test
    void tesB258_detalleAnuladoEsErrorDETALLEDEPOSITOANULADO() {
        verificar(CASOS.get(15));
    }

    // dep/GestionDeDepositos.java:294: unknown deposit: IdDeposito null -> NullPointerException (DEPOSITO_NO_EXISTE is unreachable)
    @Test
    void tesB259_depositoInexistenteLanzaNPEEnLaValidacion() {
        verificar(CASOS.get(16));
    }

    // dep/GestionDeDepositos.java:299: unknown detail: NullPointerException (DETALLE_DEPOSITO_NO_EXISTE is unreachable)
    @Test
    void tesB260_detalleInexistenteLanzaNPEEnLaValidacion() {
        verificar(CASOS.get(17));
    }

    // dep/GestionDeDepositos.java:307-313: invalid registers are removed, the valid one is processed and the error is reported
    @Test
    void tesB261_unoValidoYUnoInvalidoProcesaElValidoYReportaElError() {
        verificar(CASOS.get(18));
    }

    // dep/GestionDeDepositos.java:256-260: basic requirements with error: only those messages
    @Test
    void tesB262_requisitosBasicosConErrorDevuelveSoloEsosMensajes() {
        verificar(CASOS.get(19));
    }

    // dep/GestionDeDepositos.java:262-265: no registers with business rules: DEPOSITO_NO_EXISTE
    @Test
    void tesB263_sinRegistrosConReglasInformaDEPOSITONOEXISTE() {
        verificar(CASOS.get(20));
    }

    // dep/GestionDeDepositos.java:270-313: no registers without business rules: nothing happens
    @Test
    void tesB264_sinRegistrosSinReglasNoHaceNada() {
        verificar(CASOS.get(21));
    }

    // dep/GestionDeDepositos.java:376-431: LIQ with a cheque whose forma de pago is not LIQ: LiquidarComprobante
    @Test
    void tesB265_liqDepositoConChequeNoLiquidadaYCuentaCero() {
        verificar(CASOS.get(22));
    }

    // dep/GestionDeDepositos.java:477-502: unknown execution type: states written with that value, empty success code
    @Test
    void tesB266_tipoDeEjecucionDesconocidoActualizaEstadosConEseValor() {
        verificar(CASOS.get(23));
    }

}
