package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.CrearComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.javacall.CrearComprobanteProcedimiento;
import cl.bcs.tesoreria.javacall.CrearComprobanteJavaService;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.CrearComprobante} (entry point call(conn, json)) as {@link CrearComprobanteService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Compiled against the CURRENT modern service; a red test is an item of the Step 2 fix list. Collaborator traces (expected.json "colaboradores")
 * need the {@code Colaboradores} port: see src/test-pending/tesoreria-a/ColaboradoresPendingTest.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-250 .. TES-A-257} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class CrearComprobanteServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-250_cc-con-USARCREARCOMPROBANTEJAVA-S-delega-en-el-servicio-java",
            "TES-A-251_cc-delegacion-java-copia-las-formas-de-pago-de-entrada",
            "TES-A-252_cc-delegacion-java-propaga-los-errores",
            "TES-A-253_cc-con-USARCREARCOMPROBANTEJAVA-N-llama-al-procedimiento-y-mapea-sus-salidas",
            "TES-A-254_cc-sin-el-parametro-usa-el-procedimiento",
            "TES-A-255_cc-procedimiento-recibe-formas-de-pago-y-comprobantes-y-limpia-las-globales",
            "TES-A-256_cc-procedimiento-sin-salidas-devuelve-resultado-vacio",
            "TES-A-257_cc-delegacion-java-simulacion-no-inserta");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        CrearComprobanteProcedimiento procedimiento = new CrearComprobanteProcedimiento(caso.jdbc(), d.colaboradores());
        CrearComprobanteJavaService java = new CrearComprobanteJavaService(caso.jdbc(), d.diccionario(), procedimiento, d.colaboradores());
        CrearComprobanteService service = new CrearComprobanteService(d.configuracion(), procedimiento, java);
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("CrearComprobante");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-250 cc con USARCREARCOMPROBANTEJAVA S delega en el servicio java")
    void tesA250_ccConUSARCREARCOMPROBANTEJAVASDelegaEnElServicioJava() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-251 cc delegacion java copia las formas de pago de entrada")
    void tesA251_ccDelegacionJavaCopiaLasFormasDePagoDeEntrada() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-252 cc delegacion java propaga los errores")
    void tesA252_ccDelegacionJavaPropagaLosErrores() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-253 cc con USARCREARCOMPROBANTEJAVA N llama al procedimiento y mapea sus salidas")
    void tesA253_ccConUSARCREARCOMPROBANTEJAVANLlamaAlProcedimientoYMapeaSusSalidas() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-254 cc sin el parametro usa el procedimiento")
    void tesA254_ccSinElParametroUsaElProcedimiento() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-255 cc procedimiento recibe formas de pago y comprobantes y limpia las globales")
    void tesA255_ccProcedimientoRecibeFormasDePagoYComprobantesYLimpiaLasGlobales() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-256 cc procedimiento sin salidas devuelve resultado vacio")
    void tesA256_ccProcedimientoSinSalidasDevuelveResultadoVacio() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-257 cc delegacion java simulacion no inserta")
    void tesA257_ccDelegacionJavaSimulacionNoInserta() throws Exception {
        verificar(CASOS.get(7));
    }
}
