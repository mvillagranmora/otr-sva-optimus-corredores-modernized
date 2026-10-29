package cl.bcs.apv.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.apv.familia_b.support.Caso;
import cl.bcs.apv.familia_b.support.Equivalencia;
import cl.bcs.apv.familia_b.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.APV.GrabarRecaudacionTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code GrabarRecaudacionTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class GrabarRecaudacionTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-243_gr-sin-filas-lanza-IndexOutOfBounds",
            "APV-B-244_gr-cuenta-corriente-nula-lanza-NullPointerException",
            "APV-B-245_gr-monto-de-deposito-nulo-lanza-NullPointerException",
            "APV-B-300_gr-un-detalle-recauda-concilia-distribuye-y-completa",
            "APV-B-301_gr-dos-detalles-apv-y-bf-del-mismo-traspaso",
            "APV-B-302_gr-regimen-dc-llena-el-deposito-convenido",
            "APV-B-303_gr-error-de-tesoreria-del-deposito-se-ignora",
            "APV-B-304_gr-caja-de-recaudacion-cerrada-lanza-NullPointerException",
            "APV-B-305_gr-empresa-sin-datos-lanza-NullPointerException",
            "APV-B-306_gr-cuenta-corredora-inexistente-lanza-NullPointerException",
            "APV-B-307_gr-no-cuadra-el-deposito-no-concilia-y-no-distribuye",
            "APV-B-308_gr-error-de-tesoreria-al-traspasar-marca-el-mensaje-previo",
            "APV-B-309_gr-traspaso-de-otro-contrato-no-cambia-estado",
            "APV-B-310_gr-folio-de-contrato-desconocido-no-cambia-estado-contrato",
            "APV-B-311_gr-cuenta-con-ceros-a-la-izquierda-y-sucursal",
            "APV-B-312_gr-liquidacion-automatica-liquida-el-comprobante",
            "APV-B-313_gr-liquidacion-con-error-se-ignora-y-falla-el-traspaso",
            "APV-B-314_gr-dos-contratos-asigna-el-ultimo-a-todos",
            "APV-B-315_gr-con-detalles-de-reapertura-informados-distribuye",
            "APV-B-316_gr-reapertura-sin-fila-en-la-vista-lanza-IndexOutOfBounds");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GrabarRecaudacionTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // GrabarRecaudacionTraspaso.java:161: no p_RecaudacionConciliacion rows: get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB243_grSinFilasLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // GrabarRecaudacionTraspaso.java:165: CuentaCorriente null: NullPointerException before any collaborator is called
    @Test
    void apvB244_grCuentaCorrienteNulaLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // GrabarRecaudacionTraspaso.java:166: MontoDeposito null: unboxing NullPointerException
    @Test
    void apvB245_grMontoDeDepositoNuloLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // GrabarRecaudacionTraspaso.java:141-382 (+ CrearRecaudacionDeposito.java:221-640, CrearRecaudacionCliente.java:108-200, ConciliarDeposito, AsignarContratoARecaudacion, ProcesarDistribucion RECTRA): one APV-A detail creates the deposit (ING), the client row, reconciles it (CON), assigns the contract, distributes and completes traspaso and contract (COM)
    @Test
    void apvB300_grUnDetalleRecaudaConciliaDistribuyeYCompleta() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // GrabarRecaudacionTraspaso.java:209-210: an APV-A and a BF detail of the same traspaso become two client rows (the BF amount goes to MontoDepositoAPV), the deposit is the first row's amount and both reconcile
    @Test
    void apvB301_grDosDetallesApvYBfDelMismoTraspaso() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // GrabarRecaudacionTraspaso.java:209: a regimen containing DC puts the detail amount in MontoDepositoConvenido and 0 in MontoDepositoAPV
    @Test
    void apvB302_grRegimenDcLlenaElDepositoConvenido() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // CrearRecaudacionDeposito.java:576-580 + GrabarRecaudacionTraspaso.java:190: Tesoreria answers an ERROR for the deposit movement: the deposit service returns early but its IdRecaudacion is set and the caller never reads the messages, so the flow goes on as a success
    @Test
    void apvB303_grErrorDeTesoreriaDelDepositoSeIgnora() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // CrearRecaudacionDeposito.java:282-289 + GrabarRecaudacionTraspaso.java:190: no open PAC cash box: the deposit answers an error and P_OUT_IDREC stays null, the unboxing in the caller throws NullPointerException
    @Test
    void apvB304_grCajaDeRecaudacionCerradaLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // CrearRecaudacionDeposito.java:259-264 + GrabarRecaudacionTraspaso.java:190: the company has no default portafolio row: error message, P_OUT_IDREC null, NullPointerException
    @Test
    void apvB305_grEmpresaSinDatosLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // CrearRecaudacionDeposito.java:375-389 + GrabarRecaudacionTraspaso.java:190: the corredora account number is not in TES.CuentaBanco: error message, P_OUT_IDREC null, NullPointerException
    @Test
    void apvB306_grCuentaCorredoraInexistenteLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // GrabarRecaudacionTraspaso.java:279-289 (+ ConciliarDeposito): the deposit amount differs from the sum of the client rows: the reconciliation answers a not-matching message, typed OK with code ERROR by the caller, nothing is distributed and no state changes
    @Test
    void apvB307_grNoCuadraElDepositoNoConciliaYNoDistribuye() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // GrabarRecaudacionTraspaso.java:326-333: an ERROR from Tesoreria in the DIS traspaso: the caller overwrites the ALREADY ADDED reconciliation message (same MensajeSalida instance) with ERROR_RECAUDACION_CLIENTE and then adds the error rows; traspaso and contract stay unchanged
    @Test
    void apvB308_grErrorDeTesoreriaAlTraspasarMarcaElMensajePrevio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // GrabarRecaudacionTraspaso.java:344-374: the traspaso row belongs to another contract: the UPDATE touches 0 rows, ERROR_CAMBIO_ESTADO_TRASPASO, the contract update is not tried
    @Test
    void apvB309_grTraspasoDeOtroContratoNoCambiaEstado() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // GrabarRecaudacionTraspaso.java:353-367: the contract folio does not exist: the traspaso becomes COM but ERROR_CAMBIO_ESTADO_CONTRATO_TRASPASO is answered and the contract is untouched
    @Test
    void apvB310_grFolioDeContratoDesconocidoNoCambiaEstadoContrato() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // CrearRecaudacionDeposito.java:344,512 + GrabarRecaudacionTraspaso.java:137-173: leading zeros of the account number are removed to find the corredora account; sucursal and cartola/linea reach the deposit row
    @Test
    void apvB311_grCuentaConCerosALaIzquierdaYSucursal() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // CrearRecaudacionDeposito.java:611-640: REC.LiquidacionAutomatica = S: the deposit comprobante is sent to LiquidarComprobante (and the DIS traspaso liquidates its own)
    @Test
    void apvB312_grLiquidacionAutomaticaLiquidaElComprobante() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // CrearRecaudacionDeposito.java:632-638 + GrabarRecaudacionTraspaso.java:326-333: automatic liquidation answers an ERROR both for the deposit (ignored by the caller) and for the DIS traspaso (reported, state unchanged)
    @Test
    void apvB313_grLiquidacionConErrorSeIgnoraYFallaElTraspaso() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // GrabarRecaudacionTraspaso.java:224-236,293: two rows with different traspaso, folio and contract: both clients are assigned the contract of the LAST row and the last traspaso/contract is the one reported
    @Test
    void apvB314_grDosContratosAsignaElUltimoATodos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }

    // GrabarRecaudacionTraspaso.java:319 (+ ValidaContratoSAAPV.java:455-485): p_IdsDetalleTraspasoReapertura informed with its detail row in the view: the reapertura branch of the validation is used and the flow completes
    @Test
    void apvB315_grConDetallesDeReaperturaInformadosDistribuye() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(18));
    }

    // ValidaContratoSAAPV.java:458-463: the informed reapertura detail has no row in the view: detalleTraspaso.get(0) fails (IndexOutOfBoundsException) after the deposit and client rows were written (rolled back by the handler)
    @Test
    void apvB316_grReaperturaSinFilaEnLaVistaLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(19));
    }
}
