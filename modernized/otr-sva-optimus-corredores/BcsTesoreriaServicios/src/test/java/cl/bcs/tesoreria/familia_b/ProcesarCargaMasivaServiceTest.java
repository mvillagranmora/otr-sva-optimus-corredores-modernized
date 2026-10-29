package cl.bcs.tesoreria.familia_b;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.car.ProcesarCargaMasivaService;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.car.ProcesarCargaMasiva} (entry point {@code call(conn, json)}) as
 * {@code ProcesarCargaMasivaService#execute(JsonObject)}: validates the requested mass loads against their payment preferences, asks
 * CrearIngresoEgreso (and CrearComprobante for loads that generate a partida) to create the movements and marks the load and its details as processed.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-160..177} (output + CAR.CargaMasiva / CAR.CargaMasivaDetalle / staging table rows).
 * Collaborators (RevisarRequisitosBasicos, CrearIngresoEgreso, CrearComprobante) are recorded fakes; their traces are verified in
 * {@code src/test-pending/tesoreria-b/ColaboradoresPendingTest} (the modern service has no Colaboradores port yet).
 */
@ExtendWith(ZonaUtc.class)
class ProcesarCargaMasivaServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-160_carga-generica-con-comprobante-crea-ingresos-y-egresos",
            "TES-B-161_carga-con-partida-agrupa-por-tipo-moneda-medio-y-cuenta",
            "TES-B-162_sin-reglas-de-negocio-omite-validaciones",
            "TES-B-163_sin-cargas-NO_SE_INGRESARON_CARGAS",
            "TES-B-164_requisitos-basicos-con-error-devuelve-sus-mensajes",
            "TES-B-165_carga-inexistente-CARGA_MASIVA_NO_EXISTE",
            "TES-B-166_carga-ya-procesada-CARGA_MASIVA_PROCESADA",
            "TES-B-167_carga-invalida-CARGA_MASIVA_INVALIDA",
            "TES-B-168_tipo-de-carga-inexistente-TIPO_CARGA_MASIVA_NO_EXISTE",
            "TES-B-169_servipag-sin-configuracion-completa-CONFIGURACION_INVALIDA",
            "TES-B-170_conciliacion-bcp-movimiento-existente-no-se-reenvia-a-tesoreria",
            "TES-B-171_crear-ingreso-egreso-con-error-ERROR_AL_GENERAR_MOVIMIENTO",
            "TES-B-172_crear-comprobante-con-error-ERROR_AL_GENERAR_PARTIDA",
            "TES-B-173_dos-cargas-solo-la-ultima-se-valida-y-consulta",
            "TES-B-174_preferencia-sin-medio-de-pago-no-envia-forma-de-pago",
            "TES-B-175_movimiento-no-generado-deja-detalle-sin-comprobante",
            "TES-B-176_parametros-de-entrada-reemplazan-a-la-preferencia",
            "TES-B-177_fecha-de-proceso-nula-actualiza-fecha-proceso-nula");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            return new ProcesarCargaMasivaService(caso.jdbc(), new DiccionarioDeCaso(), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ProcesarCargaMasiva");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // car/ProcesarCargaMasiva.java:400-519,672-702: generic load: one ingreso/egreso per detail with the payment preference, load PRO, details PRO with the generated id
    @Test
    void tesB160_cargaGenericaCreaIngresosYEgresos() {
        verificar(CASOS.get(0));
    }

    // car/ProcesarCargaMasiva.java:536-663: GeneraPartida = S: the details are added up per (load, movement type, currency, means of payment, account) and sent to CrearComprobante as PARING / PAREGR
    @Test
    void tesB161_cargaConPartidaAgrupaPorTipoMonedaMedioYCuenta() {
        verificar(CASOS.get(1));
    }

    // car/ProcesarCargaMasiva.java:288-303,334-361: p_UsaReglasNegocio = N skips the basic requirements and every validation
    @Test
    void tesB162_sinReglasDeNegocioOmiteValidaciones() {
        verificar(CASOS.get(2));
    }

    // car/ProcesarCargaMasiva.java:299-302: empty request
    @Test
    void tesB163_sinCargasNoSeIngresaronCargas() {
        verificar(CASOS.get(3));
    }

    // car/ProcesarCargaMasiva.java:290-297: basic requirements with an error: its messages are returned
    @Test
    void tesB164_requisitosBasicosConErrorDevuelveSusMensajes() {
        verificar(CASOS.get(4));
    }

    // car/ProcesarCargaMasiva.java:340,355-360: unknown load id: CARGA_MASIVA_NO_EXISTE + CARGAS_MASIVAS_CON_ERRORES
    @Test
    void tesB165_cargaInexistenteCargaMasivaNoExiste() {
        verificar(CASOS.get(5));
    }

    // car/ProcesarCargaMasiva.java:342: state PRO: CARGA_MASIVA_PROCESADA
    @Test
    void tesB166_cargaYaProcesadaCargaMasivaProcesada() {
        verificar(CASOS.get(6));
    }

    // car/ProcesarCargaMasiva.java:343: state INV: CARGA_MASIVA_INVALIDA
    @Test
    void tesB167_cargaInvalidaCargaMasivaInvalida() {
        verificar(CASOS.get(7));
    }

    // car/ProcesarCargaMasiva.java:341: unknown load type
    @Test
    void tesB168_tipoDeCargaInexistenteTipoCargaMasivaNoExiste() {
        verificar(CASOS.get(8));
    }

    // car/ProcesarCargaMasiva.java:345-352: SERVIPAG load without a complete payment preference: TIPO_CARGA_MASIVA_SIN_CONFIGURACION and four CONFIGURACION_TIPO_CARGA_MASIVA_INVALIDA
    @Test
    void tesB169_servipagSinConfiguracionCompletaConfiguracionInvalida() {
        verificar(CASOS.get(9));
    }

    // car/ProcesarCargaMasiva.java:406 + sql/ProcesarCargaMasiva_SelectCargaMasivaDetalle.sql + UpdateCargaMasivaDetalle.sql: BCP conciliation: a detail whose movement already exists is not sent and gets the observation 'Movimiento ya Existe con folio'
    @Test
    void tesB170_conciliacionBcpMovimientoExistenteNoSeReenvia() {
        verificar(CASOS.get(10));
    }

    // car/ProcesarCargaMasiva.java:505-513: CrearIngresoEgreso errors: messages + ERROR_AL_GENERAR_MOVIMIENTO_TESORERIA, nothing marked
    @Test
    void tesB171_crearIngresoEgresoConErrorErrorAlGenerarMovimiento() {
        verificar(CASOS.get(11));
    }

    // car/ProcesarCargaMasiva.java:652-660: CrearComprobante errors: messages + ERROR_AL_GENERAR_PARTIDA_INGRESO_EGRESO
    @Test
    void tesB172_crearComprobanteConErrorErrorAlGenerarPartida() {
        verificar(CASOS.get(12));
    }

    // car/ProcesarCargaMasiva.java:311-315: legacy defect: the parameter IdCargaMasiva is overwritten in the loop, so only the LAST load is validated and processed
    @Test
    void tesB173_dosCargasSoloLaUltimaSeValidaYConsulta() {
        verificar(CASOS.get(13));
    }

    // car/ProcesarCargaMasiva.java:456-486: a preference without means of payment sends the movement without forma de pago
    @Test
    void tesB174_preferenciaSinMedioDePagoNoEnviaFormaDePago() {
        verificar(CASOS.get(14));
    }

    // car/ProcesarCargaMasiva.java:693-697: a movement not returned by CrearIngresoEgreso leaves its detail unmarked (id 0)
    @Test
    void tesB175_movimientoNoGeneradoDejaDetalleSinComprobante() {
        verificar(CASOS.get(15));
    }

    // car/ProcesarCargaMasiva.java:326-327 + sql/ProcesarCargaMasiva_SelectPreferenciaPagoCarga.sql: currency / concept / means of payment / account of the request win over the configured preference (COALESCE order)
    @Test
    void tesB176_parametrosDeEntradaReemplazanALaPreferencia() {
        verificar(CASOS.get(16));
    }

    // car/ProcesarCargaMasiva.java:672-687: a null p_FechaProceso is written as a NULL process date
    @Test
    void tesB177_fechaDeProcesoNulaActualizaFechaProcesoNula() {
        verificar(CASOS.get(17));
    }

}
