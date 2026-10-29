package cl.bcs.tesoreria.familia_a;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.javacall.ConfirmarRechazarAnularMovimientosTesoreriaService;
import cl.bcs.tesoreria.familia_a.support.Caso;
import cl.bcs.tesoreria.familia_a.support.Dobles;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.familia_a.support.Verifica;
import cl.bcs.tesoreria.familia_a.support.ZonaUtc;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ConfirmarRechazarAnularMovimientosTesoreria} (entry point call(conn, json)) as {@link ConfirmarRechazarAnularMovimientosTesoreriaService#execute(com.google.gson.JsonObject)}.
 *
 * <p>Goldens: {@code src/test/resources/tesoreria-a/cases/TES-A-340 .. TES-A-348} (recorded from the unmodified legacy).
 */
@ExtendWith(ZonaUtc.class)
class ConfirmarRechazarAnularMovimientosTesoreriaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-A-340_crm-rechaza-la-anulacion-rechaza-el-evento-y-libera-el-movimiento",
            "TES-A-341_crm-movimiento-sin-evento-es-estado-incorrecto",
            "TES-A-342_crm-un-movimiento-valido-y-otro-sin-evento-rechaza-el-evento-pero-termina-con-error",
            "TES-A-343_crm-evento-ya-rechazado-tambien-se-informa-como-rechazado",
            "TES-A-344_crm-lista-vacia-no-hace-nada",
            "TES-A-346_crm-sin-fecha-de-sistema-configurada-se-traga-la-excepcion-despues-de-actualizar",
            "TES-A-347_crm-registra-el-log-con-el-estado-INGRESADO",
            "TES-A-348_crm-metodo-logica-conserva-el-contador-del-error-tragado");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();
    private static final String[] SIN_ORDEN = {};

    private static void verificar(String nombre) throws Exception {
        Caso caso = Caso.cargar(nombre);
        Dobles d = Dobles.de(caso);
        ConfirmarRechazarAnularMovimientosTesoreriaService service = new ConfirmarRechazarAnularMovimientosTesoreriaService(caso.jdbc(), d.configuracion(), d.diccionario(), d.colaboradores());
        String metodo = caso.metodo();
        Verifica.Ejecucion r = Verifica.Ejecucion.de(caso, () -> "logica".equals(metodo)
                ? JsonLegacy.PLATAFORMA.toJsonTree(service.logica(JsonLegacy.PLATAFORMA.fromJson(caso.entrada().deepCopy(), ConfirmarRechazarAnularMovimientosTesoreriaService.EntradaConfirmarRechazarAnularMovimientosTesoreria.class)))
                : service.execute(caso.entrada().deepCopy()));
        EJECUTADOS.ejecutado(nombre);
        Verifica.comparar(caso, r, d.colaboradores().traza(), SIN_ORDEN);
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ConfirmarRechazarAnularMovimientosTesoreria");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    @Test
    @DisplayName("TES-A-340 crm rechaza la anulacion rechaza el evento y libera el movimiento")
    void tesA340_crmRechazaLaAnulacionRechazaElEventoYLiberaElMovimiento() throws Exception {
        verificar(CASOS.get(0));
    }

    @Test
    @DisplayName("TES-A-341 crm movimiento sin evento es estado incorrecto")
    void tesA341_crmMovimientoSinEventoEsEstadoIncorrecto() throws Exception {
        verificar(CASOS.get(1));
    }

    @Test
    @DisplayName("TES-A-342 crm un movimiento valido y otro sin evento rechaza el evento pero termina con error")
    void tesA342_crmUnMovimientoValidoYOtroSinEventoRechazaElEventoPeroTerminaConError() throws Exception {
        verificar(CASOS.get(2));
    }

    @Test
    @DisplayName("TES-A-343 crm evento ya rechazado tambien se informa como rechazado")
    void tesA343_crmEventoYaRechazadoTambienSeInformaComoRechazado() throws Exception {
        verificar(CASOS.get(3));
    }

    @Test
    @DisplayName("TES-A-344 crm lista vacia no hace nada")
    void tesA344_crmListaVaciaNoHaceNada() throws Exception {
        verificar(CASOS.get(4));
    }

    @Test
    @DisplayName("TES-A-346 crm sin fecha de sistema configurada se traga la excepcion despues de actualizar")
    void tesA346_crmSinFechaDeSistemaConfiguradaSeTragaLaExcepcionDespuesDeActualizar() throws Exception {
        verificar(CASOS.get(5));
    }

    @Test
    @DisplayName("TES-A-347 crm registra el log con el estado INGRESADO")
    void tesA347_crmRegistraElLogConElEstadoINGRESADO() throws Exception {
        verificar(CASOS.get(6));
    }

    @Test
    @DisplayName("TES-A-348 crm metodo logica conserva el contador del error tragado")
    void tesA348_crmMetodoLogicaConservaElContadorDelErrorTragado() throws Exception {
        verificar(CASOS.get(7));
    }
}
