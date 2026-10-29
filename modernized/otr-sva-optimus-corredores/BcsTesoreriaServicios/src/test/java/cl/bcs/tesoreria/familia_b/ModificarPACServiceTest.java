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
import cl.bcs.tesoreria.javacall.ModificarPACService;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.ModificarPAC} (entry point {@code call(conn, json)}) as {@code ModificarPACService#execute(JsonObject)}:
 * modifies an existing PAC mandate (state PAU, pending authorization) after validating account, document, folios and duplicated grouping folio / discount day.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-120..134} (output, PAC.MandatoPAC and CSC.ContratoSuscripcionCuotas rows). Collaborators
 * (RevisarRequisitosBasicos, CrearEventoAutorizacionMasivo, AgregarLog, the HANA procedure) are recorded fakes; they go through the Colaboradores port and the call trace is compared.
 */
@ExtendWith(ZonaUtc.class)
class ModificarPACServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-120_modifica-mandato-csc-actualiza-mandato-y-banco",
            "TES-B-121_modifica-mandato-saapv",
            "TES-B-122_tipo-pago-N-modifica-cuenta-bancaria-por-procedimiento",
            "TES-B-123_sin-solicitudes-NO_SE_INGRESARON_SOLICITUDES",
            "TES-B-124_requisitos-basicos-con-error",
            "TES-B-125_documento-inexistente-advierte-con-id-del-mandato-base",
            "TES-B-126_cuenta-corriente-inexistente-advierte-con-id-del-mandato-base",
            "TES-B-127_folio-csc-de-otro-mandato-con-estado-ING-advierte",
            "TES-B-128_folio-csc-de-otro-mandato-con-estado-distinto-de-ING-modifica",
            "TES-B-129_mismo-folio-agrupador-y-dia-descuento-advierte",
            "TES-B-130_mismo-folio-agrupador-saapv-advierte",
            "TES-B-131_evento-de-autorizacion-con-error-agrega-mensaje",
            "TES-B-132_evento-de-autorizacion-lanza-excepcion-hace-rollback",
            "TES-B-133_mandato-base-nulo-lanza-NullPointerException",
            "TES-B-134_dos-modificaciones-una-invalida-solo-aplica-la-valida",
            "TES-B-330_mandato-saapv-actualiza-el-banco-del-contrato-csc-con-folio-cero");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
            return new ModificarPACService(caso.jdbc(), diccionario, new ConfiguracionDelCaso(caso),
                    new MandatoPacComun(caso.jdbc(), diccionario, col[0]), col[0]).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("ModificarPAC");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // ModificarPAC.java:522-533: modification of a CSC mandate: UPDATE of the mandate (state PAU, EstadoPorAutorizar = EstadoMandato), bank updated, MODIFICACION_MANDATO_PAC_OK
    @Test
    void tesB120_modificaMandatoCscActualizaMandatoYBanco() {
        verificar(CASOS.get(0));
    }

    // ModificarPAC.java:404-409: modification of a SAAPV mandate
    @Test
    void tesB121_modificaMandatoSaapv() {
        verificar(CASOS.get(1));
    }

    // ModificarPAC.java:446-473: TipoPago N: call of the HANA procedure (recorded) with temporary table
    @Test
    void tesB122_tipoPagoNModificaCuentaBancariaPorProcedimiento() {
        verificar(CASOS.get(2));
    }

    // ModificarPAC.java:303-307: empty request
    @Test
    void tesB123_sinSolicitudesNoSeIngresaronSolicitudes() {
        verificar(CASOS.get(3));
    }

    // ModificarPAC.java:309-314: basic requirements with an error: counter 1, no messages
    @Test
    void tesB124_requisitosBasicosConError() {
        verificar(CASOS.get(4));
    }

    // ModificarPAC.java:345-357: unknown document: warning whose id is the base mandate id
    @Test
    void tesB125_documentoInexistenteAdvierteConIdDelMandatoBase() {
        verificar(CASOS.get(5));
    }

    // ModificarPAC.java:358-361: unknown bank account: warning with the base mandate id
    @Test
    void tesB126_cuentaCorrienteInexistenteAdvierteConIdDelMandatoBase() {
        verificar(CASOS.get(6));
    }

    // ModificarPAC.java:390-402: CSC folio of ANOTHER mandate and EstadoMandato ING: FOLIO_CSC_YA_EXISTE
    @Test
    void tesB127_folioCscDeOtroMandatoConEstadoIngAdvierte() {
        verificar(CASOS.get(7));
    }

    // ModificarPAC.java:399: same folio but EstadoMandato <> ING: the modification goes through
    @Test
    void tesB128_folioCscDeOtroMandatoConEstadoDistintoDeIngModifica() {
        verificar(CASOS.get(8));
    }

    // ModificarPAC.java:410-424: another mandate with the same IdentServicio, client and discount day: MANDATOS_PAC_CON_IGUAL_FOLIO_AGRUPADOR_Y_DIAS_DESCUENTO
    @Test
    void tesB129_mismoFolioAgrupadorYDiaDescuentoAdvierte() {
        verificar(CASOS.get(9));
    }

    // ModificarPAC.java:404-409: same check for SAAPV
    @Test
    void tesB130_mismoFolioAgrupadorSaapvAdvierte() {
        verificar(CASOS.get(10));
    }

    // ModificarPAC.java:503-509: authorization event error added as ERROR message, the mandate is still updated
    @Test
    void tesB131_eventoDeAutorizacionConErrorAgregaMensaje() {
        verificar(CASOS.get(11));
    }

    // ModificarPAC.java:540-544: collaborator exception: rollback and rethrow
    @Test
    void tesB132_eventoDeAutorizacionLanzaExcepcionHaceRollback() {
        verificar(CASOS.get(12));
    }

    // ModificarPAC.java:392: null IdMandatoPACBase: NullPointerException when comparing mandate ids
    @Test
    void tesB133_mandatoBaseNuloLanzaNullPointerException() {
        verificar(CASOS.get(13));
    }

    // ModificarPAC.java:367-372,386: an invalid request is skipped, the valid one is applied
    @Test
    void tesB134_dosModificacionesUnaInvalidaSoloAplicaLaValida() {
        verificar(CASOS.get(14));
    }

    // ModificarPAC.java:267: legacy defect: FolioCSC.equals(0L) is always false, so the bank update also runs for SAAPV mandates (FolioCSC '0') and hits a contract whose folio is '0'
    @Test
    void tesB330_mandatoSaapvActualizaElBancoDelContratoCscConFolioCero() {
        verificar(CASOS.get(15));
    }

}
