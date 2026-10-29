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
 * Characterization of legacy {@code cl.bcs.APV.IngresoRecaudacionTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code IngresarRecaudacionTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class IngresoRecaudacionTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-235_ir-sin-traspasos-no-hace-nada",
            "APV-B-236_ir-fecha-de-sistema-sin-configurar-excepcion",
            "APV-B-237_ir-traspaso-sin-detalles-ING-falla-al-grabar",
            "APV-B-238_ir-validacion-de-estado-nunca-rechaza-compara-por-referencia",
            "APV-B-239_ir-traspaso-repetido-en-la-vista-IllegalState",
            "APV-B-240_ir-tipo-de-traspaso-nulo-NullPointerException",
            "APV-B-241_ir-reapertura-sin-detalle-IndexOutOfBounds",
            "APV-B-242_ir-reapertura-sin-detalles-informados-falla",
            "APV-B-320_ir-un-traspaso-independiente-recauda-y-completa",
            "APV-B-321_ir-un-traspaso-dependiente-grabado-como-DEP",
            "APV-B-322_ir-el-monto-del-deposito-suma-la-vista-y-no-la-entrada",
            "APV-B-323_ir-cartola-y-sucursal-de-la-entrada-llegan-al-deposito",
            "APV-B-324_ir-reapertura-tipo-R-con-el-detalle-de-reapertura",
            "APV-B-325_ir-dos-traspasos-ambos-recaudan",
            "APV-B-326_ir-dos-traspasos-segundo-falla-revierte-y-devuelve-errores",
            "APV-B-327_ir-un-traspaso-con-error-devuelve-todos-los-mensajes",
            "APV-B-328_ir-tablas-de-trabajo-spot-con-filas-se-vacian",
            "APV-B-329_ir-tablas-de-trabajo-spot-vacias-no-se-tocan");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("IngresoRecaudacionTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // IngresoRecaudacionTraspaso.java:145,149,196-212: no traspasos: PLA.FechaSistema is read, nothing else; empty output
    @Test
    void apvB235_irSinTraspasosNoHaceNada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // IngresoRecaudacionTraspaso.java:145: PLATAFORMA/PLA.FechaSistema missing: Exception
    @Test
    void apvB236_irFechaDeSistemaSinConfigurarExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // IngresoRecaudacionTraspaso.java:150-155,273-286,356,GrabarRecaudacionTraspaso.java:161: the traspaso exists but has no detalle in state ING: GrabarRecaudacionTraspaso (in process) is called with an empty list and fails (IndexOutOfBoundsException)
    @Test
    void apvB237_irTraspasoSinDetallesINGFallaAlGrabar() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // IngresoRecaudacionTraspaso.java:232-243: the state/entity validation compares tipoTraspaso == "I" and IdEstado != "COM" by REFERENCE: with a JSON value the comparison is false, so a traspaso that would be rejected is never rejected (ERROR_TRASPASO_RECAUDACION is unreachable from JSON); the flow goes on to the same failure
    @Test
    void apvB238_irValidacionDeEstadoNuncaRechazaComparaPorReferencia() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // IngresoRecaudacionTraspaso.java:224-225: two rows of the view for the same IdTraspaso: Collectors.toMap throws IllegalStateException(Duplicate key)
    @Test
    void apvB239_irTraspasoRepetidoEnLaVistaIllegalState() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // IngresoRecaudacionTraspaso.java:262-267: p_TipoTraspaso null: NullPointerException in obtenerMontoDeposito
    @Test
    void apvB240_irTipoDeTraspasoNuloNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // IngresoRecaudacionTraspaso.java:288-295: reapertura (R) with a detalle id that has no EsReapertura=S row: detalleTraspasoReapertura.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB241_irReaperturaSinDetalleIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // IngresoRecaudacionTraspaso.java:288-295,356: reapertura without detalle ids: no detalle, GrabarRecaudacionTraspaso fails on the empty list
    @Test
    void apvB242_irReaperturaSinDetallesInformadosFalla() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // IngresoRecaudacionTraspaso.java:149-212 + GrabarRecaudacionTraspaso: one INDEPENDIENTE traspaso of type I is recaudado, reconciled, distributed and completed (TipoAfiliado IND)
    @Test
    void apvB320_irUnTraspasoIndependienteRecaudaYCompleta() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // IngresoRecaudacionTraspaso.java:344: Trabajador DEPENDIENTE becomes TipoAfiliado DEP on the client row
    @Test
    void apvB321_irUnTraspasoDependienteGrabadoComoDEP() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // IngresoRecaudacionTraspaso.java:266: for type I the deposit amount is the SUM of MontoPesoDetalle of the view (200000), not the MontoTraspaso of the input (999): it does not match the detail (160000) and nothing is distributed
    @Test
    void apvB322_irElMontoDelDepositoSumaLaVistaYNoLaEntrada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // IngresoRecaudacionTraspaso.java:249-255: FolioCartola, LineaCartola and IdSucursal of the input reach the deposit row (Sucursal as text)
    @Test
    void apvB323_irCartolaYSucursalDeLaEntradaLleganAlDeposito() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // IngresoRecaudacionTraspaso.java:282-296,266-267 (+ ProcesarDistribucion reapertura): type R takes the amount from the input and the detail from the reapertura view; the recaudacion is distributed to the funds (DIS, solicitud FFMM created)
    @Test
    void apvB324_irReaperturaTipoRConElDetalleDeReapertura() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // IngresoRecaudacionTraspaso.java:149-180: two traspasos, both succeed: two recaudaciones, messages and results of both are added in order
    @Test
    void apvB325_irDosTraspasosAmbosRecaudan() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // IngresoRecaudacionTraspaso.java:166-176,196-210: two traspasos, the second fails: only the error rows are returned, the REC result of the last, and con.rollback() reverts the first one too
    @Test
    void apvB326_irDosTraspasosSegundoFallaRevierteYDevuelveErrores() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // IngresoRecaudacionTraspaso.java:176: a single traspaso whose view amount (999999) does not match the detail: all messages are returned (typed OK/ERROR code), no error counted
    @Test
    void apvB327_irUnTraspasoConErrorDevuelveTodosLosMensajes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // IngresoRecaudacionTraspaso.java:159-164: rows in CrearOperacionSpot_Proceso: both work tables (SPT.CrearOperacionSpot_Proceso, GBL.SaldoFlujoCaja_Proceso_Paso) are emptied
    @Test
    void apvB328_irTablasDeTrabajoSpotConFilasSeVacian() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }

    // IngresoRecaudacionTraspaso.java:159-164: the work table is empty: neither statement runs (baseline of 328)
    @Test
    void apvB329_irTablasDeTrabajoSpotVaciasNoSeTocan() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(17));
    }
}
