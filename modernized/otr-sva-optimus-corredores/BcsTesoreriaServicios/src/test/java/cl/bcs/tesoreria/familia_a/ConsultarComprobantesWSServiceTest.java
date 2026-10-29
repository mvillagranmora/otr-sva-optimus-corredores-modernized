package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ConsultarComprobantesWSService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConsultarComprobantesWS} (entry point call(conn, json)) as {@link ConsultarComprobantesWSService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-610 .. TES-A-628} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ConsultarComprobantesWSServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-610_ccw-rango-valido-devuelve-comprobantes-con-detalle-y-formas-de-pago",
            "TES-A-611_ccw-sin-comprobantes-en-el-rango-informa-que-no-existen",
            "TES-A-612_ccw-rango-de-31-dias-excede-el-maximo-por-defecto-de-30",
            "TES-A-613_ccw-rango-de-30-dias-es-el-maximo-por-defecto-y-se-acepta",
            "TES-A-614_ccw-parametro-de-rango-configurado-en-5-rechaza-un-rango-de-6-dias",
            "TES-A-615_ccw-parametro-de-rango-configurado-en-5-acepta-5-dias",
            "TES-A-616_ccw-parametro-de-rango-no-numerico-usa-30",
            "TES-A-617_ccw-fecha-de-inicio-posterior-a-la-de-termino-es-rango-no-valido",
            "TES-A-618_ccw-un-solo-dia-es-un-rango-valido",
            "TES-A-619_ccw-sin-fechas-es-fecha-de-liquidacion-no-valida",
            "TES-A-620_ccw-solo-fecha-de-inicio-es-fecha-de-liquidacion-no-valida",
            "TES-A-621_ccw-filtra-por-tipo-de-movimiento",
            "TES-A-622_ccw-filtra-por-documento-de-identidad-y-codigo-de-portafolio",
            "TES-A-623_ccw-filtra-por-estado-concepto-moneda-y-caja",
            "TES-A-624_ccw-filtra-por-tipo-de-socio-de-negocio",
            "TES-A-625_ccw-filtra-por-medio-de-pago-bancos-alias-y-cuenta-de-la-corredora",
            "TES-A-626_ccw-filtros-que-no-coinciden-informan-que-no-existen",
            "TES-A-627_ccw-comprobante-sin-detalle-ni-forma-de-pago-devuelve-listas-vacias",
            "TES-A-628_ccw-el-portafolio-sin-codigo-no-coincide-con-el-filtro");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {"P_OUT_COMPROBANTES", "DetalleComprobante", "FormaDePagoComprobante"};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConsultarComprobantesWSService service = new ConsultarComprobantesWSService(caso.jdbc(), d.configuracion(), d.diccionario());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConsultarComprobantesWS");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-610 ccw rango valido devuelve comprobantes con detalle y formas de pago")
    void tesA610_ccwRangoValidoDevuelveComprobantesConDetalleYFormasDePago() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-611 ccw sin comprobantes en el rango informa que no existen")
    void tesA611_ccwSinComprobantesEnElRangoInformaQueNoExisten() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-612 ccw rango de 31 dias excede el maximo por defecto de 30")
    void tesA612_ccwRangoDe31DiasExcedeElMaximoPorDefectoDe30() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-613 ccw rango de 30 dias es el maximo por defecto y se acepta")
    void tesA613_ccwRangoDe30DiasEsElMaximoPorDefectoYSeAcepta() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-614 ccw parametro de rango configurado en 5 rechaza un rango de 6 dias")
    void tesA614_ccwParametroDeRangoConfiguradoEn5RechazaUnRangoDe6Dias() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-615 ccw parametro de rango configurado en 5 acepta 5 dias")
    void tesA615_ccwParametroDeRangoConfiguradoEn5Acepta5Dias() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-616 ccw parametro de rango no numerico usa 30")
    void tesA616_ccwParametroDeRangoNoNumericoUsa30() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-617 ccw fecha de inicio posterior a la de termino es rango no valido")
    void tesA617_ccwFechaDeInicioPosteriorALaDeTerminoEsRangoNoValido() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-618 ccw un solo dia es un rango valido")
    void tesA618_ccwUnSoloDiaEsUnRangoValido() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-619 ccw sin fechas es fecha de liquidacion no valida")
    void tesA619_ccwSinFechasEsFechaDeLiquidacionNoValida() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-620 ccw solo fecha de inicio es fecha de liquidacion no valida")
    void tesA620_ccwSoloFechaDeInicioEsFechaDeLiquidacionNoValida() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-621 ccw filtra por tipo de movimiento")
    void tesA621_ccwFiltraPorTipoDeMovimiento() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-622 ccw filtra por documento de identidad y codigo de portafolio")
    void tesA622_ccwFiltraPorDocumentoDeIdentidadYCodigoDePortafolio() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-623 ccw filtra por estado concepto moneda y caja")
    void tesA623_ccwFiltraPorEstadoConceptoMonedaYCaja() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-624 ccw filtra por tipo de socio de negocio")
    void tesA624_ccwFiltraPorTipoDeSocioDeNegocio() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-625 ccw filtra por medio de pago bancos alias y cuenta de la corredora")
    void tesA625_ccwFiltraPorMedioDePagoBancosAliasYCuentaDeLaCorredora() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-626 ccw filtros que no coinciden informan que no existen")
    void tesA626_ccwFiltrosQueNoCoincidenInformanQueNoExisten() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-627 ccw comprobante sin detalle ni forma de pago devuelve listas vacias")
    void tesA627_ccwComprobanteSinDetalleNiFormaDePagoDevuelveListasVacias() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-628 ccw el portafolio sin codigo no coincide con el filtro")
    void tesA628_ccwElPortafolioSinCodigoNoCoincideConElFiltro() throws Exception {
        verificar(CASOS.get(18));
    }
}
