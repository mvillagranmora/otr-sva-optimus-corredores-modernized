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
 * Characterization of legacy {@code cl.bcs.APV.CrearOrdenTraspaso} (entry point {@code call(conn, json)}) through the whole modern path:
 * legacy JSON, adapter, REAL use case {@code CrearOrdenesTraspaso} on H2, adapter, legacy JSON. The expected values are RECORDED by executing the unmodified
 * legacy class (legacy-oracle/record.sh); nothing is computed here. Goldens: {@code src/test/resources/apv-b/cases/APV-B-NN_*}.
 * Every method name starts with its behaviour id (BUSINESS_RULES.md has no rule of this module); the comment cites the legacy file:line it pins.
 */
@ExtendWith(ZonaUtc.class)
class CrearOrdenTraspasoTest {

    static final List<String> CASOS = List.of(
            "APV-B-18_orden-crea-dos-relaciones-mensajes-con-primer-id",
            "APV-B-19_orden-traspaso-cero-informa-error-de-datos",
            "APV-B-20_orden-mezcla-valida-e-invalida",
            "APV-B-21_orden-sin-filas-pide-cero-ids",
            "APV-B-22_orden-traspaso-nulo-lanza-NullPointerException",
            "APV-B-23_orden-id-duplicado-lanza-excepcion-sql");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearOrdenTraspaso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // CrearOrdenTraspaso.java:103-127: ids are asked for the whole list, but EVERY message carries the first one (get(0)); the rows take their own ids from the database sequence (OrdenTraspaso_Insert.sql NEXTVAL)
    @Test
    void apvB18_ordenCreaDosRelacionesMensajesConPrimerId() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(0));
    }

    // CrearOrdenTraspaso.java:106,130-138: IdTraspaso 0: ERROR_DATOS, id 1, counter 1
    @Test
    void apvB19_ordenTraspasoCeroInformaErrorDeDatos() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(1));
    }

    // CrearOrdenTraspaso.java:106-138: a valid and an invalid row: OK then ERROR_DATOS
    @Test
    void apvB20_ordenMezclaValidaEInvalida() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(2));
    }

    // CrearOrdenTraspaso.java:103: empty list: getSecuencia is asked for 0 ids, no messages, counter 0
    @Test
    void apvB21_ordenSinFilasPideCeroIds() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(3));
    }

    // CrearOrdenTraspaso.java:106: IdTraspaso null: unboxing NullPointerException
    @Test
    void apvB22_ordenTraspasoNuloLanzaNullPointerException() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(4));
    }

    // CrearOrdenTraspaso.java:115: the INSERT violates the primary key (id 1 already used by the sequence): the SQLException is not caught
    @Test
    void apvB23_ordenIdDuplicadoLanzaExcepcionSql() {
        Equivalencia.verificar(EJECUTADOS, CASOS.get(5));
    }
}
