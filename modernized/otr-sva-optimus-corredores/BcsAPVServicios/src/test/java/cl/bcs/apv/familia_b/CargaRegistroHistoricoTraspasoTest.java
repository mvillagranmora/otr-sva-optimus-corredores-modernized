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
 * Characterization of legacy {@code cl.bcs.APV.CargaRegistroHistoricoTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CargarHistoricoTraspasos} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CargaRegistroHistoricoTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-24_hist-registra-cabecera-y-xml-sin-mensajes",
            "APV-B-25_hist-campos-opcionales-nulos-se-guardan",
            "APV-B-26_hist-empresa-no-numerica-lanza-NumberFormatException",
            "APV-B-27_hist-fecha-sistema-sin-configurar-lanza-excepcion");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CargaRegistroHistoricoTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CargaRegistroHistoricoTraspaso.java:105-146,162: header row (type 5, 0 records, VIG, date = system date) and XML row inserted; no messages, the counter is never set
    @Test
    void apvB24_histRegistraCabeceraYXmlSinMensajes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CargaRegistroHistoricoTraspaso.java:111-146: optional text fields null are stored as NULL
    @Test
    void apvB25_histCamposOpcionalesNulosSeGuardan() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CargaRegistroHistoricoTraspaso.java:105: Long.valueOf(p_IdEmpresa) of a non numeric text: NumberFormatException before the try
    @Test
    void apvB26_histEmpresaNoNumericaLanzaNumberFormatException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CargaRegistroHistoricoTraspaso.java:105: PLATAFORMA/PLA.FechaSistema missing: Exception before the try
    @Test
    void apvB27_histFechaSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }
}
