package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.ConsultarFormatoArchivoService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConsultarFormatoArchivo} as {@code ConsultarFormatoArchivoService#execute(JsonObject)}:
 * returns the header of a bank payment-file format with its field layout, stamped with the caller's company and user.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-194..198}. The legacy typed method {@code logica} returns a LIST (golden = JSON array); the
 * entry point {@code call} tries to turn that list into a JSON OBJECT and therefore always throws (legacy defect, TES-B-197).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarFormatoArchivoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-194_formato-con-detalles-devuelve-lista-con-empresa-y-usuario",
            "TES-B-195_formato-sin-detalles-devuelve-detalles-vacios",
            "TES-B-196_formato-inexistente-devuelve-lista-vacia",
            "TES-B-197_punto-de-entrada-call-lanza-IllegalStateException-por-devolver-un-arreglo-como-objeto",
            "TES-B-198_empresa-y-usuario-nulos-se-copian-como-nulos");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            ConsultarFormatoArchivoService servicio = new ConsultarFormatoArchivoService(caso.jdbc());
            // the cases record either the typed entry (logica: a list of formats) or the JavaCall entry (call)
            return "logica".equals(caso.caso().get("metodo").getAsString()) ? servicio.consultar(caso.entrada().deepCopy()) : servicio.execute(caso.entrada().deepCopy());
        });
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarFormatoArchivo");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ConsultarFormatoArchivo.java:83-95: found format: one header with its detail rows and the company/user of the request
    @Test
    void tesB194_formatoConDetallesDevuelveListaConEmpresaYUsuario() {
        verificar(CASOS.get(0));
    }

    // ConsultarFormatoArchivo.java:90-92: a format without detail rows returns an empty Detalles list
    @Test
    void tesB195_formatoSinDetallesDevuelveDetallesVacios() {
        verificar(CASOS.get(1));
    }

    // ConsultarFormatoArchivo.java:87-89: unknown format: empty list
    @Test
    void tesB196_formatoInexistenteDevuelveListaVacia() {
        verificar(CASOS.get(2));
    }

    // ConsultarFormatoArchivo.java:75-81: legacy defect: call() converts the list to JSON and asks for an OBJECT: IllegalStateException
    @Test
    void tesB197_callLanzaIllegalStateExceptionPorDevolverUnArregloComoObjeto() {
        verificar(CASOS.get(3));
    }

    // ConsultarFormatoArchivo.java:93-94: company and user are copied as they come (null)
    @Test
    void tesB198_empresaYUsuarioNulosSeCopianComoNulos() {
        verificar(CASOS.get(4));
    }

}
