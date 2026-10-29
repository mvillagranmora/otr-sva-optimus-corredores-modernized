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
 * Characterization of legacy {@code cl.bcs.APV.CambiarEstadoTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CambiarEstadoTraspasos} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CambiarEstadoTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-08_est-cambia-estado-y-borra-cierre-solicitud",
            "APV-B-09_est-sin-traspaso-advierte",
            "APV-B-10_est-segunda-fila-hereda-resultado-de-la-primera",
            "APV-B-11_est-primera-sin-traspaso-segunda-existente",
            "APV-B-12_est-error-sql-en-una-fila-y-las-siguientes",
            "APV-B-13_est-sin-filas-informa-contador-cero",
            "APV-B-14_est-folio-nulo-lanza-NullPointerException");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CambiarEstadoTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CambiarEstadoTraspaso.java:100-116: the update matches (result[0]==1): OK CAMBIAR_ESTADO_TRASPASO; CierreSolicitud is written as NULL (line 105)
    @Test
    void apvB08_estCambiaEstadoYBorraCierreSolicitud() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CambiarEstadoTraspaso.java:117-125: no row matches: ADVERTENCIA, nothing changes
    @Test
    void apvB09_estSinTraspasoAdvierte() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CambiarEstadoTraspaso.java:98,110-111: lstTraspaso grows and is batched whole each time: result[0] is the FIRST row's result, so a second non-existent traspaso also answers OK
    @Test
    void apvB10_estSegundaFilaHeredaResultadoDeLaPrimera() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CambiarEstadoTraspaso.java:98,110-111: the reverse: the first row matches nothing, so the second (really updated to ANU) answers ADVERTENCIA
    @Test
    void apvB11_estPrimeraSinTraspasoSegundaExistente() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // CambiarEstadoTraspaso.java:126-132: an SQL error (IdEstado longer than the column) is caught: ERROR_CAMBIAR_ESTADO_TRASPASO; the failed row stays in the list so the next row fails too
    @Test
    void apvB12_estErrorSqlEnUnaFilaYLasSiguientes() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // CambiarEstadoTraspaso.java:80-86: no rows: empty messages, counter 0 (set by call from the number of ERROR messages)
    @Test
    void apvB13_estSinFilasInformaContadorCero() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }

    // CambiarEstadoTraspaso.java:123,131: IdTraspaso null: the ADVERTENCIA text and the catch text dereference it, so the NullPointerException escapes
    @Test
    void apvB14_estFolioNuloLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(6));
    }
}
