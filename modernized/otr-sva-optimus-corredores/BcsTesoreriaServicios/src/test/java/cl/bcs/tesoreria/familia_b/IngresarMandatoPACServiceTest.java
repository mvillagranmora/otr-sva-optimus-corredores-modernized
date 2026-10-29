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
import cl.bcs.tesoreria.familia_b.support.SecuenciasDeCaso;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.AnulacionMandatoPACService;
import cl.bcs.tesoreria.javacall.IngresarMandatoPACService;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.IngresarMandatoPAC} (plain-Gson {@code call}) as {@code IngresarMandatoPACService#execute(JsonObject)}:
 * registers new PAC mandates (state PAU, pending authorization).
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-97..119}. Collaborators (RevisarRequisitosBasicos, CrearEventoAutorizacionMasivo, AgregarLog,
 * AnulacionContratoCSC, CambiarEstadoContratosSAAPV, the HANA stored procedure) are recorded fakes and their call trace is compared.
 */
@ExtendWith(ZonaUtc.class)
class IngresarMandatoPACServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-97_csc-nuevo-toma-dia-descuento-del-contrato-y-quita-no-digitos",
            "TES-B-98_saapv-nuevo-toma-dia-descuento-del-contrato-saapv",
            "TES-B-99_tipo-pago-N-modifica-cuenta-bancaria-por-procedimiento",
            "TES-B-100_sin-solicitudes-NO_SE_INGRESARON_SOLICITUDES",
            "TES-B-101_requisitos-basicos-con-error-no-copia-los-mensajes",
            "TES-B-102_documento-de-identidad-inexistente-advierte-y-no-procesa",
            "TES-B-103_cuenta-corriente-inexistente-advierte-y-no-procesa",
            "TES-B-104_folio-csc-ya-existe-advierte-y-no-inserta",
            "TES-B-105_folio-saapv-ya-existe-advierte-y-no-inserta",
            "TES-B-106_mandato-con-igual-folio-agrupador-y-dia-descuento-se-anula",
            "TES-B-107_mandato-saapv-con-igual-folio-agrupador-se-anula",
            "TES-B-108_evento-de-autorizacion-con-advertencia-y-error-agrega-mensajes-de-error",
            "TES-B-109_evento-de-autorizacion-lanza-excepcion-hace-rollback",
            "TES-B-110_dos-solicitudes-validas-y-una-invalida-usan-secuencias-correlativas",
            "TES-B-111_dia-descuento-informado-no-se-consulta",
            "TES-B-112_csc-y-saapv-simultaneos-dejan-dia-descuento-nulo",
            "TES-B-113_csc-y-saapv-simultaneos-con-mandato-existente-lanza-NullPointerException",
            "TES-B-114_dos-folios-csc-existentes-solo-detecta-el-primero-por-top-1",
            "TES-B-115_folio-csc-nulo-lanza-NullPointerException",
            "TES-B-116_folio-saapv-nulo-lanza-NullPointerException",
            "TES-B-117_ident-servicio-vacio-se-reemplaza-por-cero",
            "TES-B-118_cliente-cero-con-documento-existente-y-cuenta-valida-procesa",
            "TES-B-119_tipo-pago-N-con-procedimiento-que-lanza-excepcion-hace-rollback");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
            return new IngresarMandatoPACService(caso.jdbc(), diccionario, new ConfiguracionDelCaso(caso), new SecuenciasDeCaso(caso),
                    new MandatoPacComun(caso.jdbc(), diccionario, col[0]), new AnulacionMandatoPACService(caso.jdbc(), diccionario, col[0]), col[0])
                    .execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("IngresarMandatoPAC");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // IngresarMandatoPAC.java:334,412-421,434-452,532-542: new CSC mandate: IdentServicio keeps only digits, DiaDescuento comes from the CSC contract, inserted as PAU/ING, bank of the CSC contract updated, success message of type AUTORIZACION
    @Test
    void tesB97_cscNuevoTomaDiaDescuentoDelContratoYQuitaNoDigitos() {
        verificar(CASOS.get(0));
    }

    // IngresarMandatoPAC.java:412-416: new SAAPV mandate: DiaDescuento from the SAAPV contract
    @Test
    void tesB98_saapvNuevoTomaDiaDescuentoDelContratoSaapv() {
        verificar(CASOS.get(1));
    }

    // IngresarMandatoPAC.java:455-482: TipoPago N: temporary table + call of the HANA procedure PER.ModificarPersonaCuentaBancaria (recorded as collaborator StoredProcedure), table dropped afterwards
    @Test
    void tesB99_tipoPagoNModificaCuentaBancariaPorProcedimiento() {
        verificar(CASOS.get(2));
    }

    // IngresarMandatoPAC.java:305-309: empty request: error message and counter 1
    @Test
    void tesB100_sinSolicitudesNoSeIngresaronSolicitudes() {
        verificar(CASOS.get(3));
    }

    // IngresarMandatoPAC.java:311-316: basic requirements with an error: counter 1 but the messages are NOT copied to the output (legacy defect)
    @Test
    void tesB101_requisitosBasicosConErrorNoCopiaLosMensajes() {
        verificar(CASOS.get(4));
    }

    // IngresarMandatoPAC.java:348-358: IdCliente 0 (WS origin) with an unknown document: DOCUMENTO_IDENTIDAD_NO_EXISTE warning, request skipped
    @Test
    void tesB102_documentoDeIdentidadInexistenteAdvierteYNoProcesa() {
        verificar(CASOS.get(5));
    }

    // IngresarMandatoPAC.java:359-366: unknown bank account: CUENTA_CORRIENTE_NO_EXISTE warning, request skipped
    @Test
    void tesB103_cuentaCorrienteInexistenteAdvierteYNoProcesa() {
        verificar(CASOS.get(6));
    }

    // IngresarMandatoPAC.java:401-405: CSC folio already in a non-annulled mandate: FOLIO_CSC_YA_EXISTE, no insert
    @Test
    void tesB104_folioCscYaExisteAdvierteYNoInserta() {
        verificar(CASOS.get(7));
    }

    // IngresarMandatoPAC.java:406-410: SAAPV folio already used: FOLIO_SAAPV_YA_EXISTE, no insert
    @Test
    void tesB105_folioSaapvYaExisteAdvierteYNoInserta() {
        verificar(CASOS.get(8));
    }

    // IngresarMandatoPAC.java:428-432,528-531,628-646: an existing mandate with the same IdentServicio, client and discount day is annulled through AnulacionMandatoPAC (real legacy code in the oracle)
    @Test
    void tesB106_mandatoConIgualFolioAgrupadorYDiaDescuentoSeAnula() {
        verificar(CASOS.get(9));
    }

    // IngresarMandatoPAC.java:423-427: same for a SAAPV-based request
    @Test
    void tesB107_mandatoSaapvConIgualFolioAgrupadorSeAnula() {
        verificar(CASOS.get(10));
    }

    // IngresarMandatoPAC.java:510-518: authorization event warnings and errors are both turned into ERROR messages (the mandate is still inserted)
    @Test
    void tesB108_eventoDeAutorizacionConAdvertenciaYErrorAgregaMensajesDeError() {
        verificar(CASOS.get(11));
    }

    // IngresarMandatoPAC.java:552-556: an exception of the authorization collaborator: counter 1, rethrown, nothing persisted
    @Test
    void tesB109_eventoDeAutorizacionLanzaExcepcionHaceRollback() {
        verificar(CASOS.get(12));
    }

    // IngresarMandatoPAC.java:390-393,435: sequence values are taken for the whole request size but assigned only to processed mandates
    @Test
    void tesB110_dosValidasYUnaInvalidaUsanSecuenciasCorrelativas() {
        verificar(CASOS.get(13));
    }

    // IngresarMandatoPAC.java:412: a given DiaDescuento is used as is
    @Test
    void tesB111_diaDescuentoInformadoNoSeConsulta() {
        verificar(CASOS.get(14));
    }

    // IngresarMandatoPAC.java:413-421: both folios informed: neither branch sets DiaDescuento, which is inserted as NULL
    @Test
    void tesB112_cscYSaapvSimultaneosDejanDiaDescuentoNulo() {
        verificar(CASOS.get(15));
    }

    // IngresarMandatoPAC.java:424-432: null DiaDescuento compared against an existing mandate: NullPointerException (legacy defect)
    @Test
    void tesB113_cscYSaapvSimultaneosConMandatoExistenteLanzaNullPointerException() {
        verificar(CASOS.get(16));
    }

    // IngresarMandatoPAC.java:377-378 + sql/IngresarMandatoPAC_SelectFolioPACCSC.sql: legacy defect: the folio lookup is SELECT TOP 1, so only one of several existing folios is detected
    @Test
    void tesB114_dosFoliosCscExistentesSoloDetectaElPrimeroPorTop1() {
        verificar(CASOS.get(17));
    }

    // IngresarMandatoPAC.java:398: null FolioCSC: NullPointerException
    @Test
    void tesB115_folioCscNuloLanzaNullPointerException() {
        verificar(CASOS.get(18));
    }

    // IngresarMandatoPAC.java:413: null FolioSAAPV: NullPointerException
    @Test
    void tesB116_folioSaapvNuloLanzaNullPointerException() {
        verificar(CASOS.get(19));
    }

    // IngresarMandatoPAC.java:334: an empty IdentServicio becomes 0
    @Test
    void tesB117_identServicioVacioSeReemplazaPorCero() {
        verificar(CASOS.get(20));
    }

    // IngresarMandatoPAC.java:348-358: IdCliente 0 with an existing document and account is processed
    @Test
    void tesB118_clienteCeroConDocumentoExistenteYCuentaValidaProcesa() {
        verificar(CASOS.get(21));
    }

    // IngresarMandatoPAC.java:474-481: the HANA procedure failing: the temporary table is dropped (finally) and the exception propagates
    @Test
    void tesB119_tipoPagoNConProcedimientoQueLanzaExcepcionHaceRollback() {
        verificar(CASOS.get(22));
    }

}
