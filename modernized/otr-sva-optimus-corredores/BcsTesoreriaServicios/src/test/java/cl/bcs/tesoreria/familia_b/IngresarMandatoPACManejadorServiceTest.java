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
import cl.bcs.tesoreria.javacall.IngresarMandatoPACManejadorService;
import cl.bcs.tesoreria.javacall.IngresarMandatoPACService;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun;

/**
 * Characterization of legacy {@code cl.bcs.tesoreria.IngresarMandatoPACManejador} (entry point {@code call}; plain Gson) as {@code IngresarMandatoPACManejadorService#execute(JsonObject)}:
 * validates each PAC request for null ids and delegates the valid ones, one by one, to IngresarMandatoPAC.
 * 
 * <p>Goldens: {@code src/test/resources/tesoreria-b/cases/TES-B-267..276}. IngresarMandatoPAC runs as the real legacy code in the oracle; its collaborators are recorded fakes.
 */
@ExtendWith(ZonaUtc.class)
class IngresarMandatoPACManejadorServiceTest {

    static final List<String> CASOS = List.of(
            "TES-B-267_cuenta-bancaria-nula-CUENTA_BANCARIA_NO_ENCONTRADA",
            "TES-B-268_tipo-de-pago-nulo-TIPO_PAGO_NO_ENCONTRADO",
            "TES-B-269_cliente-nulo-CLIENTE_NO_ENCONTRADO",
            "TES-B-270_portafolio-nulo-PORTAFOLIO_NO_ENCONTRADO",
            "TES-B-271_solicitud-valida-delega-en-IngresarMandatoPAC-y-devuelve-el-folio",
            "TES-B-272_dos-solicitudes-una-con-error-cuenta-un-error",
            "TES-B-273_sin-solicitudes-no-llama-al-servicio",
            "TES-B-274_folio-csc-nulo-propaga-NullPointerException-del-servicio",
            "TES-B-275_solicitud-con-advertencia-copia-mensajes-con-su-tipo",
            "TES-B-276_validas-e-invalidas-mezcladas-respetan-el-orden-de-entrada");

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    private static void verificar(String nombre) {
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
            col[0] = new ColaboradoresDeCaso(caso);
            DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
            IngresarMandatoPACService ingresar = new IngresarMandatoPACService(caso.jdbc(), diccionario, new ConfiguracionDelCaso(caso), new SecuenciasDeCaso(caso),
                    new MandatoPacComun(caso.jdbc(), diccionario, col[0]), new AnulacionMandatoPACService(caso.jdbc(), diccionario, col[0]), col[0]);
            return new IngresarMandatoPACManejadorService(diccionario, ingresar).execute(caso.entrada().deepCopy());
        }, () -> col[0].traza());
    }

    @AfterAll
    static void resumen() {
        EJECUTADOS.imprimir("IngresarMandatoPACManejador");
        assertThat(EJECUTADOS.total()).as("equivalence cases executed (zero = not proven)").isGreaterThan(0);
    }

    // IngresarMandatoPACManejador.java:64-70: IdCuentaBancaria null: error CUENTA_BANCARIA_NO_ENCONTRADA without calling IngresarMandatoPAC
    @Test
    void tesB267_cuentaBancariaNulaCUENTABANCARIANOENCONTRADA() {
        verificar(CASOS.get(0));
    }

    // IngresarMandatoPACManejador.java:71-76: TipoPago null: TIPO_PAGO_NO_ENCONTRADO
    @Test
    void tesB268_tipoDePagoNuloTIPOPAGONOENCONTRADO() {
        verificar(CASOS.get(1));
    }

    // IngresarMandatoPACManejador.java:77-82: IdCliente null: CLIENTE_NO_ENCONTRADO
    @Test
    void tesB269_clienteNuloCLIENTENOENCONTRADO() {
        verificar(CASOS.get(2));
    }

    // IngresarMandatoPACManejador.java:83-88: IdPortafolio null: PORTAFOLIO_NO_ENCONTRADO
    @Test
    void tesB270_portafolioNuloPORTAFOLIONOENCONTRADO() {
        verificar(CASOS.get(3));
    }

    // IngresarMandatoPACManejador.java:89-120,136-160: valid request: delegated to IngresarMandatoPAC (real legacy code in the oracle); output carries the folio and the messages
    @Test
    void tesB271_solicitudValidaDelegaEnIngresarMandatoPACYDevuelveElFolio() {
        verificar(CASOS.get(4));
    }

    // IngresarMandatoPACManejador.java:136-160: two requests, the second one with a warning: errors counted per request with P_OUT_ERRORESCONTADOR > 0 only
    @Test
    void tesB272_dosSolicitudesUnaConErrorCuentaUnError() {
        verificar(CASOS.get(5));
    }

    // IngresarMandatoPACManejador.java:62: no requests: empty output, nothing called
    @Test
    void tesB273_sinSolicitudesNoLlamaAlServicio() {
        verificar(CASOS.get(6));
    }

    // IngresarMandatoPACManejador.java:170-173: inner NullPointerException (null FolioCSC) is rethrown after logging
    @Test
    void tesB274_folioCscNuloPropagaNullPointerExceptionDelServicio() {
        verificar(CASOS.get(7));
    }

    // IngresarMandatoPACManejador.java:136-160: warnings are copied with their type and do not count as errors
    @Test
    void tesB275_solicitudConAdvertenciaCopiaMensajesConSuTipo() {
        verificar(CASOS.get(8));
    }

    // IngresarMandatoPACManejador.java:62-120: mixed valid/invalid requests keep the input order in MANDATO
    @Test
    void tesB276_validasEInvalidasMezcladasRespetanElOrdenDeEntrada() {
        verificar(CASOS.get(9));
    }

}
