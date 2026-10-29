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
 * Characterization of legacy {@code cl.bcs.APV.ModificarOrigenDestino} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code ModificarOrigenDestinoTransicion} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class ModificarOrigenDestinoTest {

    static final List<String> CASOS = List.of(
            "APV-B-01_od-modifica-fila-y-informa-datos-modificados",
            "APV-B-02_od-fila-inexistente-informa-error",
            "APV-B-03_od-mensaje-reutilizado-ok-luego-error-ambos-error",
            "APV-B-04_od-mensaje-reutilizado-error-luego-ok-ambos-ok",
            "APV-B-05_od-sin-filas-no-informa-contador",
            "APV-B-06_od-fecha-sistema-sin-configurar-lanza-excepcion",
            "APV-B-07_od-parametros-nulos-lanza-NullPointerException");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ModificarOrigenDestino");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ModificarOrigenDestino.java:121-127: the row exists (res[0]==1): INFORMACION UI_DATOS_MODIFICADOS, counter 0; FechaModificacion is the system clock (new Date(), line 105: masked)
    @Test
    void apvB01_odModificaFilaYInformaDatosModificados() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // ModificarOrigenDestino.java:128-134: no row updated: ERROR UI_ERROR_MODIFICACION, counter 1
    @Test
    void apvB02_odFilaInexistenteInformaError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // ModificarOrigenDestino.java:93,121-134: ONE MensajeSalida instance is reused for every row: an OK followed by an error leaves both list entries as the error, counter 1
    @Test
    void apvB03_odMensajeReutilizadoOkLuegoErrorAmbosError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // ModificarOrigenDestino.java:93,121-134: the reused instance again: an error followed by an OK leaves both entries as the OK, counter 0
    @Test
    void apvB04_odMensajeReutilizadoErrorLuegoOkAmbosOk() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // ModificarOrigenDestino.java:99,136: no rows: empty message list and the counter is never set (omitted from the JSON)
    @Test
    void apvB05_odSinFilasNoInformaContador() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // ModificarOrigenDestino.java:97: PLATAFORMA/PLA.FechaSistema is read (and not used) before the loop: missing parameter, Exception
    @Test
    void apvB06_odFechaSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // ModificarOrigenDestino.java:99: p_Parametros null (the JSON says null): NullPointerException
    @Test
    void apvB07_odParametrosNulosLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }
}
