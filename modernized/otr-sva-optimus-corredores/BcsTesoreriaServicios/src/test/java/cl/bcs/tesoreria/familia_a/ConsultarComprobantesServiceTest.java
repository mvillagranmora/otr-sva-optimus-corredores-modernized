package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ConsultarComprobantesService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConsultarComprobantes} (entry point call(conn, json)) as {@link ConsultarComprobantesService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-600 .. TES-A-605} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarComprobantesServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-600_ccs-devuelve-los-comprobantes-pedidos-con-sus-datos-de-consulta",
            "TES-A-601_ccs-comprobante-inexistente-devuelve-lista-vacia",
            "TES-A-602_ccs-lista-vacia-no-devuelve-nada",
            "TES-A-603_ccs-lista-ausente-lanza-NPE",
            "TES-A-604_ccs-un-id-repetido-devuelve-una-sola-fila",
            "TES-A-605_ccs-un-comprobante-con-varias-filas-en-la-vista-devuelve-todas");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_COMPROBANTES"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConsultarComprobantesService service = new ConsultarComprobantesService(caso.jdbc());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarComprobantes");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-600 ccs devuelve los comprobantes pedidos con sus datos de consulta")
    void tesA600_ccsDevuelveLosComprobantesPedidosConSusDatosDeConsulta() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-601 ccs comprobante inexistente devuelve lista vacia")
    void tesA601_ccsComprobanteInexistenteDevuelveListaVacia() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-602 ccs lista vacia no devuelve nada")
    void tesA602_ccsListaVaciaNoDevuelveNada() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-603 ccs lista ausente lanza NPE")
    void tesA603_ccsListaAusenteLanzaNPE() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-604 ccs un id repetido devuelve una sola fila")
    void tesA604_ccsUnIdRepetidoDevuelveUnaSolaFila() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-605 ccs un comprobante con varias filas en la vista devuelve todas")
    void tesA605_ccsUnComprobanteConVariasFilasEnLaVistaDevuelveTodas() throws Exception {
        verificar(CASOS.get(5));
    }
}
