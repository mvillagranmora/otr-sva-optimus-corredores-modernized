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
 * Characterization of legacy {@code cl.bcs.APV.CrearTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CrearTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CrearTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-15_crear-insert-sin-campos-responde-error",
            "APV-B-16_crear-sin-detalle-falla-igual",
            "APV-B-17_crear-fecha-sistema-sin-configurar-lanza-excepcion");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearTraspaso.java:138-160,210-221: Traspaso_Insert.sql binds :FechaSuscripcion which Traspaso lacks (so the INSERT never runs): the catch answers ERROR whose text is the SQL layer message (Map Object failed + query + parameters)
    @Test
    void apvB15_crearInsertSinCamposRespondeError() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CrearTraspaso.java:138-160: without details the result is the same, the traspaso INSERT fails first
    @Test
    void apvB16_crearSinDetalleFallaIgual() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CrearTraspaso.java:133: PLATAFORMA/PLA.FechaSistema is read before the try: missing parameter, Exception
    @Test
    void apvB17_crearFechaSistemaSinConfigurarLanzaExcepcion() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }
}
