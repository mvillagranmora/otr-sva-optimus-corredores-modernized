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
import cl.bcs.tesoreria.javacall.RevocacionMandatoPACService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.RevocacionMandatoPAC} (plain-Gson {@code call}) as {@code RevocacionMandatoPACService#execute(JsonObject)}:
 * revokes PAC mandates and the CSC / SAAPV contracts related to them.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-148..159}.
 */
@ExtendWith(ZonaUtc.class)
class RevocacionMandatoPACServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-148_revoca-mandato-csc-revoca-contrato-y-actualiza-estado",
            "TES-B-149_revoca-mandato-saapv-cambia-estado-del-contrato",
            "TES-B-150_desde-csc-S-no-revoca-contratos-csc",
            "TES-B-151_desde-saapv-S-no-cambia-contratos-saapv",
            "TES-B-152_sin-mandatos-NO_SE_INGRESARON_MODIFICACIONES",
            "TES-B-153_id-mandato-cero-ID_INTERNO_NO_CERO",
            "TES-B-154_id-mandato-repetido-CORRELATIIVO_INCORRECTO",
            "TES-B-155_requisitos-basicos-con-error",
            "TES-B-156_revocacion-csc-con-errores-devuelve-sus-mensajes-sin-actualizar",
            "TES-B-157_cambio-estado-saapv-con-errores-devuelve-sus-mensajes",
            "TES-B-158_mandato-inexistente-lanza-NullPointerException",
            "TES-B-159_tres-mandatos-csc-saapv-y-ambos");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new RevocacionMandatoPACService(caso.jdbc(), new DiccionarioDeCaso(), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("RevocacionMandatoPAC");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // RevocacionMandatoPAC.java:170-226: revocation of a CSC mandate: related CSC contract revoked (collaborator), mandate set to RVC
    @Test
    void tesB148_revocaMandatoCscRevocaContratoYActualizaEstado() {
        verificar(CASOS.get(0));
    }

    // RevocacionMandatoPAC.java:228-270: revocation of a SAAPV mandate (collaborator)
    @Test
    void tesB149_revocaMandatoSaapvCambiaEstadoDelContrato() {
        verificar(CASOS.get(1));
    }

    // RevocacionMandatoPAC.java:170: p_DesdeCSC = S skips the CSC collaborator
    @Test
    void tesB150_desdeCscSNoRevocaContratosCsc() {
        verificar(CASOS.get(2));
    }

    // RevocacionMandatoPAC.java:229: p_DesdeSAAPV = S skips the SAAPV collaborator
    @Test
    void tesB151_desdeSaapvSNoCambiaContratosSaapv() {
        verificar(CASOS.get(3));
    }

    // RevocacionMandatoPAC.java:143-147: empty request
    @Test
    void tesB152_sinMandatosNoSeIngresaronModificaciones() {
        verificar(CASOS.get(4));
    }

    // RevocacionMandatoPAC.java:151-155: mandate id 0 rejected
    @Test
    void tesB153_idMandatoCeroIdInternoNoCero() {
        verificar(CASOS.get(5));
    }

    // RevocacionMandatoPAC.java:156-162: duplicated mandate id
    @Test
    void tesB154_idMandatoRepetidoCorrelativoIncorrecto() {
        verificar(CASOS.get(6));
    }

    // RevocacionMandatoPAC.java:135-141: basic requirements with an error
    @Test
    void tesB155_requisitosBasicosConError() {
        verificar(CASOS.get(7));
    }

    // RevocacionMandatoPAC.java:195-201: CSC collaborator error returned, nothing updated
    @Test
    void tesB156_revocacionCscConErroresDevuelveSusMensajesSinActualizar() {
        verificar(CASOS.get(8));
    }

    // RevocacionMandatoPAC.java:257-264: SAAPV collaborator error returned
    @Test
    void tesB157_cambioEstadoSaapvConErroresDevuelveSusMensajes() {
        verificar(CASOS.get(9));
    }

    // RevocacionMandatoPAC.java:185-186: unknown mandate id: NullPointerException
    @Test
    void tesB158_mandatoInexistenteLanzaNullPointerException() {
        verificar(CASOS.get(10));
    }

    // RevocacionMandatoPAC.java:184-226: three mandates: one message per mandate
    @Test
    void tesB159_tresMandatosCscSaapvYAmbos() {
        verificar(CASOS.get(11));
    }

}
