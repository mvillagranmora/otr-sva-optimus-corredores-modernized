package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.cloper.ActualizarTransaccionService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.cloper.ActualizarTransaccion} (entry point call(conn, json)) as {@link ActualizarTransaccionService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-760 .. TES-A-779} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ActualizarTransaccionServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-760_atr-sin-transacciones-en-la-fecha-informa-que-no-hay",
            "TES-A-761_atr-sin-parametros-de-conexion-a-cloper-es-error",
            "TES-A-762_atr-cloper-sin-respuesta-es-error-de-consulta",
            "TES-A-763_atr-cloper-solo-con-filas-NO-EXISTE-informa-que-no-hay-palos",
            "TES-A-764_atr-calce-con-liquidacion-bilateral-B-marca-la-camara-y-EsBilateral-S",
            "TES-A-765_atr-liquidacion-M-con-estado-RZ-es-bilateral",
            "TES-A-766_atr-liquidacion-M-con-estado-E-es-bilateral",
            "TES-A-767_atr-liquidacion-M-con-otro-estado-no-es-bilateral",
            "TES-A-768_atr-otro-tipo-de-liquidacion-no-es-bilateral",
            "TES-A-769_atr-sin-calces-informa-que-no-hay-calces",
            "TES-A-770_atr-cantidad-distinta-no-calza",
            "TES-A-771_atr-monto-distinto-no-calza",
            "TES-A-772_atr-nemotecnico-y-folio-se-comparan-sin-distinguir-mayusculas-y-camara-se-recorta",
            "TES-A-773_atr-folio-informado-filtra-las-transacciones",
            "TES-A-774_atr-folio-cero-o-signo-de-pregunta-no-filtra",
            "TES-A-775_atr-folio-ausente-no-filtra",
            "TES-A-776_atr-dos-transacciones-solo-una-calza",
            "TES-A-777_atr-sin-corredora-configurada-consulta-cloper-con-usuario-vacio",
            "TES-A-778_atr-solo-considera-las-transacciones-de-la-fecha-y-empresa",
            "TES-A-779_atr-parametro-de-conexion-vacio-hace-que-cloper-no-responda");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ActualizarTransaccionService service = new ActualizarTransaccionService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ActualizarTransaccion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-760 atr sin transacciones en la fecha informa que no hay")
    void tesA760_atrSinTransaccionesEnLaFechaInformaQueNoHay() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-761 atr sin parametros de conexion a cloper es error")
    void tesA761_atrSinParametrosDeConexionACloperEsError() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-762 atr cloper sin respuesta es error de consulta")
    void tesA762_atrCloperSinRespuestaEsErrorDeConsulta() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-763 atr cloper solo con filas NO EXISTE informa que no hay palos")
    void tesA763_atrCloperSoloConFilasNOEXISTEInformaQueNoHayPalos() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-764 atr calce con liquidacion bilateral B marca la camara y EsBilateral S")
    void tesA764_atrCalceConLiquidacionBilateralBMarcaLaCamaraYEsBilateralS() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-765 atr liquidacion M con estado RZ es bilateral")
    void tesA765_atrLiquidacionMConEstadoRZEsBilateral() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-766 atr liquidacion M con estado E es bilateral")
    void tesA766_atrLiquidacionMConEstadoEEsBilateral() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-767 atr liquidacion M con otro estado no es bilateral")
    void tesA767_atrLiquidacionMConOtroEstadoNoEsBilateral() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-768 atr otro tipo de liquidacion no es bilateral")
    void tesA768_atrOtroTipoDeLiquidacionNoEsBilateral() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-769 atr sin calces informa que no hay calces")
    void tesA769_atrSinCalcesInformaQueNoHayCalces() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-770 atr cantidad distinta no calza")
    void tesA770_atrCantidadDistintaNoCalza() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-771 atr monto distinto no calza")
    void tesA771_atrMontoDistintoNoCalza() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-772 atr nemotecnico y folio se comparan sin distinguir mayusculas y camara se recorta")
    void tesA772_atrNemotecnicoYFolioSeComparanSinDistinguirMayusculasYCamaraSeRecorta() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-773 atr folio informado filtra las transacciones")
    void tesA773_atrFolioInformadoFiltraLasTransacciones() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-774 atr folio cero o signo de pregunta no filtra")
    void tesA774_atrFolioCeroOSignoDePreguntaNoFiltra() throws Exception {
        verificar(CASOS.get(14));
    }

    @Test
    @DisplayName("TES-A-775 atr folio ausente no filtra")
    void tesA775_atrFolioAusenteNoFiltra() throws Exception {
        verificar(CASOS.get(15));
    }

    @Test
    @DisplayName("TES-A-776 atr dos transacciones solo una calza")
    void tesA776_atrDosTransaccionesSoloUnaCalza() throws Exception {
        verificar(CASOS.get(16));
    }

    @Test
    @DisplayName("TES-A-777 atr sin corredora configurada consulta cloper con usuario vacio")
    void tesA777_atrSinCorredoraConfiguradaConsultaCloperConUsuarioVacio() throws Exception {
        verificar(CASOS.get(17));
    }

    @Test
    @DisplayName("TES-A-778 atr solo considera las transacciones de la fecha y empresa")
    void tesA778_atrSoloConsideraLasTransaccionesDeLaFechaYEmpresa() throws Exception {
        verificar(CASOS.get(18));
    }

    @Test
    @DisplayName("TES-A-779 atr parametro de conexion vacio hace que cloper no responda")
    void tesA779_atrParametroDeConexionVacioHaceQueCloperNoResponda() throws Exception {
        verificar(CASOS.get(19));
    }
}
