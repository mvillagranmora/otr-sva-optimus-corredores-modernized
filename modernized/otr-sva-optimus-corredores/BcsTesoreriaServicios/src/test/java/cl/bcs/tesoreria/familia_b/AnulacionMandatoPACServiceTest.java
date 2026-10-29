package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.SecuenciasDeCaso;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.AnulacionMandatoPACService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.AnulacionMandatoPAC} (plain-Gson {@code call}) as {@code AnulacionMandatoPACService#execute(JsonObject)}:
 * annuls PAC mandates and the CSC / SAAPV contracts related to them.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-135..147}.
 */
@ExtendWith(ZonaUtc.class)
class AnulacionMandatoPACServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-135_anula-mandato-csc-anula-contrato-y-actualiza-estado",
            "TES-B-136_anula-mandato-saapv-cambia-estado-del-contrato",
            "TES-B-137_desde-csc-S-no-anula-contratos-csc",
            "TES-B-138_desde-saapv-S-no-cambia-contratos-saapv",
            "TES-B-139_sin-contratos-NO_SE_INGRESARON_MODIFICACIONES",
            "TES-B-140_id-contrato-cero-ID_INTERNO_NO_CERO",
            "TES-B-141_id-contrato-repetido-CORRELATIIVO_INCORRECTO",
            "TES-B-142_requisitos-basicos-con-error",
            "TES-B-143_anulacion-csc-con-errores-devuelve-sus-mensajes-sin-actualizar",
            "TES-B-144_cambio-estado-saapv-con-errores-devuelve-sus-mensajes",
            "TES-B-145_mandato-inexistente-lanza-NullPointerException",
            "TES-B-146_mandato-csc-y-saapv-dos-ids-correlativos",
            "TES-B-147_anulacion-csc-con-mensajes-informativos-los-concatena");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new AnulacionMandatoPACService(caso.jdbc(), new DiccionarioDeCaso(), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AnulacionMandatoPAC");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // AnulacionMandatoPAC.java:165-221: annulment of a CSC mandate: the related CSC contract is annulled (collaborator) and the mandate set to ANU
    @Test
    void tesB135_anulaMandatoCscAnulaContratoYActualizaEstado() {
        verificar(CASOS.get(0));
    }

    // AnulacionMandatoPAC.java:223-263: annulment of a SAAPV mandate: SAAPV contract state change (collaborator)
    @Test
    void tesB136_anulaMandatoSaapvCambiaEstadoDelContrato() {
        verificar(CASOS.get(1));
    }

    // AnulacionMandatoPAC.java:165: p_DesdeCSC = S skips the CSC collaborator
    @Test
    void tesB137_desdeCscSNoAnulaContratosCsc() {
        verificar(CASOS.get(2));
    }

    // AnulacionMandatoPAC.java:224: p_DesdeSAAPV = S skips the SAAPV collaborator
    @Test
    void tesB138_desdeSaapvSNoCambiaContratosSaapv() {
        verificar(CASOS.get(3));
    }

    // AnulacionMandatoPAC.java:138-142: empty request
    @Test
    void tesB139_sinContratosNoSeIngresaronModificaciones() {
        verificar(CASOS.get(4));
    }

    // AnulacionMandatoPAC.java:146-150: mandate id 0 is rejected
    @Test
    void tesB140_idContratoCeroIdInternoNoCero() {
        verificar(CASOS.get(5));
    }

    // AnulacionMandatoPAC.java:151-157: duplicated mandate id: CORRELATIIVO_INCORRECTO (sic)
    @Test
    void tesB141_idContratoRepetidoCorrelativoIncorrecto() {
        verificar(CASOS.get(6));
    }

    // AnulacionMandatoPAC.java:129-135: basic requirements with an error: counter 1, no messages
    @Test
    void tesB142_requisitosBasicosConError() {
        verificar(CASOS.get(7));
    }

    // AnulacionMandatoPAC.java:190-197: the CSC collaborator reports errors: its messages are returned and nothing is updated
    @Test
    void tesB143_anulacionCscConErroresDevuelveSusMensajesSinActualizar() {
        verificar(CASOS.get(8));
    }

    // AnulacionMandatoPAC.java:250-257: the SAAPV collaborator reports errors (after the mandate update)
    @Test
    void tesB144_cambioEstadoSaapvConErroresDevuelveSusMensajes() {
        verificar(CASOS.get(9));
    }

    // AnulacionMandatoPAC.java:181-182: an unknown mandate id: NullPointerException (no related contract row)
    @Test
    void tesB145_mandatoInexistenteLanzaNullPointerException() {
        verificar(CASOS.get(10));
    }

    // AnulacionMandatoPAC.java:180-221: three mandates (CSC only, SAAPV only, both): one message per mandate
    @Test
    void tesB146_tresMandatosCscSaapvYAmbos() {
        verificar(CASOS.get(11));
    }

    // AnulacionMandatoPAC.java:199-201: non-error messages of the CSC collaborator are appended before the success messages
    @Test
    void tesB147_anulacionCscConMensajesInformativosLosConcatena() {
        verificar(CASOS.get(12));
    }

}
