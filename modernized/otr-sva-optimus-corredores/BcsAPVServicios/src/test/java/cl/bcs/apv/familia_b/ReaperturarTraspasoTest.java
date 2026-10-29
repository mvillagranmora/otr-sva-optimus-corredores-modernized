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
 * Characterization of legacy {@code cl.bcs.APV.ReaperturarTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code ReaperturarTraspasos} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class ReaperturarTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-246_re-sin-traspasos-lanza-IndexOutOfBounds",
            "APV-B-247_re-plan-distinto-informa-error-de-plan-y-reapertura",
            "APV-B-248_re-primera-fila-se-guarda-y-la-segunda-falla",
            "APV-B-249_re-sin-montos-llega-a-la-recaudacion-y-falla",
            "APV-B-340_re-un-detalle-nuevo-lo-recauda-y-lo-distribuye",
            "APV-B-341_re-dos-detalles-suman-el-monto-y-recaudan",
            "APV-B-342_re-el-detalle-de-monto-cero-no-se-reabre",
            "APV-B-343_re-el-detalle-no-cuadra-con-el-deposito-informa-OK",
            "APV-B-344_re-error-de-tesoreria-en-la-distribucion-informa-error");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ReaperturarTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ReaperturarTraspaso.java:157: no traspasos: in.p_Traspasos.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB246_reSinTraspasosLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // ReaperturarTraspaso.java:164-224: the in-process ModificarPortafolioTraspaso (p_Tipo R) answers UI_ERROR_PLAN_TIPO_INVERSION: its first message is copied and ERROR_MODIFICA_ESTADO added (counter 2); IngresoRecaudacionTraspaso is not called
    @Test
    void apvB247_rePlanDistintoInformaErrorDePlanYReapertura() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // ReaperturarTraspaso.java:164-224: the first row creates the reapertura detalle (rows are written) and the second fails: the call returns normally, so the first row's changes are kept (partial commit) and the output carries both messages
    @Test
    void apvB248_rePrimeraFilaSeGuardaYLaSegundaFalla() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // ReaperturarTraspaso.java:164-243: rows without MontoReapertura skip the modification and the flow reaches IngresoRecaudacionTraspaso (type R), which fails like APV-B-242
    @Test
    void apvB249_reSinMontosLlegaALaRecaudacionYFalla() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // ReaperturarTraspaso.java:164-266: one reapertura row creates the detail (EsReapertura S) and the portafolio-contract relation, then IngresoRecaudacionTraspaso R recauda, reconciles, distributes to the funds and completes the traspaso and contract
    @Test
    void apvB340_reUnDetalleNuevoLoRecaudaYLoDistribuye() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // ReaperturarTraspaso.java:164-216,226-255: two rows (DC-R and APV-A) add 300000 and send both detail ids: two client rows (convenido / APV), reconciled; the recaudacion stays CON (no distribution to funds)
    @Test
    void apvB341_reDosDetallesSumanElMontoYRecaudan() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // ReaperturarTraspaso.java:165: a row with MontoReapertura 0 is skipped (no detail, no portafolio relation); the other row is reaperturada as in 340
    @Test
    void apvB342_reElDetalleDeMontoCeroNoSeReabre() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // ReaperturarTraspaso.java:245-255: the recaudacion stage reconciles with a NOT matching message typed OK: the reapertura answers REAPERTURAR_TRASPASO_OK plus the not-matching row, no error counted
    @Test
    void apvB343_reElDetalleNoCuadraConElDepositoInformaOK() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // ReaperturarTraspaso.java:245-266: Tesoreria fails in the DIS traspaso: the detail and the relation are already written; ERROR_REAPERTURAR_TRASPASO, counter 1, state of traspaso and contract unchanged
    @Test
    void apvB344_reErrorDeTesoreriaEnLaDistribucionInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }
}
