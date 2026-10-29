package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.AutorizacionLiquidacionService;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.AutorizacionLiquidacion} (entry point call(conn, json)) as {@link AutorizacionLiquidacionService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-360 .. TES-A-375} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class AutorizacionLiquidacionServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-360_aul-un-solo-evento-liquida-el-comprobante-pero-la-salida-nunca-informa-exito-defecto",
            "TES-A-361_aul-dos-eventos-el-otro-vigente-advierte-que-falta-autorizar",
            "TES-A-362_aul-dos-eventos-el-otro-autorizado-y-comprobante-no-liquidado-liquida",
            "TES-A-363_aul-dos-eventos-el-otro-autorizado-y-comprobante-ya-liquidado-no-hace-nada",
            "TES-A-364_aul-liquidacion-con-error-de-validacion-devuelve-los-mensajes-de-liquidacion",
            "TES-A-365_aul-sin-eventos-registrados-liquida-igualmente",
            "TES-A-366_aul-numero-de-operacion-no-numerico-se-traga-el-error-de-formato",
            "TES-A-367_aul-sin-fecha-de-sistema-configurada-informa-error-generico",
            "TES-A-368_aul-liquidacion-con-requisito-que-exige-autorizacion-informa-la-autorizacion",
            "TES-A-370_aul-rechazo-con-otro-evento-vigente-rechaza-sus-etapas-y-devuelve-el-comprobante-a-ING",
            "TES-A-371_aul-rechazo-sin-otros-eventos-solo-devuelve-el-comprobante-a-ING",
            "TES-A-372_aul-rechazo-de-comprobante-que-no-esta-en-PDA-no-lo-modifica",
            "TES-A-373_aul-rechazo-con-numero-no-numerico-lanza-NumberFormatException",
            "TES-A-374_aul-rechazo-con-otro-evento-autorizado-tambien-lo-rechaza",
            "TES-A-375_aul-rechazo-de-lista-vacia-no-hace-nada");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        AutorizacionLiquidacionService service = new AutorizacionLiquidacionService(caso.jdbc(), d.configuracion(), d.reloj(),
                new LiquidarComprobanteService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores()));
        String metodo = caso.metodo();
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "autorizarEvento".equals(metodo) ? service.autorizarEvento(caso.entrada().deepCopy()) : service.rechazarEvento(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("AutorizacionLiquidacion");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-360 aul un solo evento liquida el comprobante pero la salida nunca informa exito defecto")
    void tesA360_aulUnSoloEventoLiquidaElComprobantePeroLaSalidaNuncaInformaExitoDefecto() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-361 aul dos eventos el otro vigente advierte que falta autorizar")
    void tesA361_aulDosEventosElOtroVigenteAdvierteQueFaltaAutorizar() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-362 aul dos eventos el otro autorizado y comprobante no liquidado liquida")
    void tesA362_aulDosEventosElOtroAutorizadoYComprobanteNoLiquidadoLiquida() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-363 aul dos eventos el otro autorizado y comprobante ya liquidado no hace nada")
    void tesA363_aulDosEventosElOtroAutorizadoYComprobanteYaLiquidadoNoHaceNada() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-364 aul liquidacion con error de validacion devuelve los mensajes de liquidacion")
    void tesA364_aulLiquidacionConErrorDeValidacionDevuelveLosMensajesDeLiquidacion() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-365 aul sin eventos registrados liquida igualmente")
    void tesA365_aulSinEventosRegistradosLiquidaIgualmente() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-366 aul numero de operacion no numerico se traga el error de formato")
    void tesA366_aulNumeroDeOperacionNoNumericoSeTragaElErrorDeFormato() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-367 aul sin fecha de sistema configurada informa error generico")
    void tesA367_aulSinFechaDeSistemaConfiguradaInformaErrorGenerico() throws Exception {
        verificar(CASOS.get(7));
    }

    @Test
    @DisplayName("TES-A-368 aul liquidacion con requisito que exige autorizacion informa la autorizacion")
    void tesA368_aulLiquidacionConRequisitoQueExigeAutorizacionInformaLaAutorizacion() throws Exception {
        verificar(CASOS.get(8));
    }

    @Test
    @DisplayName("TES-A-370 aul rechazo con otro evento vigente rechaza sus etapas y devuelve el comprobante a ING")
    void tesA370_aulRechazoConOtroEventoVigenteRechazaSusEtapasYDevuelveElComprobanteAING() throws Exception {
        verificar(CASOS.get(9));
    }

    @Test
    @DisplayName("TES-A-371 aul rechazo sin otros eventos solo devuelve el comprobante a ING")
    void tesA371_aulRechazoSinOtrosEventosSoloDevuelveElComprobanteAING() throws Exception {
        verificar(CASOS.get(10));
    }

    @Test
    @DisplayName("TES-A-372 aul rechazo de comprobante que no esta en PDA no lo modifica")
    void tesA372_aulRechazoDeComprobanteQueNoEstaEnPDANoLoModifica() throws Exception {
        verificar(CASOS.get(11));
    }

    @Test
    @DisplayName("TES-A-373 aul rechazo con numero no numerico lanza NumberFormatException")
    void tesA373_aulRechazoConNumeroNoNumericoLanzaNumberFormatException() throws Exception {
        verificar(CASOS.get(12));
    }

    @Test
    @DisplayName("TES-A-374 aul rechazo con otro evento autorizado tambien lo rechaza")
    void tesA374_aulRechazoConOtroEventoAutorizadoTambienLoRechaza() throws Exception {
        verificar(CASOS.get(13));
    }

    @Test
    @DisplayName("TES-A-375 aul rechazo de lista vacia no hace nada")
    void tesA375_aulRechazoDeListaVaciaNoHaceNada() throws Exception {
        verificar(CASOS.get(14));
    }
}
