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
 * Characterization of legacy {@code cl.bcs.APV.ModificarPortafolioTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code ModificarPortafoliosTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class ModificarPortafolioTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-197_mp-nuevo-con-portafolio-existente-lo-relaciona",
            "APV-B-198_mp-nuevo-con-portafolio-inactivo-lo-reactiva",
            "APV-B-199_mp-nuevo-plan-distinto-informa-error-de-plan",
            "APV-B-200_mp-nuevo-sin-portafolio-lo-crea-con-el-procedimiento",
            "APV-B-201_mp-portafolio-creado-no-autorizado-se-activa",
            "APV-B-202_mp-procedimiento-con-errores-lanza-IndexOutOfBounds",
            "APV-B-203_mp-matriz-inexistente-lanza-IndexOutOfBounds",
            "APV-B-204_mp-reapertura-se-trata-como-detalle-nuevo",
            "APV-B-205_mp-existente-sin-cambios-no-hace-nada",
            "APV-B-206_mp-existente-cambia-monto-actualiza-detalle-y-contrato",
            "APV-B-207_mp-existente-cambia-regimen-con-portafolio-existente",
            "APV-B-208_mp-existente-cambia-regimen-crea-portafolio",
            "APV-B-209_mp-dos-vigentes-iguales-inactiva-e-ingresa-uno-nuevo",
            "APV-B-210_mp-existente-sin-cliente-lo-toma-del-contrato",
            "APV-B-211_mp-detalle-que-no-viene-se-elimina-y-desactiva-portafolio",
            "APV-B-212_mp-eliminado-con-portafolio-en-otro-contrato-solo-modifica",
            "APV-B-213_mp-sin-traspasos-lanza-IndexOutOfBounds");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ModificarPortafolioTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ModificarPortafolioTraspaso.java:715-747,811-824: a new detalle (id 0) whose matrix already has a portafolio for the client: ASIGNAR_PORTAFOLIO_CONTRATO_OK (id = REFIDPORTAFOLIO, 0) and CREAR_DETALLE_TRASPASO_OK; P_OUT_IDRESULTADO holds the new detalle id
    @Test
    void apvB197_mpNuevoConPortafolioExistenteLoRelaciona() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // ModificarPortafolioTraspaso.java:724-728,919-942: the existing portafolio is INA: it is set ACT/AUT (InhabilitarPortafolioTraspaso.sql) before the relation
    @Test
    void apvB198_mpNuevoConPortafolioInactivoLoReactiva() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // ModificarPortafolioTraspaso.java:752-760,1065-1087: no portafolio for the matrix and the client already has portafolios of another plan type: UI_ERROR_PLAN_TIPO_INVERSION (dictionary), the row is skipped
    @Test
    void apvB199_mpNuevoPlanDistintoInformaErrorDePlan() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // ModificarPortafolioTraspaso.java:763-833,850-917: no portafolio for the matrix: CrearPortafolio (stored procedure) creates it, then CREACION_PORTAFOLIO_OK, CREAR_PORTAFOLIO_CONTRATO_OK, CREAR_DETALLE_TRASPASO_OK
    @Test
    void apvB200_mpNuevoSinPortafolioLoCreaConElProcedimiento() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // ModificarPortafolioTraspaso.java:908-910: a created portafolio that is not AUT/ACT is activated through ModificarEstadoPortafolioTraspaso
    @Test
    void apvB201_mpPortafolioCreadoNoAutorizadoSeActiva() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // ModificarPortafolioTraspaso.java:887-892: the stored procedure answers errors (no RefId): the select of the created portafolio is empty and get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB202_mpProcedimientoConErroresLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // ModificarPortafolioTraspaso.java:973-976: no MatrizAgrupadorAPV for (plan, producto, regimen): get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB203_mpMatrizInexistenteLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // ModificarPortafolioTraspaso.java:500,715-747: EsReapertura S is never treated as an existing detalle: a new detalle is created with EsReapertura S
    @Test
    void apvB204_mpReaperturaSeTrataComoDetalleNuevo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // ModificarPortafolioTraspaso.java:534,672,708-712: the existing detalle has the same product, regimen and amounts: nothing is changed and nothing is reported
    @Test
    void apvB205_mpExistenteSinCambiosNoHaceNada() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }

    // ModificarPortafolioTraspaso.java:672-707,1048-1063: amounts changed: detalle and portafolio-contrato are updated, MODIFICAR_PORTAFOLIO_CONTRATO_OK
    @Test
    void apvB206_mpExistenteCambiaMontoActualizaDetalleYContrato() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(9));
    }

    // ModificarPortafolioTraspaso.java:534-592: product/regimen changed and the new matrix has a portafolio: relation removed and recreated, old portafolio desactivated (ELIMINO_PORTAFOLIO_CONTRATO_OK) and the detalle moved
    @Test
    void apvB207_mpExistenteCambiaRegimenConPortafolioExistente() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(10));
    }

    // ModificarPortafolioTraspaso.java:606-668: product/regimen changed and the new matrix has no portafolio: it is created by the stored procedure; the message tells the old portafolio was desactivated
    @Test
    void apvB208_mpExistenteCambiaRegimenCreaPortafolio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(11));
    }

    // ModificarPortafolioTraspaso.java:517-532: two VIG detalles with the same id: both are inactivated (UpdateInactivarDetalleTraspRepetido) and a new detalle is inserted with the portafolio found by the validation (0 here)
    @Test
    void apvB209_mpDosVigentesIgualesInactivaEIngresaUnoNuevo() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(12));
    }

    // ModificarPortafolioTraspaso.java:508-514: IdCliente 0 is read from the contract (ModificarPortafolioTraspaso_SelectClienteContrato.sql) before comparing
    @Test
    void apvB210_mpExistenteSinClienteLoTomaDelContrato() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(13));
    }

    // ModificarPortafolioTraspaso.java:358-446: an existing detalle absent from the input is deleted with its portafolio-contrato; the portafolio without other contracts is desactivated: INFORMACION DESACTIVAR_PORTAFOLIO
    @Test
    void apvB211_mpDetalleQueNoVieneSeEliminaYDesactivaPortafolio() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(14));
    }

    // ModificarPortafolioTraspaso.java:403-418,944-964: the old portafolio still belongs to another contract: only MOFIFICACION_DETALLE_CONTRATO is reported and the portafolio stays active
    @Test
    void apvB212_mpEliminadoConPortafolioEnOtroContratoSoloModifica() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(15));
    }

    // ModificarPortafolioTraspaso.java:354-355: no traspasos: in.p_Traspasos.get(0) fails (IndexOutOfBoundsException)
    @Test
    void apvB213_mpSinTraspasosLanzaIndexOutOfBounds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(16));
    }
}
