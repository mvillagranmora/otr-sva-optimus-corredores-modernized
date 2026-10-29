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
 * Characterization of legacy {@code cl.bcs.APV.AnularTraspasoEgreso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code AnularTraspasoEgreso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class AnularTraspasoEgresoTest {

    static final List<String> CASOS = List.of(
            "APV-B-28_anular-anula-traspasos-detalles-y-reactiva-contratos",
            "APV-B-29_anular-registra-log-de-validos-e-invalidos",
            "APV-B-30_anular-sin-validos-informa-error-y-registra-log",
            "APV-B-31_anular-contrato-otro-tipo-o-cliente-no-se-reactiva",
            "APV-B-32_anular-traspaso-inexistente-responde-sin-error",
            "APV-B-33_anular-invalidos-nulos-lanza-NPE-y-revierte",
            "APV-B-34_anular-sin-validos-ni-invalidos-lanza-NPE",
            "APV-B-35_anular-log-fecha-sistema-sin-configurar-lanza-excepcion",
            "APV-B-36_anular-agregar-log-lanza-excepcion-y-revierte");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AnularTraspasoEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // AnularTraspasoEgreso.java:88-119: traspasos and their origin details go to ANU and the TRA contracts of the same client go back to VIG; counter 0
    @Test
    void apvB28_anularAnulaTraspasosDetallesYReactivaContratos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // AnularTraspasoEgreso.java:146-190: AgregarLog receives one log per valid (TRASPASOEGRESOVALIDO) and per invalid (TRASPASOEGRESOINVALIDO) traspaso
    @Test
    void apvB29_anularRegistraLogDeValidosEInvalidos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // AnularTraspasoEgreso.java:122-135: no valid traspaso: ERROR (empty code, id 0) and counter 1, nothing is annulled but the invalid ones are still logged
    @Test
    void apvB30_anularSinValidosInformaErrorYRegistraLog() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // AnularTraspasoEgreso.java:111: the contract is reactivated only when it is TRA and has the traspaso's client (ReactivarContratoTraspasosEgreso.sql)
    @Test
    void apvB31_anularContratoOtroTipoOClienteNoSeReactiva() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // AnularTraspasoEgreso.java:88-119: an unknown traspaso id changes nothing and still answers success
    @Test
    void apvB32_anularTraspasoInexistenteRespondeSinError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // AnularTraspasoEgreso.java:121: p_TraspasosInValidos null: the log line dereferences it after the updates: NullPointerException, the transaction is rolled back
    @Test
    void apvB33_anularInvalidosNulosLanzaNPEYRevierte() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // AnularTraspasoEgreso.java:167: no valid traspasos and invalid list null: agregarLogs iterates the null list: NullPointerException
    @Test
    void apvB34_anularSinValidosNiInvalidosLanzaNPE() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }

    // AnularTraspasoEgreso.java:147: PLATAFORMA/PLA.FechaSistema missing when the log is built: Exception, the updates are rolled back
    @Test
    void apvB35_anularLogFechaSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(7));
    }

    // AnularTraspasoEgreso.java:184-185: AgregarLog throws: the Exception propagates and the updates are rolled back
    @Test
    void apvB36_anularAgregarLogLanzaExcepcionYRevierte() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(8));
    }
}
