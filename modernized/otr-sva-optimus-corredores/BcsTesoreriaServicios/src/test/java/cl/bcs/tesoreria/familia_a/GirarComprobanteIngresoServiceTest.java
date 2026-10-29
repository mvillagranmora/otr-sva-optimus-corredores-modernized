package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.GirarComprobanteIngresoService;
import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.GirarComprobanteIngreso} (entry point call(conn, json)) as {@link GirarComprobanteIngresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-560 .. TES-A-573} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class GirarComprobanteIngresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-560_gci-gira-un-comprobante-actualiza-la-cuenta-de-la-corredora-y-lo-deja-GIR",
            "TES-A-561_gci-la-salida-del-procedimiento-sin-filas-no-genera-mensajes",
            "TES-A-562_gci-error-del-procedimiento-devuelve-sus-mensajes-pero-el-estado-ya-fue-cambiado",
            "TES-A-563_gci-dos-comprobantes-se-giran-juntos",
            "TES-A-564_gci-cuenta-de-la-corredora-inexistente-no-actualiza-las-formas-de-pago",
            "TES-A-565_gci-solo-actualiza-las-formas-de-pago-del-medio-indicado-y-no-las-anuladas",
            "TES-A-566_gci-las-formas-de-pago-de-la-entrada-se-agregan-a-la-agrupada",
            "TES-A-567_gci-formas-de-pago-ausentes-lanza-NPE-que-se-traga-despues-de-llenar-las-tablas-globales",
            "TES-A-568_gci-monto-ausente-lanza-NPE-que-se-traga-antes-de-girar",
            "TES-A-569_gci-sin-comprobantes-no-hace-nada-util",
            "TES-A-570_gci-con-USARCREARCOMPROBANTEJAVA-S-delega-al-servicio-java-que-llama-al-procedimiento",
            "TES-A-571_gci-comprobante-inexistente-no-modifica-nada",
            "TES-A-572_gci-simular-S-se-pasa-al-procedimiento-y-el-giro-se-ejecuta-igual",
            "TES-A-573_gci-comprobante-ya-girado-vuelve-a-quedar-GIR");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static CrearComprobanteService crearComprobante(Caso caso, Dobles d) {
        CrearComprobanteProcedimiento procedimiento = new CrearComprobanteProcedimiento(caso.jdbc(), d.colaboradores());
        CrearComprobanteJavaService java = new CrearComprobanteJavaService(caso.jdbc(), d.diccionario(), procedimiento, d.colaboradores());
        return new CrearComprobanteService(d.configuracion(), procedimiento, java);
    }

    private static void verificar(String nombre) {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        GirarComprobanteIngresoService service = new GirarComprobanteIngresoService(caso.jdbc(), crearComprobante(caso, d));
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("GirarComprobanteIngreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-560 gci gira un comprobante actualiza la cuenta de la corredora y lo deja GIR")
    void tesA560_gciGiraUnComprobanteActualizaLaCuentaDeLaCorredoraYLoDejaGIR() {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-561 gci la salida del procedimiento sin filas no genera mensajes")
    void tesA561_gciLaSalidaDelProcedimientoSinFilasNoGeneraMensajes() {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-562 gci error del procedimiento devuelve sus mensajes pero el estado ya fue cambiado")
    void tesA562_gciErrorDelProcedimientoDevuelveSusMensajesPeroElEstadoYaFueCambiado() {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-563 gci dos comprobantes se giran juntos")
    void tesA563_gciDosComprobantesSeGiranJuntos() {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-564 gci cuenta de la corredora inexistente no actualiza las formas de pago")
    void tesA564_gciCuentaDeLaCorredoraInexistenteNoActualizaLasFormasDePago() {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-565 gci solo actualiza las formas de pago del medio indicado y no las anuladas")
    void tesA565_gciSoloActualizaLasFormasDePagoDelMedioIndicadoYNoLasAnuladas() {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-566 gci las formas de pago de la entrada se agregan a la agrupada")
    void tesA566_gciLasFormasDePagoDeLaEntradaSeAgreganALaAgrupada() {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-567 gci formas de pago ausentes lanza NPE que se traga despues de llenar las tablas globales")
    void tesA567_gciFormasDePagoAusentesLanzaNPEQueSeTragaDespuesDeLlenarLasTablasGlobales() {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-568 gci monto ausente lanza NPE que se traga antes de girar")
    void tesA568_gciMontoAusenteLanzaNPEQueSeTragaAntesDeGirar() {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-569 gci sin comprobantes no hace nada util")
    void tesA569_gciSinComprobantesNoHaceNadaUtil() {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-570 gci con USARCREARCOMPROBANTEJAVA S delega al servicio java que llama al procedimiento")
    void tesA570_gciConUSARCREARCOMPROBANTEJAVASDelegaAlServicioJavaQueLlamaAlProcedimiento() {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-571 gci comprobante inexistente no modifica nada")
    void tesA571_gciComprobanteInexistenteNoModificaNada() {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-572 gci simular S se pasa al procedimiento y el giro se ejecuta igual")
    void tesA572_gciSimularSSePasaAlProcedimientoYElGiroSeEjecutaIgual() {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-573 gci comprobante ya girado vuelve a quedar GIR")
    void tesA573_gciComprobanteYaGiradoVuelveAQuedarGIR() {
        verificar(CASOS.get(13));
    }
}
