package cl.bcs.tesoreria.jobs.job;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.jobs.support.Caso;
import cl.bcs.tesoreria.jobs.support.Canon;
import cl.bcs.tesoreria.jobs.support.Equivalencia;
import cl.bcs.tesoreria.jobs.support.Observacion;
import cl.bcs.tesoreria.jobs.support.Traza;

/**
 * The two JavaCall classes of the payment-file package called directly with the legacy {@code call(con, in)} contract:
 * {@code GenerarArchivoPagos} (cases PG-Enn) and the older variant {@code GenerarArchivoPagoBancoEstado} (cases PG-Dnn). The expected
 * output, the file written (bytes) and the exception are the ones recorded from the unmodified legacy classes.
 */
class ArchivoPagoDirectoGoldenTest {

    private static final AtomicInteger EJECUTADOS = new AtomicInteger();

    static List<String> casos() {
        return Caso.todos("PG-").stream().filter(id -> id.startsWith("PG-E") || id.startsWith("PG-D")).toList();
    }

    @Test
    void inventarioDeCasos() {
        assertThat(casos()).as("direct cases on the classpath").hasSizeGreaterThanOrEqualTo(20);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("casos")
    void golden(String id) {
        Caso caso = Caso.cargar(id);
        Traza traza = new Traza(caso.colaboradores());
        JsonObject entrada = caso.caso().getAsJsonObject("entrada");
        JsonObject act = new JsonObject();
        try {
            JsonObject out = caso.job().equals("Pagos") ? new GenerarArchivoPagos(caso.bd().jdbc()).call(entrada)
                    : new GenerarArchivoPagoBancoEstado(caso.bd().jdbc()).call(entrada);
            act.addProperty("desenlace", "COMMIT");
            act.add("salida", out);
        } catch (Throwable t) {
            JsonObject ex = new JsonObject();
            ex.addProperty("tipo", t.getClass().getSimpleName());
            ex.addProperty("mensaje", Canon.mensajeDe(t, caso.rutaBase()));
            act.addProperty("desenlace", "ERROR");
            act.add("excepcion", ex);
        }
        Equivalencia.comparar(caso, Observacion.completar(caso, act, traza));
        EJECUTADOS.incrementAndGet();
    }

    @AfterAll
    static void contar() {
        System.out.println("equivalence cases executed (GenerarArchivoPagos / GenerarArchivoPagoBancoEstado): " + EJECUTADOS.get() + "/" + casos().size());
    }
}
