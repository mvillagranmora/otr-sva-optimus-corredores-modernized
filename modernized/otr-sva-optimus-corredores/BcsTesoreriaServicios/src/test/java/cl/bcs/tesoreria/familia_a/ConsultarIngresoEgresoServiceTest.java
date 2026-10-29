package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ConsultarIngresoEgresoService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConsultarIngresoEgreso} (entry point call(conn, json)) as {@link ConsultarIngresoEgresoService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-630 .. TES-A-643} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarIngresoEgresoServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-630_cie2-sin-restriccion-de-cartera-consulta-todos-los-portafolios-del-rango",
            "TES-A-631_cie2-con-portafolio-informado-solo-consulta-ese-portafolio",
            "TES-A-632_cie2-con-restriccion-de-cartera-solo-consulta-los-portafolios-del-ejecutivo",
            "TES-A-633_cie2-con-restriccion-de-cartera-sin-portafolios-no-devuelve-nada",
            "TES-A-634_cie2-filtra-por-estado-y-tipo-de-movimiento",
            "TES-A-635_cie2-filtra-por-concepto-y-moneda",
            "TES-A-636_cie2-filtra-por-ingreso-automatico-y-caja",
            "TES-A-637_cie2-filtra-por-portafolio-titular-del-pago",
            "TES-A-638_cie2-caja-negativa-se-ignora-como-filtro",
            "TES-A-639_cie2-rango-de-fechas-acota-el-resultado",
            "TES-A-640_cie2-el-limite-superior-del-rango-es-inclusivo",
            "TES-A-641_cie2-sin-movimientos-devuelve-lista-vacia",
            "TES-A-642_cie2-estado-ausente-usa-el-valor-por-defecto-vacio-y-lo-ignora",
            "TES-A-643_cie2-campos-nulos-de-la-vista-se-devuelven-con-valores-por-defecto");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_RESULTADO"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConsultarIngresoEgresoService service = new ConsultarIngresoEgresoService(caso.jdbc(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarIngresoEgreso");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-630 cie2 sin restriccion de cartera consulta todos los portafolios del rango")
    void tesA630_cie2SinRestriccionDeCarteraConsultaTodosLosPortafoliosDelRango() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-631 cie2 con portafolio informado solo consulta ese portafolio")
    void tesA631_cie2ConPortafolioInformadoSoloConsultaEsePortafolio() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-632 cie2 con restriccion de cartera solo consulta los portafolios del ejecutivo")
    void tesA632_cie2ConRestriccionDeCarteraSoloConsultaLosPortafoliosDelEjecutivo() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-633 cie2 con restriccion de cartera sin portafolios no devuelve nada")
    void tesA633_cie2ConRestriccionDeCarteraSinPortafoliosNoDevuelveNada() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-634 cie2 filtra por estado y tipo de movimiento")
    void tesA634_cie2FiltraPorEstadoYTipoDeMovimiento() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-635 cie2 filtra por concepto y moneda")
    void tesA635_cie2FiltraPorConceptoYMoneda() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-636 cie2 filtra por ingreso automatico y caja")
    void tesA636_cie2FiltraPorIngresoAutomaticoYCaja() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-637 cie2 filtra por portafolio titular del pago")
    void tesA637_cie2FiltraPorPortafolioTitularDelPago() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-638 cie2 caja negativa se ignora como filtro")
    void tesA638_cie2CajaNegativaSeIgnoraComoFiltro() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-639 cie2 rango de fechas acota el resultado")
    void tesA639_cie2RangoDeFechasAcotaElResultado() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-640 cie2 el limite superior del rango es inclusivo")
    void tesA640_cie2ElLimiteSuperiorDelRangoEsInclusivo() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-641 cie2 sin movimientos devuelve lista vacia")
    void tesA641_cie2SinMovimientosDevuelveListaVacia() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-642 cie2 estado ausente usa el valor por defecto vacio y lo ignora")
    void tesA642_cie2EstadoAusenteUsaElValorPorDefectoVacioYLoIgnora() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-643 cie2 campos nulos de la vista se devuelven con valores por defecto")
    void tesA643_cie2CamposNulosDeLaVistaSeDevuelvenConValoresPorDefecto() throws Exception {
        verificar(CASOS.get(13));
    }
}
